package com.github.mpiotr.competitionwatch


import android.app.Application
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.IOException
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.datastore.core.Serializer
import androidx.lifecycle.application
import androidx.room.withTransaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.collections.sortedWith
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transformLatest


class CompetitorViewModel(application : Application, val dao : CompetitorDao, val database: AppDatabase) : AndroidViewModel(application) {
    val colorPallete : List<Int> = listOf(Color.BLUE, Color.GREEN, Color.RED, Color.BLACK, Color.YELLOW)
    val colorNames : List<String> = listOf("Blue", "Green", "Red", "Black", "Yellow")
    var colorOrder : MutableList<Int> = mutableListOf()


    var waitDataset = false

    val _editCompetitor =MutableStateFlow(false)
    val editCompetitor = _editCompetitor.asStateFlow().stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false)
    fun changeEditMode(edit : Boolean){
        _editCompetitor.value = edit
    }


    val main_group_name = application.resources.getString(R.string.main_group_name)
    val _groupsStateFlow = MutableStateFlow(mutableSetOf<String>(main_group_name))
    val groups = dao.groups().stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        listOf(Groups(1L)))
    fun onCreateNewGroup()
    {
        viewModelScope.launch {
                val newid = dao.groups().first().size + 1L
                val new_group = Groups(newid, "New Group Name")
                dao.insertGroup(new_group)

        }
    }
    fun onGroupUpdated(updated : Groups) {
        viewModelScope.launch {
            dao.updateGroup(updated)
        }
    }
    val _groupIndex = mutableMapOf(main_group_name to 0)

// return colorSet.map{Pair(colorPallete[it], colorNames[it])}


    var competitors  = com.github.mpiotr.competitionwatch.competitors
    val competitorCountFlow = dao.competitorCount().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        0
    )
    var competitorCount: Int = 0
    //var registeredBibs : MutableSet<Bib>? = null


    val timeFlow =  flow { while(true) {
        emit(SystemClock.elapsedRealtime())
        delay(200)
    } }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        System.currentTimeMillis()
    )

    val _currentBib = MutableStateFlow(Bib(0,0))
    val currentBib = _currentBib.asStateFlow()
    val minRevision = MutableStateFlow(0L)
    fun selectBib(bib : Bib) {
        _currentBib.value = bib
        minRevision.value = 0L
    }





    @OptIn(ExperimentalCoroutinesApi::class)
    val currentItem = currentBib.transformLatest { bib ->
        combine(dao.getCompetitor(bib.bib_number, bib.bib_color), minRevision)
        {
                item, minrev ->
           /* if(item != null && item.revision >= minrev)
                item
            else
                null*/ item
        }.collect { emit(it)  }}.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null)

    val _currentSplit = MutableStateFlow(0)
    val currentSplit = _currentSplit.asStateFlow()
    fun selectSplit(i : Int) {
        _currentSplit.value = i
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
        for(c in competitors.withIndex())
            bibIndex[c.value.bib] = c.index

        viewModelScope.launch {
            competitorCountFlow.collect {value -> competitorCount = value }
           // _registeredBibs.collect { value -> registeredBibs = value.toMutableSet()}
        }

    }

    private var maxBibNumber : StateFlow<Int>  = dao.max_bib_number().stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        0)


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


    //private val _competitorsStateFlow = MutableStateFlow(competitors)
    //val competitorsStateFlow : StateFlow<List<Competitor> > =  _competitorsStateFlow.asStateFlow()
    val competitorsStateFlow : StateFlow<List<Competitor> > =
                     dao.getAll().stateIn(viewModelScope,
                                SharingStarted.WhileSubscribed(5_000),
                                emptyList())




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

        var nextid = competitorCount + 1L
        return Competitor(nextid, Bib(0,0), "", main_group_name, 1, 0)
    }

    fun arrangeStartTimes()
    {
        viewModelScope.launch {
            val start_interval = settings.value!!.start_interval_seconds
            val arranged = competitorsStateFlow.value.sortedWith { a, b ->
                if (a.group != b.group)
                    _groupIndex[a.group]?.minus(_groupIndex[b.group] ?: 0) ?: 0
                else if (a.sex != b.sex) -a.sex + b.sex
                else a.bib.compareTo(b.bib)
            }.mapIndexed { index, competitor ->
                if (!competitor.started)
                    competitor.copy(startTime = index * 1000L * start_interval + 30000)
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

        }
    }

      val nextStartingCompetitors: StateFlow<List<Competitor>> =
        competitorsStateFlow.map { list ->
            list.filter { !it.started }
                .sortedBy { it.startTime }
                .take(5)
        }
        .distinctUntilChanged{old, new ->
                            old.map { it.id to it.started } ==
                            new.map { it.id to it.started }
        }
        .stateIn(viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList<Competitor>())

    fun getResults() : Map<Pair<Int,String>,List<Competitor>> {
        val all_results = competitorsStateFlow.value.mapNotNull { if(it.splits.size > 0) it else null }.sortedWith { a, b ->

            if (a.group != b.group)  _groupIndex[a.group]?.minus(_groupIndex[b.group]?:0 ) ?: 0
            else if (a.sex != b.sex) -a.sex + b.sex
            else {
                if (a.finished != b.finished) {
                    if (a.finished) -1 else 1
                } else if (a.splits.size != b.splits.size) a.splits.size - b.splits.size
                else ((a.splits.last() - a.startTime) - (b.splits.last() - b.startTime)).toInt()
            }
        }
        var p =  0
        var prev_sex = all_results[0].sex;
        var prev_group = all_results[0].group
        for(item in all_results.withIndex())
        {
            if(item.value.sex != prev_sex || item.value.group != prev_group) p = 0
            p++
            item.value.result = p

            prev_sex = item.value.sex
            prev_group = item.value.group

        }
        return all_results.groupBy { competitor -> Pair(competitor.sex, competitor.group) }
    }


    fun onSplit(splitTime : Long, bib : Bib)
    {

        viewModelScope.launch {
            database.withTransaction {
                val item: Competitor = dao.getCompetitor(bib.bib_number, bib.bib_color).first()
                val group : Groups = dao.getGroup(item.group).first()
                Log.d("ITEMUPDATE", "!")
                val nextRevision = item.revision + 1
                minRevision.value = nextRevision

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
                val forceUpdate1 = dao.getCompetitor(bib.bib_number, bib.bib_color).first()
                val forceUpdate2 = dao.getAll().first()
                Log.d("SPLITS", "new split time for $bib: ${splitTime}")
            }
        }
    }

    fun splitsSlice(splitIndex : Int) : StateFlow<List<Pair<Long, Int>?>> {
        return competitorsStateFlow.map{ competitors ->
            competitors.mapIndexed { index, item ->
                if(item.splits.size > splitIndex)
                    Pair(item.splits[splitIndex] - item.startTime, index)
                else
                    null
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    val _racePositionCompleteRevision = MutableStateFlow(0L)
    val racePositionCompleteRevision = _racePositionCompleteRevision.asStateFlow()

    sealed interface RacePositionUiState {
        object Calculating : RacePositionUiState
        data class Data(val value: RacePositionItems) : RacePositionUiState
    }


    val racePositionCompleted : StateFlow<RacePositionUiState> =
        combine(currentItem,
            competitorsStateFlow,
            minRevision
             ) { current, all, minrev ->
            RacePositionUiState.Calculating
        }.flatMapLatest { combine(currentItem,
            competitorsStateFlow,
            minRevision
        ) { current, all, minrev ->
            val competitor = current
            if (competitor == null || competitor.revision < minrev ) {
                Log.d("RACE POSITION CALCULATION", "Rejected")
                RacePositionUiState.Calculating
            } else {
                val splits = competitor.splits
                val splitIndex = splits.size - 1
                Log.d("RACE POSITION CALCULATION", splitIndex.toString())

                try {
                    val allSplits =
                        all.map { item ->
                            if (item.splits.size > splitIndex && item.sex == competitor.sex && item.group == competitor.group) {
                                Pair((item.splits[splitIndex] - item.startTime), item)
                            } else
                                null
                        }.mapNotNull { item -> item }.sortedBy { it.first }


                    var position = 0
                    var found = false
                    for (i in 0..<allSplits.size) {
                        if (allSplits[i].second.id == competitor.id) {
                            found = true
                            if (allSplits[i].second.revision < minrev)
                                found = false
                            break
                        }
                        position++
                    }
                    if (found) {
                        val leader = if (position > 0) allSplits[position - 1] else null
                        val chaser =
                            if (position + 1 < allSplits.size) allSplits[position + 1] else null
                        Log.d(
                            "ALLSPLITS",
                            "Leader: ${leader?.second?.name}, size = ${leader?.second?.splits?.size}, ${leader?.second?.revision}"
                        )
                        Log.d(
                            "ALLSPLITS",
                            "Chaser: ${chaser?.second?.name}, size = ${chaser?.second?.splits?.size}, ${chaser?.second?.revision}"
                        )
                        RacePositionUiState.Data(RacePositionItems(position + 1, leader, chaser, allSplits.size))
                    } else
                         RacePositionUiState.Calculating
                } catch (e: Exception) {
                     RacePositionUiState.Calculating
                }
            }
        }
    }.filterNotNull().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue =  RacePositionUiState.Calculating
        )

/*
    fun racePositionCompleted(bib : Bib, splitIndex : Int) : StateFlow<RacePositionItems?> {
        return flow {
            val competitor = dao.getCompetitor(bib.bib_number, bib.bib_color).first()
            val splits = competitor.splits

            if (splitIndex > splits.size) {

                emit(RacePositionItems(-1, null, null, -1))
            }

            val allSplits =
                dao.getAll().first().map { item ->
                    if (item.splits.size > splitIndex && item.sex == competitor.sex && item.group == competitor.group)
                        Pair((item.splits[splitIndex] - item.startTime), item)
                    else
                        null
                }.mapNotNull { item -> item }.sortedBy { it.first }

            var position = 0
            var found = false
            for (i in 0..<allSplits.size) {
                if (allSplits[i].second.bib == bib) {
                    found = true
                    break
                }
                position++
            }

            if(!found) emit(RacePositionItems(-1, null, null, -1))

            /*Log.d("SPLITS (C):", "split index = $splitIndex")
            for(c in allSplits) {
                Log.d("SPLITS (C):", "${c.first}: ${c.second}")
            }*/

            val leader = if (position > 0) allSplits[position - 1].second else null
            val chaser = if (position + 1 < allSplits.size) allSplits[position + 1].second else null

            emit( RacePositionItems(position + 1, leader, chaser, allSplits.size))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )
    }*/

    val racePositionLive :  StateFlow<Pair<Competitor, RacePositionItems>?> =
        combine(currentItem, timeFlow, competitorsStateFlow) { competitor, msnow, all ->
            if(competitor == null ) {
                null
            }
            else {
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
                    if (position >= 0 && position < allSplits.size) allSplits[position]  else null

                Pair(competitor,RacePositionItems(position + 1, leader, chaser, allSplits.size))
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val isCurrentCompetitorFinishing : StateFlow<Boolean> =
        combine(currentItem, groups) {competitor, allgroup ->
            val _group = competitor?.group ?: return@combine false
            val group_info =  allgroup.first { _group == it.name }
            if(competitor.sex == 1)
                competitor.splits.size == group_info.num_splits_men - 1
            else
                competitor.splits.size == group_info.num_splits_women - 1
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )


    fun onTimeTrialStarted(startTime : Long) {
        if(settings.value != null) {
            onSettingsUpdated(settings.value!!.copy(competition_start_time = startTime))
        }
    }

    fun resultPdf(){
        val pdfDoc = PdfDocument()
        var page_count = 1
        var pageinfo = PdfDocument.PageInfo.Builder(842, 595, page_count).create()
        var pdfPage = pdfDoc.startPage(pageinfo)
        val titleOffsetX = 100.0f
        val tableOffsetX = 10.0f
        val titleOffsetY = 50.0f
        val titlePaint = Paint()
        val subTitlePaint = Paint()
        val textPaint = Paint()

        var x = titleOffsetX
        var y = titleOffsetY

        titlePaint.textSize = 24.0f
        subTitlePaint.textSize = 36.0f
        textPaint.textSize = 10.0f
        //titlePaint.measureText("Res") = 36.0f

        var canvas = pdfPage.canvas

        canvas.drawText("Results", titleOffsetX, titleOffsetY, titlePaint)

        y += titlePaint.textSize + 2.5f
        val result = getResults()
        for( kvpair in result) {
            val sexname = if (kvpair.key.first == 1) application.resources.getString(R.string.men)
                          else                       application.resources.getString(R.string.women)
            y += subTitlePaint.textSize*1.0f
            canvas.drawText("$sexname group ${kvpair.key.second}", titleOffsetX, y, subTitlePaint)
            y += subTitlePaint.textSize*0.5f
            canvas.drawLine(10.0f, y, 400.0f, y, textPaint)
            y += subTitlePaint.textSize + 0.5f


            x = tableOffsetX
            for(c in kvpair.value){
                Log.d("PDF WRITE", "${c.name}: num $x, $y")
                canvas.drawText(c.result.toString(), x, y,  textPaint)
                x += 10

                Log.d("PDF WRITE", "${c.name}: name $x, $y")
                canvas.drawText(c.name, x, y,  textPaint)
                x += 200

                for(s in c.formattedSplitsRaceTime())
                {
                    Log.d("PDF WRITE", "${c.name}: $x, $y")
                    canvas.drawText(s, x, y,  textPaint)
                    x += 75
                }
                x = tableOffsetX

                y += 15
                Log.d("PDF WRITE", "${c.name}: $x, $y")
                if(y > 560)
                {
                    pdfDoc.finishPage(pdfPage)
                    page_count ++
                    PdfDocument.PageInfo.Builder(842, 595, page_count).create()
                    pdfPage = pdfDoc.startPage(PdfDocument.PageInfo.Builder(842, 595, page_count).create())
                    y = titleOffsetY
                }
            }


        }
        pdfDoc.finishPage(pdfPage)

        //openFileOutput(file, Context.MODE_PRIVATE)


        //val extpath = getExternalStorageDirectory()
        getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)


        val myExternalFile =
            File(getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "result.pdf")
        Log.d("PDF WRITE", "$myExternalFile")
        val stream = FileOutputStream(myExternalFile )
        pdfDoc.writeTo(stream)
        pdfDoc.close()
    }

}