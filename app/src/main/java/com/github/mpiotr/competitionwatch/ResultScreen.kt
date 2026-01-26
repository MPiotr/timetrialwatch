package com.github.mpiotr.competitionwatch

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ResultScreen(viewModel: CompetitorViewModel, modifier: Modifier, onNavigateToSplits : ()->Unit)
{

    Scaffold(
        topBar = {
            Row(
                Modifier.height(64.dp)//.background(Color.Blue)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Results",
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        } ,
        bottomBar = {
            Row(horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()) {
                Button({ onNavigateToSplits() }, modifier) {
                    Text("Go to Splits")
                }
                Button( {viewModel.resultPdf()}, modifier) {
                    Text("Save pdf")
                }
            }
        }

     ) {
        innerPadding->
        val scroll_state = rememberScrollState()
        val result = viewModel.getResults()
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp),
            userScrollEnabled = true,

            modifier = Modifier.fillMaxWidth().horizontalScroll(scroll_state).padding(innerPadding))
        {
            for( kvpair in result) {

                item {
                    val sexname = if(kvpair.key.first == 1) stringResource(R.string.men) else stringResource(R.string.women)
                    val group_word = stringResource(R.string.group)
                Text("$group_word ${kvpair.key.second}: $sexname",
                    modifier = Modifier.wrapContentWidth().padding(top=10.dp, bottom = 6.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                    )
                }
                itemsIndexed(
                    items = kvpair.value,
                )
                { ind, item ->
                    CompetitorResultItem(item, ind)
                }
            }
        }
    }
}