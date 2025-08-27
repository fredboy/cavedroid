package ru.fredboy.cavedroid.common.utils

import dagger.Reusable
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@Reusable
class DateFormatter @Inject constructor() {

    @Suppress("SimpleDateFormat")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    fun format(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
}
