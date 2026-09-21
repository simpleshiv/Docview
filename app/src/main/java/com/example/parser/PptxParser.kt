package com.example.parser

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

data class PptxSlide(
    val slideNumber: Int,
    val title: String,
    val bullets: List<String>,
    val notes: String = ""
)

data class PptxPresentation(
    val title: String,
    val slides: List<PptxSlide>
)

object PptxParser {

    fun parse(file: File): PptxPresentation {
        return try {
            ZipFile(file).use { zip ->
                val slides = mutableListOf<PptxSlide>()
                var slideIndex = 1

                while (true) {
                    val entry = zip.getEntry("ppt/slides/slide$slideIndex.xml") ?: break
                    zip.getInputStream(entry).use { stream ->
                        val slide = parseSlide(stream, slideIndex)
                        slides.add(slide)
                    }
                    slideIndex++
                }

                if (slides.isEmpty()) {
                    PptxPresentation(
                        title = file.nameWithoutExtension,
                        slides = listOf(
                            PptxSlide(
                                slideNumber = 1,
                                title = file.nameWithoutExtension,
                                bullets = listOf("PowerPoint Presentation Preview", "No slide content found")
                            )
                        )
                    )
                } else {
                    PptxPresentation(title = file.nameWithoutExtension, slides = slides)
                }
            }
        } catch (e: Exception) {
            PptxPresentation(
                title = file.nameWithoutExtension,
                slides = listOf(
                    PptxSlide(
                        slideNumber = 1,
                        title = "Error",
                        bullets = listOf("Unable to parse PowerPoint file: ${e.message}")
                    )
                )
            )
        }
    }

    private fun parseSlide(stream: InputStream, slideNumber: Int): PptxSlide {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        var title = ""
        val bullets = mutableListOf<String>()
        var currentParaText = StringBuilder()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name ?: ""
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (name == "p") {
                        currentParaText.clear()
                    } else if (name == "t") {
                        val text = parser.nextText()
                        currentParaText.append(text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (name == "p") {
                        val trimmed = currentParaText.toString().trim()
                        if (trimmed.isNotEmpty()) {
                            if (title.isEmpty()) {
                                title = trimmed
                            } else {
                                bullets.add(trimmed)
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return PptxSlide(
            slideNumber = slideNumber,
            title = if (title.isEmpty()) "Slide $slideNumber" else title,
            bullets = bullets
        )
    }
}
