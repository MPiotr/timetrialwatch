package com.github.mpiotr.competitionwatch

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(context: Context, viewModel: CompetitorViewModel, modifier : Modifier, onNavigateToList : () -> Unit)
{
    val focusManager = LocalFocusManager.current
    Scaffold(modifier = modifier.fillMaxSize(),
        topBar = {
            Row(Modifier.height(64.dp)//.background(Color.Blue)
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.settings),
                    fontSize = 24.sp,
                    modifier = Modifier.padding(start = 16.dp))
            }
        },
        bottomBar = {
            Row(horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()) {
                 Button(
                    {
                        focusManager.clearFocus(force = true)
                        onNavigateToList()
                    },
                    Modifier.padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
                    content = { Text(stringResource(R.string.to_list)) },
                )
            }
        }
    )
    { innerPadding ->
        val settings = viewModel.settings.collectAsState()
        if(settings.value == null) return@Scaffold
        if(viewModel.timeTrialStarted.collectAsState().value) {
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
        var showResetAlert by remember { mutableStateOf(false) }
        val info_string = viewModel.datasetInfo.collectAsState()
        val info_text = info_string.value

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(innerPadding))
        {
            item {
                Text(info_text, modifier = Modifier.fillMaxWidth())
                Button({ showResetAlert = true },
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        disabledContentColor = MaterialTheme.colorScheme.secondary,
                        disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer
                        ))
                { Text(stringResource(R.string.reset_data)) }
                if (showResetAlert) {
                    AlertDialog(
                        { showResetAlert = false },
                        confirmButton = {
                            Button({
                                showResetAlert = false
                                viewModel.resetData()
                            }

                            ) { Text("Yes") }
                        },
                        dismissButton = { Button({ showResetAlert = false },) { Text("No") } },
                        title = { Text(stringResource(R.string.reset_data_title)) }
                    )
                }



                HorizontalDivider(modifier = Modifier.padding(4.dp, 9.dp))

                val interval_initial_value = settings.value!!.start_interval_seconds.toString()
                var local_start_interval by remember { mutableStateOf(interval_initial_value) }
                LaunchedEffect(interval_initial_value) { local_start_interval = interval_initial_value}
                TextField(
                    local_start_interval,
                    { updated ->
                        local_start_interval = updated
                    },

                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(
                            focusDirection = FocusDirection.Next,
                        )
                    }),
                    label = { Text(stringResource(R.string.starting_interval)) },
                    modifier = Modifier.onFocusChanged(
                        {
                            val newvalue = local_start_interval.toIntOrNull()
                            if (newvalue != null)
                                viewModel.onSettingsUpdated(
                                    settings.value!!.copy(
                                        start_interval_seconds = newvalue
                                    )
                                )
                            else
                                local_start_interval = interval_initial_value
                        })
                        .fillMaxWidth().padding(top = 16.dp)

                )

                val offset_initial_value = settings.value!!.start_initial_offset_seconds.toString()
                var local_start_offset by remember { mutableStateOf(offset_initial_value) }
                LaunchedEffect(offset_initial_value) {local_start_offset = offset_initial_value}
                TextField(
                    local_start_offset,
                    { updated ->
                        local_start_offset = updated
                    },

                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(
                            focusDirection = FocusDirection.Next,
                        )
                    }),
                    label = { Text(stringResource(R.string.starting_offset)) },
                    modifier = Modifier.onFocusChanged({ focusState ->
                        if (!focusState.isFocused) {
                            val newvalue = local_start_offset.toIntOrNull()
                            if (newvalue != null)
                                viewModel.onSettingsUpdated(
                                    settings.value!!.copy(
                                        start_initial_offset_seconds = newvalue
                                    )
                                )
                            else local_start_offset = offset_initial_value
                        }
                    })
                        .fillMaxWidth().padding(top = 16.dp)
                )

                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.groups), fontSize = 20.sp)
            }

            if(groups.value != null) {
                    for (g in groups.value) {
                        item {
                            HorizontalDivider(modifier = Modifier.padding(4.dp, 9.dp))
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            {
                                var gname by remember { mutableStateOf(g.name) }
                                TextField(
                                    gname,
                                    { updated ->
                                        gname = updated
                                    },
                                    label = { Text(stringResource(R.string.group_name)) },
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(onNext = {
                                        focusManager.moveFocus(
                                            focusDirection = FocusDirection.Next,
                                        )
                                    }),
                                    modifier = Modifier.onFocusChanged(
                                        {
                                            viewModel.onGroupUpdated(
                                                g.copy(name = gname)
                                            )
                                        })
                                )
                            }
                            Row(modifier = Modifier.padding(10.dp)) {
                                Column {
                                    var numSplits by remember { mutableStateOf(g.num_splits_men.toString()) }
                                    Text(stringResource(R.string.men))
                                    TextField(
                                        numSplits,
                                        { updated ->
                                            numSplits = updated
                                            val newvalue = updated.toIntOrNull()
                                            if (newvalue != null) {
                                                viewModel.onGroupUpdated(
                                                    g.copy(
                                                        num_splits_men = newvalue
                                                    )
                                                )
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                        keyboardActions = KeyboardActions(onNext = {
                                            focusManager.moveFocus(
                                                focusDirection = FocusDirection.Next,
                                            )
                                        }),
                                        label = { Text(stringResource(R.string.max_number_of_splits)) },
                                        modifier = Modifier.width(150.dp).onFocusChanged(
                                            {
                                                if (numSplits.toIntOrNull() == null) {
                                                    numSplits = g.num_splits_men.toString()
                                                }
                                            })
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1.0f))
                                Column {
                                    var numSplits by remember { mutableStateOf(g.num_splits_women.toString()) }
                                    Text(stringResource(R.string.women))
                                    TextField(
                                        numSplits,
                                        { updated ->
                                            numSplits = updated
                                            val newvalue = updated.toIntOrNull()
                                            if (newvalue != null) {
                                                viewModel.onGroupUpdated(
                                                    g.copy(
                                                        num_splits_women = newvalue
                                                    )
                                                )
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                        keyboardActions = KeyboardActions(onNext = {
                                            focusManager.moveFocus(
                                                focusDirection = FocusDirection.Next,
                                            )
                                        }),
                                        label = { Text(stringResource(R.string.max_number_of_splits)) },
                                        modifier = Modifier.width(150.dp).onFocusChanged(
                                            {
                                                if (numSplits.toIntOrNull() == null) {
                                                    numSplits = g.num_splits_women.toString()
                                                }
                                            })
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Spacer(Modifier.weight(1.0f))
                            FloatingActionButton(
                                {
                                    viewModel.onCreateNewGroup()
                                },
                                modifier = Modifier.padding(horizontal = 16.dp),
                                shape = CircleShape
                            ) {
                                Icon(Icons.Filled.Add, "Localized description")
                            }
                        }
                    }

            }


        }
    }
}