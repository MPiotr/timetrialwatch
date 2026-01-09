package com.github.mpiotr.competitionwatch

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddCompetitorDialog(context: Context, viewModel: CompetitorViewModel, modifier : Modifier, onNavigateToList : () -> Unit)
{
    val competitors by viewModel.competitorsStateFlow.collectAsState()


    var item by remember { mutableStateOf(
        viewModel.newCompetitor() )
    }

    Column {
        CompetitorsItem(
            item,
            { updated ->
                item = updated
            }, horizontal = false
        )

        Row(modifier.align(Alignment.CenterHorizontally).fillMaxWidth(), horizontalArrangement = Arrangement.Center){
            Button(
                {
                    viewModel.onItemAdded(item.copy())
                    Toast.makeText(context, " ${item.name} added", Toast.LENGTH_SHORT).show()
                    item = viewModel.newCompetitor()
                },
                modifier.padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
                content = { Text("Add and go next") },
            )
            Button(
                {onNavigateToList()},
                modifier.padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
                content = { Text("Go to List") },
            )
        }
    }
}