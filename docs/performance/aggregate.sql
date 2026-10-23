-- GoogleSQL. Replace YOUR_PROJECT.YOUR_ANALYTICS_DATASET and choose a date range.
-- Run against daily tables, or union intraday with explicit deduplication.
CREATE TEMP FUNCTION DecodeHistogram(bytes ARRAY<INT64>) RETURNS ARRAY<INT64>
LANGUAGE js AS r'''
  const result = [];
  let value = 0, scale = 1;
  for (const byte of bytes) {
    value += (byte & 127) * scale;
    if (byte & 128) {
      scale *= 128;
      if (scale > 268435456) throw new Error('Invalid histogram varint');
    } else {
      if (value > 2147483647) throw new Error('Histogram counter overflow');
      result.push(value); value = 0; scale = 1;
    }
  }
  if (scale !== 1 || result.length !== 194) throw new Error('Incomplete histogram');
  return result;
''';

CREATE TEMP TABLE performance_events AS
SELECT
  event_name,
  event_timestamp,
  device.mobile_model_name AS device_model,
  app_info.version AS app_version,
  (SELECT value.string_value FROM UNNEST(event_params) WHERE key = 'report_id') AS report_id,
  (SELECT value.string_value FROM UNNEST(event_params) WHERE key = 'metric') AS metric,
  event_params
FROM `YOUR_PROJECT.YOUR_ANALYTICS_DATASET.events_*`
WHERE _TABLE_SUFFIX BETWEEN '20261001' AND '20261031'
  AND STARTS_WITH(event_name, 'cd_perf_')
  AND (SELECT value.int_value FROM UNNEST(event_params) WHERE key = 'schema_v') = 1
QUALIFY ROW_NUMBER() OVER (
  PARTITION BY
    (SELECT value.string_value FROM UNNEST(event_params) WHERE key = 'report_id'),
    event_name,
    (SELECT value.string_value FROM UNNEST(event_params) WHERE key = 'metric')
  ORDER BY event_timestamp
) = 1;

-- Missing reports and incomplete event groups are excluded, never filled with zero.
CREATE TEMP TABLE complete_reports AS
SELECT report_id
FROM performance_events
WHERE report_id IS NOT NULL
GROUP BY report_id
HAVING COUNT(*) = MAX(IF(event_name = 'cd_perf_window',
  (SELECT value.int_value FROM UNNEST(event_params) WHERE key = 'event_n'), NULL));

CREATE TEMP TABLE cohorts AS
SELECT
  w.report_id, w.device_model, w.app_version,
  (SELECT value.double_value FROM UNNEST(w.event_params) WHERE key = 'refresh_hz') AS refresh_hz,
  (SELECT value.string_value FROM UNNEST(c.event_params) WHERE key = 'world_type') AS world_type,
  (SELECT value.string_value FROM UNNEST(c.event_params) WHERE key = 'lighting') AS lighting,
  (SELECT value.int_value FROM UNNEST(c.event_params) WHERE key = 'debug_overlay') AS debug_overlay,
  (SELECT value.int_value FROM UNNEST(c.event_params) WHERE key = 'surface_w') AS surface_w,
  (SELECT value.int_value FROM UNNEST(c.event_params) WHERE key = 'surface_h') AS surface_h,
  (SELECT value.int_value FROM UNNEST(w.event_params) WHERE key = 'interval_n') AS interval_n,
  (SELECT value.int_value FROM UNNEST(w.event_params) WHERE key = 'stall_100ms_n') AS stalls,
  (SELECT value.int_value FROM UNNEST(w.event_params) WHERE key = 'over_budget_n') AS over_budget
FROM performance_events w
JOIN complete_reports USING (report_id)
JOIN performance_events c USING (report_id)
WHERE w.event_name = 'cd_perf_window' AND c.event_name = 'cd_perf_context';

CREATE TEMP TABLE durations AS
SELECT
  c.*, d.metric,
  (SELECT value.int_value FROM UNNEST(d.event_params) WHERE key = 'n') AS n,
  (SELECT value.int_value FROM UNNEST(d.event_params) WHERE key = 'sum_us') AS sum_us,
  DecodeHistogram(TO_CODE_POINTS(FROM_BASE64(
    (SELECT STRING_AGG(value.string_value, '' ORDER BY key)
     FROM UNNEST(d.event_params) WHERE REGEXP_CONTAINS(key, r'^h[0-9]{2}$'))
  ))) AS histogram
FROM performance_events d
JOIN cohorts c USING (report_id)
WHERE d.event_name = 'cd_perf_duration'
  AND (SELECT value.int_value FROM UNNEST(d.event_params) WHERE key = 'hist_v') = 1;

-- Weighted FPS and slow-callback ratios within identical device/display/settings cohorts.
SELECT device_model, app_version, refresh_hz, world_type, lighting, debug_overlay, surface_w, surface_h,
  SUM(n) AS intervals,
  SAFE_DIVIDE(SUM(n) * 1e6, SUM(sum_us)) AS fps_avg,
  SAFE_DIVIDE(SUM(stalls), SUM(n)) AS stall_100ms_ratio,
  SAFE_DIVIDE(SUM(over_budget), SUM(n)) AS over_budget_ratio
FROM durations
WHERE metric = 'frame_interval'
GROUP BY device_model, app_version, refresh_hz, world_type, lighting, debug_overlay, surface_w, surface_h;

-- Merge buckets BEFORE computing percentiles. Never average per-window p95/p99.
WITH merged AS (
  SELECT device_model, app_version, refresh_hz, world_type, lighting, debug_overlay, surface_w, surface_h,
    metric, bucket, SUM(bucket_n) AS bucket_n
  FROM durations, UNNEST(histogram) AS bucket_n WITH OFFSET AS bucket
  GROUP BY device_model, app_version, refresh_hz, world_type, lighting, debug_overlay, surface_w, surface_h,
    metric, bucket
), ranks AS (
  SELECT *,
    SUM(bucket_n) OVER cohort AS n,
    SUM(bucket_n) OVER (cohort ORDER BY bucket ROWS UNBOUNDED PRECEDING) AS cumulative
  FROM merged
  WINDOW cohort AS (
    PARTITION BY device_model, app_version, refresh_hz, world_type, lighting, debug_overlay, surface_w, surface_h, metric
  )
)
SELECT device_model, app_version, refresh_hz, world_type, lighting, debug_overlay, surface_w, surface_h, metric,
  MIN(IF(cumulative >= CEIL(n * 0.95), POW(2, LEAST(bucket, 192) / 8.0) / 1000, NULL)) AS p95_ms,
  MIN(IF(cumulative >= CEIL(n * 0.99), POW(2, LEAST(bucket, 192) / 8.0) / 1000, NULL)) AS p99_ms,
  MIN(IF(cumulative >= CEIL(n * 0.99), bucket, NULL)) = 193 AS p99_is_overflow_lower_bound
FROM ranks
GROUP BY device_model, app_version, refresh_hz, world_type, lighting, debug_overlay, surface_w, surface_h, metric;
