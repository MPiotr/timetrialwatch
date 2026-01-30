package com.github.mpiotr.competitionwatch

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTrialScreen(viewModel: CompetitorViewModel, modifier: Modifier,
                    onNavigateToList : ()->Unit,
                    onNavigateToSplit : () -> Unit)
{
    val comp_start_time by viewModel.startTime.collectAsState()
    val timeTrialStarted = viewModel.timeTrialStarted.collectAsState()
    val nextStartingCompetitors = viewModel.nextStartingCompetitors.collectAsState()
    val vSettings by  viewModel.settings.collectAsState()
    val onTrack = viewModel.notYetFinished.collectAsState()
    val numOnTrack = onTrack.value.size


    var elapsedMs by remember { mutableLongStateOf(0L) }
    var showStopDialog = remember { mutableStateOf(false)}

    LaunchedEffect(comp_start_time, elapsedMs) {
            val now = SystemClock.elapsedRealtime()
            elapsedMs = now
            delay(100) // 10 FPS precision

    }
    LaunchedEffect(nextStartingCompetitors) {

    }



    Scaffold(modifier = modifier.fillMaxSize(),
        topBar = {
            Row(Modifier.height(64.dp)//.background(Color.Blue)
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Text("Starting order",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(start = 16.dp))
            }
        },
        bottomBar = {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {

                Button({ onNavigateToList() }, Modifier.padding(8.dp)) {
                    Text(stringResource(R.string.to_list))
                }
                Button({ onNavigateToSplit() }, Modifier.padding(8.dp)) {
                    Text("Go to Split")
                }
            }

        }
    )
    {iner_padding ->
        Column(Modifier.fillMaxSize().padding(iner_padding), horizontalAlignment = Alignment.CenterHorizontally) {
            if (!timeTrialStarted.value) {
                Button(
                    {
                        val start_time = SystemClock.elapsedRealtime()
                        viewModel.onTimeTrialStarted(start_time)
                    },
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp)
                )
                {
                    Text(stringResource(R.string.start_race))
                }
            } else {
                Text(
                    viewModel.formattedRaceTime(SystemClock.elapsedRealtime()),
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp),
                    fontSize = 30.sp
                )

                Text(
                    stringResource(R.string.next_participants_to_start),
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp),
                    fontSize = 18.sp
                )
                LazyColumn(Modifier.weight(1.0f)) {
                    itemsIndexed(nextStartingCompetitors.value) { ind, competitor ->
                        CompetitorTimeTrialItem(competitor, Modifier.background(if(ind+1 % 2 == 0)
                            MaterialTheme.colorScheme.surfaceVariant
                        else
                            MaterialTheme.colorScheme.surface, RectangleShape), viewModel)
                    }
                }

                if(numOnTrack > 0)
                    Text( stringResource(R.string.not_yet_finished).format(numOnTrack), fontSize = 18.sp)
                Button({
                    showStopDialog.value = true
                       }, modifier = Modifier.combinedClickable(
                    onClick = {
                        showStopDialog.value = true
                    },
                    onLongClick = {
                    },
                )) {
                    Text(stringResource(R.string.stop_race))
                }

                if(showStopDialog.value) {
                AlertDialog(
                    { showStopDialog.value = false},
                    confirmButton = { TextButton({
                        showStopDialog.value = false
                        viewModel.onSettingsUpdated(vSettings!!.copy(competition_start_time = 0L))
                    })
                    {Text("Yes")} },
                    dismissButton = { TextButton({showStopDialog.value = false}) {Text("No")} },
                    title = {Text("Do you really want to stop the race")},
                    text = {Text(if (numOnTrack != 0)
                                            stringResource(R.string.not_yet_finished).format(numOnTrack)
                                        else "")},
                    )
                }
            }
        }
    }
}