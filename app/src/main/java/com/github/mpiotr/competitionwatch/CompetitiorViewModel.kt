package com.github.mpiotr.competitionwatch


import android.app.Application
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileOutputStream
import java.lang.Math.pow
import kotlin.math.log10
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds


class CompetitorViewModel(application : Application) : AndroidViewModel(application) {
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
    data class Settings(val start_interval_seconds : Int, val max_num_of_splits : Int)  {}


//TODO bib_numbers uniuque!!!
    var competitors  = listOf(
        Competitor("1", "1", "Andrew Anderson",  1, 18, 0 ),
        Competitor("2", "2", "Bob Brown", 1, 19, 0 ),
        Competitor("3", "3", "Cute Candid", 0, 19, 0 ),
        Competitor("4", "4", "Donald the Dumb", 0),
        Competitor("5", "5", "Rich Royal",  1, 18, 0 ),
        Competitor("6", "6", "Fucking Fred", 1, 19, 0 ),
        Competitor("7", "7", "Gentle Genry", 0, 19, 0 ),
        Competitor("8", "8", "Honest Hue", 0),
        Competitor("9", "9", "Idilic Idol",  1, 18, 0 ),
        Competitor("10", "10", "John Wick", 1, 19, 0 ),
        Competitor("11", "11", "Keen Kate", 0, 19, 0 ),
        Competitor("12", "12", "Lame Larry", 0),
        Competitor("13", "13", "Mindful Michelangelo",  1, 18, 0 ),
        Competitor("14", "14", "Neat Natan", 1, 19, 0 ),
        Competitor("15", "15", "Optimistic Oppenheimer", 0, 19, 0 ),
        Competitor("16", "16", "Picky Peter", 1),

    )

    var bibIndex : MutableMap<String, Int> = mutableMapOf()
    init {
        for(c in competitors.withIndex())
            bibIndex[c.value.bib_number] = c.index
    }

    private var maxBibNumber : Int = 0

    var max_number_or_splits = 4
    var start_interval = 30
    private val _settings = MutableStateFlow(Settings(30, 4))
    val settings : StateFlow<Settings> = _settings.asStateFlow()

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

    fun onSettingsUpdated(updated: Settings)
    {
        _settings.value = updated
    }

    fun newCompetitor() : Competitor {
        val nexid = _competitorsStateFlow.value.size + 1
        return Competitor(nexid.toString(), "", "", 0, 0, 0)
    }

    fun arrangeStartTimes()
    {
        val start_interval = _settings.value.start_interval_seconds

        _competitorsStateFlow.update { list ->
            list.sortedWith { a, b ->
                if (a.sex != b.sex) -a.sex + b.sex
                else if (a.group != b.group) a.group - b.group
                else a.bib_number.toInt() - b.bib_number.toInt()
            }
                .mapIndexed { index, competitor -> if(!competitor.started) competitor.copy(startTime = index * 1000L*start_interval + 30000) else competitor }
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

    fun getResults() : Map<Pair<Int,Int>,List<Competitor>> {
        val all_results = _competitorsStateFlow.value.sortedWith { a, b ->

            if (a.sex != b.sex) -a.sex + b.sex
            else if (a.group != b.group) a.group - b.group
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


    fun onSplit(splitTime : Long, itemIndex : Int)
    {
        _competitorsStateFlow.value[itemIndex].splits.add(splitTime)
        if(_competitorsStateFlow.value[itemIndex].splits.size == _settings.value.max_num_of_splits)
            _competitorsStateFlow.value[itemIndex].finished = true
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
            val sexname = if (kvpair.key.first == 1) "Men" else "Women"
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
        val stream = FileOutputStream(myExternalFile )
        pdfDoc.writeTo(stream)
        pdfDoc.close()
    }

}