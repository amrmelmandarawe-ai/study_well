package com.example.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.SubjectEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class NotePageStyle(val displayName: String, val description: String) {
    BLANK("Blank Paper", "Clean plain white notebook paper"),
    RULED("Ruled Lines", "Standard lined paper for handwriting"),
    GRID("Grid / Graph", "Squared graph paper for Math & Science"),
    DOTTED("Dotted Matrix", "Subtle dot grid for diagrams & sketches")
}

object BlankNotePdfGenerator {

    suspend fun generateBlankNotePdf(
        context: Context,
        subject: SubjectEnum,
        noteTitle: String,
        pageCount: Int,
        pageStyle: NotePageStyle = NotePageStyle.RULED
    ): File? = withContext(Dispatchers.IO) {
        try {
            val pdfDoc = PdfDocument()
            val pageWidth = 595 // Standard A4 width in points
            val pageHeight = 842 // Standard A4 height in points

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val dateFormatted = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
            val cleanTitle = noteTitle.ifBlank { "${subject.title} Notes" }
            val fileTitle = FileUtils.cleanFileNameForTitle(cleanTitle).replace(" ", "_")
            val fileName = "${fileTitle}_${pageCount}Pages_$timeStamp.pdf"

            val effectivePageCount = pageCount.coerceIn(1, 100)

            for (pageNumber in 1..effectivePageCount) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDoc.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                // 1. Draw Page Background
                canvas.drawColor(Color.WHITE)

                val margin = 36f
                val contentWidth = pageWidth - (margin * 2)
                val headerHeight = if (pageNumber == 1) 90f else 46f
                val footerHeight = 30f
                val startContentY = margin + headerHeight
                val endContentY = pageHeight - margin - footerHeight

                // 2. Draw Background Pattern based on Style
                when (pageStyle) {
                    NotePageStyle.BLANK -> {
                        // Keep plain white
                    }
                    NotePageStyle.RULED -> {
                        val linePaint = Paint().apply {
                            color = Color.rgb(226, 232, 240) // Slate 200
                            strokeWidth = 1f
                            isAntiAlias = true
                        }
                        val marginLinePaint = Paint().apply {
                            color = Color.rgb(254, 202, 202) // Light red margin line
                            strokeWidth = 1.2f
                            isAntiAlias = true
                        }

                        // Left vertical margin line
                        val marginX = margin + 30f
                        canvas.drawLine(marginX, startContentY, marginX, endContentY + 10f, marginLinePaint)

                        // Horizontal ruled lines
                        val lineSpacing = 24f
                        var y = startContentY + 20f
                        while (y <= endContentY) {
                            canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
                            y += lineSpacing
                        }
                    }
                    NotePageStyle.GRID -> {
                        val gridPaint = Paint().apply {
                            color = Color.rgb(238, 242, 246) // Light slate grid
                            strokeWidth = 0.8f
                            isAntiAlias = true
                        }
                        val majorGridPaint = Paint().apply {
                            color = Color.rgb(220, 228, 238) // Accent grid lines
                            strokeWidth = 1f
                            isAntiAlias = true
                        }

                        val gridSize = 18f
                        var x = margin
                        var colIndex = 0
                        while (x <= pageWidth - margin) {
                            val paint = if (colIndex % 5 == 0) majorGridPaint else gridPaint
                            canvas.drawLine(x, startContentY, x, endContentY, paint)
                            x += gridSize
                            colIndex++
                        }

                        var y = startContentY
                        var rowIndex = 0
                        while (y <= endContentY) {
                            val paint = if (rowIndex % 5 == 0) majorGridPaint else gridPaint
                            canvas.drawLine(margin, y, pageWidth - margin, y, paint)
                            y += gridSize
                            rowIndex++
                        }
                    }
                    NotePageStyle.DOTTED -> {
                        val dotPaint = Paint().apply {
                            color = Color.rgb(203, 213, 225) // Slate 300 dots
                            style = Paint.Style.FILL
                            isAntiAlias = true
                        }

                        val dotSpacing = 18f
                        var y = startContentY + 10f
                        while (y <= endContentY) {
                            var x = margin + 10f
                            while (x <= pageWidth - margin - 10f) {
                                canvas.drawCircle(x, y, 1f, dotPaint)
                                x += dotSpacing
                            }
                            y += dotSpacing
                        }
                    }
                }

                // 3. Draw Header
                if (pageNumber == 1) {
                    // Prominent Cover / First Page Header Banner
                    val bannerPaint = Paint().apply {
                        color = Color.rgb(248, 250, 252) // Slate 50
                        style = Paint.Style.FILL
                    }
                    val bannerBorderPaint = Paint().apply {
                        color = Color.rgb(226, 232, 240)
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                    }
                    val accentPaint = Paint().apply {
                        color = Color.rgb(15, 23, 42) // Navy accent
                        style = Paint.Style.FILL
                    }

                    canvas.drawRoundRect(margin, margin, margin + contentWidth, margin + 74f, 8f, 8f, bannerPaint)
                    canvas.drawRoundRect(margin, margin, margin + contentWidth, margin + 74f, 8f, 8f, bannerBorderPaint)
                    canvas.drawRoundRect(margin, margin, margin + 6f, margin + 74f, 4f, 4f, accentPaint)

                    val titlePaint = Paint().apply {
                        color = Color.rgb(15, 23, 42)
                        textSize = 15f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        isAntiAlias = true
                    }
                    val subtitlePaint = Paint().apply {
                        color = Color.rgb(71, 85, 105)
                        textSize = 10f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        isAntiAlias = true
                    }
                    val datePaint = Paint().apply {
                        color = Color.rgb(100, 116, 139)
                        textSize = 9f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        isAntiAlias = true
                    }

                    canvas.drawText(cleanTitle, margin + 16f, margin + 28f, titlePaint)
                    canvas.drawText("${subject.title} (${subject.syllabusCode}) • ${pageStyle.displayName} • $effectivePageCount Pages", margin + 16f, margin + 46f, subtitlePaint)
                    canvas.drawText("Date: $dateFormatted  •  StudyWell Student Note", margin + 16f, margin + 62f, datePaint)
                } else {
                    // Running Top Header for subsequent pages
                    val runningHeaderPaint = Paint().apply {
                        color = Color.rgb(100, 116, 139)
                        textSize = 9f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        isAntiAlias = true
                    }
                    val runningLinePaint = Paint().apply {
                        color = Color.rgb(226, 232, 240)
                        strokeWidth = 1f
                    }
                    canvas.drawText("${subject.title} • $cleanTitle", margin, margin + 20f, runningHeaderPaint)
                    canvas.drawText("Page $pageNumber of $effectivePageCount", pageWidth - margin - 70f, margin + 20f, runningHeaderPaint)
                    canvas.drawLine(margin, margin + 26f, pageWidth - margin, margin + 26f, runningLinePaint)
                }

                // 4. Draw Footer & Official Watermark
                val footerPaint = Paint().apply {
                    color = Color.rgb(148, 163, 184)
                    textSize = 8.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    isAntiAlias = true
                }
                canvas.drawText("Page $pageNumber of $effectivePageCount • StudyWell Notes", margin, pageHeight - 16f, footerPaint)
                FileUtils.drawAppWatermark(context, canvas, pageWidth.toFloat(), pageHeight.toFloat())

                pdfDoc.finishPage(page)
            }

            // Save PDF to Downloads & Cache
            val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            if (!storageDir.exists()) storageDir.mkdirs()
            val pdfFile = File(storageDir, fileName)

            FileOutputStream(pdfFile).use { out ->
                pdfDoc.writeTo(out)
                out.flush()
            }
            pdfDoc.close()

            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
