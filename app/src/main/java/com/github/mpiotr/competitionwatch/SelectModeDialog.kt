package com.github.mpiotr.competitionwatch

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SelectModeDialog(context : Context, viewModel: CompetitorViewModel, modifier : Modifier)
{
    Column {
        Text("Select the competition mode")
        Row(modifier, Arrangement.Center) {
            Button({

            }, modifier) {
                "Time Trial"

            }
            Button({},modifier) {
                "Mass Start"
            }
        }
    }

}