// File: view/UploadScreen.kt
package com.example.foundbuddy.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.foundbuddy.R
import com.example.foundbuddy.model.FoundItem
import java.util.UUID

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
        ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    val itemOptions = listOf(
        "Schlüssel",
        "Handy",
        "Geldbörse",
        "Jacke",
        "Kopfhörer",
        "Schülerausweis",
        "Sonstiges"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Neuer Eintrag", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(32.dp))

        // ----------------------------------------------------------
        // 1) Auswahl: Gefunden oder Verloren
        // ----------------------------------------------------------
        if (selectedType == null) {
            Text("Was möchtest du melden?", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(32.dp))

            // Button: Gefunden
            Button(
                onClick = { selectedType = "Gefunden" },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7B68EE),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.camera_icon),
                    contentDescription = null,
                    tint = Color.Unspecified, // ← Damit die PNG-Farbe erhalten bleibt
                    modifier = Modifier.size(30.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text("Ich habe etwas gefunden")
            }

            Spacer(Modifier.height(20.dp))

            // Button: Verloren
            Button(
                onClick = { selectedType = "Verloren" },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFDADA),
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.camera_icon),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text("Ich habe etwas verloren")
            }

            return@Column
        }

        // ----------------------------------------------------------
        // 2) Auswahl: Art des Gegenstands
        // ----------------------------------------------------------
        Text(
            text = if (selectedType == "Gefunden") "Ich habe etwas gefunden"
            else "Ich habe etwas verloren",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(24.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedItem,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gegenstand auswählen") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                itemOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedItem = option
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ----------------------------------------------------------
        // 3) Beschreibungstext
        // ----------------------------------------------------------
        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Beschreibung (optional)") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        // ----------------------------------------------------------
        // 4) Bildauswahl
        // ----------------------------------------------------------
        Button(
            onClick = { imagePicker.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painterResource(id = R.drawable.camera_icon),
                contentDescription = null,
                tint = Color.Unspecified
            )
            Spacer(Modifier.width(8.dp))
            Text(if (imageUri == null) "Bild hinzufügen" else "Bild ändern")
        }

        imageUri?.let { uri ->
            Spacer(Modifier.height(16.dp))
            AsyncImage(
                model = uri,
                contentDescription = "Vorschau",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.height(32.dp))

        // ----------------------------------------------------------
        // 5) Item hochladen
        // ----------------------------------------------------------
        Button(
            onClick = {
                if (selectedItem.isNotBlank()) {
                    onUpload(
                        FoundItem(
                            id = UUID.randomUUID().toString(),
                            title = selectedItem,
                            description = if (desc.isBlank()) null else desc,
                            imagePath = imageUri?.toString(),
                            status = selectedType!!,
                            isResolved = false
                        )
                    )

                    // Formular zurücksetzen
                    selectedType = null
                    selectedItem = ""
                    desc = ""
                    imageUri = null
                }
            },
            enabled = selectedItem.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7B68EE),
                contentColor = Color.White
            )
        ) {
            Icon(
                painterResource(id = R.drawable.save_icon),
                contentDescription = null,
                tint = Color.Unspecified
            )
            Spacer(Modifier.width(8.dp))
            Text("Hochladen")
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = {
            selectedType = null
            selectedItem = ""
            desc = ""
            imageUri = null
        }) {
            Text("Abbrechen")
        }
    }
}
