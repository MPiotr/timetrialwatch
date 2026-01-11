package com.github.mpiotr.competitionwatch

import kotlin.time.Duration.Companion.milliseconds

fun formattedTimeSplit(now : Long, comp_start_time : Long) : String {
    val duration = (now - comp_start_time).milliseconds
    val competitionTime = duration.toComponents {
            hours, minutes, seconds, nanoseconds ->
        val centiseconds = (nanoseconds / 10e7.toFloat()).toInt()
        "%02d:%02d:%02d.%d".format(hours, minutes, seconds, centiseconds)  }
    return competitionTime
}