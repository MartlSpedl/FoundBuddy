package com.example.FoundBuddy.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.FoundBuddy.R
import com.example.FoundBuddy.model.FoundItem

@Composable
fun UploadScreen(onUpload: (FoundItem) -> Unit, modifier: Modifier = Modifier) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titel") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Beschreibung") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = { imagePicker.launch("image/*") }) {
            Icon(
                painter = painterResource(R.drawable.camera_icon),
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.Unspecified
            )
            Spacer(Modifier.width(8.dp))
            Text(if (imageUri == null) "Bild auswählen" else "Bild geändert ✅")
        }

        Button(
            onClick = {
                if (imageUri != null && title.isNotBlank()) {
                    onUpload(
                        FoundItem(
                            title = title,
                            description = desc,
                            imageUri = imageUri.toString()
                        )
                    )
                    title = ""
                    desc = ""
                    imageUri = null
                }
            },
            enabled = imageUri != null && title.isNotBlank()
        ) {
            Icon(
                painter = painterResource(R.drawable.save_icon),
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.Unspecified
            )
            Spacer(Modifier.width(8.dp))
            Text("Fund posten")
        }
    }
}
