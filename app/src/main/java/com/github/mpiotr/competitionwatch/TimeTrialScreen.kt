package com.github.mpiotr.competitionwatch

import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun TimeTrialScreen(viewModel: CompetitorViewModel, modifier: Modifier,
                    onNavigateToList : ()->Unit,
                    onNavigateToSplit : () -> Unit)
{
    val start_time by viewModel.startTime.collectAsState()
    val competitors = viewModel.competitorsStateFlow.collectAsState()
    val timeTrialStarted = viewModel.timeTrialStarted.collectAsState()
    val nextStartingCompetitors = viewModel.nextStartingCompetitors.collectAsState()

    var baseMs by remember { mutableLongStateOf(0L) }
    var elapsedMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(baseMs, elapsedMs) {
            val now = SystemClock.elapsedRealtime()
            elapsedMs = now - start_time
            delay(100) // 10 FPS precision

    }
    LaunchedEffect(nextStartingCompetitors) {

    }


    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if(!timeTrialStarted.value) {
            Button(
                {
                    viewModel.onTimeTrialStarted( SystemClock.elapsedRealtime())
                },
                modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp)
            )
            {
                Text("Start")
            }
        }
        else {
            Text((elapsedMs).toString(),
                modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp))
        }
        LazyColumn {
            items(nextStartingCompetitors.value){
                    competitor -> CompetitorTimeTrialItem(competitor, modifier, viewModel)
            }

        }

        Row(horizontalArrangement = Arrangement.Center) {

            Button({onNavigateToList()}, modifier.padding(8.dp)) {
                Text("Go to Participant List")
            }
            Button({onNavigateToSplit()}, modifier.padding(8.dp)) {
                Text("Go to Split")
            }
        }
    }
}