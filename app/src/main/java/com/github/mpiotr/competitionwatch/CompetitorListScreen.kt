package com.github.mpiotr.competitionwatch

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mpiotr.competitionwatch.Competitor

@Composable

    fun CompetitorList(viewModel: CompetitorViewModel,
                   modifier : Modifier = Modifier,
                   onNavigateToAdd : ()->Unit,
                   onNavigateToTimeTrial : ()->Unit)
{
    val competitors by viewModel.competitorsStateFlow.collectAsState();

    Scaffold(modifier = modifier.fillMaxSize(),
        topBar = {
            Row(Modifier.height(64.dp).background(Color.Blue).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Text("List of Participants",
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp))
            }
        },
        bottomBar = {
                       Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Button({ onNavigateToAdd() } ) {
                        Text("Add Participants")
                    }
                    Button({ onNavigateToTimeTrial() }) {
                        Text("Go to Start")
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
                modifier = Modifier.fillMaxSize().horizontalScroll(scroll_state).padding(innerPadding),
            ) {
                items(
                    items = competitors,
                    key = { it.id }
                )
                { competitor ->
                    CompetitorsItem(
                        competitor,
                        onItemChanged = { updated: Competitor -> viewModel.onItemChanged(updated) },
                        comp_start_time= viewModel.startTime.collectAsState().value
                    )
                }
            }

        }
    }
}
