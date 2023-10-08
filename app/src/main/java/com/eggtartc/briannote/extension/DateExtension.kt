package com.eggtartc.briannote.extension

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Monday: 1, ..., Sunday: 7
 */
fun Date.getDayOfWeek(): Int {
    val calendar = java.util.Calendar.getInstance()
    calendar.time = this
    val dow = calendar.get(java.util.Calendar.DAY_OF_WEEK)
    return if (dow == java.util.Calendar.SUNDAY) 7 else dow - 1
}

/**
 * 1, ..., 31
 */
fun Date.getDayOfMonth(): Int {
    val calendar = java.util.Calendar.getInstance()
    calendar.time = this
    return calendar.get(java.util.Calendar.DAY_OF_MONTH)
}

fun Date.getMonth2(): Int {
    val calendar = java.util.Calendar.getInstance()
    calendar.time = this
    return calendar.get(java.util.Calendar.MONTH)
}

fun Date.getWeekOfMonth(): Int {
    val calendar = java.util.Calendar.getInstance()
    calendar.time = this
    return calendar.get(java.util.Calendar.WEEK_OF_MONTH)
}

fun Date.getYear2(): Int {
    val calendar = java.util.Calendar.getInstance()
    calendar.time = this
    return calendar.get(java.util.Calendar.YEAR)
}

fun Date.getHourOfDay(): Int {
    val calendar = java.util.Calendar.getInstance()
    calendar.time = this
    return calendar.get(java.util.Calendar.HOUR_OF_DAY)
}

fun Date.format(formatString: String): String {
    val formatter = SimpleDateFormat(formatString, Locale.CHINESE)
    return formatter.format(this)
}
