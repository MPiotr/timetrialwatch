package com.github.mpiotr.competitionwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mpiotr.competitionwatch.placeholder.PlaceholderContent.Competitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class CompetitorViewModel : ViewModel() {

    var competitors  = listOf(
        Competitor("1", "1", "John Dow",  1, 18, 0 ),
        Competitor("2", "2", "Dow Jones", 1, 19, 0 ),
        Competitor("3", "3", "Rocky Balboa", 1, 19, 0 ),
        Competitor("4", "5", "Conan the Barbarian" ) ,
        Competitor("5", "7", "Scorpion" ),
        Competitor("6", "9", "Shang Tsung" ),
        Competitor("7", "10", "Vasya Pupkin" )
    )

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

    fun onItemChanged(updated: Competitor)
    {
        _competitorsStateFlow.update { list ->
            list.map { if(it.id == updated.id) updated else it}
        }
    }
    fun onItemAdded( updated: Competitor)
    {
        _competitorsStateFlow.update {
            list ->list + updated
        }
    }

    fun newCompetitor() : Competitor {
        val nexid = _competitorsStateFlow.value.size + 1
        return Competitor(nexid.toString(), "", "", 0, 0, 0)
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
            }.take(3)
        }.stateIn(viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList())




    fun onTimeTrialStarted(startTime : Long) {
        if(!_timeTrialStarted.value) {
            _startTime.value = startTime
            _timeTrialStarted.value = true
        }
    }

}