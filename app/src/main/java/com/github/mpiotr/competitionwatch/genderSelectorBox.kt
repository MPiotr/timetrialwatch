package com.github.mpiotr.competitionwatch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.setValue


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun GenderSelectorBox(selected: Int, onItemSelected : (Int)-> Unit)
{
    var sex by remember { mutableStateOf(selected) }
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.background(Color.LightGray, shape = RectangleShape)
    )
    {
        Text(
            if (sex == 1) "Man" else "Woman",
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(4.dp).width(50.dp)
        )
        Box(Modifier.wrapContentSize()) {
            IconButton(onClick = { expanded = !expanded
                focusManager.clearFocus(true)} )
            {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "", Modifier.size(24.dp))
            }
            DropdownMenu(expanded = expanded, { expanded = false }) {
                DropdownMenuItem(
                    { Text("Man", fontSize = 10.sp) },
                    {
                        expanded = false;
                        sex = 1
                        onItemSelected(sex)
                    }
                )
                DropdownMenuItem(
                    { Text("Woman", fontSize = 10.sp) }, {
                        expanded = false
                        sex = 0
                        onItemSelected(sex)
                    }
                )
            }
        }
    }


}

