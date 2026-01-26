package com.github.mpiotr.competitionwatch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ColorPicker(onColorSet : (Int) -> Unit)
{
    var name by remember { mutableStateOf("Default") }
}