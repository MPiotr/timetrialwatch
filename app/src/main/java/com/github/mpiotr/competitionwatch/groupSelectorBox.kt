package com.github.mpiotr.competitionwatch


import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
fun GroupSelectorBox(selectedGroup : String, viewModel: CompetitorViewModel, onGroupSelected : (String) -> Unit)
{
    val groups = viewModel.groups.collectAsState()
    var expanded by remember{mutableStateOf(false)}
    val focusManager = LocalFocusManager.current


    Column {
        Text(stringResource(R.string.group), fontSize = 10.sp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceDim, shape = RectangleShape),

        ) {
            Text(
                selectedGroup,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )

            Box(Modifier.wrapContentSize()) {
                IconButton(onClick = {
                    expanded = !expanded
                    focusManager.clearFocus(true)
                                     }, )
                {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "",
                        Modifier.size(24.dp,24.dp).padding(0.dp))

                }
                DropdownMenu(expanded = expanded, { expanded = false }) {

                    if(groups.value != null) {
                        for (g in groups.value) {
                            DropdownMenuItem(
                                { Text(g.name, fontSize = 12.sp) },
                                {
                                    expanded = false;
                                    onGroupSelected(g.name);
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

