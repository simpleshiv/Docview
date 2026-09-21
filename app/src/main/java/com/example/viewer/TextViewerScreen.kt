package com.example.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentItem
import com.example.parser.CsvParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun TextViewerScreen(
    doc: DocumentItem,
    modifier: Modifier = Modifier
) {
    var rawText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showLineNumbers by remember { mutableStateOf(true) }
    var fontSizeSp by remember { mutableStateOf(13) }
    var isTableView by remember { mutableStateOf(doc.extension.equals("csv", ignoreCase = true)) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(doc.path) {
        isLoading = true
        rawText = withContext(Dispatchers.IO) {
            try {
                File(doc.path).readText()
            } catch (e: Exception) {
                "Unable to read file contents: ${e.message}"
            }
        }
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val lines = remember(rawText) { rawText.lines() }
        val wordCount = remember(rawText) {
            rawText.split("\\s+".toRegex()).count { it.isNotBlank() }
        }
        val isCsv = doc.extension.equals("csv", ignoreCase = true)

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("text_viewer_screen")
        ) {
            // Metrics bar (Lines, Words, Chars, and Controls)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${lines.size} lines • $wordCount words • ${rawText.length} chars",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isCsv) {
                            TextButton(
                                onClick = { isTableView = !isTableView },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(if (isTableView) "Raw Text" else "Grid Table")
                            }
                        }

                        IconButton(
                            onClick = { showLineNumbers = !showLineNumbers },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (showLineNumbers) Icons.Outlined.FormatListNumbered else Icons.Outlined.Notes,
                                contentDescription = "Toggle Line Numbers",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (fontSizeSp < 22) fontSizeSp += 2 else fontSizeSp = 11
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FormatSize,
                                contentDescription = "Adjust Font Size",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (isCsv && isTableView) {
                // Table View for CSV
                val parsedCsv = remember(rawText) { CsvParser.parse(rawText) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    TableView(rows = parsedCsv)
                }
            } else {
                // Monospace Text View with line numbers
                val horizScroll = rememberScrollState()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    itemsIndexed(lines) { index, line ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp)
                        ) {
                            if (showLineNumbers) {
                                Text(
                                    text = "${index + 1}".padStart(4, ' '),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = fontSizeSp.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .width(42.dp)
                                        .padding(end = 8.dp)
                                )
                            }

                            // Colorize JSON/XML/LOG syntax or search highlight
                            val formattedLine = buildAnnotatedString {
                                val trimmed = line.trim()
                                when {
                                    trimmed.startsWith("[ERROR") || trimmed.contains("[ERROR]") -> {
                                        withStyle(SpanStyle(color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)) {
                                            append(line)
                                        }
                                    }
                                    trimmed.startsWith("[WARN") || trimmed.contains("[WARN]") -> {
                                        withStyle(SpanStyle(color = Color(0xFFD97706), fontWeight = FontWeight.SemiBold)) {
                                            append(line)
                                        }
                                    }
                                    trimmed.startsWith("[INFO") || trimmed.contains("[INFO]") -> {
                                        withStyle(SpanStyle(color = Color(0xFF2563EB))) {
                                            append(line)
                                        }
                                    }
                                    trimmed.startsWith("\"") && trimmed.contains("\":") -> {
                                        // JSON Key
                                        val parts = line.split(":", limit = 2)
                                        withStyle(SpanStyle(color = Color(0xFF7C3AED), fontWeight = FontWeight.SemiBold)) {
                                            append(parts[0])
                                        }
                                        if (parts.size > 1) {
                                            append(":")
                                            withStyle(SpanStyle(color = Color(0xFF059669))) {
                                                append(parts[1])
                                            }
                                        }
                                    }
                                    else -> {
                                        append(line)
                                    }
                                }
                            }

                            Text(
                                text = formattedLine,
                                fontFamily = FontFamily.Monospace,
                                fontSize = fontSizeSp.sp,
                                lineHeight = (fontSizeSp + 6).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
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
