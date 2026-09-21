package com.example.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocFormat
import com.example.data.model.DocumentItem
import com.example.parser.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun OfficeViewerScreen(
    doc: DocumentItem,
    modifier: Modifier = Modifier
) {
    when (doc.format) {
        DocFormat.WORD -> DocxViewer(doc, modifier)
        DocFormat.EXCEL -> XlsxViewer(doc, modifier)
        DocFormat.POWERPOINT -> PptxViewer(doc, modifier)
        else -> FallbackViewerScreen(doc, modifier)
    }
}

// -------------------------------------------------------------
// 1. DOCX VIEWER
// -------------------------------------------------------------
@Composable
fun DocxViewer(
    doc: DocumentItem,
    modifier: Modifier = Modifier
) {
    var docxData by remember { mutableStateOf<DocxDocument?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(doc.path) {
        isLoading = true
        docxData = withContext(Dispatchers.IO) {
            DocxParser.parse(File(doc.path))
        }
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val data = docxData
        if (data == null) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Unable to load Word document", color = MaterialTheme.colorScheme.error)
            }
        } else {
            Surface(
                modifier = modifier
                    .fillMaxSize()
                    .testTag("docx_viewer"),
                color = MaterialTheme.colorScheme.background
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = Color(0xFFDBEAFE),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "DOCX",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF1D4ED8)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = data.title,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    items(data.elements) { element ->
                        when (element) {
                            is DocxElement.Heading -> {
                                Text(
                                    text = element.text,
                                    style = if (element.level == 1) {
                                        MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                    } else {
                                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    },
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                                )
                            }
                            is DocxElement.Paragraph -> {
                                val annotated = buildAnnotatedString {
                                    for (run in element.runs) {
                                        withStyle(
                                            SpanStyle(
                                                fontWeight = if (run.isBold) FontWeight.Bold else FontWeight.Normal,
                                                fontStyle = if (run.isItalic) FontStyle.Italic else FontStyle.Normal
                                            )
                                        ) {
                                            append(run.text)
                                        }
                                    }
                                }
                                Text(
                                    text = annotated,
                                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                                )
                            }
                            is DocxElement.BulletItem -> {
                                Row(modifier = Modifier.padding(start = 12.dp, top = 2.dp, bottom = 2.dp)) {
                                    Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        text = element.text,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            is DocxElement.Table -> {
                                TableView(rows = element.rows)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. XLSX VIEWER (Interactive Grid & Sheet Tabs)
// -------------------------------------------------------------
@Composable
fun XlsxViewer(
    doc: DocumentItem,
    modifier: Modifier = Modifier
) {
    var workbook by remember { mutableStateOf<XlsxWorkbook?>(null) }
    var selectedSheetIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(doc.path) {
        isLoading = true
        workbook = withContext(Dispatchers.IO) {
            XlsxParser.parse(File(doc.path))
        }
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val wb = workbook
        if (wb == null || wb.sheets.isEmpty()) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Unable to open Excel spreadsheet", color = MaterialTheme.colorScheme.error)
            }
        } else {
            val sheet = wb.sheets.getOrElse(selectedSheetIndex) { wb.sheets.first() }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .testTag("xlsx_viewer")
            ) {
                // Spreadsheet Grid
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val horizScroll = rememberScrollState()
                    val vertScroll = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(vertScroll)
                            .horizontalScroll(horizScroll)
                            .padding(12.dp)
                    ) {
                        // Header Row (Column Letters A, B, C, ...)
                        Row {
                            // Row number corner
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 36.dp)
                                    .background(Color(0xFFE2E8F0))
                                    .border(0.5.dp, Color(0xFFCBD5E1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("#", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }

                            for (header in sheet.columnHeaders) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 130.dp, height = 36.dp)
                                        .background(Color(0xFFF1F5F9))
                                        .border(0.5.dp, Color(0xFFCBD5E1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = header,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF334155)
                                    )
                                }
                            }
                        }

                        // Data Rows
                        sheet.rows.forEachIndexed { rowIndex, row ->
                            Row {
                                // Row number
                                Box(
                                    modifier = Modifier
                                        .size(width = 44.dp, height = 36.dp)
                                        .background(Color(0xFFF8FAFC))
                                        .border(0.5.dp, Color(0xFFCBD5E1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${rowIndex + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray
                                    )
                                }

                                // Cells
                                for (colIndex in sheet.columnHeaders.indices) {
                                    val cellText = row.getOrElse(colIndex) { "" }
                                    val isHeaderRow = rowIndex == 0

                                    Box(
                                        modifier = Modifier
                                            .size(width = 130.dp, height = 36.dp)
                                            .background(
                                                if (isHeaderRow) Color(0xFFD1FAE5) else Color.White
                                            )
                                            .border(0.5.dp, Color(0xFFE2E8F0))
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = cellText,
                                            fontSize = 12.sp,
                                            fontWeight = if (isHeaderRow) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isHeaderRow) Color(0xFF065F46) else Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Sheet Tabs (e.g. Sheet 1, Sheet 2...)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sheets: ",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(wb.sheets) { idx, s ->
                                val isSelected = idx == selectedSheetIndex
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedSheetIndex = idx },
                                    label = { Text(s.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF059669),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. PPTX VIEWER (Slide Deck Presentation)
// -------------------------------------------------------------
@Composable
fun PptxViewer(
    doc: DocumentItem,
    modifier: Modifier = Modifier
) {
    var presentation by remember { mutableStateOf<PptxPresentation?>(null) }
    var currentSlideIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(doc.path) {
        isLoading = true
        presentation = withContext(Dispatchers.IO) {
            PptxParser.parse(File(doc.path))
        }
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val pres = presentation
        if (pres == null || pres.slides.isEmpty()) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Unable to open PowerPoint presentation", color = MaterialTheme.colorScheme.error)
            }
        } else {
            val totalSlides = pres.slides.size
            val activeSlide = pres.slides.getOrElse(currentSlideIndex) { pres.slides.first() }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
                    .testTag("pptx_viewer")
            ) {
                // Main Slide Canvas (16:9 aspect card)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                // Slide Title
                                Text(
                                    text = activeSlide.title,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = Color(0xFFF8FAFC)
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = Color(0xFF334155))
                                Spacer(modifier = Modifier.height(14.dp))

                                // Bullets
                                for (bullet in activeSlide.bullets) {
                                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(
                                            text = "• ",
                                            color = Color(0xFFF59E0B),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = bullet,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                lineHeight = 20.sp
                                            ),
                                            color = Color(0xFFCBD5E1)
                                        )
                                    }
                                }
                            }

                            // Slide Footer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = pres.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "Slide ${activeSlide.slideNumber} of $totalSlides",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }
                    }
                }

                // Slide Navigation Carousel
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1E293B),
                    tonalElevation = 6.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Slides ($totalSlides)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )

                            Row {
                                IconButton(
                                    onClick = {
                                        if (currentSlideIndex > 0) currentSlideIndex--
                                    },
                                    enabled = currentSlideIndex > 0
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Slide", tint = Color.White)
                                }
                                IconButton(
                                    onClick = {
                                        if (currentSlideIndex < totalSlides - 1) currentSlideIndex++
                                    },
                                    enabled = currentSlideIndex < totalSlides - 1
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Slide", tint = Color.White)
                                }
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            itemsIndexed(pres.slides) { idx, slide ->
                                val isSelected = idx == currentSlideIndex
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(90.dp)
                                        .clickable { currentSlideIndex = idx }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 86.dp, height = 50.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF334155))
                                            .border(
                                                width = if (isSelected) 2.dp else 0.5.dp,
                                                color = if (isSelected) Color(0xFFF59E0B) else Color(0xFF475569),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = slide.title,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                            color = Color.White,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${idx + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color(0xFFF59E0B) else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Shared Table View Composable for DOCX and CSV
// -------------------------------------------------------------
@Composable
fun TableView(rows: List<List<String>>) {
    if (rows.isEmpty()) return
    val horizScroll = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizScroll)
                .padding(8.dp)
        ) {
            rows.forEachIndexed { rowIndex, row ->
                val isHeader = rowIndex == 0
                Row(
                    modifier = Modifier
                        .background(
                            if (isHeader) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else if (rowIndex % 2 == 1) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            else Color.Transparent
                        )
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    for (cell in row) {
                        Box(
                            modifier = Modifier
                                .widthIn(min = 100.dp, max = 220.dp)
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cell,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
