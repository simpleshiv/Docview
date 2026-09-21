package com.example.viewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentItem
import com.example.data.model.ReadingBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    doc: DocumentItem,
    continuousScroll: Boolean,
    darkReadingMode: Boolean,
    readingBackground: ReadingBackground,
    pageRotation: Int,
    currentPage: Int,
    totalPages: Int,
    onPageChanged: (Int) -> Unit,
    onTotalPagesLoaded: (Int) -> Unit,
    onToggleBookmark: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var fileDescriptor by remember { mutableStateOf<ParcelFileDescriptor?>(null) }
    var pageCount by remember { mutableStateOf(1) }
    var renderError by remember { mutableStateOf<String?>(null) }

    // Page bitmap cache
    val pageBitmaps = remember { mutableStateMapOf<Int, Bitmap>() }

    // Zoom & Pan state
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Thumbnail strip visibility
    var showThumbnails by remember { mutableStateOf(false) }
    var showJumpDialog by remember { mutableStateOf(false) }
    var jumpPageInput by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    // Initialize PdfRenderer
    DisposableEffect(doc.path) {
        val file = File(doc.path)
        if (!file.exists()) {
            renderError = "File not found at: ${doc.path}"
        } else {
            try {
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                fileDescriptor = pfd
                val renderer = PdfRenderer(pfd)
                pdfRenderer = renderer
                val count = renderer.pageCount
                pageCount = count
                onTotalPagesLoaded(count)
            } catch (e: Exception) {
                renderError = "Failed to initialize PDF renderer: ${e.message}"
            }
        }

        onDispose {
            try {
                pdfRenderer?.close()
                fileDescriptor?.close()
                pageBitmaps.values.forEach { it.recycle() }
                pageBitmaps.clear()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    // Function to render page asynchronously
    fun loadPageBitmap(pageIndex: Int) {
        val renderer = pdfRenderer ?: return
        if (pageBitmaps.containsKey(pageIndex)) return

        coroutineScope.launch(Dispatchers.IO) {
            try {
                synchronized(renderer) {
                    val page = renderer.openPage(pageIndex)
                    val width = (page.width * 1.6f).toInt().coerceAtLeast(300)
                    val height = (page.height * 1.6f).toInt().coerceAtLeast(400)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    pageBitmaps[pageIndex] = bitmap
                }
            } catch (e: Exception) {
                // handle page render error
            }
        }
    }

    // Preload current and adjacent pages
    LaunchedEffect(currentPage, pageCount, pdfRenderer) {
        if (pdfRenderer != null) {
            val idx = (currentPage - 1).coerceIn(0, pageCount - 1)
            loadPageBitmap(idx)
            if (idx + 1 < pageCount) loadPageBitmap(idx + 1)
            if (idx - 1 >= 0) loadPageBitmap(idx - 1)
        }
    }

    // Background and color matrix for dark reading mode
    val backgroundColor = when (readingBackground) {
        ReadingBackground.SEPIA -> Color(0xFFFBF0D9)
        ReadingBackground.PAPER -> Color(0xFFF5F5F0)
        ReadingBackground.DARK -> Color(0xFF18181B)
        ReadingBackground.INVERT -> Color(0xFF0F172A)
        ReadingBackground.DEFAULT -> if (darkReadingMode) Color(0xFF18181B) else Color(0xFFE2E8F0)
    }

    val colorFilter: ColorFilter? = if (darkReadingMode || readingBackground == ReadingBackground.INVERT) {
        // High contrast inverted colors for dark reading mode
        val invertMatrix = ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        ColorFilter.colorMatrix(invertMatrix)
    } else if (readingBackground == ReadingBackground.SEPIA) {
        val sepiaMatrix = ColorMatrix().apply {
            setToSaturation(0.7f)
        }
        ColorFilter.colorMatrix(sepiaMatrix)
    } else {
        null
    }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        offset = if (scale > 1f) {
            Offset(
                x = (offset.x + offsetChange.x).coerceIn(-500f * (scale - 1), 500f * (scale - 1)),
                y = (offset.y + offsetChange.y).coerceIn(-800f * (scale - 1), 800f * (scale - 1))
            )
        } else {
            Offset.Zero
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale > 1f) 1f else 2.2f
                        offset = Offset.Zero
                    }
                )
            }
            .transformable(state = transformState)
            .testTag("pdf_viewer_screen")
    ) {
        if (renderError != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Unable to render PDF",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = renderError ?: "Unknown error",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (pdfRenderer == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            if (continuousScroll) {
                // --- CONTINUOUS MULTI-PAGE SCROLL ---
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                            rotationZ = pageRotation.toFloat()
                        },
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(pageCount) { pageIdx ->
                        val pageNum = pageIdx + 1
                        LaunchedEffect(pageIdx) {
                            loadPageBitmap(pageIdx)
                        }

                        val bitmap = pageBitmaps[pageIdx]

                        // Report current visible page
                        DisposableEffect(pageNum) {
                            onPageChanged(pageNum)
                            onDispose { }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            ) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "PDF Page $pageNum",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight(),
                                        contentScale = ContentScale.FillWidth,
                                        colorFilter = colorFilter
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(450.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                    }
                                }

                                // Page label badge top-right
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "$pageNum / $pageCount",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // --- SINGLE PAGE VIEW ---
                val currentIdx = (currentPage - 1).coerceIn(0, pageCount - 1)
                LaunchedEffect(currentIdx) {
                    loadPageBitmap(currentIdx)
                }
                val bitmap = pageBitmaps[currentIdx]

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                            rotationZ = pageRotation.toFloat()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "PDF Page $currentPage",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                contentScale = ContentScale.FillWidth,
                                colorFilter = colorFilter
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(480.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }

            // Floating Controls Overlay (Thumbnails toggle, Jump to page, Bookmarks)
            // Floating Page Navigation Pill (e.g. Page 1 / 4)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .clickable { showJumpDialog = true },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shadowElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Page $currentPage of $pageCount",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Jump to page",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Floating Back to Top button
            AnimatedVisibility(
                visible = currentPage > 2,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, end = 20.dp),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                            onPageChanged(1)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Back to top")
                }
            }

            // Thumbnail drawer at the bottom if enabled
            AnimatedVisibility(
                visible = showThumbnails,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Page Thumbnails ($pageCount)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            IconButton(onClick = { showThumbnails = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close thumbnails")
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(pageCount) { idx ->
                                val pageNum = idx + 1
                                val isCur = pageNum == currentPage
                                LaunchedEffect(idx) {
                                    loadPageBitmap(idx)
                                }
                                val b = pageBitmaps[idx]

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(70.dp)
                                        .clickable {
                                            onPageChanged(pageNum)
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(idx)
                                            }
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 66.dp, height = 90.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(
                                                width = if (isCur) 2.5.dp else 1.dp,
                                                color = if (isCur) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (b != null) {
                                            Image(
                                                bitmap = b.asImageBitmap(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$pageNum",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isCur) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isCur) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Jump To Page Dialog
        if (showJumpDialog) {
            AlertDialog(
                onDismissRequest = { showJumpDialog = false },
                title = { Text("Go to Page") },
                text = {
                    Column {
                        Text(
                            text = "Enter a page number between 1 and $pageCount:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = jumpPageInput,
                            onValueChange = { jumpPageInput = it.filter { char -> char.isDigit() } },
                            placeholder = { Text("1 - $pageCount") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val target = jumpPageInput.toIntOrNull()
                            if (target != null && target in 1..pageCount) {
                                onPageChanged(target)
                                coroutineScope.launch {
                                    listState.animateScrollToItem(target - 1)
                                }
                            }
                            showJumpDialog = false
                            jumpPageInput = ""
                        }
                    ) {
                        Text("Go")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJumpDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
