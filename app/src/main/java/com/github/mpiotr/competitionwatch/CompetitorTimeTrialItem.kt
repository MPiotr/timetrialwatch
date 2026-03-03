package com.github.mpiotr.competitionwatch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.github.mpiotr.competitionwatch.dataset.Competitor

@Composable
fun CompetitorTimeTrialItem(item : Competitor, index : Int, autostart : Boolean, modifier : Modifier, viewModel: CompetitorViewModel, onAudioEvent : ()->Unit)
{
    val comp_start_time by viewModel.startTime.collectAsState()



    var name by remember(item.id) { mutableStateOf(item.name) }
    var bib_number by remember(item.id) { mutableStateOf(item.bib) }
    var sex by remember(item.id) { mutableStateOf(item.sex) }
    var started by remember(item.id) {mutableStateOf(item.started) }
    var start_time by remember(item.id) {mutableStateOf(item.startTime) }
    val msnow by viewModel.timeFlow.collectAsState()


    LaunchedEffect(item.startTime) {
        start_time = item.startTime
    }
    LaunchedEffect(item.name) {  if (name != item.name) name = item.name   }
    LaunchedEffect(item.bib) {   if (bib_number != item.bib) bib_number = item.bib}
    LaunchedEffect(item.started) {   if (started != item.started) started = item.started}
    LaunchedEffect(item.startTime) {   if (start_time != item.startTime) start_time = item.startTime}


    if(autostart && index == 0)
    {
        Column(modifier = Modifier.fillMaxWidth().background(
            color = if(!item.started) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryFixed),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val timeToStartString = item.timeBeforeStart(msnow, comp_start_time)
            val timeToStartMs = -(msnow - start_time - comp_start_time)
            if (timeToStartMs < 3000 && timeToStartMs > 0) {
                onAudioEvent()
            }
            if (timeToStartMs <= 0 && timeToStartMs > -10000L &&!item.started){

                started = true
                start_time = System.currentTimeMillis()
                viewModel.onItemChanged(
                    item.copy(
                        started = true,
                        startTime = start_time
                    )
                )
            }

            val textColor = if(!item.started) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryFixed

            Text(
                name,
                Modifier.padding(8.dp),
                style =  MaterialTheme.typography.headlineMedium,
                color = textColor
            )
            Text(
                "${bib_number.bib_number}",
                modifier = Modifier.padding(8.dp).wrapContentSize(),
                style =  MaterialTheme.typography.headlineSmall,
                color = textColor
            )
            if(!item.started) {
                AnimatedTimer(
                    timeToStartString,
                    Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                    MaterialTheme.typography.displayLarge,
                    color = textColor
                )
            }
            else {
                Text( "START", Modifier,
                    style =  MaterialTheme.typography.displayLarge,
                    fontFamily = FontFamily.Monospace,
                    color = textColor)
            }

        }
    }
    else {

        Row(
            modifier.fillMaxWidth().wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            Text(
                "$name (${if (sex == 1) stringResource(R.string.M) else stringResource(R.string.W)})",
                Modifier.padding(8.dp).weight(1f)
            )
            Text(
                "${bib_number.bib_number}",
                color = Color(viewModel.colorPallete[bib_number.bib_color]),
                modifier = Modifier.padding(8.dp).wrapContentSize()
            )

            if (!started) {
                Button(
                    {
                        started = true
                        start_time = System.currentTimeMillis()
                        viewModel.onItemChanged(
                            item.copy(
                                started = true,
                                startTime = start_time
                            )
                        )
                    },
                    Modifier.wrapContentWidth().padding(8.dp),
                    enabled = !autostart
                )
                {
                    val timeToStartString = item.timeBeforeStart(msnow, comp_start_time)
                    val timeToStartMs = -(msnow - item.startTime - comp_start_time)
                    if (timeToStartMs < 3000 && timeToStartMs > 0) onAudioEvent()
                    Column {
                        Text("Start in", style = MaterialTheme.typography.bodyMedium)
                        AnimatedTimer(timeToStartString, style = MaterialTheme.typography.bodyMedium)
                    }
                }

            } else {
                Text("Competitor has started", Modifier)
            }
        }
    }

}