package com.example.viewer

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DocumentItem
import java.io.File

@Composable
fun ImageViewerScreen(
    doc: DocumentItem,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotationAngle by remember { mutableStateOf(0f) }
    var showInfoSheet by remember { mutableStateOf(false) }

    // Image metadata
    val imageFile = remember(doc.path) { File(doc.path) }
    val dimensions = remember(doc.path) {
        try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(doc.path, options)
            Pair(options.outWidth, options.outHeight)
        } catch (e: Exception) {
            Pair(0, 0)
        }
    }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset = if (scale > 1f) {
            Offset(
                x = (offset.x + offsetChange.x).coerceIn(-400f * (scale - 1), 400f * (scale - 1)),
                y = (offset.y + offsetChange.y).coerceIn(-600f * (scale - 1), 600f * (scale - 1))
            )
        } else {
            Offset.Zero
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale > 1f) 1f else 2.5f
                        offset = Offset.Zero
                    }
                )
            }
            .transformable(state = transformState)
            .testTag("image_viewer_screen"),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageFile,
            contentDescription = doc.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                    rotationZ = rotationAngle
                }
        )

        // Overlay toolbar with Rotate, Reset, and Info
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E293B).copy(alpha = 0.9f),
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = { rotationAngle = (rotationAngle + 90f) % 360f }) {
                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate Image", tint = Color.White)
                }
                IconButton(onClick = {
                    scale = 1f
                    offset = Offset.Zero
                    rotationAngle = 0f
                }) {
                    Icon(Icons.Default.FitScreen, contentDescription = "Fit to Screen", tint = Color.White)
                }
                IconButton(onClick = { showInfoSheet = true }) {
                    Icon(Icons.Outlined.Info, contentDescription = "Image Details", tint = Color.White)
                }
            }
        }

        // Image Information Dialog
        if (showInfoSheet) {
            AlertDialog(
                onDismissRequest = { showInfoSheet = false },
                title = { Text("Image Information") },
                text = {
                    Column {
                        Text("Dimensions: ${dimensions.first} x ${dimensions.second} pixels")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Format: ${doc.extension.uppercase()}")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("File Size: ${doc.formattedSize}")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Location: ${doc.path}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showInfoSheet = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
