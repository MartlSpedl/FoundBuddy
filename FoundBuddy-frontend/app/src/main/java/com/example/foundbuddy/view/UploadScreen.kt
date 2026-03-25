package com.example.foundbuddy.view

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.foundbuddy.R
import com.example.foundbuddy.data.FoundItemRepository
import com.example.foundbuddy.model.FoundItem
import com.example.foundbuddy.network.ApiClient
import com.example.foundbuddy.network.FoundBuddyApi
import kotlinx.coroutines.launch
import com.example.foundbuddy.controller.UserViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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

    var selectedType by remember { mutableStateOf("Gefunden") }
    var selectedItem by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) imageUri = uri
    }

    var photoUri by remember { mutableStateOf<Uri?>(null) }

    fun createImageFile(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMANY).format(Date())
        val imageFileName = "FoundBuddy_${timeStamp}_"
        val storageDir = File(context.cacheDir, "images")
        if (!storageDir.exists()) storageDir.mkdirs()
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            imageUri = photoUri
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val newPhotoUri = createImageFile()
            photoUri = newPhotoUri
            cameraLauncher.launch(newPhotoUri)
        }
    }

    val itemOptions = listOf(
        "Schlüssel", "Handy", "Geldbörse", "Jacke",
        "Kopfhörer", "Schülerausweis", "Sonstiges"
    )

    val canUpload = selectedItem.isNotBlank() && imageUri != null && currentUser?.id != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Beitrag erstellen", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        TextButton(
                            onClick = {
                                if (!canUpload) return@TextButton
                                uploadError = null
                                isUploading = true
                                scope.launch {
                                    try {
                                        val uri = imageUri ?: throw IllegalStateException("Kein Bild")
                                        val imageUrl = apiRepo.uploadImageAndGetUrl(uri)
                                        val created = apiRepo.createFoundItem(
                                            FoundItem(
                                                id = UUID.randomUUID().toString(),
                                                title = selectedItem,
                                                description = if (desc.isBlank()) null else desc,
                                                imagePath = imageUrl,
                                                status = selectedType,
                                                isResolved = false,
                                                uploaderId = currentUser?.id ?: "",
                                                uploaderName = username
                                            )
                                        )
                                        onUpload(created)
                                        // Reset UI
                                        selectedItem = ""
                                        desc = ""
                                        imageUri = null
                                    } catch (e: Exception) {
                                        uploadError = e.message ?: "Fehler beim Upload"
                                    } finally {
                                        isUploading = false
                                    }
                                }
                            },
                            enabled = canUpload
                        ) {
                            Text(
                                "Hochladen",
                                color = if (canUpload) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Bild (Instagram Style Square) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .clickable(enabled = !isUploading) {
                        showImageSourceDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Beitrag erstellen",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Vorschau",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Text(
                            "Ändern",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // --- Formular ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp)
                ) {
                    listOf("Gefunden", "Verloren").forEach { type ->
                        val isSel = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { selectedType = type },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                type,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Item Select
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { if (!isUploading) expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedItem,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Was hast du ${if(selectedType=="Gefunden") "gefunden" else "verloren"}?") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
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

                // Description
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Schreibe eine Bildunterschrift...") },
                    placeholder = { Text("Details wie Marke, Farbe oder Fundort...") },
                    minLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )

                if (uploadError != null) {
                    Text(
                        text = uploadError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                
                Spacer(Modifier.height(40.dp))
            }
        }
    }

    // Image Source Selection Dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Bild auswählen") },
            text = {
                Column {
                    Text("Möchtest du ein Foto machen oder ein Bild aus der Galerie auswählen?")
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp).padding(end = 8.dp))
                        Text("Galerie")
                    }
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp).padding(end = 8.dp))
                        Text("Kamera")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}
