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
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay


@Composable
fun SplitScreen(viewModel: CompetitorViewModel, modifier: Modifier,
                onNavigateToList : () -> Unit,
                onNavigateToStart: ()-> Unit)
{
    var nowms by remember { mutableLongStateOf(0L) }
    LaunchedEffect(nowms ) {
        val now = SystemClock.elapsedRealtime()
        nowms = now
        delay(200)
    }
    Scaffold(modifier = modifier.fillMaxSize(),
        topBar = {
            Row(Modifier.height(64.dp).background(Color.Blue).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Text("Splits and Finish",
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp))
            }
        },
        bottomBar = {
            Row(horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()) {
                Button({ onNavigateToStart() }, modifier) {
                    Text("Go to Start")
                }
                Button({ onNavigateToList() }, modifier) {
                    Text("Go to Participant List")
                }
            }
        }
    )
    { innerPadding ->
        Column(Modifier.fillMaxWidth().padding(innerPadding)) {
            Text(
                viewModel.formattedRaceTime(nowms),
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp),
                fontSize = 30.sp
            )
            CompetitorSplitItem(viewModel, modifier)
            HorizontalDivider()
            CompetitorSplitItem(viewModel, modifier)
        }
    }
}

