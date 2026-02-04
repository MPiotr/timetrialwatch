package com.github.mpiotr.competitionwatch

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mpiotr.competitionwatch.dataset.Competitor

@Composable

    fun CompetitorList(viewModel: CompetitorViewModel,
                   modifier : Modifier = Modifier,
                   onNavigateToAdd : ()->Unit,
                   onNavigateToTimeTrial : ()->Unit,
                   onNavigateToSettings : () -> Unit)
{
    val competitors by viewModel.competitorsStateFlow.collectAsState()

    Scaffold(modifier = modifier.fillMaxSize(),
        topBar = {
            Row(Modifier.height(64.dp)//.background(Color.Blue)
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Text("List of Participants",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(start = 16.dp))
            }
        },
        bottomBar = {
                       Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Button({ viewModel.changeEditMode(false); onNavigateToSettings() } ) {
                        Text(stringResource(R.string.goto_settings))
                    }
                    Button({ onNavigateToTimeTrial() }) {
                        Text(stringResource(R.string.to_start))
                    }
                }

        }
    )
    {
        innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        )
        {
            val scroll_state = rememberScrollState()
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                userScrollEnabled = true,
                modifier = Modifier.horizontalScroll(scroll_state).padding(innerPadding).weight(1.0f),
            ) {
                itemsIndexed(
                    items = competitors,
                    { ind : Int, item  : Competitor -> item.id }
                )
                {

                    ind, competitor ->
                    CompetitorListItem(
                        competitor,
                        viewModel,
                        comp_start_time= viewModel.startTime.collectAsState().value,
                        onNavigateToEdit = onNavigateToAdd,
                        modifier = Modifier.background(if(ind % 2 == 0)
                            MaterialTheme.colorScheme.surfaceVariant
                        else
                            MaterialTheme.colorScheme.surface, RectangleShape)
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.weight(1.0f))
                FloatingActionButton({
                     viewModel.changeEditMode(false); onNavigateToAdd()
                }, modifier = Modifier.padding(end = 16.dp, bottom = 50.dp), shape = CircleShape) {
                    Icon(Icons.Filled.Add, stringResource(R.string.add_participant))
                }
            }

        }
    }
}
