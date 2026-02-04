package com.github.mpiotr.competitionwatch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mpiotr.competitionwatch.dataset.Competitor

@Composable
fun CompetitorResultItem(competitor: Competitor, index : Int) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    Row(Modifier.fillMaxWidth().background(if(index % 2 == 0) Color.LightGray else Color.Unspecified).widthIn(min = screenWidth),
     horizontalArrangement = Arrangement.spacedBy(8.dp))
    {
        Text(competitor.result.toString())
        val gap_text = if(competitor.gap != null) competitor.formattedGapTime(competitor.gap!!) else ""
        Text(gap_text, modifier = Modifier.width(60.dp))
        Text(competitor.bib.bib_number.toString(), modifier = Modifier.width(40.dp))
        Text(competitor.name, modifier = Modifier.width(100.dp))
        Text(competitor.formattedDayTime(competitor.startTime))


        val splits = competitor.formattedSplitsRaceTime()
        val times = competitor.formattedSplitsDayTime()
        val laps = competitor.formattedSplitsLapTime()
        Column {
            Text("Race", fontSize = 10.sp)
            Text("Lap", fontSize = 10.sp)
            Text("Day", fontSize = 10.sp)

        }
        for(s in splits.withIndex())
        {
            Column {
                Text(s.value,  fontSize = 10.sp)
                Text(laps[s.index], fontSize = 10.sp)
                Text(times[s.index], fontSize = 10.sp)
            }
        }
        Text("", modifier = Modifier.weight(1.0f))

    }
}

