package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.DocFormat
import com.example.data.model.DocumentItem
import com.example.data.model.ReadingBackground
import com.example.ui.viewmodel.MainViewModel
import com.example.viewer.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentViewerContainer(
    doc: DocumentItem,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val continuousScroll by viewModel.continuousScroll.collectAsState()
    val darkReadingMode by viewModel.darkReadingMode.collectAsState()
    val readingBackground by viewModel.readingBackground.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()
    val pageRotation by viewModel.pageRotation.collectAsState()
    val isFullScreen by viewModel.isFullScreen.collectAsState()

    var showMoreMenu by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }

    // Android back navigation handler
    BackHandler {
        viewModel.closeDocument()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("document_viewer_container"),
        topBar = {
            if (!isFullScreen) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = doc.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${doc.format.displayName} • ${doc.formattedSize}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.closeDocument() },
                            modifier = Modifier.testTag("viewer_back_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Bookmark button for PDF
                        if (doc.format == DocFormat.PDF) {
                            val isBookmarked = doc.bookmarks.contains(currentPage)
                            IconButton(onClick = { viewModel.toggleBookmark(currentPage) }) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Share
                        IconButton(onClick = { shareDocument(context, doc) }) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share")
                        }

                        // Fullscreen toggle
                        IconButton(onClick = { viewModel.toggleFullScreen() }) {
                            Icon(
                                imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen"
                            )
                        }

                        // More dropdown menu
                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options")
                            }

                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Rotate 90°") },
                                    leadingIcon = { Icon(Icons.Outlined.RotateRight, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.rotatePages()
                                    }
                                )
                                if (doc.format == DocFormat.PDF) {
                                    DropdownMenuItem(
                                        text = { Text(if (continuousScroll) "Single Page Mode" else "Continuous Scroll") },
                                        leadingIcon = { Icon(Icons.Outlined.ViewCarousel, contentDescription = null) },
                                        onClick = {
                                            showMoreMenu = false
                                            viewModel.toggleContinuousScroll()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("View Bookmarks (${doc.bookmarks.size})") },
                                        leadingIcon = { Icon(Icons.Outlined.Bookmarks, contentDescription = null) },
                                        onClick = {
                                            showMoreMenu = false
                                            showBookmarksSheet = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Print Document") },
                                        leadingIcon = { Icon(Icons.Outlined.Print, contentDescription = null) },
                                        onClick = {
                                            showMoreMenu = false
                                            printDocument(context, doc)
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Reading Theme") },
                                    leadingIcon = { Icon(Icons.Outlined.Palette, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        showThemeMenu = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("File Information") },
                                    leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.showDocumentDetails(doc)
                                    }
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
        },
        bottomBar = {
            if (!isFullScreen && doc.format == DocFormat.PDF && totalPages > 1) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .navigationBarsPadding()
                    ) {
                        // Slider row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (currentPage > 1) viewModel.updateCurrentPage(currentPage - 1)
                                },
                                enabled = currentPage > 1,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Page")
                            }

                            Slider(
                                value = currentPage.toFloat(),
                                onValueChange = { viewModel.updateCurrentPage(it.toInt()) },
                                valueRange = 1f..totalPages.toFloat(),
                                steps = if (totalPages > 2) totalPages - 2 else 0,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    if (currentPage < totalPages) viewModel.updateCurrentPage(currentPage + 1)
                                },
                                enabled = currentPage < totalPages,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Page")
                            }
                        }

                        // Toolbar quick buttons (Rotate, Dark Mode, Page Indicator)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { viewModel.rotatePages() }) {
                                Icon(Icons.Outlined.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rotate", fontSize = 12.sp)
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "Page $currentPage / $totalPages",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            TextButton(onClick = { viewModel.toggleDarkReadingMode() }) {
                                Icon(
                                    imageVector = if (darkReadingMode) Icons.Filled.DarkMode else Icons.Outlined.DarkMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (darkReadingMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (darkReadingMode) "Night" else "Day", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (doc.format) {
                DocFormat.PDF -> {
                    PdfViewerScreen(
                        doc = doc,
                        continuousScroll = continuousScroll,
                        darkReadingMode = darkReadingMode,
                        readingBackground = readingBackground,
                        pageRotation = pageRotation,
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onPageChanged = { viewModel.updateCurrentPage(it) },
                        onTotalPagesLoaded = { viewModel.setTotalPages(it) },
                        onToggleBookmark = { viewModel.toggleBookmark(it) }
                    )
                }
                DocFormat.WORD, DocFormat.EXCEL, DocFormat.POWERPOINT -> {
                    OfficeViewerScreen(doc = doc)
                }
                DocFormat.TEXT -> {
                    TextViewerScreen(doc = doc)
                }
                DocFormat.IMAGE -> {
                    ImageViewerScreen(doc = doc)
                }
                DocFormat.OTHER -> {
                    FallbackViewerScreen(doc = doc)
                }
            }

            // Exit Fullscreen floating badge
            if (isFullScreen) {
                IconButton(
                    onClick = { viewModel.toggleFullScreen() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen", tint = Color.White)
                }
            }
        }
    }

    // Bookmarks Modal Sheet
    if (showBookmarksSheet) {
        ModalBottomSheet(onDismissRequest = { showBookmarksSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Page Bookmarks",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (doc.bookmarks.isEmpty()) {
                    Text(
                        text = "No pages bookmarked yet. Tap the bookmark icon on any page to save it here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    for (bm in doc.bookmarks) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            onClick = {
                                viewModel.updateCurrentPage(bm)
                                showBookmarksSheet = false
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Page $bm", fontWeight = FontWeight.SemiBold)
                                Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Reading Background Theme Dialog
    if (showThemeMenu) {
        AlertDialog(
            onDismissRequest = { showThemeMenu = false },
            title = { Text("Reading Palette") },
            text = {
                Column {
                    for (theme in ReadingBackground.values()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setReadingBackground(theme)
                                    showThemeMenu = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = readingBackground == theme,
                                onClick = {
                                    viewModel.setReadingBackground(theme)
                                    showThemeMenu = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(theme.label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeMenu = false }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun shareDocument(context: Context, doc: DocumentItem) {
    try {
        val file = File(doc.path)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share ${doc.name}"))
    } catch (e: Exception) {
        // ignore
    }
}

private fun printDocument(context: Context, doc: DocumentItem) {
    try {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: android.os.CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: android.os.Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = android.print.PrintDocumentInfo.Builder(doc.name)
                    .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(doc.pageCount)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out android.print.PageRange>?,
                destination: android.os.ParcelFileDescriptor?,
                cancellationSignal: android.os.CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                try {
                    val file = File(doc.path)
                    file.inputStream().use { input ->
                        android.os.ParcelFileDescriptor.AutoCloseOutputStream(destination).use { output ->
                            input.copyTo(output)
                        }
                    }
                    callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }
        printManager.print(doc.name, printAdapter, PrintAttributes.Builder().build())
    } catch (e: Exception) {
        // ignore
    }
}
