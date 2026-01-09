package com.github.mpiotr.competitionwatch

import android.content.Context
import android.os.SystemClock
import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable fun SplitItem( viewModel: CompetitorViewModel, modifier: Modifier)
{
    var number = remember { mutableStateOf(0)}
    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {

        AndroidView({context ->
            val view = LayoutInflater.from(context).inflate(R.layout.number_picker, null)
            val numberPicker = view.findViewById<NumberPicker>(R.id.numberPicker)
            numberPicker.value = number.value
            numberPicker.minValue = 0
            numberPicker.maxValue = 9
            numberPicker.setOnValueChangedListener({picker, old, new -> number.value = new})
            numberPicker
        } )
        Column {
            Button({
                //viewModel.onSplit(number, SystemClock.elapsedRealtime())
            }) { Text("Split!") }
            Text("${number.value}",modifier.align(Alignment.CenterHorizontally))
        }
    }

}


@Composable
fun SplitScreen(viewModel: CompetitorViewModel, modifier: Modifier,
                onNavigateToList : () -> Unit,
                onNavigateToStart: ()-> Unit)
{
    Column(modifier.fillMaxWidth()) {
        SplitItem(viewModel, modifier)
        Row(horizontalArrangement = Arrangement.Center) {
            Button({onNavigateToStart()}, modifier) {
                Text("Go to Start")
            }
            Button({onNavigateToList()}, modifier) {
                Text("Go to Participant List")
            }
        }
    }
}

