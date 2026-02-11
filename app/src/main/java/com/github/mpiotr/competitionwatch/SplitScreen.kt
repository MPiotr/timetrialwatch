package com.github.mpiotr.competitionwatch

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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SplitScreen(viewModel: CompetitorViewModel, modifier: Modifier,
                onNavigateToList : () -> Unit,
                onNavigateToStart: ()-> Unit,
                onNavigateToResults: ()-> Unit)
{
    val nowms = viewModel.timeFlow.collectAsState()
    Scaffold(modifier = modifier.fillMaxSize(),
        topBar = {
            Row(Modifier.height(64.dp)
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.splits_and_finish),
                    fontSize = 24.sp,
                    modifier = Modifier.padding(start = 16.dp))
            }
        },
        bottomBar = {

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button({ onNavigateToStart() }, modifier) {
                        Text(stringResource(R.string.to_start))
                    }
                    Button({ onNavigateToResults() }, modifier) {
                        Text(stringResource(R.string.to_results))
                    }
                }



        }
    )
    { innerPadding ->
        Column(Modifier.fillMaxWidth().padding(innerPadding)) {
            if(viewModel.startTime.collectAsState().value != 0L) {
                Text(
                    viewModel.formattedRaceTime(nowms.value),
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp),
                    fontSize = 30.sp
                )
                CompetitorSplitItem(viewModel, modifier, 0)
                HorizontalDivider()
                CompetitorSplitItem(viewModel, modifier, 1)
            }
        }
    }
}

