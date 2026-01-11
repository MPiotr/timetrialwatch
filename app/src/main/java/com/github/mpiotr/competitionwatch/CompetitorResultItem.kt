package com.github.mpiotr.competitionwatch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.magnifier
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun CompetitorResultItem(competitor: Competitor, index : Int) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    Row(Modifier.fillMaxWidth().background(if(index % 2 == 0) Color.LightGray else Color.Unspecified).widthIn(min = screenWidth),
     horizontalArrangement = Arrangement.spacedBy(8.dp))
    {
        Text(competitor.result.toString())
        Text(competitor.name, modifier = Modifier.width(100.dp))

        val splits = competitor.formattedSplitsRaceTime()
        for(s in splits.withIndex())
        {
            Text(s.value)
        }
        Text("", modifier = Modifier.weight(1.0f))

    }
}
