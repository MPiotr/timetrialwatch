package com.github.mpiotr.competitionwatch

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddCompetitorDialog(context: Context, viewModel: CompetitorViewModel, modifier : Modifier, onNavigateToList : () -> Unit)
{
    val competitors by viewModel.competitorsStateFlow.collectAsState()
    val focusManager = LocalFocusManager.current
    val edit = viewModel.editCompetitor.collectAsState()
    val editItem = viewModel.currentItem(0).collectAsState()
    var item by remember { mutableStateOf(
        if(edit.value ) editItem.value  else viewModel.newCompetitor() )
    }
    LaunchedEffect(edit.value, editItem.value) {
        if(edit.value) item = editItem.value
    }
    if(item == null) return
    if(edit.value && item?.bib == Bib(0,0)) return


    var bibCount = viewModel.countBib(item!!.bib).collectAsState()

    Scaffold(modifier = modifier.fillMaxSize(),
        topBar = {
            Row(Modifier.height(64.dp)//.background(Color.Blue)
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Text(if(edit.value) "Edit Participant" else "Add new participant",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(start = 16.dp))
            }
        },
        bottomBar = {
            Row(horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()) {
                if(edit.value){
                    Button(
                        {
                            focusManager.clearFocus(force = true)
                            viewModel.onItemChanged(item!!.copy())
                            Toast.makeText(context,
                                context.getString(R.string.toast_on_competitor_changed).format(item!!.name),
                                Toast.LENGTH_SHORT).show()
                            viewModel.changeEditMode(false)
                            onNavigateToList()
                        },
                        Modifier.padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
                        content = { Text(stringResource(R.string.save)) },
                        enabled = (item!!.bib.bib_number != 0)
                    )
                }
                else {
                    Button(
                        {
                            focusManager.clearFocus(force = true)
                            viewModel.onItemAdded(item!!.copy())
                            Toast.makeText(context,
                                context.getString(R.string.toast_on_competitor_added).format(item!!.name),
                                Toast.LENGTH_SHORT)
                                .show()
                            item = viewModel.newCompetitor()
                        },
                        Modifier.padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
                        content = { Text(stringResource(R.string.add_and_next)) },
                        enabled = (bibCount.value == 0 && item!!.bib.bib_number != 0)

                    )
                    Button(
                            {onNavigateToList()},
                    Modifier.padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
                    content = { Text(stringResource(R.string.to_list)) },
                    )
                }

            }
        }
    )
    { innerPadding ->
        Column(Modifier.fillMaxWidth().padding(innerPadding)) {
            CompetitorAddForm(
                item!!,
                viewModel,
                context,
                { changed -> item = changed }
            )
        }
    }
}