package com.github.mpiotr.competitionwatch

import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay


@Composable fun CompetitorInfoScreen(number : Int,
                                     competitor: Competitor?,
                                     racePosition : CompetitorViewModel.RacePositionItems?,
                                     nowMs : Long,
                                     modifier: Modifier,)
{
    AndroidView(
        { context ->
        val view = LayoutInflater.from(context).inflate(R.layout.split_info, null)
        view
    },
        modifier,
        {view ->
            val numberView = view.findViewById<TextView>(R.id.numberView)
            val competitorNameView = view.findViewById<TextView>(R.id.competitor_name_view)
            val competitorTimeView = view.findViewById<TextView>(R.id.competitor_time_view)
            val chaserNameView = view.findViewById<TextView>(R.id.chaser_name_view)
            val chaserTimeView = view.findViewById<TextView>(R.id.chaser_time_view)
            val leaderNameView = view.findViewById<TextView>(R.id.leader_name_view)
            val leaderTimeView = view.findViewById<TextView>(R.id.leaders_time_view)

            val splitView = view.findViewById<TextView>(R.id.splitView)

            numberView.text = number.toString()

            if(competitor != null) {
                competitorTimeView.visibility = View.VISIBLE
                competitorNameView.text = competitor.name
                competitorNameView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                competitorTimeView.text = competitor.formattedRaceTime(nowMs)
                splitView.text = "Coming to split ${competitor.splits.size + 1}"
                if(racePosition!!.leader != null)
                {
                    leaderNameView.text = racePosition.leader.name
                    leaderTimeView.text = racePosition.leader.formattedSplitsRaceTime()[competitor.splits.size]
                }
                else {
                    if(racePosition.numCompleted == 0){
                        leaderNameView.visibility= View.GONE
                        leaderTimeView.visibility= View.GONE
                    }
                }

            }
            else {
                competitorNameView.text = "No such number"
                competitorNameView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                competitorTimeView.visibility = View.GONE
                chaserNameView.visibility = View.GONE
                chaserTimeView.visibility = View.GONE
                leaderNameView.visibility= View.GONE
                leaderTimeView.visibility= View.GONE
                splitView.visibility= View.GONE
            }
        })
}


@Composable fun CompetitorSplitItem(viewModel: CompetitorViewModel, modifier: Modifier)
{
    val comp_start_time by viewModel.startTime.collectAsState()
    var nowms by remember { mutableLongStateOf(0L) }
    LaunchedEffect(nowms, comp_start_time) {
        val now = SystemClock.elapsedRealtime()
        nowms = now
        delay(200)
    }

    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top, modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp).height(200.dp) ) {
        var number = remember { mutableStateOf(0)}
        LaunchedEffect(number)
        {}

        NumberDial( {
            updated : Int -> number.value = updated
            Log.d("Split Item", "new number = ${number.value}")
        })

        Column()
        {
            val valid_bib = viewModel.bibIndex.contains(number.value.toString())
            if (valid_bib) {
                val item_ind = viewModel.bibIndex[number.value.toString()]
                val item: Competitor =
                    viewModel.competitorsStateFlow.collectAsState().value[item_ind!!]
                val racePosition = viewModel.racePositionInfo(item_ind, item.splits.size)
                CompetitorInfoScreen(number.value, item, racePosition, nowms, Modifier.fillMaxWidth().weight(1.0f))
            } else {
                CompetitorInfoScreen(number.value, null, null, nowms, Modifier.fillMaxWidth().weight(1.0f))
            }

            Button(
                {
                    if (valid_bib) {
                        val item_ind = viewModel.bibIndex[number.value.toString()]
                        viewModel.onSplit(SystemClock.elapsedRealtime(), item_ind!!)
                    }
                },
                enabled = valid_bib,
                modifier = Modifier
                    .height(75.dp)
                    .width(180.dp)
                    .align(Alignment.CenterHorizontally)
            )
            { Text("Split") }
        }

    }

}
