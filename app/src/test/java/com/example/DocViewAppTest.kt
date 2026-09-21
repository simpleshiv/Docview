package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DocumentEntity
import com.example.data.model.DocFormat
import com.example.data.model.DocumentItem
import com.example.data.repository.SampleDocumentGenerator
import com.example.parser.CsvParser
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DocViewAppTest {

    @Test
    fun testDocFormatMapping() {
        assertEquals(DocFormat.PDF, DocFormat.fromExtension("pdf"))
        assertEquals(DocFormat.PDF, DocFormat.fromExtension("PDF"))

        assertEquals(DocFormat.WORD, DocFormat.fromExtension("docx"))
        assertEquals(DocFormat.WORD, DocFormat.fromExtension("doc"))

        assertEquals(DocFormat.EXCEL, DocFormat.fromExtension("xlsx"))
        assertEquals(DocFormat.EXCEL, DocFormat.fromExtension("xls"))

        assertEquals(DocFormat.POWERPOINT, DocFormat.fromExtension("pptx"))
        assertEquals(DocFormat.POWERPOINT, DocFormat.fromExtension("ppt"))

        assertEquals(DocFormat.TEXT, DocFormat.fromExtension("txt"))
        assertEquals(DocFormat.TEXT, DocFormat.fromExtension("csv"))
        assertEquals(DocFormat.TEXT, DocFormat.fromExtension("json"))
        assertEquals(DocFormat.TEXT, DocFormat.fromExtension("xml"))
        assertEquals(DocFormat.TEXT, DocFormat.fromExtension("log"))

        assertEquals(DocFormat.IMAGE, DocFormat.fromExtension("png"))
        assertEquals(DocFormat.IMAGE, DocFormat.fromExtension("jpg"))
        assertEquals(DocFormat.IMAGE, DocFormat.fromExtension("jpeg"))
        assertEquals(DocFormat.IMAGE, DocFormat.fromExtension("webp"))

        assertEquals(DocFormat.OTHER, DocFormat.fromExtension("unknown_ext_xyz"))
    }

    @Test
    fun testCsvParser() {
        val sampleCsv = """
            Name,Age,Role
            Alice,30,"Software Engineer"
            Bob,25,"UX Designer, Senior"
        """.trimIndent()

        val parsed = CsvParser.parse(sampleCsv)
        assertEquals(3, parsed.size)
        assertEquals(listOf("Name", "Age", "Role"), parsed[0])
        assertEquals(listOf("Alice", "30", "Software Engineer"), parsed[1])
        assertEquals(listOf("Bob", "25", "UX Designer, Senior"), parsed[2])
    }

    @Test
    fun testDocumentEntityConversion() {
        val item = DocumentItem(
            id = "test_doc_1",
            name = "Test Report.pdf",
            extension = "pdf",
            format = DocFormat.PDF,
            path = "/storage/test.pdf",
            sizeBytes = 2048L,
            lastModified = 1700000000000L,
            lastOpened = 1700000005000L,
            isFavorite = true,
            pageCount = 12,
            lastViewedPage = 4,
            bookmarks = listOf(1, 4, 8),
            folderName = "Reports"
        )

        val entity = DocumentEntity.fromDocumentItem(item)
        assertEquals("test_doc_1", entity.id)
        assertEquals("Test Report.pdf", entity.name)
        assertEquals("PDF", entity.formatName)
        assertEquals("1,4,8", entity.bookmarksCsv)

        val restored = entity.toDocumentItem()
        assertEquals(item.id, restored.id)
        assertEquals(item.name, restored.name)
        assertEquals(item.format, restored.format)
        assertEquals(item.isFavorite, restored.isFavorite)
        assertEquals(item.bookmarks, restored.bookmarks)
        assertEquals(item.lastViewedPage, restored.lastViewedPage)
    }

    @Test
    fun testSampleDocumentGeneration() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val samples = SampleDocumentGenerator.generateSampleDocuments(context)

        assertTrue("Should generate at least 5 sample documents", samples.size >= 5)

        val pdfSample = samples.find { it.format == DocFormat.PDF }
        assertNotNull("Should contain a sample PDF", pdfSample)
        assertTrue("PDF file should exist on disk", File(pdfSample!!.path).exists())
        assertTrue("PDF file should not be empty", pdfSample.sizeBytes > 0)

        val docxSample = samples.find { it.format == DocFormat.WORD }
        assertNotNull("Should contain a sample DOCX", docxSample)
        assertTrue("DOCX file should exist", File(docxSample!!.path).exists())

        val xlsxSample = samples.find { it.format == DocFormat.EXCEL }
        assertNotNull("Should contain a sample XLSX", xlsxSample)
        assertTrue("XLSX file should exist", File(xlsxSample!!.path).exists())

        val pptxSample = samples.find { it.format == DocFormat.POWERPOINT }
        assertNotNull("Should contain a sample PPTX", pptxSample)
        assertTrue("PPTX file should exist", File(pptxSample!!.path).exists())
    }
}
