package com.github.mpiotr.competitionwatch

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun NumberDial(onNumberChanged : ( Int)->Unit)
{
    val number = remember { mutableIntStateOf(0) }
    AndroidView({ context ->
        val view = LayoutInflater.from(context).inflate(R.layout.number_dial, null)

        val buttonDigit : MutableList<Button> = mutableListOf()
        val but0 = view.findViewById<Button>(R.id.button0); buttonDigit.add(but0)
        val but1 = view.findViewById<Button>(R.id.button1); buttonDigit.add(but1)
        val but2 = view.findViewById<Button>(R.id.button2); buttonDigit.add(but2)
        val but3 = view.findViewById<Button>(R.id.button3); buttonDigit.add(but3)
        val but4 = view.findViewById<Button>(R.id.button4); buttonDigit.add(but4)
        val but5 = view.findViewById<Button>(R.id.button5); buttonDigit.add(but5)
        val but6 = view.findViewById<Button>(R.id.button6); buttonDigit.add(but6)
        val but7 = view.findViewById<Button>(R.id.button7); buttonDigit.add(but7)
        val but8 = view.findViewById<Button>(R.id.button8); buttonDigit.add(but8)
        val but9 = view.findViewById<Button>(R.id.button9); buttonDigit.add(but9)

        for(pair in buttonDigit.withIndex())
        {
            pair.value.setOnClickListener {
                val newNumber =  pair.index + number.intValue*10
                number.intValue = newNumber
                Log.d("NumberDial", "call ${pair.index}new number = ${number.intValue}($newNumber)")
                onNumberChanged(number.intValue)
            }
        }

        val butX = view.findViewById<Button>(R.id.buttonX)
        val butC = view.findViewById<Button>(R.id.buttonC)

        butX.setOnClickListener {
            number.intValue = 0 ;
            Log.d("NumberDial", "call X new number = ${number.intValue}")
            onNumberChanged(number.intValue)}
        butC.setOnClickListener { number.intValue /= 10
            Log.d("NumberDial", "call C new number = ${number.intValue}")
            onNumberChanged(number.intValue)
        }

        view
    })
}