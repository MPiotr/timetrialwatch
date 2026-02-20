package com.github.mpiotr.competitionwatch

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Start
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController


enum class Destinations(
    val route: String,
    val icon: ImageVector,
)
{
    SETTINGS("SettingScreen",  Icons.Default.Settings, ),
    LIST("Competitors",  Icons.Default.FormatListNumbered),
    START("TimeTrial",  Icons.Default.Start),
    SPLIT("SplitScreen",  Icons.Default.Timer),
    RESULT("Results",  Icons.Default.FormatListNumbered)
}

data class Labels(
    val label: String,
    val contentDescription: String
)
fun getLabels(application : Application) : Map<String, Labels>
{
    val aR = application.resources
    return mapOf(
        Destinations.SETTINGS.route to Labels( aR.getString(R.string.settings), aR.getString(R.string.settings)),
        Destinations.LIST.route to Labels(aR.getString(R.string.list_of_participants_short),  aR.getString(R.string.list_of_participants)),
        Destinations.START.route to Labels( aR.getString(R.string.to_start),  aR.getString(R.string.to_start)),
        Destinations.SPLIT.route to Labels( aR.getString(R.string.goto_splits),  aR.getString(R.string.goto_splits)),
        Destinations.RESULT.route  to Labels( aR.getString(R.string.to_results),  aR.getString(R.string.to_results))
        )
}

@Composable
fun NavigationBar(modifier: Modifier = Modifier, application : Application, viewModel : CompetitorViewModel, navController : NavHostController) {
    val startDestination = Destinations.SETTINGS
    val labels = getLabels(application)
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                Destinations.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {
                            if(destination == Destinations.START) {
                                viewModel.arrangeStartTimes()
                            }
                            navController.navigate(route = destination.route)
                            selectedDestination = index
                        },
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = labels[destination.route]!!.contentDescription
                            )
                        },
                        label = { Text(labels[destination.route]!!.label, textAlign = TextAlign.Center) }
                    )
                }
            }
    }