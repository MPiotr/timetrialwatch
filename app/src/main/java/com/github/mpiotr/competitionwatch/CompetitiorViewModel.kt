package com.github.mpiotr.competitionwatch


import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.media.SoundPool
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.github.mpiotr.competitionwatch.dataset.AppDatabase
import com.github.mpiotr.competitionwatch.dataset.Bib
import com.github.mpiotr.competitionwatch.dataset.Competitor
import com.github.mpiotr.competitionwatch.dataset.CompetitorDao
import com.github.mpiotr.competitionwatch.dataset.Groups
import com.github.mpiotr.competitionwatch.dataset.RacePositionItems
import com.github.mpiotr.competitionwatch.dataset.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.milliseconds


class CompetitorViewModel(application : Application,
                          val dao : CompetitorDao,
                          val database: AppDatabase
                          ) : AndroidViewModel(application) {
    val colorPallete : List<Int> = listOf(Color.BLUE, Color.GREEN, Color.RED, Color.BLACK, Color.YELLOW)
    val colorNames : List<String> = listOf("Blue", "Green", "Red", "Black", "Yellow")
    var colorOrder : MutableList<Int> = mutableListOf()

    var soundPool : SoundPool? = null
    var soundId : Int? = null


    var waitDataset = false

    val _editCompetitor =MutableStateFlow(false)
    val editCompetitor = _editCompetitor.asStateFlow().stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false)
    fun changeEditMode(edit : Boolean){
        _editCompetitor.value = edit
    }


    val main_group_name = application.resources.getString(R.string.main_group_name)
    val groups = dao.groups().stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null)
    fun onCreateNewGroup()
    {
        viewModelScope.launch {
                val newid = dao.groups().first().size + 1L
                val new_group = Groups(newid, application.getString(R.string.newgroupname).format(newid))
                dao.insertGroup(new_group)

        }
    }
    fun onGroupUpdated(updated : Groups) {
        viewModelScope.launch {
            dao.updateGroup(updated)
        }
    }
    val _groupIndex = mutableMapOf(main_group_name to 0)


    val competitorCountFlow = dao.competitorCount().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        0
    )
    var competitorCount: Int = 0


    val timeFlow =  flow { while(true) {
        emit(System.currentTimeMillis())
        delay(100)
    } }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        System.currentTimeMillis()
    )

    val _currentBib =  MutableStateFlow< Map<Int, Bib>> (emptyMap())

    fun currentBib(id : Int): StateFlow<Bib> {
        return _currentBib.map { it[id] ?: Bib(0, 0) }.stateIn( viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            Bib(0, 0)
        )
    }
    val minRevision = MutableStateFlow(0L)
    fun isBibAlreadySelected(bib : Bib)  = _currentBib.map{ bibs -> bibs.containsValue(bib)}.stateIn( viewModelScope,
        SharingStarted.WhileSubscribed(1_000),
        false)

    fun selectBib(bib : Bib, id : Int)  {
        _currentBib.update { old -> old + (id to bib) }
        minRevision.value = 0L
    }

    val currentItemMap = mutableMapOf<Int, StateFlow<Competitor?>>()
    @OptIn(ExperimentalCoroutinesApi::class)
    fun currentItem(widgetId: Int) : StateFlow<Competitor?> {
        return currentItemMap.getOrPut(widgetId) {
            _currentBib
                .map { it[widgetId] }
                .filterNotNull()
                .flatMapLatest { bib ->
                    dao.getCompetitor(bib.bib_number, bib.bib_color)
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = null )
            }

    }

     val notYetFinished = dao.getAll()
             .map { all ->
                 all.mapNotNull { one ->
                     if (one.started && !one.finished)
                         one
                     else
                         null
                 }
             }
            .stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList<Competitor>())


    fun countBib(bib : Bib): StateFlow<Int> {
        return dao.countBib(bib.bib_number, bib.bib_color).stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            0
        )
    }

    fun getCompetitor(bib : Bib): StateFlow<Competitor> {
        return dao.getCompetitor(bib.bib_number, bib.bib_color).stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            newCompetitor()
        )
    }

    var bibIndex : MutableMap<Bib, Int> = mutableMapOf()

    init {
        viewModelScope.launch {
            competitorCountFlow.collect {value -> competitorCount = value }
        }
    }


    val settings : StateFlow<Settings?> = dao.settings().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val timeTrialStarted : StateFlow<Boolean> =
        dao.settings().map{
            it.competition_start_time != 0L
        }
            .stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        false)


    val competitorsStateFlow : StateFlow<List<Competitor> > =
                     dao.getAll().stateIn(viewModelScope,
                                SharingStarted.WhileSubscribed(5_000),
                                emptyList())

    val datasetInfo : StateFlow<String> =
        combine(competitorsStateFlow, settings) { competitors, settings ->

            if(competitors.size != 0 && settings != null ) {
                val started = competitors.filter({ it.started }).size
                val finished = competitors.filter({ it.finished }).size

                val start_time =
                    if (settings.competition_start_time != 0L)
                        competitors[0].formattedDayTime(settings.competition_start_time)
                    else ""


                var result = application.applicationContext.getString(R.string.databaseInfo)
                    .format(competitors.size, started, finished)
                if(start_time != "")
                    result += application.applicationContext.getString(R.string.competitionStartedAt).format(start_time)
                result
            }
            else {
                application.applicationContext.getString(R.string.databaseIsEmpty)
            }
        }.stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            "")




    val startTime : StateFlow<Long> = dao.settings().map { it.competition_start_time }.stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        0L)

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
        viewModelScope.launch {
            dao.update(updated)
        }
    }
    fun onItemAdded( added: Competitor)
    {
        competitorCount++
        viewModelScope.launch {
            waitDataset = true
            dao.insert(added)
            //registeredBibs!!.add(added.bib)
            bibIndex[added.bib] = competitorCount
            waitDataset = false
        }

    }

    fun onSettingsUpdated(updated: Settings) {
        viewModelScope.launch {
            dao.updateSettings( updated )
        }
    }

    fun newCompetitor() : Competitor {

        val nextid = competitorCount + 1L
        return Competitor(nextid, Bib(0, 0), "", main_group_name, 1, 0, startTime = 0L)
    }

    fun resetData()
    {
        viewModelScope.launch {
            database.withTransaction {
                dao.deleteAllCompetitors()
                dao.deleteAllGroups()
                dao.deleteAllSettings()
                val defaultSettings = Settings(1L, 15, 30, 0L)
                dao.insertSettings(defaultSettings)

                val defaultGroup = Groups(
                    1L,
                    application.applicationContext.getString(R.string.main_group_name),
                )
                dao.insertGroup(defaultGroup)
            }
        }
    }

    val _preStartUpdateComplete = MutableStateFlow(true)
    val startTimeReady : StateFlow<Boolean> = _preStartUpdateComplete.stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        true)

    fun arrangeStartTimes()
    {   _preStartUpdateComplete.value = false
        viewModelScope.launch {
            val _settings = dao.settings().first()
            //val comp_start_time = settings.value?.competition_start_time ?: 0L
            val start_interval = _settings.start_interval_seconds
            val start_offset = _settings.start_initial_offset_seconds
            val arranged = competitorsStateFlow.value.sortedWith { a, b ->
                if (a.group != b.group)
                    _groupIndex[a.group]?.minus(_groupIndex[b.group] ?: 0) ?: 0
                else if (a.sex != b.sex) -a.sex + b.sex
                else a.bib.compareTo(b.bib)
            }.filter({!it.started}).mapIndexed { index, competitor ->
                if (!competitor.started ) {
                        competitor.copy(startTime = index * 1000L * start_interval + start_offset*1000)

                }
                else competitor
            }
            dao.updateAll(arranged)


            val colorMap: MutableMap<Int, Int> = mutableMapOf()
            for (c in competitorsStateFlow.value) {
                val color = c.bib.bib_color
                if (colorMap.contains(color)) colorMap[color] = colorMap[color]!! + 1
                else colorMap[color] = 1
            }

            colorOrder = colorMap.map { Pair(it.value, it.key) }
                .sortedByDescending { it.first }.map{it.second}.toMutableList()
            _preStartUpdateComplete.value = true
            Log.d("START", "completed")
        }
    }

      val nextStartingCompetitors: StateFlow<List<Competitor>> =
        competitorsStateFlow.map { list ->
            list.filter { !it.started }
                .sortedBy { it.startTime }
                .take(7)
        }
        .distinctUntilChanged{old, new ->
                            old.map { it.id to it.started } ==
                            new.map { it.id to it.started }
        }
        .stateIn(viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList<Competitor>())

    val startingOrder: StateFlow<List<Competitor>> =
        competitorsStateFlow.map { list ->
            list.filter { !it.started }
                .sortedBy { it.startTime }

        }.stateIn(viewModelScope,
                started = SharingStarted.WhileSubscribed(1_000),
                initialValue = emptyList<Competitor>())

    fun getResults() : Map<Pair<Int,String>,List<Competitor>> {
        val all_results = competitorsStateFlow.value.mapNotNull { if(it.splits.size > 0) it else null }.sortedWith { a, b ->

            if (a.group != b.group)  _groupIndex[a.group]?.minus(_groupIndex[b.group]?:0 ) ?: 0
            else if (a.sex != b.sex) -a.sex + b.sex
            else {
                if (a.finished != b.finished) {
                    if (a.finished) -1 else 1
                } else if (a.splits.size != b.splits.size) b.splits.size - a.splits.size
                else ((a.splits.last() - a.startTime) - (b.splits.last() - b.startTime)).toInt()
            }
        }
        if(all_results.isEmpty()) return emptyMap()
        var p =  0
        var prev_sex = all_results[0].sex
        var prev_group = all_results[0].group
        var winner_splits = 0
        var winner_ind = 0
        for((j,item) in all_results.withIndex())
        {
            if(item.sex != prev_sex || item.group != prev_group) p = 0
            p++
            item.result = p
            if(p == 1) {
                item.gap = 0L
                winner_splits = item.splits.size
                winner_ind = j
            }
            else {
                val finished = item.splits.size == winner_splits
                item.gap = if(finished) (item.splits.last()- item.startTime) - (all_results[winner_ind].splits.last()-all_results[winner_ind].startTime)
                           else null
            }

            prev_sex = item.sex
            prev_group = item.group

        }
        return all_results.groupBy { competitor -> Pair(competitor.sex, competitor.group) }
    }


    fun onSplit(splitTime : Long, bib : Bib)
    {

        viewModelScope.launch {
            database.withTransaction {
                val item: Competitor = dao.getCompetitor(bib.bib_number, bib.bib_color).first()
                val group : Groups = dao.getGroup(item.group).first()
                val nextRevision = item.revision + 1
                minRevision.value = nextRevision

                if(!item.splits.isEmpty() &&  splitTime - item.splits.last() < 3000) return@withTransaction // Sanity check

                val updated_splits =
                    (item.splits + splitTime).toMutableList()
                var finished = false
                if (updated_splits.size == if(item.sex == 1) group.num_splits_men else group.num_splits_women)
                    finished = true
                val updated_item = item.copy(
                    splits = updated_splits,
                    finished = finished,
                    revision = nextRevision
                )
                dao.update(updated_item)
            }
        }
    }

    fun racePositionLive(id:Int) :  StateFlow<Pair<Competitor, RacePositionItems>?> {
        return  combine(currentItem(id), timeFlow, competitorsStateFlow) { competitor, msnow, all ->
            if (competitor == null) {
                null
            } else {
                val splits = competitor.splits
                val start_time = competitor.startTime
                val splitIndex = splits.size

                val allSplits =
                    all.map { item ->
                        if (item.splits.size > splitIndex && item.sex == competitor.sex && item.group == competitor.group)
                            Pair((item.splits[splitIndex] - item.startTime), item)
                        else
                            null
                    }.mapNotNull { item -> item }.sortedBy { it.first }

                var position = 0
                for (i in 0..<allSplits.size) {
                    if (allSplits[i].first > msnow - start_time) break
                    position++
                }

                val leader =
                    if (position > 0) allSplits[position - 1] else null
                val chaser =
                    if (position >= 0 && position < allSplits.size) allSplits[position] else null

                Pair(competitor, RacePositionItems(position + 1, leader, chaser, allSplits.size))
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
    }

    fun isCurrentCompetitorFinishing(id : Int) : StateFlow<Boolean> {
        return combine(currentItem(id), groups) { competitor, allgroup ->
            if(allgroup == null) return@combine false
            val _group = competitor?.group ?: return@combine false
            val group_info = allgroup.firstOrNull() { _group == it.name } ?: return@combine false

            if (competitor.sex == 1)
                competitor.splits.size == group_info.num_splits_men - 1
            else
                competitor.splits.size == group_info.num_splits_women - 1
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )
    }


    fun onTimeTrialStarted(startTime : Long) {
        if(settings.value != null) {
            onSettingsUpdated(settings.value!!.copy(competition_start_time = startTime))
        }
    }

    var startSoundPlaying by mutableStateOf(false)
        private set
    fun onSoundStart()
    {
        startSoundPlaying = true
        viewModelScope.launch {
            delay(4000)
            startSoundPlaying = false
        }
    }

    fun sendResultPDF() {
        thread {
            makeResultPDF(this, application ) { file ->
                val recipients = dao.allEmails().distinct()
                val intent = getEmailIntent(file, recipients, application)
                onSendEmails(intent)
            }
        }
    }

    fun onSendEmails(intent : Intent)
    {
        viewModelScope.launch {
            application.startActivity(intent)
        }
    }
}