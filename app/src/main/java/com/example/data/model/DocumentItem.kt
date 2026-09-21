package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DocumentItem(
    val id: String,
    val name: String,
    val extension: String,
    val format: DocFormat,
    val path: String,
    val uriString: String? = null,
    val sizeBytes: Long = 0L,
    val lastModified: Long = System.currentTimeMillis(),
    val lastOpened: Long = 0L,
    val isFavorite: Boolean = false,
    val pageCount: Int = 1,
    val lastViewedPage: Int = 1,
    val bookmarks: List<Int> = emptyList(),
    val isSample: Boolean = false,
    val folderName: String = "Documents"
) {
    val formattedSize: String
        get() = formatFileSize(sizeBytes)

    val formattedLastOpened: String
        get() {
            if (lastOpened <= 0L) return "Never opened"
            val diffMs = System.currentTimeMillis() - lastOpened
            val minutes = diffMs / (1000 * 60)
            val hours = minutes / 60
            val days = hours / 24

            return when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "Opened $minutes min ago"
                hours < 24 -> "Opened $hours hr${if (hours > 1) "s" else ""} ago"
                days == 1L -> "Opened yesterday"
                days < 7 -> "Opened $days days ago"
                else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(lastOpened))
            }
        }

    val formattedModified: String
        get() = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(lastModified))

    companion object {
        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            val clamped = digitGroups.coerceIn(0, units.size - 1)
            val value = bytes / Math.pow(1024.0, clamped.toDouble())
            return String.format(Locale.US, "%.1f %s", value, units[clamped])
        }
    }
}
