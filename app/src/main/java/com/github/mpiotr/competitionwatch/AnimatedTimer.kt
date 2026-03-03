package com.github.mpiotr.competitionwatch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

@Composable
fun AnimatedTimer(timeString : String,
                  modifier : Modifier = Modifier,
                  style : TextStyle = TextStyle.Default,
                  color : Color = Color.Unspecified)
{
    val textMeasurer = rememberTextMeasurer()
    val widest = textMeasurer.measure(
        text = "8",
        style = style
    ).size.width

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        for ( ch in timeString) {
            Box(
                modifier = Modifier.width(with(LocalDensity.current) { widest.toDp() }),
                contentAlignment = Alignment.Center
            )
            {
                Text(
                    ch.toString(),
                    style = style,
                    color = color,
                    letterSpacing = 0.sp)
            }
        }
    }
}
