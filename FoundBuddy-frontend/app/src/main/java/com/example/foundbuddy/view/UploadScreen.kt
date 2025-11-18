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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.foundbuddy.model.FoundItem

@Composable
fun UploadScreen(
    onUpload: (FoundItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // Zustand für Formular & Status
    var selectedType by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Neuer Eintrag",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF4A2D67)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Wenn noch kein Typ gewählt ist → Auswahl anzeigen
        if (selectedType == null) {
            Text("Was möchtest du melden?", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { selectedType = "Gefunden" },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDB8FF)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Ich habe etwas gefunden", color = Color(0xFF4A2D68))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { selectedType = "Verloren" },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD7C7FF)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Ich habe etwas verloren", color = Color(0xFF4A2D68))
            }

        } else {
            // Formular, wenn Typ gewählt ist
            Text(
                text = if (selectedType == "Gefunden")
                    "Ich habe etwas gefunden"
                else "Ich habe etwas verloren",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF4A2D68)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Bezeichnung") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Beschreibung (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { imagePicker.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDB8FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (imageUri == null) "Bild auswählen" else "Anderes Bild wählen",
                    color = Color(0xFF4A2D68)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Ausgewähltes Bild",
                    modifier = Modifier
                        .size(250.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onUpload(
                            FoundItem(
                                title = title,
                                description = desc,
                                imagePath = imageUri?.toString(),
                                status = selectedType ?: "Gefunden"
                            )
                        )
                        // Reset
                        title = ""
                        desc = ""
                        imageUri = null
                        selectedType = null
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A4B9A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Hochladen", color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { selectedType = null }) {
                Text("Zurück zur Auswahl", color = Color(0xFF7A4B9A))
            }
        }
    }
}
