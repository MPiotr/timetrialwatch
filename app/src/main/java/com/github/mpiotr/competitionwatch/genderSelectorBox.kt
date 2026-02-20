package com.github.mpiotr.competitionwatch


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GenderSelectorBox(selected: Int, onItemSelected : (Int)-> Unit)
{
    var sex by remember { mutableStateOf(selected) }
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(selected) {   if (sex != selected) sex = selected}

    Column {
        Text(stringResource(R.string.sex), fontSize = 10.sp)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceDim, shape = RectangleShape)
        )
        {
            Text(
                if (sex == 1) stringResource(R.string.man) else stringResource(R.string.woman),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(4.dp).width(50.dp)
            )
            Box(Modifier.wrapContentSize()) {
                IconButton(onClick = {
                    expanded = !expanded
                    focusManager.clearFocus(true)
                })
                {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "", Modifier.size(24.dp))
                }
                DropdownMenu(expanded = expanded, { expanded = false }) {
                    DropdownMenuItem(
                        { Text(stringResource(R.string.man), fontSize = 10.sp) },
                        {
                            expanded = false
                            sex = 1
                            onItemSelected(sex)
                        }
                    )
                    DropdownMenuItem(
                        { Text(stringResource(R.string.woman), fontSize = 10.sp) }, {
                            expanded = false
                            sex = 0
                            onItemSelected(sex)
                        }
                    )
                }
            }
        }
    }


}

