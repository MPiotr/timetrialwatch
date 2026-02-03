package com.github.mpiotr.competitionwatch

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mpiotr.competitionwatch.dataset.Bib
import kotlin.math.min

@Composable
fun NumberDial(onNumberChanged : (Bib)->Unit,
               init_number : Int,
               viewModel: CompetitorViewModel,
               enabled : Boolean = true)
{
    val number = remember { mutableIntStateOf(init_number) }
    val color = remember { mutableIntStateOf(0) }
    LaunchedEffect(init_number) {
        number.intValue = init_number
    }
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
                onNumberChanged(Bib(number.intValue, color.intValue))
            }
        }

        val butX = view.findViewById<Button>(R.id.buttonX)
        val butC = view.findViewById<Button>(R.id.buttonC)

        butX.setOnClickListener {
            number.intValue = 0
            onNumberChanged(Bib(number.intValue, color.intValue))}
        butC.setOnClickListener { number.intValue /= 10
            onNumberChanged(Bib(number.intValue, color.intValue))
        }

        val buttonColors : MutableList<Button> = mutableListOf()

        val butColor1 = view.findViewById<Button>(R.id.buttonColor1); buttonColors.add(butColor1)
        val butColor0 = view.findViewById<Button>(R.id.buttonColor2); buttonColors.add(butColor0)
        val butColor2 = view.findViewById<Button>(R.id.buttonColor3); buttonColors.add(butColor2)
        butColor1.visibility =  View.GONE
        butColor2.visibility =  View.GONE
        butColor0.visibility = View.GONE


        for(i in 0..<min(3,viewModel.colorOrder.size)) {
            if (viewModel.colorOrder.size == 1) break
            buttonColors[i].visibility = View.VISIBLE
            buttonColors[i].setBackgroundColor(viewModel.colorPallete[viewModel.colorOrder[i]])
            buttonColors[i].setOnClickListener {
                color.intValue = i
                onNumberChanged(
                    Bib(
                        number.intValue,
                        viewModel.colorOrder[i]
                    )
                )
            }

        }
        view
    })
    {
        view ->
        val allbuttons : MutableList<Button> = mutableListOf()
        val but0 = view.findViewById<Button>(R.id.button0); allbuttons.add(but0)
        val but1 = view.findViewById<Button>(R.id.button1); allbuttons.add(but1)
        val but2 = view.findViewById<Button>(R.id.button2); allbuttons.add(but2)
        val but3 = view.findViewById<Button>(R.id.button3); allbuttons.add(but3)
        val but4 = view.findViewById<Button>(R.id.button4); allbuttons.add(but4)
        val but5 = view.findViewById<Button>(R.id.button5); allbuttons.add(but5)
        val but6 = view.findViewById<Button>(R.id.button6); allbuttons.add(but6)
        val but7 = view.findViewById<Button>(R.id.button7); allbuttons.add(but7)
        val but8 = view.findViewById<Button>(R.id.button8); allbuttons.add(but8)
        val but9 = view.findViewById<Button>(R.id.button9); allbuttons.add(but9)
        val butX = view.findViewById<Button>(R.id.buttonX); allbuttons.add(butX)
        val butC = view.findViewById<Button>(R.id.buttonC); allbuttons.add(butC)
        val butColor1 = view.findViewById<Button>(R.id.buttonColor1); allbuttons.add(butColor1)
        val butColor0 = view.findViewById<Button>(R.id.buttonColor2); allbuttons.add(butColor0)
        val butColor2 = view.findViewById<Button>(R.id.buttonColor3); allbuttons.add(butColor2)
        for(b in allbuttons)
        {
            b.isEnabled = enabled
        }



    }
}