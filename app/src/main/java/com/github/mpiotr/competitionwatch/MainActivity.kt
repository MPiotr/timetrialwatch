package com.github.mpiotr.competitionwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.mpiotr.competitionwatch.placeholder.PlaceholderContent.Competitor
import com.github.mpiotr.competitionwatch.ui.theme.CompetitionWatchTheme

class MainActivity : ComponentActivity() {
    //private lateinit var competitors : MutableList<Competitor>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setTitle("Participants")

        val viewModel = CompetitorViewModel()



        setContent {
            val navController = rememberNavController()
            CompetitionWatchTheme {
                Scaffold(
                    snackbarHost = {
                        val snackbarHostState = remember { SnackbarHostState() }
                        SnackbarHost(snackbarHostState)
                    },
                )
                { paddingValues ->
                    NavHost(navController = navController, startDestination = "Competitors") {
                        composable("Competitors") {
                            CompetitorList(
                                viewModel,
                                Modifier.padding(paddingValues),
                                { navController.navigate("AddCompetitor") },
                                { navController.navigate("TimeTrial") })
                        }
                        composable("AddCompetitor") {
                            AddCompetitorDialog(
                                this@MainActivity,
                                viewModel,
                                Modifier.padding(paddingValues),
                                { navController.navigate("Competitors") })
                        }
                        composable("TimeTrial") {
                            TimeTrialScreen(
                                viewModel,
                                Modifier.padding(paddingValues),
                                { navController.navigate("Competitors") },
                                { navController.navigate("SplitScreen") })

                        }
                        composable("SplitScreen") {
                            SplitScreen(
                                viewModel,
                                Modifier.padding(paddingValues),
                                { navController.navigate("Competitors") },
                                { navController.navigate("TimeTrial") })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompetitorList(viewModel: CompetitorViewModel = CompetitorViewModel(),
                   modifier : Modifier = Modifier,
                   onNavigateToAdd : ()->Unit,
                   onNavigateToTimeTrial : ()->Unit)
{
    val competitors by viewModel.competitorsStateFlow.collectAsState();



    Column(modifier = modifier.safeDrawingPadding())
    {
        Column(modifier = modifier.weight(1f))
        {
            val scroll_state = rememberScrollState()
            LazyColumn(
                contentPadding = PaddingValues(vertical = 30.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                userScrollEnabled = true,
                modifier = modifier.horizontalScroll(scroll_state)
            ) {
                items(
                    items = competitors,
                    key = { it.id }
                )
                { competitor ->
                    CompetitorsItem(
                        competitor,
                        onItemChanged = { updated: Competitor -> viewModel.onItemChanged(updated) },
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxWidth()) {
            Button({onNavigateToAdd()}, modifier.padding(8.dp)) {
                Text("Add Participants")
            }
            Button({onNavigateToTimeTrial()}, modifier.padding(8.dp)) {
                Text("Go to Start")
            }
        }
    }
}


