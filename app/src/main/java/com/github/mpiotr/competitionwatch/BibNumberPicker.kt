package com.github.mpiotr.competitionwatch

import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.lang.Math.pow
import kotlin.math.log10

@Composable
fun BibNumberPicker(viewModel: CompetitorViewModel,
                    onNumberChanged : (Int) -> Unit,
                    modifier : Modifier = Modifier)
{
    Row(modifier = modifier.border(1.dp, Color.Gray)) {
        val info = viewModel.getDigitsForPicker()
        val digits = remember { mutableStateOf(Array(info.numDigits, { 0 })) }

        for (i in 0..<info.numDigits) {
            AndroidView({ context ->
                val view = LayoutInflater.from(context).inflate(R.layout.number_picker, null)
                val numberPicker = view.findViewById<NumberPicker>(R.id.numberPicker)
                numberPicker.value = digits.value[i]
                numberPicker.minValue = 0
                numberPicker.maxValue = if (i == 0) info.maxGreaterDigit else 9

                numberPicker.setOnValueChangedListener({ picker, old, new ->
                    digits.value[i] = new
                    var bib_number = 0;
                    for(i in 0..<info.numDigits) {
                        bib_number +=  digits.value[i]
                        if(i != info.numDigits - 1) bib_number *= 10
                    }
                    onNumberChanged(bib_number)
                })
                numberPicker
            })
        }
    }
}