package com.github.mpiotr.competitionwatch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SplitScreen(viewModel: CompetitorViewModel, modifier: Modifier,
                onNavigateToList : () -> Unit,
                onNavigateToStart: ()-> Unit)
{
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
            CompetitorSplitItem(viewModel, modifier)
        }
    }
}

