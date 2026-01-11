package com.github.mpiotr.competitionwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.mpiotr.competitionwatch.ui.theme.CompetitionWatchTheme

class MainActivity : ComponentActivity() {
    //private lateinit var competitors : MutableList<Competitor>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setTitle("Participants")

        this.application
        val viewModel = CompetitorViewModel(this.application)



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
                                {
                                    if(viewModel.timeTrialStarted.value) viewModel.arrangeStartTimes() // Only for late-registered competitors, normally on start button
                                    navController.navigate("TimeTrial")
                                })
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
                                { navController.navigate("TimeTrial") },
                                { navController.navigate("Results") })
                        }
                        composable("Results") {
                            ResultScreen (
                                viewModel,
                                Modifier.padding(paddingValues),
                                { navController.navigate("SplitScreen") },
                               )
                        }
                    }
                }
            }
        }
    }
}



