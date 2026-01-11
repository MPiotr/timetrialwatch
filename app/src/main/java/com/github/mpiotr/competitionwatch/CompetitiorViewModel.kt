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
import kotlin.collections.emptyList
import kotlin.collections.mapIndexed
import kotlin.collections.sortedWith
import kotlin.math.log10
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

class CompetitorViewModel : ViewModel() {
    data class DigitsForPicker(val maxBibNumber : Int, val numDigits : Int, val maxGreaterDigit : Int)
    data class RacePositionInd(val currentPosition : Int, val leaderInd : Int, val chaserInd : Int, val numCompleted : Int)
    {
        override fun toString() : String
        {
            return "current: $currentPosition, leader $leaderInd; chaser $chaserInd; numCompleted $numCompleted"
        }
    }
    data class RacePositionItems(val currentPosition : Int, val leader : Competitor?, val chaser : Competitor?, val numCompleted : Int)
    {
        override fun toString() : String
        {
            return "current: $currentPosition, leader: $leader, chaser $chaser, numCompleted $numCompleted"
        }
    }


    var competitors  = listOf(
        Competitor("1", "1", "Andrew Anderson",  1, 18, 0 ),
        Competitor("2", "2", "Bob Brown", 1, 19, 0 ),
        Competitor("3", "3", "Cute Candid", 1, 19, 0 ),
        Competitor("4", "4", "Donald the Dumb" )
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
        Log.d("SPLITS", "new split time for $itemIndex: ${splitTime}")
    }

    fun splitsSlice(splitIndex : Int) : StateFlow<List<Pair<Long, Int>?>> {
        return _competitorsStateFlow.map{ competitors ->
            competitors.mapIndexed { index, item ->
                if(item.splits.size > splitIndex)
                    Pair(item.splits[splitIndex] - item.startTime, index)
                else
                    null
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList<Pair<Long, Int>?>()
        )
    }

    fun racePositionCompleted(itemIndex : Int, splitIndex : Int) : RacePositionItems {
        val splits = competitorsStateFlow.value[itemIndex].splits
        val start_time =  competitorsStateFlow.value[itemIndex].startTime
        if( splitIndex > splits.size)  {

            return RacePositionItems(-1, null, null, -1)
        }

        val allSplits =
            competitorsStateFlow.value.mapIndexed {
                    index, item ->
                if(item.splits.size > splitIndex)
                    Pair((item.splits[splitIndex] - item.startTime), index)
                else
                    null}.mapNotNull {
                    item -> item }.sortedBy { it.first }

        var position  = 0
        for(i in 0..<allSplits.size) {
            if(allSplits[i].second == itemIndex ) break
            position++
        }

        val leader = if(position > 0) competitorsStateFlow.value[allSplits[position-1].second] else null
        val chaser = if(position +1 < allSplits.size) competitorsStateFlow.value[allSplits[position+1].second] else null

        return RacePositionItems(position+1,leader,chaser,allSplits.size)
    }

    fun racePositionLive(msnow : Long, itemIndex : Int, splitIndex : Int) : RacePositionItems {
        val splits = competitorsStateFlow.value[itemIndex].splits
        val start_time =  competitorsStateFlow.value[itemIndex].startTime
        if( splitIndex > splits.size)  {

            return RacePositionItems(-1, null, null, -1)
        }



        val allSplits =
            competitorsStateFlow.value.mapIndexed {
                    index, item ->
                if(item.splits.size > splitIndex)
                    Pair((item.splits[splitIndex] - item.startTime), index)
                else
                    null}.mapNotNull {
                    item -> item }.sortedBy { it.first }

        var position  = 0
        for(i in 0..<allSplits.size) {
            if(allSplits[i].first > msnow - start_time) break
            position++
        }

        val leader = if(position > 0) competitorsStateFlow.value[allSplits[position-1].second] else null
        val chaser = if(position  >= 0 && position < allSplits.size) competitorsStateFlow.value[allSplits[position].second] else null

        return RacePositionItems(position+1,leader,chaser,allSplits.size)
    }


    fun onTimeTrialStarted(startTime : Long) {
        if(!_timeTrialStarted.value) {
            _startTime.update {  startTime }
            _timeTrialStarted.update {  true}
        }
    }

}