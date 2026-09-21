package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.local.DocViewDatabase
import com.example.data.local.DocumentDao
import com.example.data.local.DocumentEntity
import com.example.data.model.DocFormat
import com.example.data.model.DocumentItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class DocumentRepository(
    private val context: Context,
    private val documentDao: DocumentDao = DocViewDatabase.getInstance(context).documentDao()
) {

    val allDocuments: Flow<List<DocumentItem>> = documentDao.getAllDocuments().map { entities ->
        entities.map { it.toDocumentItem() }
    }

    val favoriteDocuments: Flow<List<DocumentItem>> = documentDao.getFavoriteDocuments().map { entities ->
        entities.map { it.toDocumentItem() }
    }

    val recentDocuments: Flow<List<DocumentItem>> = documentDao.getRecentDocuments(15).map { entities ->
        entities.map { it.toDocumentItem() }
    }

    suspend fun initializeSamplesIfNeeded() = withContext(Dispatchers.IO) {
        val count = documentDao.getDocumentCount()
        if (count == 0) {
            val samples = SampleDocumentGenerator.generateSampleDocuments(context)
            documentDao.insertAll(samples.map { DocumentEntity.fromDocumentItem(it) })
        }
    }

    suspend fun getDocumentById(id: String): DocumentItem? = withContext(Dispatchers.IO) {
        documentDao.getDocumentById(id)?.toDocumentItem()
    }

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        documentDao.updateFavorite(id, isFavorite)
    }

    suspend fun recordDocumentOpened(id: String, pageNumber: Int = 1) = withContext(Dispatchers.IO) {
        documentDao.updateLastOpened(id, System.currentTimeMillis(), pageNumber)
    }

    suspend fun updateBookmarks(id: String, bookmarks: List<Int>) = withContext(Dispatchers.IO) {
        documentDao.updateBookmarks(id, bookmarks.joinToString(","))
    }

    suspend fun renameDocument(id: String, newNameWithoutExt: String): Boolean = withContext(Dispatchers.IO) {
        val doc = documentDao.getDocumentById(id) ?: return@withContext false
        val cleanName = if (newNameWithoutExt.endsWith(".${doc.extension}", ignoreCase = true)) {
            newNameWithoutExt
        } else {
            "$newNameWithoutExt.${doc.extension}"
        }

        val oldFile = File(doc.path)
        val newFile = File(oldFile.parentFile, cleanName)
        val renamed = if (oldFile.exists()) {
            oldFile.renameTo(newFile)
        } else {
            true
        }

        if (renamed) {
            documentDao.renameDocument(id, cleanName, newFile.absolutePath)
            true
        } else {
            false
        }
    }

    suspend fun deleteDocument(id: String): Boolean = withContext(Dispatchers.IO) {
        val doc = documentDao.getDocumentById(id) ?: return@withContext false
        val file = File(doc.path)
        if (file.exists()) {
            file.delete()
        }
        documentDao.deleteDocumentById(id)
        true
    }

    suspend fun deleteMultiple(ids: List<String>) = withContext(Dispatchers.IO) {
        for (id in ids) {
            val doc = documentDao.getDocumentById(id)
            if (doc != null) {
                val file = File(doc.path)
                if (file.exists()) file.delete()
            }
        }
        documentDao.deleteDocumentsByIds(ids)
    }

    suspend fun importFromUri(uri: Uri): DocumentItem? = withContext(Dispatchers.IO) {
        try {
            var fileName = "Imported_Document"
            var fileSize = 0L

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }

            val docsDir = File(context.filesDir, "Documents").apply { if (!exists()) mkdirs() }
            val destFile = File(docsDir, fileName)
            var finalFile = destFile
            var counter = 1
            while (finalFile.exists()) {
                val nameWithoutExt = fileName.substringBeforeLast(".")
                val ext = fileName.substringAfterLast(".", "")
                finalFile = File(docsDir, "${nameWithoutExt}_$counter.$ext")
                counter++
            }

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(finalFile).use { output ->
                    input.copyTo(output)
                }
            }

            val ext = finalFile.extension
            val format = DocFormat.fromExtension(ext)
            val docItem = DocumentItem(
                id = UUID.randomUUID().toString(),
                name = finalFile.name,
                extension = ext,
                format = format,
                path = finalFile.absolutePath,
                uriString = uri.toString(),
                sizeBytes = finalFile.length(),
                lastModified = System.currentTimeMillis(),
                lastOpened = System.currentTimeMillis(),
                isFavorite = false,
                pageCount = 1,
                lastViewedPage = 1,
                folderName = "Imported"
            )

            documentDao.insertDocument(DocumentEntity.fromDocumentItem(docItem))
            docItem
        } catch (e: Exception) {
            null
        }
    }
}
