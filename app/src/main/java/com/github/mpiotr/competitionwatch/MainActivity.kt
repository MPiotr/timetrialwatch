package com.github.mpiotr.competitionwatch

import android.media.SoundPool
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
import androidx.room.Room
import com.example.compose.AppTheme
import com.github.mpiotr.competitionwatch.dataset.AppDatabase
import com.github.mpiotr.competitionwatch.dataset.CompetitorDao
import com.github.mpiotr.competitionwatch.dataset.getDatabaseCallbacks


class MainActivity : ComponentActivity() {
    private lateinit var database : AppDatabase
    private lateinit var dao : CompetitorDao

    override fun onCreate(savedInstanceState: Bundle?) {

        val callback = getDatabaseCallbacks(this) // Fills in default values
        database = Room.databaseBuilder(
            application,
            AppDatabase::class.java, "competition-db.sqlite",
        ).addCallback(callback).build()
        dao = database.competitorDao()

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val viewModel = CompetitorViewModel(this.application, dao, database)
        val soundPool = SoundPool.Builder().setMaxStreams(1).build()
        val soundId =  soundPool.load(this, R.raw.racestart_wav, 1)
        viewModel.soundPool = soundPool
        viewModel.soundId = soundId

        setContent {
            AppTheme {
                val navController = rememberNavController()
                Scaffold(
                    snackbarHost = {
                        val snackbarHostState = remember { SnackbarHostState() }
                        SnackbarHost(snackbarHostState)
                    },
                    bottomBar = { NavigationBar(Modifier, application, viewModel, navController ) }
                )
                { paddingValues ->
                    NavHost(navController = navController, startDestination = "SettingScreen") {
                        composable("Competitors") {
                            CompetitorList(
                                viewModel,
                                Modifier.padding(paddingValues),
                                {
                                        navController.navigate("AddCompetitor")
                                }
                            )
                        }
                        composable("SettingScreen") {
                            SettingsScreen (
                                viewModel,
                                Modifier.padding(paddingValues),
                               )
                        }
                        composable("AddCompetitor")
                         {
                            AddCompetitorDialog(
                                application,
                                viewModel,
                                Modifier.padding(paddingValues),
                                {
                                    navController.navigate("Competitors")
                                    {
                                        popUpTo("Competitors") {inclusive = true}
                                    }})
                        }
                        composable("TimeTrial") {
                            TimeTrialScreen(
                                viewModel,
                                Modifier.padding(paddingValues),
                                )
                        }
                        composable("SplitScreen") {
                            SplitScreen(
                                viewModel,
                                Modifier.padding(paddingValues))
                        }
                        composable("Results") {
                            ResultScreen (
                                viewModel,
                                Modifier.padding(paddingValues)
                               )
                        }
                    }
                }
            }
        }
    }
}



