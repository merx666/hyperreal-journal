package info.hyperreal.journal.ui.substances

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import info.hyperreal.journal.domain.model.Substance

@Composable
fun SubstancesListScreen(
    viewModel: SubstancesViewModel = hiltViewModel(),
    onSubstanceClick: (String) -> Unit
) {
    val substances by viewModel.substances.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                viewModel.onSearchQueryChanged(it)
            },
            label = { Text("Szukaj substancji") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true
        )
        
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(substances, key = { it.id }) { substance ->
                ListItem(
                    headlineContent = { Text(substance.name) },
                    supportingContent = { 
                        if (substance.aliases.isNotEmpty()) {
                            Text(substance.aliases.joinToString(", "))
                        }
                    },
                    modifier = Modifier.clickable { onSubstanceClick(substance.id) }
                )
                Divider()
            }
        }
    }
}
