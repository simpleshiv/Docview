package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class DocFormat(
    val displayName: String,
    val brandColor: Color,
    val containerColor: Color,
    val extensions: List<String>,
    val emoji: String
) {
    PDF(
        displayName = "PDF",
        brandColor = FormatPdf,
        containerColor = FormatPdfBg,
        extensions = listOf("pdf"),
        emoji = "📄"
    ),
    WORD(
        displayName = "Word",
        brandColor = FormatWord,
        containerColor = FormatWordBg,
        extensions = listOf("doc", "docx"),
        emoji = "📝"
    ),
    EXCEL(
        displayName = "Excel",
        brandColor = FormatExcel,
        containerColor = FormatExcelBg,
        extensions = listOf("xls", "xlsx"),
        emoji = "📊"
    ),
    POWERPOINT(
        displayName = "PowerPoint",
        brandColor = FormatPowerPoint,
        containerColor = FormatPowerPointBg,
        extensions = listOf("ppt", "pptx"),
        emoji = "📽️"
    ),
    IMAGE(
        displayName = "Images",
        brandColor = FormatImage,
        containerColor = FormatImageBg,
        extensions = listOf("jpg", "jpeg", "png", "webp", "bmp", "gif", "tiff"),
        emoji = "🖼️"
    ),
    TEXT(
        displayName = "Text / Code",
        brandColor = FormatText,
        containerColor = FormatTextBg,
        extensions = listOf("txt", "csv", "rtf", "log", "json", "xml", "html", "md"),
        emoji = "📋"
    ),
    OTHER(
        displayName = "Other Files",
        brandColor = FormatOther,
        containerColor = FormatOtherBg,
        extensions = emptyList(),
        emoji = "📁"
    );

    companion object {
        fun fromExtension(ext: String): DocFormat {
            val lower = ext.lowercase().trim().removePrefix(".")
            return values().firstOrNull { it.extensions.contains(lower) } ?: OTHER
        }

        fun fromFileName(fileName: String): DocFormat {
            val ext = fileName.substringAfterLast(".", "")
            return fromExtension(ext)
        }
    }
}
