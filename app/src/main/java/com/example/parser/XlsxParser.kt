package com.example.parser

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

data class XlsxCell(
    val row: Int,
    val col: Int,
    val colName: String,
    val value: String
)

data class XlsxSheet(
    val name: String,
    val rows: List<List<String>>,
    val columnHeaders: List<String>
)

data class XlsxWorkbook(
    val title: String,
    val sheets: List<XlsxSheet>
)

object XlsxParser {

    fun parse(file: File): XlsxWorkbook {
        return try {
            ZipFile(file).use { zip ->
                // 1. Read shared strings if present
                val sharedStrings = mutableListOf<String>()
                val sstEntry = zip.getEntry("xl/sharedStrings.xml")
                if (sstEntry != null) {
                    zip.getInputStream(sstEntry).use { stream ->
                        parseSharedStrings(stream, sharedStrings)
                    }
                }

                // 2. Discover sheets
                val sheets = mutableListOf<XlsxSheet>()
                var sheetIndex = 1
                while (true) {
                    val sheetEntry = zip.getEntry("xl/worksheets/sheet$sheetIndex.xml") ?: break
                    zip.getInputStream(sheetEntry).use { stream ->
                        val sheetData = parseSheet(stream, "Sheet $sheetIndex", sharedStrings)
                        sheets.add(sheetData)
                    }
                    sheetIndex++
                }

                if (sheets.isEmpty()) {
                    XlsxWorkbook(
                        title = file.nameWithoutExtension,
                        sheets = listOf(
                            XlsxSheet(
                                name = "Sheet 1",
                                rows = listOf(listOf("Empty spreadsheet")),
                                columnHeaders = listOf("A")
                            )
                        )
                    )
                } else {
                    XlsxWorkbook(title = file.nameWithoutExtension, sheets = sheets)
                }
            }
        } catch (e: Exception) {
            XlsxWorkbook(
                title = file.nameWithoutExtension,
                sheets = listOf(
                    XlsxSheet(
                        name = "Error",
                        rows = listOf(listOf("Failed to parse Excel file: ${e.message}")),
                        columnHeaders = listOf("A")
                    )
                )
            )
        }
    }

    private fun parseSharedStrings(stream: InputStream, list: MutableList<String>) {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        var eventType = parser.eventType
        var currentText = StringBuilder()
        var insideT = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name ?: ""
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (name == "t") {
                        insideT = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideT) {
                        currentText.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (name == "t") {
                        insideT = false
                    } else if (name == "si") {
                        list.add(currentText.toString())
                        currentText.clear()
                    }
                }
            }
            eventType = parser.next()
        }
    }

    private fun parseSheet(stream: InputStream, sheetName: String, sharedStrings: List<String>): XlsxSheet {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        val rows = mutableListOf<List<String>>()
        var currentRow = mutableListOf<String>()
        var currentCellRef = ""
        var currentCellType = ""
        var cellValue = StringBuilder()
        var maxCols = 0

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name ?: ""
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (name) {
                        "row" -> {
                            currentRow = mutableListOf()
                        }
                        "c" -> {
                            currentCellRef = parser.getAttributeValue(null, "r") ?: ""
                            currentCellType = parser.getAttributeValue(null, "t") ?: ""
                            cellValue.clear()
                        }
                        "v" -> {
                            cellValue.append(parser.nextText())
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (name) {
                        "c" -> {
                            val rawVal = cellValue.toString()
                            val displayVal = if (currentCellType == "s") {
                                val idx = rawVal.toIntOrNull()
                                if (idx != null && idx in sharedStrings.indices) {
                                    sharedStrings[idx]
                                } else {
                                    rawVal
                                }
                            } else {
                                rawVal
                            }
                            currentRow.add(displayVal)
                        }
                        "row" -> {
                            if (currentRow.isNotEmpty()) {
                                if (currentRow.size > maxCols) maxCols = currentRow.size
                                rows.add(currentRow.toList())
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        val colHeaders = (0 until maxCols.coerceAtLeast(1)).map { index ->
            val char = ('A'.code + (index % 26)).toChar()
            if (index < 26) "$char" else "${('A'.code + (index / 26 - 1)).toChar()}$char"
        }

        return XlsxSheet(
            name = sheetName,
            rows = rows,
            columnHeaders = colHeaders
        )
    }
}
