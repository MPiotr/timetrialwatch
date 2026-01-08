package com.github.mpiotr.competitionwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.mpiotr.competitionwatch.placeholder.PlaceholderContent.Competitor
import com.github.mpiotr.competitionwatch.ui.theme.CompetitionWatchTheme

class MainActivity : ComponentActivity() {
    //private lateinit var competitors : MutableList<Competitor>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setTitle("Participants")


        setContent {
            CompetitionWatchTheme {
                Scaffold(
                    snackbarHost = {
                        val snackbarHostState = remember { SnackbarHostState() }
                        SnackbarHost(snackbarHostState)
                    }
                        )
                {
                    CompetitorList()

                   // FloatingActionButton({ } ) { }
                }
            }
        }
    }
}

@Composable
fun CompetitorList()
{
    var competitors  = remember {mutableStateListOf(
        Competitor("1", "1", "John Dow",  1, 18, 0 ),
        Competitor("2", "2", "Dow Jones", 1, 19, 0 ),
        Competitor("3", "3", "Lara Croft", 0, 25, 0)
    )}

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f))
        {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 30.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                userScrollEnabled = true,
            ) {
                items(
                    items = competitors,
                    key = { it.id }
                )
                { competitor ->
                    CompetitorsItem(
                        competitor,
                        onItemChanged =
                            { updated: Competitor ->
                                val index = competitors.indexOfFirst { it.id == updated.id }
                                if (index != -1) {
                                    competitors[index] = updated
                                }
                            },
                    )
                }
            }
            CompetitorsItem(
                Competitor((competitors.size + 1).toString(), "", "", 0, 25, 0),
                { updated: Competitor ->
                    competitors.add(updated)

                })
        }
        Button({}, content = {Text("Go to start")},
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 20.dp))
    }

}
@Composable
fun CompetitorsItem(item : Competitor, onItemChanged : (Competitor)-> Unit, modifier : Modifier = Modifier)
{
    var expanded by remember { mutableStateOf(false) }
    var name by remember(item.id) { mutableStateOf(item.name) }
    var age by remember(item.id) { mutableStateOf(item.age) }
    var bib_number by remember(item.id) { mutableStateOf(item.bib_number) }
    var sex by remember(item.id) { mutableStateOf(item.sex) }
    val scroll_state = rememberScrollState()

    LaunchedEffect(item.name) {  if (name != item.name) name = item.name   }
    LaunchedEffect(item.bib_number) {   if (bib_number != item.bib_number) bib_number = item.bib_number}
    LaunchedEffect(item.age) {   if (age != item.age) age = item.age}
    LaunchedEffect(item.sex) {   if (sex != item.sex) sex = item.sex}


    Row(modifier.fillMaxWidth().horizontalScroll(scroll_state),
        horizontalArrangement = Arrangement.spacedBy(14.dp),


        ) {
        Text(item.id, Modifier.align(Alignment.CenterVertically))
        TextField(name,
            {
            text ->
                name = text
            },
            modifier = Modifier.width(175.dp).onFocusChanged({focusState ->
                if(!focusState.isFocused) {
                    onItemChanged(item.copy(name=name))
                }
            })
        )
        TextField(bib_number,
            { num ->
                bib_number = num
                onItemChanged(
                    Competitor(item.id,num, item.name, item.sex, item.age, item.group)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(75.dp)
        )
        Text(item.group.toString())

        Box {
            IconButton(onClick = { expanded = !expanded }) {
                //Icon(Icons.Default.MoreVert, contentDescription = "More options")
                Text(if (item.sex == 1) "Man" else "Woman")
            }
            DropdownMenu(expanded = expanded, { expanded = false }) {
                DropdownMenuItem(
                    { Text("Man") },
                    { expanded = false;
                        sex = 1
                        onItemChanged(
                            Competitor(item.id,item.bib_number, item.name, 1, item.age, item.group));
                    }
                )
                DropdownMenuItem(
                    { Text("Woman") }, { expanded = false;
                        sex = 0
                        onItemChanged(
                            Competitor(item.id,item.bib_number, item.name, 0, item.age, item.group))
                    }
                )
            }
        }

        TextField("Team",
            {},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(75.dp)
        )

    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CompetitionWatchTheme {
        Greeting("Android")
    }
}