package com.github.mpiotr.competitionwatch

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(context: Context, viewModel: CompetitorViewModel, modifier : Modifier, onNavigateToList : () -> Unit)
{
    val cs = MaterialTheme.colorScheme
    LaunchedEffect(Unit) {
        Log.d("THEME_TRACE", "Settings: Primary = ${cs.primary}")
        Log.d("THEME_TRACE", "Settings: OnPrimary = ${cs.onPrimary}")
    }
    Scaffold(modifier = modifier.fillMaxSize(),
        topBar = {
            Row(Modifier.height(64.dp)//.background(Color.Blue)
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Text("Setup",
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp))
            }
        },
        bottomBar = {
            Row(horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()) {
                LaunchedEffect(Unit) {
                    Log.d("THEME_TRACE", "Settings Bottom Button: Primary = ${cs.primary}")
                    Log.d("THEME_TRACE", "Settings Bottom Button: OnPrimary = ${cs.onPrimary}")
                }
                Button(
                    {onNavigateToList()},
                    Modifier.padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
                    content = { Text(stringResource(R.string.to_list)) },
                )
            }
        }
    )
    { innerPadding ->

        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth().padding(innerPadding))
        {
            val settings = viewModel.settings.collectAsState()
            if(settings.value == null) return@Scaffold
            if(viewModel.timeTrialStarted.collectAsState().value) {
                Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("The competition has started")
                    }
                }
                return@Scaffold
            }

            val groups = viewModel.groups.collectAsState()

            TextField(
                settings.value!!.start_interval_seconds.toString(),
                { updated ->
                    settings.value!!.copy(
                        start_interval_seconds = updated.toIntOrNull() ?: 15
                    )
                },

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Starting interval, s") },
                modifier = Modifier.onFocusChanged(
                    { viewModel.onSettingsUpdated(settings.value!!) }).fillMaxWidth()
            )

            LazyColumn() {
                for (g in groups.value) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(4.dp, 9.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        {
                            TextField(
                                g.name,
                                { updated ->
                                    viewModel.onGroupUpdated(
                                        g.copy(
                                            name = updated.trim()
                                        )
                                    )
                                },
                                label = { Text(stringResource(R.string.group_name)) },
                            )
                        }
                        Row(modifier = Modifier.padding(10.dp)) {
                            Column {
                                Text( stringResource(R.string.men))
                                TextField(
                                    g.num_splits_men.toString(),
                                    { updated ->
                                        viewModel.onGroupUpdated(
                                            g.copy(
                                                num_splits_men = updated.toIntOrNull() ?: 4
                                            )
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    label = { Text("Max number of splits") },
                                    modifier = Modifier.width(150.dp)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1.0f))
                            Column {
                                Text(stringResource(R.string.women))
                                TextField(
                                    g.num_splits_women.toString(),
                                    { updated ->
                                        viewModel.onGroupUpdated(
                                            g.copy(
                                                num_splits_women = updated.toIntOrNull() ?: 4
                                            )
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    label = { Text("Max number of splits") },
                                    modifier = Modifier.width(150.dp)
                                )
                            }
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.weight(1.0f))
                FloatingActionButton({
                    viewModel.onCreateNewGroup()
                }, modifier = Modifier.padding(horizontal = 16.dp), shape = CircleShape) {
                    Icon(Icons.Filled.Add, "Localized description")
                }
            }

        }
    }
}