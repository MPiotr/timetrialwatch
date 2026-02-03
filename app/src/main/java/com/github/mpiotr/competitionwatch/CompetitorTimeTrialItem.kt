package com.github.mpiotr.competitionwatch

import android.os.SystemClock
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.mpiotr.competitionwatch.dataset.Competitor
import kotlinx.coroutines.delay

@Composable
fun CompetitorTimeTrialItem(item : Competitor, modifier : Modifier, viewModel: CompetitorViewModel, onAudioEvent : ()->Unit)
{
    val trial_started by viewModel.timeTrialStarted.collectAsState()
    val comp_start_time by viewModel.startTime.collectAsState()



    var name by remember(item.id) { mutableStateOf(item.name) }
    var bib_number by remember(item.id) { mutableStateOf(item.bib) }
    var sex by remember(item.id) { mutableStateOf(item.sex) }
    var started by remember(item.id) {mutableStateOf(item.started) }
    var start_time by remember(item.id) {mutableStateOf(item.startTime) }
    var msnow by remember { mutableLongStateOf(0L) }


    LaunchedEffect(msnow, comp_start_time, item.startTime) {
        msnow = System.currentTimeMillis()
        start_time = item.startTime
        delay(200)
    }
    LaunchedEffect(item.name) {  if (name != item.name) name = item.name   }
    LaunchedEffect(item.bib) {   if (bib_number != item.bib) bib_number = item.bib}
    LaunchedEffect(item.started) {   if (started != item.started) started = item.started}
    LaunchedEffect(item.startTime) {   if (start_time != item.startTime) start_time = item.startTime}


    Row(
        modifier.fillMaxWidth().wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Text("$name ( ${if(sex == 1) "M" else "W"})", Modifier.padding(8.dp).weight(1f))
        Text("${bib_number.bib_number}",
            color = Color(viewModel.colorPallete[bib_number.bib_color]),
            modifier = Modifier.padding(8.dp).wrapContentSize())
        if(trial_started) {
            if (!started) {
                Button({
                                    started = true
                                    start_time = System.currentTimeMillis()
                                    viewModel.onItemChanged(item.copy(started = true, startTime = start_time))
                                },
                       Modifier.wrapContentWidth().padding(8.dp)
                    )
                {
                    val timeToStartString = item.timeBeforeStart(msnow, comp_start_time)
                    /*Log.d("TIMING", "${item.formattedDayTime(msnow)}, " +
                            "${item.formattedDayTime(item.startTime )}, " +
                            "${item.formattedDayTime(comp_start_time )},")*/
                    val timeToStartMs = -(msnow - item.startTime - comp_start_time)
                    if(  timeToStartMs < 3000 && timeToStartMs > 0 ) onAudioEvent()
                    Text("Start in \n $timeToStartString", Modifier)
                }
            } else {
                Text("Competitor has started", Modifier)
            }
        }
    }

}