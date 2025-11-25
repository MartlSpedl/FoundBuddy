package com.example.foundbuddy.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.foundbuddy.controller.HomeViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: String,
    navController: NavController,
    vm: HomeViewModel
) {
    val item = vm.getItemById(itemId)
    var commentText by remember { mutableStateOf("") }
    val comments by vm.getComments(itemId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(item?.title ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Kommentar hinzufügen…") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            vm.addComment(itemId, commentText.trim())
                            commentText = ""
                        }
                    },
                    enabled = commentText.isNotBlank()
                ) {
                    Text("Senden")
                }
            }
        }
    ) { innerPadding ->
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Item nicht gefunden", style = MaterialTheme.typography.titleMedium)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                ZoomableImage(imagePath = item.imagePath)
                Spacer(Modifier.height(16.dp))

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    item.description?.let {
                        Text(it, style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(Modifier.height(16.dp))

                    AssistChip(
                        onClick = { },
                        label = { Text(item.status.uppercase()) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (item.status.lowercase()) {
                                "verloren" -> MaterialTheme.colorScheme.errorContainer
                                "gefunden" -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    )
                }

                Spacer(Modifier.height(32.dp))
                Text(
                    "Kommentare",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            items(comments) { comment ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = comment.author,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = vm.formatTimeAgo(comment.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(text = comment.text, style = MaterialTheme.typography.bodyMedium)
                    Divider(modifier = Modifier.padding(top = 12.dp))
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun ZoomableImage(imagePath: String?, modifier: Modifier = Modifier) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when {
            imagePath.isNullOrBlank() -> PlaceholderImage()

            imagePath.startsWith("http://") || imagePath.startsWith("https://") -> {
                AsyncImage(
                    model = imagePath,
                    contentDescription = "Item Bild",
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                    error = painterResource(android.R.drawable.ic_menu_gallery),
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.8f, 6f)
                                if (scale > 1f) {
                                    offsetX += pan.x
                                    offsetY += pan.y
                                }
                            }
                        }
                )
            }

            else -> {
                val resourceId = remember(imagePath) {
                    context.resources.getIdentifier(
                        imagePath.substringAfterLast("/").substringBeforeLast("."),
                        "drawable",
                        context.packageName
                    )
                }
                if (resourceId != 0) {
                    Image(
                        painter = painterResource(resourceId),
                        contentDescription = "Item Bild",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(0.8f, 6f)
                                    if (scale > 1f) {
                                        offsetX += pan.x
                                        offsetY += pan.y
                                    }
                                }
                            }
                    )
                } else {
                    PlaceholderImage()
                }
            }
        }
    }
}

@Composable
private fun PlaceholderImage() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Broken image",
            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Kein Bild verfügbar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
