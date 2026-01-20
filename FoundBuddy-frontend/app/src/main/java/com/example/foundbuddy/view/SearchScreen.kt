package com.example.foundbuddy.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Suche") }) }) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            OutlinedTextField(value = q, onValueChange = { q = it }, label = { Text("Suchbegriff") })
            Spacer(Modifier.padding(8.dp))
            Button(onClick = { vm.search(q) }) { Text("Suchen") }
            Spacer(Modifier.padding(8.dp))
            // IMPORTANT:
            // LazyColumn in einer Column braucht eine definierte Hoehe (z.B. weight(1f)),
            // sonst kann Compose "infinite height constraints" werfen.
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(results) { item ->
                    ListItem(
                        headlineContent = { Text(item.title) },
                        supportingContent = { Text(item.description) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
