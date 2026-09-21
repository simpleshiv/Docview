package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.data.model.DocFormat
import com.example.data.model.DocumentItem
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SampleDocumentGenerator {

    fun generateSampleDocuments(context: Context): List<DocumentItem> {
        val docsDir = File(context.filesDir, "Documents").apply { if (!exists()) mkdirs() }
        val generatedItems = mutableListOf<DocumentItem>()

        // 1. Generate Valid Multi-Page PDF
        val pdfFile = File(docsDir, "Annual_Executive_Report_2026.pdf")
        if (!pdfFile.exists()) {
            createMultiPagePdf(pdfFile)
        }
        if (pdfFile.exists()) {
            generatedItems.add(
                DocumentItem(
                    id = "sample_pdf_1",
                    name = pdfFile.name,
                    extension = "pdf",
                    format = DocFormat.PDF,
                    path = pdfFile.absolutePath,
                    sizeBytes = pdfFile.length(),
                    lastModified = System.currentTimeMillis() - (1000 * 60 * 12),
                    lastOpened = System.currentTimeMillis() - (1000 * 60 * 12),
                    isFavorite = true,
                    pageCount = 4,
                    lastViewedPage = 1,
                    bookmarks = listOf(1, 3),
                    isSample = true,
                    folderName = "Reports"
                )
            )
        }

        // 2. Generate Valid Word DOCX
        val docxFile = File(docsDir, "Project_Alpha_Specification.docx")
        if (!docxFile.exists()) {
            createValidDocx(docxFile)
        }
        if (docxFile.exists()) {
            generatedItems.add(
                DocumentItem(
                    id = "sample_docx_1",
                    name = docxFile.name,
                    extension = "docx",
                    format = DocFormat.WORD,
                    path = docxFile.absolutePath,
                    sizeBytes = docxFile.length(),
                    lastModified = System.currentTimeMillis() - (1000 * 60 * 65),
                    lastOpened = System.currentTimeMillis() - (1000 * 60 * 65),
                    isFavorite = true,
                    pageCount = 3,
                    lastViewedPage = 1,
                    isSample = true,
                    folderName = "Specifications"
                )
            )
        }

        // 3. Generate Valid Excel XLSX
        val xlsxFile = File(docsDir, "Financial_Forecast_Q3_2026.xlsx")
        if (!xlsxFile.exists()) {
            createValidXlsx(xlsxFile)
        }
        if (xlsxFile.exists()) {
            generatedItems.add(
                DocumentItem(
                    id = "sample_xlsx_1",
                    name = xlsxFile.name,
                    extension = "xlsx",
                    format = DocFormat.EXCEL,
                    path = xlsxFile.absolutePath,
                    sizeBytes = xlsxFile.length(),
                    lastModified = System.currentTimeMillis() - (1000 * 60 * 60 * 5),
                    lastOpened = System.currentTimeMillis() - (1000 * 60 * 60 * 5),
                    isFavorite = false,
                    pageCount = 1,
                    lastViewedPage = 1,
                    isSample = true,
                    folderName = "Finance"
                )
            )
        }

        // 4. Generate Valid PowerPoint PPTX
        val pptxFile = File(docsDir, "Board_Pitch_Deck_2026.pptx")
        if (!pptxFile.exists()) {
            createValidPptx(pptxFile)
        }
        if (pptxFile.exists()) {
            generatedItems.add(
                DocumentItem(
                    id = "sample_pptx_1",
                    name = pptxFile.name,
                    extension = "pptx",
                    format = DocFormat.POWERPOINT,
                    path = pptxFile.absolutePath,
                    sizeBytes = pptxFile.length(),
                    lastModified = System.currentTimeMillis() - (1000 * 60 * 60 * 24),
                    lastOpened = System.currentTimeMillis() - (1000 * 60 * 60 * 24),
                    isFavorite = true,
                    pageCount = 4,
                    lastViewedPage = 1,
                    isSample = true,
                    folderName = "Presentations"
                )
            )
        }

        // 5. Generate CSV Data File
        val csvFile = File(docsDir, "Global_Customer_Orders_2026.csv")
        if (!csvFile.exists()) {
            val csvContent = """
                Order ID,Customer Name,Region,Product Category,Quantity,Unit Price,Total,Status,Payment Method,Order Date
                ORD-9021,Acme Corporation,North America,Enterprise Cloud,12,$1,450,$17,400,Completed,Wire Transfer,2026-09-14
                ORD-9022,Nexus Global,Europe,Security Suite,8,$890,$7,120,Processing,Credit Card,2026-09-15
                ORD-9023,Apex Systems,Asia Pacific,Data Analytics,5,$2,100,$10,500,Completed,Wire Transfer,2026-09-16
                ORD-9024,Vanguard Health,North America,Compliance Audit,2,$3,200,$6,400,Pending,Purchase Order,2026-09-17
                ORD-9025,Starlight Media,Latin America,Content Delivery,24,$350,$8,400,Completed,Credit Card,2026-09-18
                ORD-9026,Hyperion Tech,Europe,AI Model Hosting,16,$1,800,$28,800,Completed,Wire Transfer,2026-09-19
                ORD-9027,Zenith Financial,North America,High-Freq Ledger,4,$6,500,$26,000,Completed,Wire Transfer,2026-09-20
                ORD-9028,BlueWave Logistics,Middle East,Tracking Hub,10,$750,$7,500,Processing,Credit Card,2026-09-21
            """.trimIndent()
            csvFile.writeText(csvContent, StandardCharsets.UTF_8)
        }
        if (csvFile.exists()) {
            generatedItems.add(
                DocumentItem(
                    id = "sample_csv_1",
                    name = csvFile.name,
                    extension = "csv",
                    format = DocFormat.TEXT,
                    path = csvFile.absolutePath,
                    sizeBytes = csvFile.length(),
                    lastModified = System.currentTimeMillis() - (1000 * 60 * 60 * 36),
                    lastOpened = 0L,
                    isFavorite = false,
                    pageCount = 1,
                    isSample = true,
                    folderName = "Data"
                )
            )
        }

        // 6. Generate JSON Config File
        val jsonFile = File(docsDir, "Server_Cluster_Config.json")
        if (!jsonFile.exists()) {
            val jsonContent = """
                {
                  "cluster_name": "production-us-east-cluster",
                  "version": "4.12.0",
                  "environment": "production",
                  "region": "us-east-1",
                  "nodes": [
                    {
                      "id": "node-alpha-01",
                      "role": "primary_coordinator",
                      "ip": "10.0.12.45",
                      "vcpus": 32,
                      "memory_gb": 128,
                      "status": "healthy"
                    },
                    {
                      "id": "node-beta-02",
                      "role": "data_replica",
                      "ip": "10.0.12.46",
                      "vcpus": 64,
                      "memory_gb": 256,
                      "status": "healthy"
                    }
                  ],
                  "security_policy": {
                    "tls_enabled": true,
                    "min_tls_version": "1.3",
                    "mtls_internal": true,
                    "audit_logging": true
                  },
                  "replication": {
                    "factor": 3,
                    "write_concern": "majority",
                    "auto_failover": true,
                    "heartbeat_interval_ms": 1500
                  }
                }
            """.trimIndent()
            jsonFile.writeText(jsonContent, StandardCharsets.UTF_8)
        }
        if (jsonFile.exists()) {
            generatedItems.add(
                DocumentItem(
                    id = "sample_json_1",
                    name = jsonFile.name,
                    extension = "json",
                    format = DocFormat.TEXT,
                    path = jsonFile.absolutePath,
                    sizeBytes = jsonFile.length(),
                    lastModified = System.currentTimeMillis() - (1000 * 60 * 60 * 48),
                    lastOpened = 0L,
                    isFavorite = false,
                    pageCount = 1,
                    isSample = true,
                    folderName = "Configurations"
                )
            )
        }

        // 7. Generate Server Audit Log
        val logFile = File(docsDir, "System_Deployment_Audit.log")
        if (!logFile.exists()) {
            val logContent = """
                [2026-09-21 04:00:12.412] [INFO]  [system-init] Starting DocView Pro Cloud Worker Engine v2.6.0
                [2026-09-21 04:00:13.104] [INFO]  [db-pool] Initialized connection pool with 16 active connections
                [2026-09-21 04:00:13.589] [INFO]  [cache] Redis cluster connected at cache-prod.internal:6379 (Ping: 1.2ms)
                [2026-09-21 04:00:14.002] [INFO]  [auth-service] Keycloak JWT verification keys refreshed successfully
                [2026-09-21 04:02:45.890] [INFO]  [ingestion] Batch document processing pipeline started: job_id=9821
                [2026-09-21 04:02:46.331] [DEBUG] [pdf-engine] Extracted 84 vector pages from file payload
                [2026-09-21 04:03:10.120] [WARN]  [rate-limit] Client IP 192.168.4.12 approaching query threshold (92/100)
                [2026-09-21 04:05:00.000] [INFO]  [health-check] System nominal: CPU 18%, Mem 24.8GB/64GB, Storage OK
                [2026-09-21 04:15:22.781] [INFO]  [backup] Incremental snapshots created successfully. Checksum verified.
            """.trimIndent()
            logFile.writeText(logContent, StandardCharsets.UTF_8)
        }
        if (logFile.exists()) {
            generatedItems.add(
                DocumentItem(
                    id = "sample_log_1",
                    name = logFile.name,
                    extension = "log",
                    format = DocFormat.TEXT,
                    path = logFile.absolutePath,
                    sizeBytes = logFile.length(),
                    lastModified = System.currentTimeMillis() - (1000 * 60 * 60 * 72),
                    lastOpened = 0L,
                    isFavorite = false,
                    pageCount = 1,
                    isSample = true,
                    folderName = "Logs"
                )
            )
        }

        // 8. Generate Image Blueprint PNG
        val imgFile = File(docsDir, "Architecture_Design_Blueprint.png")
        if (!imgFile.exists()) {
            createBlueprintImage(imgFile)
        }
        if (imgFile.exists()) {
            generatedItems.add(
                DocumentItem(
                    id = "sample_img_1",
                    name = imgFile.name,
                    extension = "png",
                    format = DocFormat.IMAGE,
                    path = imgFile.absolutePath,
                    sizeBytes = imgFile.length(),
                    lastModified = System.currentTimeMillis() - (1000 * 60 * 60 * 18),
                    lastOpened = System.currentTimeMillis() - (1000 * 60 * 60 * 18),
                    isFavorite = true,
                    pageCount = 1,
                    isSample = true,
                    folderName = "Designs"
                )
            )
        }

        return generatedItems
    }

    private fun createMultiPagePdf(file: File) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(30, 41, 59)
                textSize = 12f
            }
            val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(15, 23, 42)
                textSize = 20f
                isFakeBoldText = true
            }
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(100, 116, 139)
                textSize = 13f
            }

            // --- PAGE 1: Cover & Executive Summary ---
            val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page1 = pdfDocument.startPage(pageInfo1)
            val canvas1 = page1.canvas

            // Header brand bar
            paint.color = Color.rgb(29, 78, 216) // DocBluePrimary
            canvas1.drawRect(0f, 0f, pageWidth.toFloat(), 18f, paint)

            // Title
            headingPaint.textSize = 24f
            canvas1.drawText("DocView Pro Executive Briefing", 48f, 72f, headingPaint)
            canvas1.drawText("Strategic Roadmap & Operational Review • 2026", 48f, 96f, subPaint)

            // Accent divider
            paint.color = Color.rgb(226, 232, 240)
            canvas1.drawRect(48f, 114f, (pageWidth - 48).toFloat(), 116f, paint)

            // Card box for executive highlights
            paint.color = Color.rgb(241, 245, 249)
            val rect1 = RectF(48f, 134f, (pageWidth - 48).toFloat(), 240f)
            canvas1.drawRoundRect(rect1, 12f, 12f, paint)

            paint.color = Color.rgb(29, 78, 216)
            canvas1.drawRoundRect(RectF(48f, 134f, 54f, 240f), 4f, 4f, paint)

            headingPaint.textSize = 14f
            canvas1.drawText("Executive Summary", 68f, 160f, headingPaint)
            textPaint.textSize = 11f
            canvas1.drawText("During the third fiscal quarter of 2026, our enterprise document ecosystem", 68f, 184f, textPaint)
            canvas1.drawText("achieved record throughput, handling over 450,000 documents with 99.98% reliability.", 68f, 202f, textPaint)
            canvas1.drawText("Key advancements were made in offline document rendering, high-speed text search,", 68f, 220f, textPaint)

            // Section 1 Body
            headingPaint.textSize = 16f
            canvas1.drawText("1. Core Architectural Pillars", 48f, 276f, headingPaint)
            textPaint.textSize = 11.5f
            canvas1.drawText("• Universal Format Compatibility: Native hardware-accelerated PDF rendering, full support", 52f, 304f, textPaint)
            canvas1.drawText("  for Microsoft Office documents (DOCX, XLSX, PPTX), rich tabular data, and raw code.", 52f, 322f, textPaint)
            canvas1.drawText("• Absolute Privacy & Zero Cloud Leakage: Every document remains strictly in local sandboxed", 52f, 348f, textPaint)
            canvas1.drawText("  storage without non-consensual telemetry or external data collection.", 52f, 366f, textPaint)
            canvas1.drawText("• Precision Ergonomics: Sub-millisecond pagination, pinch-to-zoom gestures, night mode,", 52f, 392f, textPaint)
            canvas1.drawText("  and adaptive screen optimization across both handheld devices and foldable tablets.", 52f, 410f, textPaint)

            // Footer
            paint.color = Color.rgb(148, 163, 184)
            subPaint.textSize = 10f
            canvas1.drawText("Page 1 of 4 • Confidential Executive Report", 48f, (pageHeight - 36).toFloat(), subPaint)
            canvas1.drawText("DocView Pro 2026", (pageWidth - 140).toFloat(), (pageHeight - 36).toFloat(), subPaint)
            pdfDocument.finishPage(page1)

            // --- PAGE 2: Analytics & Metrics ---
            val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
            val page2 = pdfDocument.startPage(pageInfo2)
            val canvas2 = page2.canvas

            paint.color = Color.rgb(29, 78, 216)
            canvas2.drawRect(0f, 0f, pageWidth.toFloat(), 18f, paint)

            headingPaint.textSize = 20f
            canvas2.drawText("2. Quantitative Performance Benchmarks", 48f, 64f, headingPaint)

            // KPI metric cards
            val cardWidth = 150f
            val kpis = listOf(
                Triple("99.98%", "Rendering Reliability", Color.rgb(5, 150, 105)),
                Triple("14ms", "Page Switch Latency", Color.rgb(37, 99, 235)),
                Triple("0 MB", "Network Data Leaked", Color.rgb(124, 58, 237))
            )
            for (i in kpis.indices) {
                val kpi = kpis[i]
                val left = 48f + i * (cardWidth + 24f)
                val rect = RectF(left, 90f, left + cardWidth, 175f)
                paint.color = Color.rgb(248, 250, 252)
                canvas2.drawRoundRect(rect, 10f, 10f, paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.2f
                paint.color = Color.rgb(226, 232, 240)
                canvas2.drawRoundRect(rect, 10f, 10f, paint)
                paint.style = Paint.Style.FILL

                val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = kpi.third
                    textSize = 24f
                    isFakeBoldText = true
                }
                canvas2.drawText(kpi.first, left + 16f, 130f, valPaint)
                subPaint.textSize = 10.5f
                canvas2.drawText(kpi.second, left + 16f, 154f, subPaint)
            }

            // Table Header
            paint.color = Color.rgb(241, 245, 249)
            canvas2.drawRect(48f, 210f, (pageWidth - 48).toFloat(), 238f, paint)
            headingPaint.textSize = 11f
            canvas2.drawText("DOCUMENT FORMAT", 58f, 228f, headingPaint)
            canvas2.drawText("FILE TYPES", 210f, 228f, headingPaint)
            canvas2.drawText("PARSER ENGINE", 340f, 228f, headingPaint)
            canvas2.drawText("STATUS", 460f, 228f, headingPaint)

            val tableRows = listOf(
                listOf("Portable Document", "PDF, PDF/A", "Hardware PdfRenderer", "Active"),
                listOf("Microsoft Word", "DOC, DOCX", "Structured XML Parser", "Active"),
                listOf("Microsoft Excel", "XLS, XLSX", "Workbook Cell Grid", "Active"),
                listOf("Microsoft PowerPoint", "PPT, PPTX", "Slide Stack Engine", "Active"),
                listOf("Structured Data", "CSV, JSON, XML", "Syntax Highlighting", "Active"),
                listOf("High-Res Images", "PNG, JPG, WEBP", "Pinch Matrix Engine", "Active")
            )

            var currentY = 264f
            paint.color = Color.rgb(241, 245, 249)
            for (row in tableRows) {
                paint.color = Color.rgb(241, 245, 249)
                canvas2.drawLine(48f, currentY + 8f, (pageWidth - 48).toFloat(), currentY + 8f, paint)
                textPaint.textSize = 11f
                canvas2.drawText(row[0], 58f, currentY, textPaint)
                canvas2.drawText(row[1], 210f, currentY, textPaint)
                canvas2.drawText(row[2], 340f, currentY, textPaint)

                val statusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(5, 150, 105)
                    textSize = 11f
                    isFakeBoldText = true
                }
                canvas2.drawText(row[3], 460f, currentY, statusPaint)
                currentY += 32f
            }

            canvas2.drawText("Page 2 of 4 • Confidential Executive Report", 48f, (pageHeight - 36).toFloat(), subPaint)
            canvas2.drawText("DocView Pro 2026", (pageWidth - 140).toFloat(), (pageHeight - 36).toFloat(), subPaint)
            pdfDocument.finishPage(page2)

            // --- PAGE 3: Technology Infrastructure ---
            val pageInfo3 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create()
            val page3 = pdfDocument.startPage(pageInfo3)
            val canvas3 = page3.canvas

            paint.color = Color.rgb(29, 78, 216)
            canvas3.drawRect(0f, 0f, pageWidth.toFloat(), 18f, paint)

            headingPaint.textSize = 20f
            canvas3.drawText("3. Technical Architecture & Security Model", 48f, 64f, headingPaint)

            textPaint.textSize = 11.5f
            canvas3.drawText("The DocView Pro platform enforces strict architectural boundaries to guarantee", 48f, 96f, textPaint)
            canvas3.drawText("zero-copy file streaming, minimal memory overhead, and total device isolation.", 48f, 114f, textPaint)

            // Box with bullet points
            val boxRect = RectF(48f, 140f, (pageWidth - 48).toFloat(), 340f)
            paint.color = Color.rgb(248, 250, 252)
            canvas3.drawRoundRect(boxRect, 10f, 10f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(226, 232, 240)
            canvas3.drawRoundRect(boxRect, 10f, 10f, paint)
            paint.style = Paint.Style.FILL

            headingPaint.textSize = 13f
            canvas3.drawText("Security Guarantees:", 64f, 168f, headingPaint)
            canvas3.drawText("1. Scoped Storage: Android Storage Access Framework (SAF) integration", 64f, 196f, textPaint)
            canvas3.drawText("2. No Network Ingress: Document contents are never transmitted across sockets", 64f, 224f, textPaint)
            canvas3.drawText("3. Ephemeral Memory: Render buffers are freed immediately on view exit", 64f, 252f, textPaint)
            canvas3.drawText("4. Local Room Persistence: Metadata, favorites, and reading marks stay on-device", 64f, 280f, textPaint)
            canvas3.drawText("5. Graceful Fallback: Corrupted or proprietary binaries fail safely without crashing", 64f, 308f, textPaint)

            canvas3.drawText("Page 3 of 4 • Confidential Executive Report", 48f, (pageHeight - 36).toFloat(), subPaint)
            canvas3.drawText("DocView Pro 2026", (pageWidth - 140).toFloat(), (pageHeight - 36).toFloat(), subPaint)
            pdfDocument.finishPage(page3)

            // --- PAGE 4: Roadmap & Sign-Off ---
            val pageInfo4 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 4).create()
            val page4 = pdfDocument.startPage(pageInfo4)
            val canvas4 = page4.canvas

            paint.color = Color.rgb(29, 78, 216)
            canvas4.drawRect(0f, 0f, pageWidth.toFloat(), 18f, paint)

            headingPaint.textSize = 20f
            canvas4.drawText("4. Strategic Roadmap & Approvals", 48f, 64f, headingPaint)

            textPaint.textSize = 11.5f
            canvas4.drawText("Q1: Advanced PDF Vector Annotation & Freehand Ink Tools", 48f, 100f, textPaint)
            canvas4.drawText("Q2: Offline Optical Character Recognition (OCR) for Scanned Documents", 48f, 126f, textPaint)
            canvas4.drawText("Q3: Multi-Document Split View & Tabbed Workspace Management", 48f, 152f, textPaint)
            canvas4.drawText("Q4: Enterprise End-to-End Cryptographic Document Signing", 48f, 178f, textPaint)

            // Signatures area
            paint.color = Color.rgb(226, 232, 240)
            canvas4.drawLine(48f, 260f, 240f, 260f, paint)
            canvas4.drawLine(300f, 260f, (pageWidth - 48).toFloat(), 260f, paint)

            subPaint.textSize = 11f
            canvas4.drawText("Chief Technology Officer", 48f, 280f, subPaint)
            canvas4.drawText("Principal Document Architect", 300f, 280f, subPaint)

            canvas4.drawText("Page 4 of 4 • Confidential Executive Report", 48f, (pageHeight - 36).toFloat(), subPaint)
            canvas4.drawText("DocView Pro 2026", (pageWidth - 140).toFloat(), (pageHeight - 36).toFloat(), subPaint)
            pdfDocument.finishPage(page4)

            try {
                FileOutputStream(file).use { out ->
                    pdfDocument.writeTo(out)
                }
            } finally {
                pdfDocument.close()
            }
        } catch (e: Throwable) {
            val fallbackPdf = "%PDF-1.4\n1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >> endobj\nxref\n0 4\n0000000000 65535 f \n0000000009 00000 n \n0000000058 00000 n \n0000000115 00000 n \ntrailer << /Size 4 /Root 1 0 R >>\nstartxref\n200\n%%EOF"
            file.writeText(fallbackPdf, StandardCharsets.UTF_8)
        }
    }

    private fun createValidDocx(file: File) {
        val documentXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
              <w:body>
                <w:p>
                  <w:pPr><w:pStyle w:val="Heading1"/></w:pPr>
                  <w:r><w:rPr><w:b/></w:rPr><w:t>Project Alpha Technical Specification</w:t></w:r>
                </w:p>
                <w:p>
                  <w:r><w:t>Author: Enterprise Systems Architecture Group</w:t></w:r>
                </w:p>
                <w:p>
                  <w:r><w:t>Status: Approved for Engineering Implementation</w:t></w:r>
                </w:p>
                <w:p>
                  <w:pPr><w:pStyle w:val="Heading2"/></w:pPr>
                  <w:r><w:rPr><w:b/></w:rPr><w:t>1. Executive Overview</w:t></w:r>
                </w:p>
                <w:p>
                  <w:r><w:t>Project Alpha outlines our next-generation document runtime container. Designed specifically for low-latency Android rendering, it combines asynchronous background processing with responsive Compose view trees.</w:t></w:r>
                </w:p>
                <w:p>
                  <w:pPr><w:pStyle w:val="Heading2"/></w:pPr>
                  <w:r><w:rPr><w:b/></w:rPr><w:t>2. Core System Requirements</w:t></w:r>
                </w:p>
                <w:p>
                  <w:r><w:t>• Sub-second initial document open time for files under 20MB.</w:t></w:r>
                </w:p>
                <w:p>
                  <w:r><w:t>• Complete support for nested tables, headers, and formatted runs.</w:t></w:r>
                </w:p>
                <w:p>
                  <w:r><w:t>• Robust fallback rendering when custom typography fonts are absent.</w:t></w:r>
                </w:p>
                <w:p>
                  <w:pPr><w:pStyle w:val="Heading2"/></w:pPr>
                  <w:r><w:rPr><w:b/></w:rPr><w:t>3. Component Allocation Matrix</w:t></w:r>
                </w:p>
                <w:tbl>
                  <w:tr>
                    <w:tc><w:p><w:r><w:rPr><w:b/></w:rPr><w:t>Module</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:rPr><w:b/></w:rPr><w:t>Responsibility</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:rPr><w:b/></w:rPr><w:t>Target SLA</w:t></w:r></w:p></w:tc>
                  </w:tr>
                  <w:tr>
                    <w:tc><w:p><w:r><w:t>DocxParser</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:t>Streaming XML Token Extraction</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:t>&lt; 15 ms</w:t></w:r></w:p></w:tc>
                  </w:tr>
                  <w:tr>
                    <w:tc><w:p><w:r><w:t>PdfRenderer</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:t>Multi-threaded Rasterizer</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:t>&lt; 25 ms / page</w:t></w:r></w:p></w:tc>
                  </w:tr>
                  <w:tr>
                    <w:tc><w:p><w:r><w:t>RoomStore</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:t>Bookmarks &amp; Reading Position</w:t></w:r></w:p></w:tc>
                    <w:tc><w:p><w:r><w:t>&lt; 5 ms</w:t></w:r></w:p></w:tc>
                  </w:tr>
                </w:tbl>
              </w:body>
            </w:document>
        """.trimIndent()

        val contentTypes = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
              <Default Extension="xml" ContentType="application/xml"/>
              <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
              <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
            </Types>
        """.trimIndent()

        val rels = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
              <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
            </Relationships>
        """.trimIndent()

        ZipOutputStream(FileOutputStream(file)).use { zos ->
            zos.putNextEntry(ZipEntry("[Content_Types].xml"))
            zos.write(contentTypes.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("_rels/.rels"))
            zos.write(rels.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("word/document.xml"))
            zos.write(documentXml.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()
        }
    }

    private fun createValidXlsx(file: File) {
        val sharedStringsXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="15" uniqueCount="15">
              <si><t>Department</t></si>
              <si><t>Q3 Budget</t></si>
              <si><t>Actual Spend</t></si>
              <si><t>Variance</t></si>
              <si><t>% Consumed</t></si>
              <si><t>Status</t></si>
              <si><t>Engineering</t></si>
              <si><t>Product Design</t></si>
              <si><t>Marketing &amp; Growth</t></si>
              <si><t>Operations &amp; Security</t></si>
              <si><t>Legal &amp; Compliance</t></si>
              <si><t>On Target</t></si>
              <si><t>Under Budget</t></si>
              <si><t>Review Needed</t></si>
              <si><t>Total Portfolio</t></si>
            </sst>
        """.trimIndent()

        val sheet1Xml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
              <sheetData>
                <row r="1">
                  <c r="A1" t="s"><v>0</v></c>
                  <c r="B1" t="s"><v>1</v></c>
                  <c r="C1" t="s"><v>2</v></c>
                  <c r="D1" t="s"><v>3</v></c>
                  <c r="E1" t="s"><v>4</v></c>
                  <c r="F1" t="s"><v>5</v></c>
                </row>
                <row r="2">
                  <c r="A2" t="s"><v>6</v></c>
                  <c r="B2"><v>1250000</v></c>
                  <c r="C2"><v>1180000</v></c>
                  <c r="D2"><v>70000</v></c>
                  <c r="E2"><v>94.4%</v></c>
                  <c r="F2" t="s"><v>11</v></c>
                </row>
                <row r="3">
                  <c r="A3" t="s"><v>7</v></c>
                  <c r="B3"><v>450000</v></c>
                  <c r="C3"><v>410000</v></c>
                  <c r="D3"><v>40000</v></c>
                  <c r="E3"><v>91.1%</v></c>
                  <c r="F3" t="s"><v>11</v></c>
                </row>
                <row r="4">
                  <c r="A4" t="s"><v>8</v></c>
                  <c r="B4"><v>800000</v></c>
                  <c r="C4"><v>830000</v></c>
                  <c r="D4"><v>-30000</v></c>
                  <c r="E4"><v>103.7%</v></c>
                  <c r="F4" t="s"><v>13</v></c>
                </row>
                <row r="5">
                  <c r="A5" t="s"><v>9</v></c>
                  <c r="B5"><v>350000</v></c>
                  <c r="C5"><v>320000</v></c>
                  <c r="D5"><v>30000</v></c>
                  <c r="E5"><v>91.4%</v></c>
                  <c r="F5" t="s"><v>12</v></c>
                </row>
                <row r="6">
                  <c r="A6" t="s"><v>10</v></c>
                  <c r="B6"><v>200000</v></c>
                  <c r="C6"><v>185000</v></c>
                  <c r="D6"><v>15000</v></c>
                  <c r="E6"><v>92.5%</v></c>
                  <c r="F6" t="s"><v>11</v></c>
                </row>
                <row r="7">
                  <c r="A7" t="s"><v>14</v></c>
                  <c r="B7"><v>3050000</v></c>
                  <c r="C7"><v>2925000</v></c>
                  <c r="D7"><v>125000</v></c>
                  <c r="E7"><v>95.9%</v></c>
                  <c r="F7" t="s"><v>11</v></c>
                </row>
              </sheetData>
            </worksheet>
        """.trimIndent()

        ZipOutputStream(FileOutputStream(file)).use { zos ->
            zos.putNextEntry(ZipEntry("xl/sharedStrings.xml"))
            zos.write(sharedStringsXml.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            zos.write(sheet1Xml.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()
        }
    }

    private fun createValidPptx(file: File) {
        val slide1Xml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <p:sld xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"
                   xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
              <p:cSld>
                <p:spTree>
                  <p:sp>
                    <p:txBody>
                      <a:p><a:r><a:t>DocView Pro: Board of Directors Briefing</a:t></a:r></a:p>
                      <a:p><a:r><a:t>Q3 Strategic Review &amp; 2026 Vision</a:t></a:r></a:p>
                      <a:p><a:r><a:t>Presenter: Chief Product &amp; Technology Officer</a:t></a:r></a:p>
                    </p:txBody>
                  </p:sp>
                </p:spTree>
              </p:cSld>
            </p:sld>
        """.trimIndent()

        val slide2Xml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <p:sld xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"
                   xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
              <p:cSld>
                <p:spTree>
                  <p:sp>
                    <p:txBody>
                      <a:p><a:r><a:t>Market Opportunity &amp; User Traction</a:t></a:r></a:p>
                      <a:p><a:r><a:t>• 1.2 Billion mobile knowledge workers demand offline document utility.</a:t></a:r></a:p>
                      <a:p><a:r><a:t>• Legacy office apps are bloated with cloud synchronizers and paywalls.</a:t></a:r></a:p>
                      <a:p><a:r><a:t>• DocView Pro delivers instantaneous launch, native rendering, and zero tracking.</a:t></a:r></a:p>
                    </p:txBody>
                  </p:sp>
                </p:spTree>
              </p:cSld>
            </p:sld>
        """.trimIndent()

        val slide3Xml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <p:sld xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"
                   xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
              <p:cSld>
                <p:spTree>
                  <p:sp>
                    <p:txBody>
                      <a:p><a:r><a:t>Product Architecture &amp; Core Strengths</a:t></a:r></a:p>
                      <a:p><a:r><a:t>• Multi-engine pipeline: Hardware PdfRenderer + XML Zip parsers.</a:t></a:r></a:p>
                      <a:p><a:r><a:t>• Modern Jetpack Compose UI with adaptive foldable &amp; tablet panes.</a:t></a:r></a:p>
                      <a:p><a:r><a:t>• Fully responsive zoom, continuous scrolling, and bookmarks.</a:t></a:r></a:p>
                    </p:txBody>
                  </p:sp>
                </p:spTree>
              </p:cSld>
            </p:sld>
        """.trimIndent()

        val slide4Xml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <p:sld xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"
                   xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
              <p:cSld>
                <p:spTree>
                  <p:sp>
                    <p:txBody>
                      <a:p><a:r><a:t>Conclusions &amp; Investment Allocation</a:t></a:r></a:p>
                      <a:p><a:r><a:t>• Accelerate optical character recognition (OCR) local pipeline.</a:t></a:r></a:p>
                      <a:p><a:r><a:t>• Expand stylus drawing &amp; annotation canvas capabilities.</a:t></a:r></a:p>
                      <a:p><a:r><a:t>• Deliver enterprise encryption and biometric file vault.</a:t></a:r></a:p>
                    </p:txBody>
                  </p:sp>
                </p:spTree>
              </p:cSld>
            </p:sld>
        """.trimIndent()

        ZipOutputStream(FileOutputStream(file)).use { zos ->
            zos.putNextEntry(ZipEntry("ppt/slides/slide1.xml"))
            zos.write(slide1Xml.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("ppt/slides/slide2.xml"))
            zos.write(slide2Xml.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("ppt/slides/slide3.xml"))
            zos.write(slide3Xml.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("ppt/slides/slide4.xml"))
            zos.write(slide4Xml.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()
        }
    }

    private fun createBlueprintImage(file: File) {
        try {
            val width = 1200
            val height = 800
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Dark tech canvas
            canvas.drawColor(Color.rgb(15, 23, 42))

            val gridPaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                strokeWidth = 1.5f
            }
            for (x in 0..width step 40) {
                canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), gridPaint)
            }
            for (y in 0..height step 40) {
                canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), gridPaint)
            }

            val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(30, 58, 138)
            }
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(56, 189, 248)
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 28f
                isFakeBoldText = true
            }
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(148, 163, 184)
                textSize = 18f
            }

            // Draw Nodes
            val nodes = listOf(
                Triple(RectF(100f, 300f, 360f, 500f), "Doc Ingestion", "SAF & Storage Engine"),
                Triple(RectF(480f, 300f, 740f, 500f), "Parser Router", "PDF • Word • Excel • Code"),
                Triple(RectF(860f, 300f, 1120f, 500f), "Compose Viewport", "Hardware Raster & Matrix")
            )

            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(56, 189, 248)
                strokeWidth = 4f
            }
            canvas.drawLine(360f, 400f, 480f, 400f, linePaint)
            canvas.drawLine(740f, 400f, 860f, 400f, linePaint)

            for (node in nodes) {
                canvas.drawRoundRect(node.first, 16f, 16f, nodePaint)
                canvas.drawRoundRect(node.first, 16f, 16f, borderPaint)
                canvas.drawText(node.second, node.first.left + 24f, node.first.top + 70f, textPaint)
                canvas.drawText(node.third, node.first.left + 24f, node.first.top + 120f, subPaint)
            }

            // Blueprint Header
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(56, 189, 248)
                textSize = 38f
                isFakeBoldText = true
            }
            canvas.drawText("SYSTEM ARCHITECTURE BLUEPRINT: DOCVIEW PRO RUNTIME", 100f, 120f, titlePaint)
            canvas.drawText("Zero-Latency Pipeline • Hardware Rasterization • Local Sandboxing", 100f, 165f, subPaint)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            bitmap.recycle()
        } catch (e: Throwable) {
            val minimalPng = byteArrayOf(
                0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4.toByte(), 0x89.toByte(),
                0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54,
                0x78, 0x9C.toByte(), 0x63, 0x00, 0x01, 0x00, 0x00, 0x05, 0x00, 0x01,
                0x0D, 0x0A, 0x2D, 0xB4.toByte(),
                0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44,
                0xAE.toByte(), 0x42, 0x60, 0x82.toByte()
            )
            file.writeBytes(minimalPng)
        }
    }
}
