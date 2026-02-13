package com.github.mpiotr.competitionwatch



import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.mpiotr.competitionwatch.dataset.Competitor


@Composable
fun CompetitorListItem(item : Competitor,
                       viewModel: CompetitorViewModel,
                       modifier : Modifier = Modifier,
                       comp_start_time: Long = 0,
                       onNavigateToEdit : () -> Unit) {
    var name by remember(item.id) { mutableStateOf(item.name) }
    var bib_number by remember(item.id) { mutableStateOf(item.bib) }
    LaunchedEffect(item.name) { if (name != item.name) name = item.name }
    LaunchedEffect(item.bib) {
        if (bib_number != item.bib) bib_number = item.bib
    }
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),

        )
    {
        Text(item.id.toString())
        IconButton({
            viewModel.selectBib(item.bib, 0)
            viewModel.changeEditMode(true)
            onNavigateToEdit()
        }, enabled = !item.started
        )
        {
            Icon(Icons.Outlined.Edit, "Edit")
        }
        Text(
            name,
            modifier = Modifier.width(175.dp) )

        Text(
            bib_number.bib_number.toString(),
            color = Color(viewModel.colorPallete[bib_number.bib_color]),
            modifier = Modifier.width(75.dp),
            textAlign = TextAlign.Center
        )
        Text(item.group, modifier = Modifier.width(75.dp))
        Text(if (item.sex == 1) stringResource(R.string.M) else stringResource(R.string.W), modifier = Modifier.width(75.dp) )
    }
}

