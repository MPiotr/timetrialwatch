package com.github.mpiotr.competitionwatch.dataset

import android.icu.util.ULocale
import android.util.Log
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

class SplitConverters {

    @TypeConverter
    fun fromList(value: List<Long>): String =
        value.joinToString(",")

    @TypeConverter
    fun toList(value: String): List<Long> =
        if (value.isBlank()) emptyList()
        else value.split(",").map { it.toLong() }
}

@Entity(tableName = "competitors")
@TypeConverters(SplitConverters::class)
data class Competitor
    (@PrimaryKey(autoGenerate = true) val id: Long,
     @Embedded(prefix = "bib_") val bib : Bib,
     val name: String,
     val group : String,
     val sex: Int = 1,
     val age : Int = 18,
     val started : Boolean = false,
     val finished : Boolean = false,
     val startTime : Long = 0L,
     var result : Int = Int.MAX_VALUE,
     var gap : Long? = null,
     val splits : MutableList<Long> = mutableListOf(),
     val revision : Long = 0L,
     val email : String? = null)
{

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

    fun formattedLapTime(ms : Long, start : Long ) : String{
        val duration = (ms - start).milliseconds
        val competitionTime = duration.toComponents {
                hours, minutes, seconds, nanoseconds ->
            val centiseconds = (nanoseconds / 10e7.toFloat()).toInt()
            "%02d:%02d:%02d.%d".format(hours, minutes, seconds, centiseconds)  }
        return competitionTime
    }

    fun formattedDayTime(ms : Long) : String{
        val datetime = Date(ms)
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(datetime)
    }
    fun formattedTime(ms : Long) : String{
        val duration = ms.milliseconds
        val competitionTime = duration.toComponents {
                hours, minutes, seconds, nanoseconds ->
            val centiseconds = (nanoseconds / 10e7.toFloat()).toInt()
            "%02d:%02d:%02d.%d".format(hours, minutes, seconds, centiseconds)  }
        return competitionTime
    }

        fun formattedGapTime(ms : Long) : String{
        val duration = ms.milliseconds
        val competitionTime = duration.toComponents {
                hours, minutes, seconds, nanoseconds ->
            val centiseconds = (nanoseconds / 10e7.toFloat()).toInt()
            if(hours > 0)
                "+%d:%02d:%02d.%d".format(hours, minutes, seconds, centiseconds)
            else if(minutes > 0)
                "+%d:%02d.%d".format( minutes, seconds, centiseconds)
            else
                "+%d.%d".format(seconds, centiseconds)
        }
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
    fun formattedSplitsDayTime( ) : List<String> {
        return splits.map{ split_time ->  formattedDayTime(split_time)}
    }
    fun formattedSplitsLapTime( ) : List<String> {
        return splits.mapIndexed{ ind, split_time ->
            formattedLapTime(split_time, if(ind > 0) splits[ind - 1] else startTime)}

    }

    override fun toString(): String {
        return "${bib.bib_number}: $name"
    }
}
