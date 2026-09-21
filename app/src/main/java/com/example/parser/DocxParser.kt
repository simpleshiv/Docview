package com.example.parser

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStream
import java.io.StringReader
import java.util.zip.ZipFile

sealed class DocxElement {
    data class Heading(val text: String, val level: Int = 1) : DocxElement()
    data class Paragraph(val runs: List<TextRun>) : DocxElement() {
        val fullText: String get() = runs.joinToString("") { it.text }
    }
    data class Table(val rows: List<List<String>>) : DocxElement()
    data class BulletItem(val text: String) : DocxElement()
}

data class TextRun(
    val text: String,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val colorHex: String? = null
)

data class DocxDocument(
    val title: String,
    val elements: List<DocxElement>
)

object DocxParser {

    fun parse(file: File): DocxDocument {
        return try {
            ZipFile(file).use { zip ->
                val entry = zip.getEntry("word/document.xml")
                if (entry == null) {
                    DocxDocument(file.nameWithoutExtension, listOf(DocxElement.Paragraph(listOf(TextRun("Empty or invalid Word document structure")))))
                } else {
                    zip.getInputStream(entry).use { inputStream ->
                        parseXml(inputStream, file.nameWithoutExtension)
                    }
                }
            }
        } catch (e: Exception) {
            DocxDocument(
                file.nameWithoutExtension,
                listOf(DocxElement.Paragraph(listOf(TextRun("Failed to parse Word document: ${e.message}"))))
            )
        }
    }

    fun parseFromStream(inputStream: InputStream, title: String): DocxDocument {
        return try {
            parseXml(inputStream, title)
        } catch (e: Exception) {
            DocxDocument(title, listOf(DocxElement.Paragraph(listOf(TextRun("Unable to parse Word stream: ${e.message}")))))
        }
    }

    private fun parseXml(stream: InputStream, title: String): DocxDocument {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        val elements = mutableListOf<DocxElement>()
        var eventType = parser.eventType

        var inParagraph = false
        var inHeading = false
        var headingLevel = 1
        var inRun = false
        var inTable = false
        var inRow = false
        var inCell = false

        var isBold = false
        var isItalic = false
        var isUnderline = false

        val currentRuns = mutableListOf<TextRun>()
        val currentTable = mutableListOf<MutableList<String>>()
        var currentRow = mutableListOf<String>()
        val currentCellText = StringBuilder()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name ?: ""

            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (name) {
                        "tbl" -> {
                            inTable = true
                            currentTable.clear()
                        }
                        "tr" -> {
                            inRow = true
                            currentRow = mutableListOf()
                        }
                        "tc" -> {
                            inCell = true
                            currentCellText.clear()
                        }
                        "p" -> {
                            inParagraph = true
                            currentRuns.clear()
                            inHeading = false
                            headingLevel = 1
                        }
                        "pStyle" -> {
                            val styleVal = parser.getAttributeValue(null, "val") ?: ""
                            if (styleVal.contains("Heading", ignoreCase = true) || styleVal.contains("Title", ignoreCase = true)) {
                                inHeading = true
                                val num = styleVal.filter { it.isDigit() }
                                headingLevel = num.toIntOrNull() ?: 1
                            }
                        }
                        "r" -> {
                            inRun = true
                            isBold = false
                            isItalic = false
                            isUnderline = false
                        }
                        "b" -> isBold = true
                        "i" -> isItalic = true
                        "u" -> isUnderline = true
                        "t" -> {
                            val text = parser.nextText()
                            if (inCell) {
                                if (currentCellText.isNotEmpty()) currentCellText.append(" ")
                                currentCellText.append(text)
                            }
                            if (inParagraph) {
                                currentRuns.add(TextRun(text, isBold, isItalic, isUnderline))
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (name) {
                        "p" -> {
                            inParagraph = false
                            if (!inCell) {
                                val fullText = currentRuns.joinToString("") { it.text }.trim()
                                if (fullText.isNotEmpty()) {
                                    if (inHeading) {
                                        elements.add(DocxElement.Heading(fullText, headingLevel))
                                    } else {
                                        elements.add(DocxElement.Paragraph(currentRuns.toList()))
                                    }
                                }
                            }
                        }
                        "tc" -> {
                            inCell = false
                            currentRow.add(currentCellText.toString().trim())
                        }
                        "tr" -> {
                            inRow = false
                            if (currentRow.isNotEmpty()) {
                                currentTable.add(currentRow.toList().toMutableList())
                            }
                        }
                        "tbl" -> {
                            inTable = false
                            if (currentTable.isNotEmpty()) {
                                elements.add(DocxElement.Table(currentTable.map { it.toList() }))
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return DocxDocument(title = title, elements = elements)
    }
}
