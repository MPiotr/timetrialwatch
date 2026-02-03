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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mpiotr.competitionwatch.dataset.Bib
import com.github.mpiotr.competitionwatch.dataset.Competitor
import com.github.mpiotr.competitionwatch.dataset.RacePositionItems
import kotlinx.coroutines.delay


@Composable fun CompetitorInfoScreen(number : Bib,
                                     competitor: Competitor?,
                                     racePosition : RacePositionItems?,
                                     nowMs : Long,
                                     modifier: Modifier,
                                     split_index : Int?,
                                     bib_colors : List<Int>)
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

            numberView.text = number.bib_number.toString()
            numberView.setTextColor(bib_colors[number.bib_color])

            Log.d("INFO ITEM", "$number : $competitor, $split_index, $racePosition")

            if(competitor != null && racePosition != null) {
                split_index!!

                competitorTimeView.visibility = View.VISIBLE
                competitorNameView.text = "${racePosition.currentPosition}: ${competitor.name}"
                competitorNameView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                competitorTimeView.text = competitor.formattedRaceTime(nowMs)
                splitView.visibility = View.VISIBLE
                splitView.text = "Coming to split ${split_index + 1}"
                if(racePosition!!.leader != null)
                {
                    leaderNameView.visibility= View.VISIBLE
                    leaderTimeView.visibility= View.VISIBLE
                    leaderNameView.text = "${racePosition.currentPosition - 1}: ${racePosition.leader.second.name}"
                    leaderTimeView.text = racePosition.leader.second.formattedTime(racePosition.leader.first)
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
                    chaserNameView.text = "${racePosition.currentPosition+1}: ${racePosition.chaser.second.name}"
                    chaserTimeView.text = racePosition.chaser.second.formattedTime(racePosition.chaser.first)
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


@Composable fun CompetitorSplitItem(viewModel: CompetitorViewModel, modifier: Modifier, id : Int)
{
    val comp_start_time by viewModel.startTime.collectAsState()
    var nowms by remember { mutableLongStateOf(0L) }
    var splittime by remember { mutableLongStateOf(0L) }
    var splitindex_local by remember { mutableStateOf(0) }
    var isFinishing by remember { mutableStateOf(false) }
    var number = remember { mutableStateOf(Bib(0, 0))}
    var localRacePosition by remember { mutableStateOf< RacePositionItems?>(null) }
    var localCompetitor by remember { mutableStateOf<Competitor?>(null) }
    val currentItem by viewModel.currentItem(id).collectAsStateWithLifecycle()
    val currentBib by viewModel.currentBib(id).collectAsState()



    LaunchedEffect(nowms, comp_start_time) {
        val now = SystemClock.elapsedRealtime()
        nowms = now
        delay(200)
    }

    val competitor = viewModel.getCompetitor(number.value).collectAsState()
    LaunchedEffect(competitor) { }


    isFinishing = false
    if(splittime != 0L && nowms - splittime > 1000) {
        splittime = 0
        viewModel.selectSplit(0)
        number.value = Bib(0, 0);
        viewModel.selectBib(number.value, id)
        localCompetitor = null
        localRacePosition = null
    }

    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top, modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
        .height(230.dp) ) {

        val valid_bib = number.value != Bib(0, 0)
        val split_buttons_enabled = valid_bib && splittime == 0L && currentItem != null && !(currentItem?.finished ?: true) && (currentItem?.started ?: false)
        val dial_buttons_enabled = splittime == 0L

        NumberDial({updated : Bib ->
                   number.value = updated
                   viewModel.selectBib(updated, id)},
                number.value.bib_number,
                viewModel,
                dial_buttons_enabled
            )

        Column()
        {

            val racePositionLive = viewModel.racePositionLive(id).collectAsState()


            if (valid_bib && currentItem != null && currentItem == racePositionLive.value?.first &&
                ((currentItem!!.started && !currentItem!!.finished )|| (currentItem!!.finished && splittime != 0L) )
                && racePositionLive.value != null && splittime == 0L) {
                localCompetitor = currentItem
                localRacePosition = racePositionLive.value!!.second
                splitindex_local = currentItem!!.splits.size
                isFinishing = viewModel.isCurrentCompetitorFinishing(id).collectAsState().value
            }
            else {
                if(splittime == 0L || currentItem == null || currentItem?.finished ?: true) {
                    localCompetitor = null
                    localRacePosition = null
                }
            }

            CompetitorInfoScreen(
                number.value, localCompetitor, localRacePosition,
                if (splittime != 0L) splittime else nowms,
                Modifier
                    .fillMaxWidth()
                    .weight(1.0f), splitindex_local,
                viewModel.colorPallete
            )

            Button(
                {
                    if (valid_bib && currentItem != null) {
                        splittime = SystemClock.elapsedRealtime()
                        viewModel.onSplit(splittime, number.value)

                    }
                },
                enabled = split_buttons_enabled,
                modifier = Modifier
                    .height(75.dp)
                    .width(180.dp)
                    .align(Alignment.CenterHorizontally)
            )

            {
               val text =  if(!isFinishing) stringResource(R.string.split)  else stringResource(R.string.finish)
              Text(text, color = colorResource(R.color.md_theme_onPrimary))
            }
        }

    }

}
