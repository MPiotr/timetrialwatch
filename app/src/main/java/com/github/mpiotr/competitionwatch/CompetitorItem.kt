package com.github.mpiotr.competitionwatch



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mpiotr.competitionwatch.Competitor


@Composable
fun CompetitorsItem(item : Competitor, onItemChanged : (Competitor)-> Unit, modifier : Modifier = Modifier, horizontal: Boolean = true, comp_start_time: Long = 0)
{


    if(horizontal) {
        Row(
            modifier.fillMaxWidth(), //.align(Alignment.CenterVertically),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = { CompetitorContent(item, onItemChanged,  horizontal, comp_start_time) }
        )
    }
    else {
        Column(
            modifier.fillMaxWidth().padding(10.dp),
            content = { CompetitorContent(item, onItemChanged,  horizontal) }
        )

    }
}

@Composable
fun CompetitorContent(item : Competitor, onItemChanged : (Competitor)-> Unit,  horizontal : Boolean, comp_start_time : Long = 0L)
{
    var expanded by remember { mutableStateOf(false) }
    var name by remember(item.id) { mutableStateOf(item.name) }
    var age by remember(item.id) { mutableStateOf(item.age) }
    var bib_number by remember(item.id) { mutableStateOf(item.bib_number) }
    var sex by remember(item.id) { mutableStateOf(item.sex) }
    var team by remember { mutableStateOf("Team") }

    LaunchedEffect(item.name) {  if (name != item.name) name = item.name   }
    LaunchedEffect(item.bib_number) {   if (bib_number != item.bib_number) bib_number = item.bib_number}
    LaunchedEffect(item.age) {   if (age != item.age) age = item.age}
    LaunchedEffect(item.sex) {   if (sex != item.sex) sex = item.sex}

    Text(item.id)

    TextField(name,
        {
                text ->
            name = text
        },
        enabled = !item.started,
        modifier = (if(horizontal) Modifier.wrapContentSize() else  Modifier.fillMaxWidth())
            .onFocusChanged({focusState ->
            if(!focusState.isFocused && !item.started) { onItemChanged(item.copy(name=name)) }
                                            } ),
        label = {if(!horizontal) Text("Name") else null}

             )
    TextField(bib_number,
        { num ->
            bib_number = num
            onItemChanged(
                item.copy(bib_number = num)
            )
        },
        enabled = !item.started,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = (if(horizontal) Modifier.width(75.dp) else Modifier.fillMaxWidth()),
        label = {if(!horizontal) Text("Bib number") else null}
    )
    Text(item.group.toString())

    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End, modifier=Modifier.background(Color.LightGray, shape = RectangleShape))
    {
        Text(if (item.sex == 1) "Man" else "Woman",
            fontSize = 12.sp,
        )
        Box(Modifier.wrapContentSize()) {
                IconButton(onClick = { expanded = !expanded }, enabled = !item.started,)
                {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "", Modifier.size(24.dp))
                }
            DropdownMenu(expanded = expanded, { expanded = false }) {
                DropdownMenuItem(
                    { Text("Man", fontSize = 10.sp) },
                    {
                        expanded = false;
                        sex = 1
                        onItemChanged(
                            item.copy(sex = 1)
                        );
                    }
                )
                DropdownMenuItem(
                    { Text("Woman", fontSize = 10.sp) }, {
                        expanded = false
                        sex = 0
                        onItemChanged(
                            item.copy(sex = 0)
                        )
                    }
                )
            }
        }
    }

    TextField(team,
        {updated -> team = updated},
        enabled = !item.started,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = if(horizontal) Modifier.wrapContentSize() else Modifier.fillMaxWidth(),
        label = {if(!horizontal) Text("Team") else null}
    )

    if(item.started)
    {
        Text(item.formattedStartRaceTime(comp_start_time), modifier = Modifier.wrapContentWidth(), fontSize = 12.sp)
        for(s in item.formattedSplitsRaceTime()) {
            Text(s, modifier = Modifier.wrapContentWidth(), fontSize = 12.sp)
        }
    }

}

