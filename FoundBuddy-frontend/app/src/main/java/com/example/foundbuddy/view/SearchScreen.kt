package com.example.foundbuddy.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        Column(Modifier.fillMaxSize().padding(inner).padding(16.dp)) {
            OutlinedTextField(value = q, onValueChange = { q = it }, label = { Text("Suchbegriff") })
            Spacer(Modifier.padding(8.dp))
            Button(onClick = { vm.search(q) }) { Text("Suchen") }
            Spacer(Modifier.padding(8.dp))
            LazyColumn {
                items(results) { item ->
                    ListItem(headlineContent = { Text(item.title) }, supportingContent = { Text(item.description) })
                    HorizontalDivider()
                }
            }
        }
    }
}
