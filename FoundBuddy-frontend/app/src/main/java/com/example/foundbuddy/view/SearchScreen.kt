package com.example.foundbuddy.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foundbuddy.controller.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(padding: PaddingValues, vm: SearchViewModel) {
    var q by remember { mutableStateOf("") }
    val results by vm.results.observeAsState(emptyList())
    val isLoading by vm.isLoading.observeAsState(false)
    val error by vm.error.observeAsState(null)

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("KI-Bildersuche") }) }) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = q, 
                onValueChange = { q = it }, 
                label = { Text("Suche nach Bildinhalten (z.B. 'goldener Schlüssel')") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.padding(8.dp))
            
            Button(
                onClick = { vm.search(q) },
                enabled = q.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) { 
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Suchen") 
                }
            }
            
            if (isLoading) {
                Spacer(Modifier.padding(4.dp))
                Text(
                    text = "KI-Suche läuft… (erster Start kann ~1–2 Min. dauern)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            error?.let { err ->
                Spacer(Modifier.padding(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        text = err,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(Modifier.padding(8.dp))
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(results) { item ->
                    ListItem(
                        headlineContent = { Text(item.title) },
                        supportingContent = { 
                            Column {
                                Text(item.description ?: "")
                                if (item.imagePath != null) {
                                    Text(
                                        "Bild verfügbar",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
