package com.github.mpiotr.competitionwatch

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.github.mpiotr.competitionwatch.Competitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.lang.Math.pow
import java.math.BigInteger
import kotlin.collections.sortedWith
import kotlin.math.log10
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

class CompetitorViewModel : ViewModel() {
    data class DigitsForPicker(val maxBibNumber : Int, val numDigits : Int, val maxGreaterDigit : Int)
    data class RacePositionInd(val currentPosition : Int, val leaderInd : Int, val chaserInd : Int, val numCompleted : Int)
    data class RacePositionItems(val currentPosition : Int, val leader : Competitor?, val chaser : Competitor?, val numCompleted : Int)


    var competitors  = listOf(
        Competitor("1", "1", "John Dow",  1, 18, 0 ),
        Competitor("2", "2", "Dow Jones", 1, 19, 0 ),
        Competitor("3", "3", "Rocky Balboa", 1, 19, 0 ),
        Competitor("4", "5", "Conan the Barbarian" ) ,
        Competitor("5", "7", "Scorpion" ),
        Competitor("6", "9", "Shang Tsung" ),
        Competitor("7", "10", "Vasya Pupkin" )
    )

    var bibIndex : MutableMap<String, Int> = mutableMapOf()
    init {
        for(c in competitors.withIndex())
            bibIndex[c.value.bib_number] = c.index
    }

    private var maxBibNumber : Int = 0
    fun getMaxBibNumber() : Int { return maxBibNumber}
    fun getDigitsForPicker() : DigitsForPicker {
        val maxBibNumber = maxBibNumber
        val numDigits = log10(maxBibNumber.toDouble()).toInt() + 1
        val maxGreaterDigit = maxBibNumber / pow(10.0, (numDigits - 1).toDouble()).toInt()
        return DigitsForPicker(maxBibNumber, numDigits, maxGreaterDigit)
    }

    var timeTrialStart = System.currentTimeMillis()
    private val _timeTrialStarted = MutableStateFlow(false);
    val timeTrialStarted : StateFlow<Boolean> = _timeTrialStarted.asStateFlow()


    private val _competitorsStateFlow = MutableStateFlow(competitors)
    val competitorsStateFlow : StateFlow<List<Competitor> > =  _competitorsStateFlow.asStateFlow()

    private val _startTime = MutableStateFlow(timeTrialStart)
    val startTime : StateFlow<Long> = _startTime.asStateFlow()

    fun getStartTime() : Long {
        return _startTime.value
    }

    fun formattedRaceTime(nowms : Long) : String {
        val duration = (nowms - startTime.value).milliseconds
        val competitionTime = duration.toComponents {
                hours, minutes, seconds, nanoseconds ->
            val centiseconds = (nanoseconds / 10e7.toFloat()).toInt()
            "%02d:%02d:%02d.%d".format(hours, minutes, seconds, centiseconds)  }
        return competitionTime
    }

    fun onItemChanged(updated: Competitor)
    {
        maxBibNumber = max(maxBibNumber,updated.bib_number.toInt())
        _competitorsStateFlow.update { list ->
            list.mapIndexed { ind, item ->
                if(item.id == updated.id) {
                bibIndex[item.bib_number] = ind
                updated
            } else item}
        }
    }
    fun onItemAdded( added: Competitor)
    {
        maxBibNumber = max(maxBibNumber,added.bib_number.toInt())
        _competitorsStateFlow.update {
            list ->
            bibIndex[added.bib_number] = list.size
            list + added
        }
    }

    fun newCompetitor() : Competitor {
        val nexid = _competitorsStateFlow.value.size + 1
        return Competitor(nexid.toString(), "", "", 0, 0, 0)
    }

    fun arrangeStartTimes()
    {
        _competitorsStateFlow.update { list ->
            list.sortedWith { a, b ->
                if (a.sex != b.sex) -a.sex + b.sex
                else if (a.group != b.group) a.group - b.group
                else a.bib_number.toInt() - b.bib_number.toInt()
            }
                .mapIndexed { index, competitor -> if(!competitor.started) competitor.copy(startTime = index * 15000L + 30000) else competitor }
        }

        maxBibNumber = _competitorsStateFlow.value.maxOf { it.bib_number.toInt() }
    }

    val nextStartingCompetitors: StateFlow<List<Competitor>> =
        _competitorsStateFlow.map {
            list ->
            list.mapNotNull {item ->  if(!item.started)  item else null }
            .sortedWith {
                    a, b ->
                if(a.sex != b.sex)    -a.sex + b.sex
                else if(a.group != b.group)  a.group - b.group
                else a.bib_number.toInt() - b.bib_number.toInt()
            }.take(5)
        }.stateIn(viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList())

    fun onSplit(splitTime : Long, itemIndex : Int)
    {
        _competitorsStateFlow.value[itemIndex].splits.add(splitTime)
    }

    fun splitsSlice(splitIndex : Int) : List<Pair<Long, Int>?> {
         return competitorsStateFlow.value.mapIndexed { index, item ->
             if(item.splits.size > splitIndex)
                  Pair(item.splits[splitIndex] - item.startTime, index)
             else
                 null
         }
    }

    fun racePositionIndxs(itemIndex : Int, splitIndex : Int) : RacePositionInd {
        val splits = competitorsStateFlow.value[itemIndex].splits
        if( splitIndex >= splits.size)  return RacePositionInd(-1, -1, -1, -1)

        val allSplits = splitsSlice(splitIndex).mapNotNull { item -> item }.sortedBy { it.first }
        if(allSplits.size == 1) return RacePositionInd(1, -1, -1, 1)
        var position  = 0

        var chaseInd = -1;
        for(i in 0..<allSplits.size) {
            position++
            if(allSplits[i].second == itemIndex) break
        }
        return RacePositionInd(position,
            if(position > 1) allSplits[position-2].second else -1,
            if(position < allSplits.size) allSplits[position].second else -1,
            allSplits.size)
    }

    fun racePositionInfo(itemIndex : Int, splitIndex : Int) : RacePositionItems {
        val indxs = racePositionIndxs(itemIndex, splitIndex)

        return RacePositionItems(indxs.currentPosition,
                       if(indxs.leaderInd != -1) competitorsStateFlow.value[indxs.leaderInd] else null,
                       if(indxs.chaserInd != -1) competitorsStateFlow.value[indxs.chaserInd] else null,
                                indxs.numCompleted)
    }

    fun onTimeTrialStarted(startTime : Long) {
        if(!_timeTrialStarted.value) {
            _startTime.update {  startTime }
            _timeTrialStarted.update {  true}
        }
    }

}