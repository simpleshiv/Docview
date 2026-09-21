package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocFormat
import com.example.data.model.DocumentItem
import com.example.data.model.FilterType
import com.example.ui.components.DocItemCard
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onImportClick: () -> Unit,
    onNavigateToLibrary: (FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    val allDocs by viewModel.allDocuments.collectAsState()
    val recentDocs by viewModel.recentDocuments.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedDocIds by viewModel.selectedDocIds.collectAsState()

    // Format counts
    val pdfCount = remember(allDocs) { allDocs.count { it.format == DocFormat.PDF } }
    val wordCount = remember(allDocs) { allDocs.count { it.format == DocFormat.WORD } }
    val excelCount = remember(allDocs) { allDocs.count { it.format == DocFormat.EXCEL } }
    val pptCount = remember(allDocs) { allDocs.count { it.format == DocFormat.POWERPOINT } }
    val textCount = remember(allDocs) { allDocs.count { it.format == DocFormat.TEXT } }
    val imgCount = remember(allDocs) { allDocs.count { it.format == DocFormat.IMAGE } }

    val formatCategories = listOf(
        FormatQuickBadge("PDF", "📕", Color(0xFFEF4444), Color(0xFFFEE2E2), pdfCount, FilterType.PDF),
        FormatQuickBadge("Word", "📘", Color(0xFF2563EB), Color(0xFFDBEAFE), wordCount, FilterType.WORD),
        FormatQuickBadge("Excel", "📗", Color(0xFF059669), Color(0xFFD1FAE5), excelCount, FilterType.EXCEL),
        FormatQuickBadge("PowerPoint", "📙", Color(0xFFD97706), Color(0xFFFEF3C7), pptCount, FilterType.POWERPOINT),
        FormatQuickBadge("Text & Data", "📄", Color(0xFF4B5563), Color(0xFFF3F4F6), textCount, FilterType.TEXT),
        FormatQuickBadge("Images", "🖼️", Color(0xFF7C3AED), Color(0xFFEDE9FE), imgCount, FilterType.IMAGES)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // App Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DocView Pro",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Every Document. One Powerful Viewer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalIconButton(
                        onClick = onImportClick,
                        modifier = Modifier.size(44.dp).testTag("header_import_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Import Document", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search files, formats, or folders...") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_bar"),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        // Hero Storage & Privacy Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${allDocs.size} Documents Local & Secure",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "100% offline viewing • Zero cloud telemetry",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Format Categories Horizontal Carousel
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "Document Categories",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(formatCategories) { item ->
                        Surface(
                            modifier = Modifier
                                .width(120.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onNavigateToLibrary(item.filter) }
                                .testTag("category_badge_${item.name}"),
                            color = item.containerColor,
                            tonalElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(item.emoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = item.tintColor
                                )
                                Text(
                                    text = "${item.count} files",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = item.tintColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Documents Carousel
        if (recentDocs.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 20.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Documents",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        TextButton(
                            onClick = { onNavigateToLibrary(FilterType.RECENT) }
                        ) {
                            Text("See All", fontSize = 12.sp)
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recentDocs.take(6)) { doc ->
                            DocItemCard(
                                doc = doc,
                                isGrid = true,
                                isSelected = selectedDocIds.contains(doc.id),
                                isSelectionMode = isSelectionMode,
                                onClick = { viewModel.openDocument(doc) },
                                onLongClick = { viewModel.toggleSelectDoc(doc.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(doc) },
                                onShowDetails = { viewModel.showDocumentDetails(doc) },
                                onRename = { viewModel.showRenameDialog(doc) },
                                onShare = { /* handled by details or dropdown */ },
                                onDelete = { viewModel.deleteDocument(doc) },
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    }
                }
            }
        }

        // All Documents List Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Files",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = { onNavigateToLibrary(FilterType.ALL) }) {
                    Text("View All (${allDocs.size})", fontSize = 12.sp)
                }
            }
        }

        val displayList = if (searchQuery.isNotBlank()) {
            allDocs.filter { it.name.contains(searchQuery, ignoreCase = true) }
        } else {
            allDocs.take(10)
        }

        items(displayList) { doc ->
            DocItemCard(
                doc = doc,
                isGrid = false,
                isSelected = selectedDocIds.contains(doc.id),
                isSelectionMode = isSelectionMode,
                onClick = { viewModel.openDocument(doc) },
                onLongClick = { viewModel.toggleSelectDoc(doc.id) },
                onToggleFavorite = { viewModel.toggleFavorite(doc) },
                onShowDetails = { viewModel.showDocumentDetails(doc) },
                onRename = { viewModel.showRenameDialog(doc) },
                onShare = { /* handled */ },
                onDelete = { viewModel.deleteDocument(doc) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }
    }
}

private data class FormatQuickBadge(
    val name: String,
    val emoji: String,
    val tintColor: Color,
    val containerColor: Color,
    val count: Int,
    val filter: FilterType
)
