package com.example.foundbuddy.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.foundbuddy.model.FoundItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onUpload: (FoundItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf<String?>(null) }
    var selectedItem by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    val itemOptions = listOf(
        "Schlüssel", "Trinkflasche", "Jacke", "Handy", "Geldbörse",
        "Schülerausweis", "Kopfhörer", "USB-Stick", "Sporttasche"
    )

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Neuer Eintrag", style = MaterialTheme.typography.titleLarge, color = Color(0xFF4A2D67))
        Spacer(modifier = Modifier.height(24.dp))

        if (selectedType == null) {
            Text("Was möchtest du melden?", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { selectedType = "Gefunden" }, modifier = Modifier.fillMaxWidth(0.8f)) {
                Text("Ich habe etwas gefunden")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { selectedType = "Verloren" }, modifier = Modifier.fillMaxWidth(0.8f)) {
                Text("Ich habe etwas verloren")
            }
        } else {
            Text(
                text = if (selectedType == "Gefunden") "Ich habe etwas gefunden" else "Ich habe etwas verloren",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF4A2D68)
            )
            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                TextField(
                    value = selectedItem,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gegenstand auswählen") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    itemOptions.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                selectedItem = item
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Beschreibung (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { imagePicker.launch("image/*") }) {
                Text(if (imageUri == null) "Bild auswählen" else "Anderes Bild wählen")
            }

            Spacer(modifier = Modifier.height(16.dp))

            imageUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Bild",
                    modifier = Modifier.size(250.dp).padding(8.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                if (selectedItem.isNotBlank()) {
                    onUpload(
                        FoundItem(
                            title = selectedItem,
                            description = desc,
                            imagePath = imageUri?.toString(),
                            status = selectedType ?: "Gefunden"
                        )
                    )
                    selectedItem = ""
                    desc = ""
                    imageUri = null
                    selectedType = null
                }
            }) {
                Text("Hochladen")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { selectedType = null }) {
                Text("Zurück zur Auswahl")
            }
        }
    }
}
