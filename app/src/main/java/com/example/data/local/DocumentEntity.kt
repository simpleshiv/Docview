package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DocFormat
import com.example.data.model.DocumentItem

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val path: String,
    val uriString: String?,
    val extension: String,
    val formatName: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val lastOpened: Long = 0L,
    val isFavorite: Boolean = false,
    val pageCount: Int = 1,
    val lastViewedPage: Int = 1,
    val bookmarksCsv: String = "",
    val isSample: Boolean = false,
    val folderName: String = "Documents"
) {
    fun toDocumentItem(): DocumentItem {
        val bookmarksList = if (bookmarksCsv.isBlank()) {
            emptyList()
        } else {
            bookmarksCsv.split(",").mapNotNull { it.trim().toIntOrNull() }
        }
        val docFormat = try {
            DocFormat.valueOf(formatName)
        } catch (e: Exception) {
            DocFormat.fromExtension(extension)
        }

        return DocumentItem(
            id = id,
            name = name,
            extension = extension,
            format = docFormat,
            path = path,
            uriString = uriString,
            sizeBytes = sizeBytes,
            lastModified = lastModified,
            lastOpened = lastOpened,
            isFavorite = isFavorite,
            pageCount = pageCount,
            lastViewedPage = lastViewedPage,
            bookmarks = bookmarksList,
            isSample = isSample,
            folderName = folderName
        )
    }

    companion object {
        fun fromDocumentItem(item: DocumentItem): DocumentEntity {
            return DocumentEntity(
                id = item.id,
                name = item.name,
                path = item.path,
                uriString = item.uriString,
                extension = item.extension,
                formatName = item.format.name,
                sizeBytes = item.sizeBytes,
                lastModified = item.lastModified,
                lastOpened = item.lastOpened,
                isFavorite = item.isFavorite,
                pageCount = item.pageCount,
                lastViewedPage = item.lastViewedPage,
                bookmarksCsv = item.bookmarks.joinToString(","),
                isSample = item.isSample,
                folderName = item.folderName
            )
        }
    }
}
