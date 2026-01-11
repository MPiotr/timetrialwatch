package com.github.mpiotr.competitionwatch

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun TimeTrialScreen(viewModel: CompetitorViewModel, modifier: Modifier,
                    onNavigateToList : ()->Unit,
                    onNavigateToSplit : () -> Unit)
{
    val comp_start_time by viewModel.startTime.collectAsState()
    val timeTrialStarted = viewModel.timeTrialStarted.collectAsState()
    val nextStartingCompetitors = viewModel.nextStartingCompetitors.collectAsState()
    val vSettings by  viewModel.settings.collectAsState()

    var settings =  remember   { mutableStateOf(CompetitorViewModel.Settings(30, 4)) }




    var elapsedMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(comp_start_time, elapsedMs) {
            val now = SystemClock.elapsedRealtime()
            elapsedMs = now
            delay(100) // 10 FPS precision

    }
    LaunchedEffect(vSettings) {
        settings.value = vSettings

    }
    LaunchedEffect(nextStartingCompetitors) {

    }



    Scaffold(modifier = modifier.fillMaxSize(),
        topBar = {
            Row(Modifier.height(64.dp).background(Color.Blue).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Text("Starting order",
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp))
            }
        },
        bottomBar = {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Blue, RectangleShape),
                verticalAlignment = Alignment.Bottom
            ) {

                Button({ onNavigateToList() }, Modifier.padding(8.dp)) {
                    Text("Go to Participant List")
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
                Column()
                {

                    TextField(settings.value.start_interval_seconds.toString(),
                        {
                        updated -> settings.value = settings.value.copy(start_interval_seconds =  updated.toIntOrNull() ?: 15)},

                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text("Starting interval, s")},
                        modifier = Modifier.onFocusChanged(
                            {viewModel.onSettingsUpdated(settings.value)})
                        )
                    TextField(settings.value.max_num_of_splits.toString(),
                        {
                            updated -> settings.value =settings.value.copy(max_num_of_splits =  updated.toIntOrNull() ?: 4)},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Max number of splits")} ,
                        modifier = Modifier.onFocusChanged(
                            {viewModel.onSettingsUpdated(settings.value)})
                    )
                    Button(
                        {
                            val start_time = SystemClock.elapsedRealtime()
                            viewModel.arrangeStartTimes()
                            viewModel.onTimeTrialStarted(start_time)
                        },
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 10.dp)
                    )
                    {
                        Text("Start Competitions")
                    }
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
                    "Next participant to start",
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp),
                    fontSize = 18.sp
                )
                LazyColumn(Modifier.fillMaxSize()) {
                    items(nextStartingCompetitors.value) { competitor ->
                        CompetitorTimeTrialItem(competitor, Modifier, viewModel)
                    }
                }
            }
        }
    }
}