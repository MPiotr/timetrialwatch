package com.github.mpiotr.competitionwatch



import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mpiotr.competitionwatch.placeholder.PlaceholderContent.Competitor
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds


@Composable
fun CompetitorsItem(item : Competitor, onItemChanged : (Competitor)-> Unit, modifier : Modifier = Modifier, horizontal: Boolean = true)
{



    if(horizontal) {
        Row(
            modifier.fillMaxWidth(), //.align(Alignment.CenterVertically),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            content = { CompetitorContent(item, onItemChanged, modifier, horizontal) }
        )
    }
    else {
        Column(
            modifier.fillMaxWidth().padding(10.dp),
            content = { CompetitorContent(item, onItemChanged, modifier, horizontal) }
        )

    }
}

@Composable
fun CompetitorContent(item : Competitor, onItemChanged : (Competitor)-> Unit, modifier : Modifier = Modifier, horizontal : Boolean)
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
        modifier = (if(horizontal) modifier.wrapContentSize() else  modifier.fillMaxWidth())
            .onFocusChanged({focusState ->
            if(!focusState.isFocused) { onItemChanged(item.copy(name=name)) }
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = (if(horizontal) modifier.width(75.dp) else modifier.fillMaxWidth()),
        label = {if(!horizontal) Text("Bib number") else null}
    )
    Text(item.group.toString())

    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End, modifier=modifier.background(Color.LightGray, shape = RectangleShape))
    {
        Text(if (item.sex == 1) "Man" else "Woman",
            fontSize = 12.sp,
        )
        Box(modifier.wrapContentSize()) {
                IconButton(onClick = { expanded = !expanded })
                {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "", modifier.size(24.dp))
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = if(horizontal) modifier.width(75.dp) else modifier.fillMaxWidth(),
        label = {if(!horizontal) Text("Team") else null}
    )

}

@Composable
fun CompetitorTimeTrialItem(item : Competitor, modifier : Modifier, viewModel: CompetitorViewModel)
{
    val competitors by viewModel.competitorsStateFlow.collectAsState()
    val trial_started by viewModel.timeTrialStarted.collectAsState()


    var name by remember(item.id) { mutableStateOf(item.name) }
    var bib_number by remember(item.id) { mutableStateOf(item.bib_number) }
    var sex by remember(item.id) { mutableStateOf(item.sex) }
    var started by remember(item.id) {mutableStateOf(item.started) }
    var start_time by remember(item.id) {mutableStateOf(item.startTime) }
    var elapsedMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(elapsedMs) {
        val now = SystemClock.elapsedRealtime()
        val duration = (now - start_time).milliseconds
        val durationFormatted = duration.toComponents {
            hours, minutes, seconds, nanoseconds -> "$hours:$minutes:$seconds.${(nanoseconds/10e8).toInt()}"  }
        elapsedMs = (now - start_time)
        delay(200)
    }




    LaunchedEffect(item.name) {  if (name != item.name) name = item.name   }
    LaunchedEffect(item.bib_number) {   if (bib_number != item.bib_number) bib_number = item.bib_number}
    LaunchedEffect(item.started) {   if (started != item.started) started = item.started}
    LaunchedEffect(item.startTime) {   if (start_time != item.startTime) start_time = item.startTime}



    Row(
        modifier.fillMaxWidth().wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Text(if(sex == 1) "Man" else "Woman", modifier.padding(8.dp).wrapContentSize())
        Text(name, modifier.padding(8.dp).wrapContentSize())
        Text(bib_number, modifier.padding(8.dp).wrapContentSize())
        if(trial_started) {
            if (!started) {
                Button({
                    started = true
                    start_time = SystemClock.elapsedRealtime()
                    viewModel.onItemChanged(item.copy(started = true, startTime = start_time))
                })
                {
                    Text("Start ${elapsedMs - start_time}", Modifier)
                }
            } else {
                Text((elapsedMs - start_time).toString(), modifier)
            }
        }
    }

}