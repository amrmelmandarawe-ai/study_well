package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.QuizQuestion
import com.example.data.model.SubjectEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExamPdfGenerator {

    suspend fun generateExamPdf(
        context: Context,
        subject: SubjectEnum,
        topic: String,
        questions: List<QuizQuestion>,
        examBoard: com.example.data.model.ExamBoard = com.example.data.model.ExamBoard.CAMBRIDGE,
        pastYear: String = "2023"
    ): File? = withContext(Dispatchers.IO) {
        try {
            val pdfDoc = PdfDocument()
            val pageWidth = 595 // A4 standard width in points (72 dpi)
            val pageHeight = 842 // A4 standard height in points

            val titlePaint = Paint().apply {
                color = Color.rgb(15, 23, 42) // SleekNavy900
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(71, 85, 105)
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val headerBoxPaint = Paint().apply {
                color = Color.rgb(241, 245, 249)
                style = Paint.Style.FILL
            }

            val borderPaint = Paint().apply {
                color = Color.rgb(203, 213, 225)
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }

            val questionPaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val optionPaint = Paint().apply {
                color = Color.rgb(51, 65, 85)
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val tipPaint = Paint().apply {
                color = Color.rgb(30, 58, 138)
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                isAntiAlias = true
            }

            val markPaint = Paint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            var currentPageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
            var page = pdfDoc.startPage(pageInfo)
            var canvas: Canvas = page.canvas

            val margin = 40f
            val contentWidth = pageWidth - (margin * 2)
            var currentY = margin

            // Draw Header on First Page
            fun drawHeader() {
                // Top Banner
                canvas.drawRect(margin, currentY, margin + contentWidth, currentY + 74f, headerBoxPaint)
                canvas.drawRect(margin, currentY, margin + contentWidth, currentY + 74f, borderPaint)

                val headerBoardTitle = when (examBoard) {
                    com.example.data.model.ExamBoard.CAMBRIDGE -> "CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION"
                    com.example.data.model.ExamBoard.EDEXCEL -> "PEARSON EDEXCEL INTERNATIONAL GCSE (9-1)"
                    com.example.data.model.ExamBoard.OXFORD_AQA -> "OXFORD AQA INTERNATIONAL EXAMINATIONS"
                }

                canvas.drawText(headerBoardTitle, margin + 14f, currentY + 20f, titlePaint)
                canvas.drawText("${examBoard.subtitle} — Series $pastYear", margin + 14f, currentY + 36f, subtitlePaint)
                canvas.drawText("Subject: ${subject.title}  |  Paper Code: ${subject.syllabusCode}  |  Topic: $topic", margin + 14f, currentY + 52f, subtitlePaint)
                canvas.drawText("Official Past Paper Real Assessment Collection (Year 10 IGCSE / O-Level)", margin + 14f, currentY + 66f, tipPaint)

                currentY += 88f

                // Candidate Details Box
                canvas.drawRect(margin, currentY, margin + contentWidth, currentY + 40f, headerBoxPaint)
                canvas.drawRect(margin, currentY, margin + contentWidth, currentY + 40f, borderPaint)

                val detailsText = "Candidate Name: __________________________   Center Number: _______   Candidate Number: _____"
                canvas.drawText(detailsText, margin + 12f, currentY + 18f, subtitlePaint)
                val infoText = "TIME ALLOWED: 45 minutes        TOTAL MARKS: ${questions.size} Marks        READ INSTRUCTIONS CAREFULLY"
                canvas.drawText(infoText, margin + 12f, currentY + 32f, tipPaint)

                currentY += 55f
            }

            drawHeader()

            // Helper to handle multi-line text wrapping
            fun drawWrappedText(text: String, startX: Float, maxWidth: Float, paint: Paint): Float {
                val words = text.split(" ")
                var currentLine = ""
                var lineY = currentY

                for (word in words) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    val measure = paint.measureText(testLine)
                    if (measure > maxWidth) {
                        canvas.drawText(currentLine, startX, lineY, paint)
                        currentLine = word
                        lineY += paint.textSize + 4f
                    } else {
                        currentLine = testLine
                    }
                }
                if (currentLine.isNotEmpty()) {
                    canvas.drawText(currentLine, startX, lineY, paint)
                    lineY += paint.textSize + 4f
                }
                return lineY
            }

            // Draw Questions
            questions.forEachIndexed { index, q ->
                // Check if we need a new page
                if (currentY > pageHeight - 120f) {
                    // Page footer
                    canvas.drawText("Page $currentPageNumber   •   [Turn over", margin + contentWidth - 100f, pageHeight - 20f, subtitlePaint)
                    FileUtils.drawAppWatermark(context, canvas, pageWidth.toFloat(), pageHeight.toFloat())
                    pdfDoc.finishPage(page)

                    currentPageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                    page = pdfDoc.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = margin + 20f

                    // Header on sub-pages
                    canvas.drawText("${subject.title} (${subject.syllabusCode}) Exam Paper — Page $currentPageNumber", margin, currentY, subtitlePaint)
                    canvas.drawLine(margin, currentY + 6f, margin + contentWidth, currentY + 6f, borderPaint)
                    currentY += 24f
                }

                val qNumberStr = "Question ${index + 1}:"
                canvas.drawText(qNumberStr, margin, currentY + 12f, questionPaint)
                canvas.drawText("[${q.marks} mark]", margin + contentWidth - 45f, currentY + 12f, markPaint)
                currentY += 18f

                // Draw question body
                currentY = drawWrappedText(q.questionText, margin + 10f, contentWidth - 20f, questionPaint)
                currentY += 6f

                // Draw Options
                val optionLabels = listOf("A", "B", "C", "D")
                q.options.forEachIndexed { optIndex, optText ->
                    val optLabel = if (optIndex < optionLabels.size) optionLabels[optIndex] else "${optIndex + 1}"
                    val fullOption = "($optLabel)  $optText"
                    currentY = drawWrappedText(fullOption, margin + 20f, contentWidth - 30f, optionPaint)
                }

                // Lined Answer Space
                currentY += 6f
                canvas.drawLine(margin + 20f, currentY, margin + contentWidth - 20f, currentY, borderPaint)
                currentY += 16f
            }

            // Final Page Footer & Examiner Mark Scheme section
            if (currentY > pageHeight - 160f) {
                canvas.drawText("Page $currentPageNumber", margin + contentWidth - 60f, pageHeight - 20f, subtitlePaint)
                FileUtils.drawAppWatermark(context, canvas, pageWidth.toFloat(), pageHeight.toFloat())
                pdfDoc.finishPage(page)

                currentPageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                page = pdfDoc.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin + 20f
            }

            // Mark Scheme Section on Last Page
            currentY += 20f
            canvas.drawLine(margin, currentY, margin + contentWidth, currentY, borderPaint)
            currentY += 16f
            canvas.drawText("CAMBRIDGE IGCSE EXAMINER MARK SCHEME & ANSWERS KEY", margin, currentY, titlePaint)
            currentY += 16f

            questions.forEachIndexed { i, q ->
                val correctLetter = when (q.correctOptionIndex) {
                    0 -> "A"
                    1 -> "B"
                    2 -> "C"
                    3 -> "D"
                    else -> "${q.correctOptionIndex + 1}"
                }
                val ansLine = "Q${i + 1}: Correct Answer: ($correctLetter) — ${q.explanation}"
                currentY = drawWrappedText(ansLine, margin + 6f, contentWidth - 12f, tipPaint)
                currentY += 4f
            }

            canvas.drawText("Page $currentPageNumber   •   END OF EXAM PAPER", margin + contentWidth - 160f, pageHeight - 20f, subtitlePaint)
            FileUtils.drawAppWatermark(context, canvas, pageWidth.toFloat(), pageHeight.toFloat())
            pdfDoc.finishPage(page)

            // Save PDF to downloads or app files
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val safeSubject = subject.title.replace(" ", "_")
            val fileName = "${examBoard.shortName}_${safeSubject}_${pastYear}_Official_Exam_Paper_$timeStamp.pdf"

            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            if (!storageDir.exists()) storageDir.mkdirs()
            val pdfFile = File(storageDir, fileName)

            val outputStream = FileOutputStream(pdfFile)
            pdfDoc.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDoc.close()

            // Also copy to system public Downloads on Android Q+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                try {
                    val contentValues = android.content.ContentValues().apply {
                        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { mediaOut ->
                            pdfFile.inputStream().use { input ->
                                input.copyTo(mediaOut)
                            }
                        }
                    }
                } catch (_: Exception) {}
            }

            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateRealExamPdfSync(
        context: Context,
        paper: com.example.data.repository.RealPastPaper,
        includeMarkScheme: Boolean = true
    ): File? {
        return try {
            val cleanCode = paper.paperCode.replace("/", "_")
            val cleanSession = paper.session.replace("/", "_")
            val suffix = if (includeMarkScheme) "MS" else "QP"
            val fileName = "Real_${paper.examBoard.shortName}_${paper.subjectId}_${paper.year}_${cleanSession}_${cleanCode}_${suffix}.pdf"
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            val existingFile = File(storageDir, fileName)
            if (existingFile.exists() && existingFile.length() > 0) {
                return existingFile
            }

            val pdfDoc = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842

            val titlePaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(71, 85, 105)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val headerBoxPaint = Paint().apply {
                color = Color.rgb(241, 245, 249)
                style = Paint.Style.FILL
            }

            val borderPaint = Paint().apply {
                color = Color.rgb(203, 213, 225)
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }

            val questionPaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val markPaint = Paint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            var currentPageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
            var page = pdfDoc.startPage(pageInfo)
            var canvas: Canvas = page.canvas

            val margin = 38f
            val contentWidth = pageWidth - (margin * 2)
            var currentY = margin

            fun drawWrappedText(text: String, startX: Float, maxWidth: Float, paint: Paint): Float {
                val lines = text.split("\n")
                var lineY = currentY
                for (singleLine in lines) {
                    val words = singleLine.split(" ")
                    var currentLine = ""
                    for (word in words) {
                        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                        if (paint.measureText(testLine) > maxWidth) {
                            canvas.drawText(currentLine, startX, lineY, paint)
                            currentLine = word
                            lineY += paint.textSize + 3.5f
                        } else {
                            currentLine = testLine
                        }
                    }
                    if (currentLine.isNotEmpty()) {
                        canvas.drawText(currentLine, startX, lineY, paint)
                        lineY += paint.textSize + 3.5f
                    }
                }
                return lineY
            }

            fun checkPageBreak(neededHeight: Float) {
                if (currentY + neededHeight > pageHeight - 45f) {
                    canvas.drawText("Page $currentPageNumber   •   [Turn over", margin + contentWidth - 110f, pageHeight - 20f, subtitlePaint)
                    FileUtils.drawAppWatermark(context, canvas, pageWidth.toFloat(), pageHeight.toFloat())
                    pdfDoc.finishPage(page)

                    currentPageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                    page = pdfDoc.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = margin + 15f

                    canvas.drawText("${paper.examBoard.shortName} • ${paper.paperCode} (${paper.session} ${paper.year}) — Page $currentPageNumber", margin, currentY, subtitlePaint)
                    canvas.drawLine(margin, currentY + 5f, margin + contentWidth, currentY + 5f, borderPaint)
                    currentY += 22f
                }
            }

            // --- Cover Page Header ---
            canvas.drawRect(margin, currentY, margin + contentWidth, currentY + 80f, headerBoxPaint)
            canvas.drawRect(margin, currentY, margin + contentWidth, currentY + 80f, borderPaint)

            val boardTitle = when (paper.examBoard) {
                com.example.data.model.ExamBoard.CAMBRIDGE -> "CAMBRIDGE ASSESSMENT INTERNATIONAL EDUCATION"
                com.example.data.model.ExamBoard.EDEXCEL -> "PEARSON EDEXCEL INTERNATIONAL GCSE (9-1)"
                com.example.data.model.ExamBoard.OXFORD_AQA -> "OXFORD AQA INTERNATIONAL EXAMINATIONS"
            }
            canvas.drawText(boardTitle, margin + 12f, currentY + 18f, titlePaint)
            canvas.drawText("${paper.paperTitle} • Series: ${paper.session} ${paper.year}", margin + 12f, currentY + 34f, subtitlePaint)
            canvas.drawText("Official Syllabus Paper: ${paper.paperCode}   |   Syllabus Code: ${paper.syllabusCode}", margin + 12f, currentY + 50f, subtitlePaint)
            canvas.drawText("TIME ALLOWED: ${paper.duration}      MAXIMUM MARKS: ${paper.maxMarks}", margin + 12f, currentY + 68f, markPaint)
            currentY += 92f

            // Candidate Box
            canvas.drawRect(margin, currentY, margin + contentWidth, currentY + 36f, headerBoxPaint)
            canvas.drawRect(margin, currentY, margin + contentWidth, currentY + 36f, borderPaint)
            canvas.drawText("Candidate Name: __________________________   Centre No: [ _____ ]   Candidate No: [ _____ ]", margin + 12f, currentY + 22f, subtitlePaint)
            currentY += 46f

            // Instructions Box
            canvas.drawRect(margin, currentY, margin + contentWidth, currentY + 46f, headerBoxPaint)
            canvas.drawRect(margin, currentY, margin + contentWidth, currentY + 46f, borderPaint)
            canvas.drawText("INSTRUCTIONS TO CANDIDATES:", margin + 10f, currentY + 14f, markPaint)
            canvas.drawText("• Answer all questions. Use a black or dark blue pen. Write clearly in the spaces provided.", margin + 10f, currentY + 28f, subtitlePaint)
            canvas.drawText("• You may use an HB pencil for diagrams. Calculator permitted. Marks are shown in brackets [ ].", margin + 10f, currentY + 40f, subtitlePaint)
            currentY += 56f

            // Draw Questions
            paper.questions.forEach { q ->
                checkPageBreak(110f)

                val cleanSub = if (q.subPart.isNotBlank()) " ${q.subPart}" else ""
                val qHeader = "Question ${q.questionNumber}$cleanSub:"
                canvas.drawText(qHeader, margin, currentY + 10f, questionPaint)
                canvas.drawText("[${q.marks} mark${if (q.marks > 1) "s" else ""}]", margin + contentWidth - 55f, currentY + 10f, markPaint)
                currentY += 16f

                currentY = drawWrappedText(q.questionText, margin + 8f, contentWidth - 16f, bodyPaint)
                currentY += 6f

                if (q.options.isNotEmpty()) {
                    val optionLabels = listOf("A", "B", "C", "D")
                    q.options.forEachIndexed { optIdx, optTxt ->
                        checkPageBreak(20f)
                        val lbl = if (optIdx < optionLabels.size) optionLabels[optIdx] else "${optIdx + 1}"
                        currentY = drawWrappedText("($lbl)  $optTxt", margin + 18f, contentWidth - 36f, bodyPaint)
                    }
                    currentY += 8f
                } else {
                    // Candidate Answer Lines
                    val lineCount = (q.marks.coerceIn(2, 6))
                    for (i in 0 until lineCount) {
                        checkPageBreak(16f)
                        canvas.drawLine(margin + 12f, currentY + 10f, margin + contentWidth - 12f, currentY + 10f, borderPaint)
                        currentY += 14f
                    }
                    currentY += 6f
                }
            }

            // Mark Scheme Appendix if requested
            if (includeMarkScheme && paper.markSchemeContent.isNotBlank()) {
                canvas.drawText("Page $currentPageNumber   •   [Turn over", margin + contentWidth - 110f, pageHeight - 20f, subtitlePaint)
                FileUtils.drawAppWatermark(context, canvas, pageWidth.toFloat(), pageHeight.toFloat())
                pdfDoc.finishPage(page)

                currentPageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                page = pdfDoc.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin + 15f

                canvas.drawRect(margin, currentY, margin + contentWidth, currentY + 44f, headerBoxPaint)
                canvas.drawRect(margin, currentY, margin + contentWidth, currentY + 44f, borderPaint)
                canvas.drawText("OFFICIAL MARK SCHEME (EXAMINER CRITERIA)", margin + 12f, currentY + 18f, titlePaint)
                canvas.drawText("${paper.examBoard.shortName} ${paper.paperCode} • Series: ${paper.session} ${paper.year}", margin + 12f, currentY + 34f, subtitlePaint)
                currentY += 56f

                currentY = drawWrappedText(paper.markSchemeContent, margin + 6f, contentWidth - 12f, bodyPaint)
            }

            canvas.drawText("Page $currentPageNumber   •   END OF EXAMINATION", margin + (contentWidth / 2) - 80f, pageHeight - 20f, markPaint)
            FileUtils.drawAppWatermark(context, canvas, pageWidth.toFloat(), pageHeight.toFloat())
            pdfDoc.finishPage(page)

            // Save PDF
            if (!storageDir.exists()) storageDir.mkdirs()
            val pdfFile = existingFile

            val outputStream = FileOutputStream(pdfFile)
            pdfDoc.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDoc.close()

            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun generateRealExamPdf(
        context: Context,
        paper: com.example.data.repository.RealPastPaper,
        includeMarkScheme: Boolean = true
    ): File? = withContext(Dispatchers.IO) {
        generateRealExamPdfSync(context, paper, includeMarkScheme)
    }

    fun shareExamPdf(context: Context, pdfFile: File) {
        val getUri: (File) -> Uri = { file ->
            try {
                FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            } catch (_: Exception) {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }
        }
        try {
            val uri: Uri = getUri(pdfFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, pdfFile.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Exam Paper PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "Saved PDF to: ${pdfFile.name}", Toast.LENGTH_LONG).show()
        }
    }

    fun openOrShareExamPdf(context: Context, pdfFile: File) {
        val getUri: (File) -> Uri = { file ->
            try {
                FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            } catch (_: Exception) {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }
        }
        try {
            val uri: Uri = getUri(pdfFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Open Exam Paper PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    val uri: Uri = getUri(pdfFile)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Exam Paper PDF"))
            } catch (ex: Exception) {
                Toast.makeText(context, "Saved PDF to: ${pdfFile.name}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
