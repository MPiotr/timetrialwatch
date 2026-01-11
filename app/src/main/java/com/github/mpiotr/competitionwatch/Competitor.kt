package com.github.mpiotr.competitionwatch

import kotlin.time.Duration.Companion.milliseconds

data class Competitor(val id: String,
                      var bib_number : String,
                      var name: String,
                      var sex: Int = 1,
                      var age : Int = 18,
                      var group : Int = 0,
                      val bib_color : Int = 0,
                      var started : Boolean = false,
                      var finished : Boolean = false,
                      var startTime : Long = 0L,
                      var result : Int = Int.MAX_VALUE,
                      val splits : MutableList<Long> = mutableListOf()) {

    override fun toString(): String = "$bib_number: $name"


    fun formattedStartRaceTime(comp_start_time : Long) : String{
        val duration = (startTime - comp_start_time).milliseconds
        val competitionTime = duration.toComponents {
                hours, minutes, seconds, nanoseconds ->
            val centiseconds = (nanoseconds / 10e7.toFloat()).toInt()
            "%02d:%02d:%02d.%d".format(hours, minutes, seconds, centiseconds)  }
        return competitionTime
    }

    fun formattedRaceTime(ms : Long) : String{
        val duration = (ms - startTime).milliseconds
        val competitionTime = duration.toComponents {
                hours, minutes, seconds, nanoseconds ->
            val centiseconds = (nanoseconds / 10e7.toFloat()).toInt()
            "%02d:%02d:%02d.%d".format(hours, minutes, seconds, centiseconds)  }
        return competitionTime
    }

    fun timeBeforeStart(msnow : Long, comp_start_time: Long) : String{
        val elapsedMs = msnow - startTime -  comp_start_time
        val duration = -elapsedMs.milliseconds
        if(duration > 0.milliseconds) {
            return duration.toComponents { _, minutes, seconds, nanoseconds ->
                val centiseconds = (nanoseconds / 10e7.toFloat()).toInt()
                "%02d:%02d.%d".format(minutes, seconds, centiseconds)
            }
        }
        else {
            return (-duration).toComponents { _, minutes, seconds, nanoseconds ->
                val centiseconds = (nanoseconds / 10e7.toFloat()).toInt()
                "-%02d:%02d.%d".format(minutes, seconds, centiseconds)
            }
        }
    }

    fun formattedSplitsRaceTime( ) : List<String> { //TODO: Cache it!!!
        return splits.map{ split_time ->  formattedRaceTime(split_time)}
    }
}
