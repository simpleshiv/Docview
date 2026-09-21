package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentItem

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DocItemCard(
    doc: DocumentItem,
    isGrid: Boolean = false,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShowDetails: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val starColor by animateColorAsState(
        targetValue = if (doc.isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        animationSpec = spring(),
        label = "star_color"
    )

    if (isGrid) {
        // --- GRID VIEW CARD ---
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(16.dp)
                )
                .combinedClickable(
                    onClick = {
                        if (isSelectionMode) onLongClick() else onClick()
                    },
                    onLongClick = onLongClick
                )
                .testTag("doc_grid_card_${doc.id}"),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Top row: Format badge and star
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(doc.format.containerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = doc.format.emoji,
                            fontSize = 18.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isSelectionMode) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onLongClick() },
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            IconButton(
                                onClick = onToggleFavorite,
                                modifier = Modifier.size(32.dp).testTag("favorite_btn_${doc.id}")
                            ) {
                                Icon(
                                    imageVector = if (doc.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = starColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = doc.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Metadata row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${doc.format.displayName} • ${doc.formattedSize}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DocActionDropdown(
                            expanded = menuExpanded,
                            onDismiss = { menuExpanded = false },
                            isFavorite = doc.isFavorite,
                            onToggleFavorite = {
                                menuExpanded = false
                                onToggleFavorite()
                            },
                            onShowDetails = {
                                menuExpanded = false
                                onShowDetails()
                            },
                            onRename = {
                                menuExpanded = false
                                onRename()
                            },
                            onShare = {
                                menuExpanded = false
                                onShare()
                            },
                            onDelete = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    } else {
        // --- LIST VIEW CARD ---
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
                .combinedClickable(
                    onClick = {
                        if (isSelectionMode) onLongClick() else onClick()
                    },
                    onLongClick = onLongClick
                )
                .testTag("doc_list_card_${doc.id}"),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selection checkbox if in multi-select mode
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onLongClick() },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(24.dp)
                    )
                }

                // File Type Icon Box
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(doc.format.containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = doc.format.emoji,
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Text info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = doc.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = doc.format.brandColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = doc.format.displayName.uppercase(),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = doc.format.brandColor
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "${doc.formattedSize} • ${doc.formattedLastOpened}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Favorite & Menu
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("favorite_btn_${doc.id}")
                ) {
                    Icon(
                        imageVector = if (doc.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = starColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("more_menu_btn_${doc.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DocActionDropdown(
                        expanded = menuExpanded,
                        onDismiss = { menuExpanded = false },
                        isFavorite = doc.isFavorite,
                        onToggleFavorite = {
                            menuExpanded = false
                            onToggleFavorite()
                        },
                        onShowDetails = {
                            menuExpanded = false
                            onShowDetails()
                        },
                        onRename = {
                            menuExpanded = false
                            onRename()
                        },
                        onShare = {
                            menuExpanded = false
                            onShare()
                        },
                        onDelete = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DocActionDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onShowDetails: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites") },
            leadingIcon = {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = if (isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            onClick = onToggleFavorite
        )
        DropdownMenuItem(
            text = { Text("Document Details") },
            leadingIcon = {
                Icon(Icons.Outlined.Info, contentDescription = null)
            },
            onClick = onShowDetails
        )
        DropdownMenuItem(
            text = { Text("Rename") },
            leadingIcon = {
                Icon(Icons.Outlined.Edit, contentDescription = null)
            },
            onClick = onRename
        )
        DropdownMenuItem(
            text = { Text("Share") },
            leadingIcon = {
                Icon(Icons.Outlined.Share, contentDescription = null)
            },
            onClick = onShare
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            onClick = onDelete
        )
    }
}
