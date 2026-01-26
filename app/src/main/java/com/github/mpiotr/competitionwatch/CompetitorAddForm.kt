package com.github.mpiotr.competitionwatch

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import androidx.compose.ui.Alignment.Companion
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp


@Composable
fun CompetitorAddForm(item : Competitor, viewModel: CompetitorViewModel, context : Context,
                      onItemChanged : (Competitor)-> Unit)
{
    var name by remember(item.id) { mutableStateOf(item.name) }
    var age by remember(item.id) { mutableStateOf(item.age) }
    var bib by remember(item.id) { mutableStateOf(item.bib) }
    var sex by remember(item.id) { mutableStateOf(item.sex) }
    var group by remember { mutableStateOf(item.group) }
    val focusRequester = remember { FocusRequester() }
    val edit = viewModel.editCompetitor.collectAsState()

    val bib_count = viewModel.countBib(bib).collectAsState()
    val focusManager = LocalFocusManager.current

    //val registred_bibs = viewModel.registredBibs.collectAsState()



    LaunchedEffect(item.name) {  if (name != item.name) name = item.name   }
    LaunchedEffect(item.bib) {   if (bib != item.bib) bib = item.bib}
    LaunchedEffect(item.age) {   if (age != item.age) age = item.age}
    LaunchedEffect(item.sex) {   if (sex != item.sex) sex = item.sex}
    LaunchedEffect(item.group) { if (group != item.group) group = item.group }

    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp))
    {
        TextField(
            name,
            { text ->
                name = text
            },
            enabled = !item.started,
            modifier = Modifier.fillMaxWidth().onFocusChanged({ focusState ->
                if (!focusState.isFocused && !item.started) {
                    onItemChanged(item.copy(name = name)) // changes upstream
                }
            }).focusRequester(focusRequester),
            label = { Text("Name") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions ( onNext = {focusManager.clearFocus(force = true)})
        )

        Row {
            TextField(
                bib.bib_number.toString(),

                { num ->
                    if (num.length > 0) {
                        try {
                            bib = Bib(num.toInt(), bib.bib_color)
                        } catch (e: Exception) {
                            Log.w("CompetitorItem.kt", "Error on string to Int")
                        }
                    }
                },

                enabled = !item.started,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions ( onNext = {focusManager.clearFocus(force = true)}),
                textStyle = TextStyle(color = Color(viewModel.colorPallete[bib.bib_color])),
                modifier = Modifier.width(100.dp)
                    .background(
                        Color(viewModel.colorPallete[bib.bib_color]), RectangleShape
                    )
                    .onFocusChanged({ focusState ->
                        if (!focusState.isFocused) {
                            if (bib_count.value == 0 || edit.value) {
                                onItemChanged(item.copy(bib = bib))
                            } else {
                                Toast.makeText(
                                    context,
                                    "Bib ${bib.bib_number} already registered",
                                    Toast.LENGTH_SHORT
                                ).show()
                                bib = Bib(0, 0)
                            }
                        }
                    }).focusRequester(focusRequester),
                label = { Text("Bib number") }


            )

            for((j, color) in viewModel.colorPallete.withIndex()){
                val c = Color(color)
                val colors = ButtonColors(c,c,c,c)
                Box( Modifier.width(50.dp)
                    .background( color = if(bib.bib_color == j) Color.LightGray else Color.Unspecified, RectangleShape),
                    contentAlignment = Alignment.Center) {
                    Button({
                        bib = Bib(bib.bib_number, j)
                        onItemChanged(item.copy(bib = bib))
                    }, colors = colors, shape = RectangleShape, modifier = Modifier.width(40.dp)) {}
                }
            }
        }

        Row {
            Spacer(Modifier.weight(0.33f))
            GroupSelectorBox(
                item.group, viewModel,
                { newgroup ->
                    onItemChanged(item.copy(group = newgroup))
                })
            Spacer(Modifier.weight(0.33f))
            GenderSelectorBox(item.sex, { newsex ->
                onItemChanged(item.copy(sex = newsex))
            })
            Spacer(Modifier.weight(0.33f))
        }

    }

}