package com.github.mpiotr.competitionwatch

import android.content.ContentValues
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.OnConflictStrategy
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.compose.AppTheme
import com.github.mpiotr.competitionwatch.dataset.AppDatabase
import com.github.mpiotr.competitionwatch.dataset.CompetitorDao


class MainActivity : ComponentActivity() {
    //private lateinit var competitors : MutableList<Competitor>

    private lateinit var database : AppDatabase
    private lateinit var dao : CompetitorDao

    override fun onCreate(savedInstanceState: Bundle?) {

        val callback: RoomDatabase.Callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                val cv = ContentValues()
                cv.put("id",  1L)
                cv.put("name", getString(R.string.main_group_name))
                cv.put("num_splits_men", 4)
                cv.put("num_splits_women", 4)
                db.insert("groups", OnConflictStrategy.REPLACE,cv)
                cv.clear()
                cv.put("start_interval_seconds", 15)
                cv.put("competition_start_time", 0L)
                db.insert("info", OnConflictStrategy.REPLACE,cv)
            }

            /*fun onOpen(db: SupportSQLiteDatabase?) {
                // do something every time database is open
            }*/
        }

        database = Room.databaseBuilder(
            application,
            AppDatabase::class.java, "competition-db.sqlite",
        ).addCallback(callback).build()
        dao = database.competitorDao()

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setTitle("Participants")

        this.application
        val viewModel = CompetitorViewModel(this.application, dao, database)
        val soundPool = SoundPool.Builder().setMaxStreams(1).build()
        val soundId =  soundPool.load(this, R.raw.racestart_wav, 1)



        setContent {
            AppTheme {
                val cs = MaterialTheme.colorScheme
                LaunchedEffect(Unit) {
                    Log.d("THEME_TRACE", "Main: Primary = ${cs.primary}")
                    Log.d("THEME_TRACE", "Main: OnPrimary = ${cs.onPrimary}")
                }
                val navController = rememberNavController()
                Scaffold(
                    snackbarHost = {
                        val snackbarHostState = remember { SnackbarHostState() }
                        SnackbarHost(snackbarHostState)
                    },
                )
                { paddingValues ->
                    NavHost(navController = navController, startDestination = "SettingScreen") {
                        composable("Competitors") {
                            CompetitorList(
                                viewModel,
                                Modifier.padding(paddingValues),
                                {
                                        navController.navigate("AddCompetitor")
                                },
                                {
                                    viewModel.arrangeStartTimes() // Only for late-registered competitors, normally on start button
                                    navController.navigate("TimeTrial")
                                })
                        }
                        composable("SettingScreen") {
                            SettingsScreen (
                                this@MainActivity,
                                viewModel,
                                Modifier.padding(paddingValues),
                                { navController.navigate("Competitors") })
                        }
                        composable("AddCompetitor")
                         {
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
                                soundPool,
                                soundId,
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



