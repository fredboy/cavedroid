# Performance monitoring, schema 1

Android store release enables FIREBASE by default, for 100% of game sessions. Its settings button toggles OFF ↔ FIREBASE; changes apply immediately and persist in `performance_mode`. Release builds cannot select or restore LOCAL: a previously stored LOCAL preference becomes OFF without enabling transmission. FOSS release stays OFF and hides the diagnostics setting. Debug builds default to OFF and offer OFF/LOCAL only; their manifests permanently deactivate both new Firebase collectors. Desktop, iOS and web inject the no-op implementation by default. Crashlytics collection policy is unchanged.

LOCAL writes private `files/performance.jsonl`, rotating at 1 MiB to `performance.previous.jsonl`. Each line contains `event` and `parameters`. Files contain diagnostics, not saves. On a debuggable Android install:

```sh
adb exec-out run-as ru.fredboy.cavedroid.debug cat files/performance.jsonl > performance.jsonl
```

OFF clears queued reports, invalidates in-flight collection generations and cancels sampling. An already executing platform/SDK call cannot be recalled. Previously submitted SDK data and existing local files are not erased. No disk outbox is maintained. Collection mode changes do not change Crashlytics consent.

## Timing and lifecycle

All durations are elapsed monotonic wall time in nanoseconds internally and integer microseconds in events. Driver waits and scheduling delays are included. These are neither thread CPU execution times nor GPU timings nor SurfaceFlinger presentation intervals.

Only a foreground `GameScreen` after its first normal callback is gameplay. Menu, death/pause screens, initial construction, save callbacks and interstitial transitions are excluded. Inventory remains gameplay. State transitions reset the inter-frame baseline. A genuine long callback gap while active is retained. A partial nonempty report is closed on pause, context/display changes, and release. Normal windows close after 60 seconds of active gameplay.

`frame_interval` is the difference between starts of consecutive callbacks. Weighted FPS is `SUM(interval_n) / SUM(interval_duration_seconds)`. `fps_p5` is explicitly defined as the reciprocal of the histogram's nearest-rank p95 interval estimate (not the mean of instantaneous FPS). A stall is **strictly greater than** 100 ms. The budget is `1 / refresh_hz`, marked `target_source=display_assumed`; Box2D's 60 Hz timestep is unrelated. Unknown budgets have no budget parameters.

`frame_callback` includes the normal `GameProc.update`. `update_total` sums the portions before and after `GameRenderer.render`. `world_update`, `entities_update` and `other_update` are children; `render_total` is independent. `physics` includes collision readiness and native stepping; `box2d` is its nested native step. Multiple physical steps are summed once per callback, with `physics_steps` reporting the number of steps. Do not add parents to their children.

Render children cover world renderers, blocks, player, other mobs, drops, projectiles, HUD and lighting. `render_other` is total minus world/HUD/lighting and contains camera work, sky, debug rendering and deferred batch flushes. No extra GL flush is introduced. Block timings include cache rebuilds. `render_chunks` counts rendered cache entries across layers; this differs from resident `terrain_chunks`. `active_entities` counts entities visited by the existing renderer loops, including offscreen residents; `drawn_entities` counts actual draw candidates, including the player.

Periodic block/fluid/grow/fire/spawn/damage tasks are per-invocation distributions, separate from callback stages. The direct initialization spawn has its own operation. Background metrics use a captured world generation and collection epoch. Cancellation is observed with `Job.invokeOnCompletion`, including cancellation before execution. Release does not add waits to game logic. `world_release` measures resource disposal; `world_unload` starts before disposal and ends only after tracked jobs have completed. A late result never becomes a sample in the next world's gameplay window.

Create/load spans start before resolving Dagger game dependencies and finish after the first normal callback. Initial save has `trigger=initial`; other saves use `user`. Save spans include pre-render and PNG creation. `bytes_written` counts completed writes in instrumented save paths during the operation, including compression output and PNG, not the size of a world directory. Concurrent tracked writes can overlap operation intervals. Nested operations may include the same bytes and must not be summed.

`android_startup` distinguishes the first launcher in the process, Activity recreation, and foreground resume. Initialization of preferences/monitor precedes the measured boundary; it is not a process-fork timestamp. `menu_ready` ends after input installation and the first menu draw. The automatic Firebase app-start trace has different boundaries.

## Histograms and event schema

`hist_v=1`: 194 unsigned counters. Bucket 0 covers `[0, 1 µs)`. Buckets 1–192 cover `[2^((i-1)/8), 2^(i/8)) µs`. Bucket 193 is overflow, starting at 16,777,216 µs. The first bucket whose cumulative count reaches `ceil(q * n)` determines a percentile. Return its upper bound, except overflow returns its lower bound. Maximum and sum remain exact to the internal clock (export truncates to µs). Normal-range relative bucket error is below approximately 9.1%; underflow and overflow do not have that guarantee.

Counters are unsigned varints encoded with standard Base64, split in order into `h00`…`h12`, each at most 100 characters. Merge counters by bucket for aggregate quantiles. Never average minute percentiles. Compute means as `SUM(sum_us)/SUM(n)`.

A report has a random `report_id` used only to join that report's events. The final `cd_perf_window.event_n` counts all events in the logical report, including itself. Match this count after deduplication to detect incomplete delivery. Missing metrics are absent, not zero.

| Event | Purpose |
| --- | --- |
| `cd_perf_window` | Active duration, intervals, weighted FPS, budget and stall counts/ratios, refresh, partial flag, sampling, dropped reports, expected event count |
| `cd_perf_duration` | Fixed metric ID, histogram version, count/sum/max/mean, p50/p95/p99, encoded histogram; maximum 25 parameters |
| `cd_perf_memory` | Metric, sample count, average/max/last bytes |
| `cd_perf_gc` | Runtime counter deltas and measured coverage |
| `cd_perf_context` | Build code, world type, lighting, surface/viewport, debug overlay, bounded GPU strings |
| `cd_perf_scene` | Fixed scene counter ID and count/average/max/last; separate events keep context below 25 parameters |
| `cd_perf_background` | Per-metric success/error/cancelled counts, separate from the 25-parameter duration schema |
| `cd_perf_operation` | Exact operation duration/result, trigger/start kind, bytes, optional heap before/after/delta, background flag |

Metric names are the lower-case enum names in `PerformanceMetric` and `PerformanceGauge`. Error kinds are fixed `error`/`cancelled`; no exception text, coordinates, seed, world name/path/ID or user strings are included. Device and app version come from Firebase's standard fields. Counts and durations use Long; ratios use Double. Unavailable optional values are omitted.

Managed heap, native heap (5 s) and process PSS (30 s) are sampled on the worker. They overlap and **must not be added together**. GC keys are approximate Android runtime counters; resets/unavailable values break coverage. GC time is not labelled main-thread pause time. Heap differences across unload are not proof of a leak; no forced GC occurs.

A fully populated schema fixture (including all background outcomes) produces 95 events and 73,275 UTF-8 JSONL bytes. This is a synthetic upper-coverage fixture, not a typical minute.

The render path uses primitive timestamps and preallocated arrays. Three snapshot buffers back a queue of at most two waiting reports plus one worker-owned buffer; the oldest waiting report is dropped on pressure. Background summaries use a separate short lock. Serialization, percentiles, memory APIs, JSON and Firebase calls run on one low-priority worker. Operation notifications are separately bounded at 128 entries. Windows close early at one million samples per metric, well before 32-bit histogram counters can overflow.

## Firebase and analysis

Only store depends on Analytics and Performance, through the existing BoM. No Performance Gradle plugin or `@AddTrace` bytecode instrumentation is enabled. Manual traces are `cd_app_init`, `cd_assets_load`, `cd_world_create`, `cd_world_load`, `cd_world_save`, `cd_world_unload`.

Start and stop notifications are delivered to the worker when their boundaries occur. The SDK trace duration includes queue scheduling error. The exact source is `measured_duration_us` and the Analytics operation duration. No historical-duration reconstruction is claimed. Frequent chunk work has aggregated Analytics metrics, not individual traces. Crashlytics summary keys update only while FIREBASE is selected; no slow-frame nonfatal exceptions are generated.

Register only low-cardinality event dimensions needed for analysis: `metric`, `operation`, `result`, `world_type`, `lighting`, `target_source`. Do not register report IDs or histogram fragments. Enabling BigQuery export and configuring custom definitions require Firebase Console access. The repository's Google Services JSON does not establish that export is enabled.

[`aggregate.sql`](aggregate.sql) checks report completeness, computes weighted FPS/stall ratios and merges histograms before deriving quantiles. Replace its project/dataset/date range. It compares versions within device, refresh, world, lighting, surface and debug cohorts. SQL must be validated against the actual export before production use. Event volume depends on scene and background activity and can exceed the original 35–50 events/minute estimate because outcomes and scene counters are separate events. Review actual event counts and BigQuery quota before rollout.

## Verification before release

Implementation checks on 2026-10-23 used JDK 17: `testCore` (170 tests, no failures/errors, one existing skipped test), `ktlintCheck buildSrc:ktlintCheck`, both Android debug flavors, `desktop:dist`, and `html:buildJs` passed. The warmed JVM accumulator allocation test passed with zero measured allocations over 10,000 frames; this does not measure the Android game or SDK overhead.

A Pixel 6 Pro smoke test used an isolated FOSS debug package in LOCAL mode. Creation/loading, save/release, a full 60-second window, pause, and background/resume produced local reports. Validation of 454 events found 11 complete reports, correct 194-bucket histogram counts, and no event above 25 parameters. One full window contained 3,562 intervals over 60,004 ms of active gameplay with no dropped reports. These debug observations are not a performance baseline. OFF selected in settings persisted across a process restart and stopped new local records.

The device also exposed an existing lifecycle issue: a `uiMode` configuration change recreated the Activity on foreground return, reopening the menu while the process stayed alive. The launcher manifest now handles `uiMode` without recreation because the libGDX UI does not use Android night resources. Ordinary background/resume passed after the change; a forced theme-change test remains to be performed. The startup report correctly distinguished Activity recreation from a cold process and ordinary resume.

Firebase DebugView/traces, actual BigQuery export/SQL execution, release overhead comparisons, and testing on a weak Android device remain unverified.

Run core tests, repository ktlint, both Android flavors, desktop distribution and TeaVM compilation. Pure tests cover clocks, bounded queues, histogram boundaries/merge/codec, Analytics limits, multiple physics steps, pause/resume, GC coverage, opt-out and coroutine cancellation lifetimes without GL/Box2D objects.

On a release install, verify settings persistence, no transmissions in OFF/LOCAL, Firebase DebugView event shapes, manual traces and BigQuery completeness. Debug builds intentionally cannot transmit; use a controlled release install for DebugView. Check original world load/save/collision behavior and finite/infinite world switching in-game.

Measure baseline, OFF, LOCAL and FIREBASE after warmup on both a weak and a modern Android device with identical scenes/settings and alternating repeated runs. Use Perfetto/Android Studio Profiler to check allocations, callback intervals, GC and worker bursts. Targets: no added per-frame allocations, bounded monitor buffers below 1 MiB, no measurable OFF regression, enabled overhead ≤1% mean work and ≤0.2 ms callback p95. These require device evidence and are not guarantees from JVM tests.

References: [Android Debug API](https://developer.android.com/reference/android/os/Debug), [Firebase custom code traces](https://firebase.google.com/docs/perf-mon/custom-code-traces?platform=android), [GA4 collection limits](https://support.google.com/analytics/answer/9267744), [BigQuery export](https://support.google.com/analytics/answer/9358801).
