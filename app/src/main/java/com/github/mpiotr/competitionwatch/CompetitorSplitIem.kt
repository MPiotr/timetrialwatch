package com.github.mpiotr.competitionwatch

import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
                                     modifier: Modifier,
                                     split_index : Int?)
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
                racePosition!!
                split_index!!
                competitorTimeView.visibility = View.VISIBLE
                competitorNameView.text = "${racePosition.currentPosition}: ${competitor.name}"
                competitorNameView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                competitorTimeView.text = competitor.formattedRaceTime(nowMs)
                splitView.text = "Coming to split ${split_index + 1}"
                if(racePosition!!.leader != null)
                {
                    leaderNameView.visibility= View.VISIBLE
                    leaderTimeView.visibility= View.VISIBLE
                    leaderNameView.text = "${racePosition.currentPosition - 1}: ${racePosition.leader.name}"
                    leaderTimeView.text = racePosition.leader.formattedSplitsRaceTime()[split_index]
                }
                else {
                    if(racePosition.numCompleted == 0){
                        leaderNameView.visibility= View.GONE
                        leaderTimeView.visibility= View.GONE
                    }
                    else {
                        leaderNameView.visibility= View.VISIBLE
                        leaderNameView.text = "First at this split (${racePosition.numCompleted} completed)"
                        leaderTimeView.visibility= View.GONE
                    }
                }
                if(racePosition.chaser != null) {
                    chaserNameView.visibility= View.VISIBLE
                    chaserTimeView.visibility= View.VISIBLE
                    chaserNameView.text = "${racePosition.currentPosition+1}: ${racePosition.chaser.name}"
                    chaserTimeView.text = racePosition.chaser.formattedSplitsRaceTime()[split_index]
                }
                else {
                    chaserNameView.visibility= View.GONE
                    chaserTimeView.visibility= View.GONE
                }

            }
            else {
                competitorNameView.text = "No number or Not started"
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
    var splittime by remember { mutableLongStateOf(0L) }
    var isFinishing by remember { mutableStateOf(false) }
    var number = remember { mutableIntStateOf(0)}
    LaunchedEffect(nowms, comp_start_time) {
        val now = SystemClock.elapsedRealtime()
        nowms = now
        delay(200)
    }
    isFinishing = false
    if(splittime != 0L && nowms - splittime > 1000) {
        splittime = 0
        number.intValue = 0;
    }
    Log.d("SPLIT IME", "$splittime, ${splittime -  nowms}")

    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top, modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp).height(200.dp) ) {

        LaunchedEffect(number)       {}

        NumberDial({
            updated : Int -> number.value = updated
            Log.d("Split Item", "new number = ${number.value}")
        },
                number.value
            )

        Column()
        {
            var valid_bib = viewModel.bibIndex.contains(number.value.toString())

            if (valid_bib) {
                val item_ind = viewModel.bibIndex[number.value.toString()]
                val item: Competitor =
                    viewModel.competitorsStateFlow.collectAsState().value[item_ind!!]
                if(item.finished) valid_bib = false
                if(item.started && !item.finished) {
                    var splitIndex: Int
                    var racePosition: CompetitorViewModel.RacePositionItems
                    if (splittime == 0L) {
                        splitIndex = item.splits.size
                        if(splitIndex == viewModel.settings.value.max_num_of_splits - 1) {
                            isFinishing = true
                        }
                        else false

                        racePosition = viewModel.racePositionLive(nowms, item_ind, splitIndex)
                    } else {
                        splitIndex = item.splits.size - 1
                        racePosition = viewModel.racePositionCompleted(item_ind, splitIndex)
                    }
                    CompetitorInfoScreen(
                        number.value, item, racePosition,
                        if (splittime != 0L) splittime else nowms,
                        Modifier.fillMaxWidth().weight(1.0f), splitIndex
                    )
                }
                else CompetitorInfoScreen(number.value, null, null, nowms, Modifier.fillMaxWidth().weight(1.0f), null)
            } else {
                CompetitorInfoScreen(number.value, null, null, nowms, Modifier.fillMaxWidth().weight(1.0f), null)
            }

            Button(
                {
                    if (valid_bib) {
                        val item_ind = viewModel.bibIndex[number.value.toString()]
                        splittime = SystemClock.elapsedRealtime()
                        viewModel.onSplit(splittime, item_ind!!)

                    }
                },
                enabled = valid_bib && splittime == 0L,
                modifier = Modifier
                    .height(75.dp)
                    .width(180.dp)
                    .align(Alignment.CenterHorizontally)
            )
            { if(!isFinishing) Text("Split") else Text("Finish") }
        }

    }

}
