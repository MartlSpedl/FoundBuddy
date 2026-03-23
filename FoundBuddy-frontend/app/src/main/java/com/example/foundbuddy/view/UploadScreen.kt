package com.example.foundbuddy.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.foundbuddy.R
import com.example.foundbuddy.data.FoundItemRepository
import com.example.foundbuddy.model.FoundItem
import com.example.foundbuddy.network.ApiClient
import com.example.foundbuddy.network.FoundBuddyApi
import kotlinx.coroutines.launch
import com.example.foundbuddy.controller.UserViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    userViewModel: UserViewModel,
    onUpload: (FoundItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { ApiClient.retrofit.create(FoundBuddyApi::class.java) }
    val apiRepo = remember { FoundItemRepository(context, api) }

    val currentUser by userViewModel.currentUserFlow.collectAsState(initial = null)
    val username by userViewModel.username.collectAsState(initial = "Unbekannt")

    var selectedType by remember { mutableStateOf<String?>(null) }
    var selectedItem by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }

    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
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
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Neuer Eintrag", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))

        if (selectedType == null) {
            Text("Was möchtest du melden?", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))

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
                    tint = Color.Unspecified,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text("Ich habe etwas gefunden")
            }

            Spacer(Modifier.height(16.dp))

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

        Text(
            text = if (selectedType == "Gefunden") "Ich habe etwas gefunden" else "Ich habe etwas verloren",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedItem,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gegenstand auswählen") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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

        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Beschreibung (optional)") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(18.dp))

        Text(
            text = "Bild",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clickable(enabled = !isUploading) {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (imageUri == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.camera_icon),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text("Tippe hier, um ein Bild auszuwählen")
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Galerie öffnet sich automatisch",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Vorschau",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                enabled = !isUploading,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painterResource(id = R.drawable.camera_icon),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer(Modifier.width(8.dp))
                Text(if (imageUri == null) "Bild auswählen" else "Bild ändern")
            }

            OutlinedButton(
                onClick = { imageUri = null },
                enabled = imageUri != null && !isUploading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Entfernen")
            }
        }

        Spacer(Modifier.height(24.dp))

        val canUpload = selectedItem.isNotBlank() && selectedType != null

        Button(
            onClick = {
                val type = selectedType ?: return@Button
                if (!canUpload || isUploading) return@Button

                uploadError = null
                isUploading = true

                scope.launch {
                    try {
                        // 1) Bild muss vorhanden sein
                        val uri = imageUri ?: throw IllegalStateException("Bitte ein Bild auswählen")

                        // 2) Upload -> URL (multiuser tauglich)
                        val imageUrl = apiRepo.uploadImageAndGetUrl(uri)

                        // 3) Item mit URL speichern
                        val created = apiRepo.createFoundItem(
                            FoundItem(
                                id = UUID.randomUUID().toString(),
                                title = selectedItem,
                                description = if (desc.isBlank()) null else desc,
                                imagePath = imageUrl, // ✅ URL statt content:// oder file://
                                status = type,
                                isResolved = false,
                                uploaderName = username
                            )
                        )

                        // 4) UI aktualisieren
                        onUpload(created)

                        // Reset
                        selectedType = null
                        selectedItem = ""
                        desc = ""
                        imageUri = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                        uploadError = e.message ?: "Upload fehlgeschlagen"
                    } finally {
                        isUploading = false
                    }
                }
            },
            enabled = canUpload && !isUploading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7B68EE),
                contentColor = Color.White
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.save_icon),
                contentDescription = null,
                tint = Color.Unspecified
            )
            Spacer(Modifier.width(8.dp))
            Text(if (isUploading) "Lade hoch..." else "Hochladen")
        }

        uploadError?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = {
                if (isUploading) return@TextButton
                selectedType = null
                selectedItem = ""
                desc = ""
                imageUri = null
                uploadError = null
            }
        ) {
            Text("Abbrechen")
        }

        Spacer(Modifier.height(10.dp))
    }
}
