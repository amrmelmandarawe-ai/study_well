package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.SubjectEnum
import com.example.ui.player.HandStroke
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EditedDocumentPdfExporter {

    /**
     * Exports only the edited pages (or all pages if onlyEditedPages=false) with all handwriting strokes & highlights rendered.
     */
    suspend fun exportAnnotatedPdf(
        context: Context,
        material: StudyMaterialEntity,
        pdfFile: File?,
        totalPages: Int,
        pageStrokes: Map<Int, List<HandStroke>>,
        onlyEditedPages: Boolean = false
    ): File? = withContext(Dispatchers.IO) {
        try {
            val pdfDoc = PdfDocument()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val baseName = FileUtils.cleanFileNameForTitle(material.title).replace(" ", "_").ifBlank { "StudyDocument" }
            val fileName = "${baseName}_Edited_$timeStamp.pdf"

            // Determine which pages to export
            val pagesToExport = if (onlyEditedPages) {
                (0 until totalPages).filter { (pageStrokes[it]?.isNotEmpty() == true) }
            } else {
                (0 until totalPages).toList()
            }

            if (pagesToExport.isEmpty()) {
                // If onlyEditedPages was selected but no pages have strokes, export page 0
                return@withContext null
            }

            var exportedCount = 0

            for (pageIndex in pagesToExport) {
                exportedCount++
                // Standard A4 dimensions in PostScript points (595 x 842 pt)
                val targetPdfWidth = 595
                val targetPdfHeight = 842

                val pageInfo = PdfDocument.PageInfo.Builder(targetPdfWidth, targetPdfHeight, exportedCount).create()
                val page = pdfDoc.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                // Fill background white
                canvas.drawColor(android.graphics.Color.WHITE)

                // Render high-res background page bitmap
                val pageBitmap: Bitmap? = if (pdfFile != null && pdfFile.exists()) {
                    FileUtils.renderPdfPage(pdfFile, pageIndex, targetWidth = 1200)
                } else {
                    val url = material.contentUrl
                    if (url.startsWith("/")) {
                        val f = File(url)
                        if (f.exists()) FileUtils.renderPdfPage(f, pageIndex, targetWidth = 1200) else null
                    } else if (url.startsWith("content://")) {
                        FileUtils.renderPdfPageFromUri(context, Uri.parse(url), pageIndex, targetWidth = 1200)
                    } else {
                        val fallback = FileUtils.ensurePdfFile(context, material)
                        if (fallback != null) FileUtils.renderPdfPage(fallback, pageIndex, targetWidth = 1200) else null
                    }
                }

                if (pageBitmap != null) {
                    val srcRect = android.graphics.Rect(0, 0, pageBitmap.width, pageBitmap.height)
                    val dstRect = android.graphics.RectF(0f, 0f, targetPdfWidth.toFloat(), targetPdfHeight.toFloat())
                    val bmPaint = Paint().apply { isFilterBitmap = true }
                    canvas.drawBitmap(pageBitmap, srcRect, dstRect, bmPaint)
                } else {
                    // Fallback placeholder background if page bitmap couldn't be loaded
                    val bgPaint = Paint().apply {
                        color = android.graphics.Color.rgb(248, 250, 252)
                        style = Paint.Style.FILL
                    }
                    canvas.drawRect(0f, 0f, targetPdfWidth.toFloat(), targetPdfHeight.toFloat(), bgPaint)

                    val textPaint = Paint().apply {
                        color = android.graphics.Color.rgb(30, 41, 59)
                        textSize = 14f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        isAntiAlias = true
                    }
                    canvas.drawText("${material.title} — Page ${pageIndex + 1}", 30f, 50f, textPaint)
                }

                // Render handwritten strokes on top of canvas
                val strokes = pageStrokes[pageIndex] ?: emptyList()
                if (strokes.isNotEmpty()) {
                    for (stroke in strokes) {
                        if (stroke.points.isEmpty()) continue

                        val srcW = if (stroke.canvasWidth > 0f) stroke.canvasWidth else {
                            val maxPtX = stroke.points.maxOfOrNull { it.x } ?: 1080f
                            if (maxPtX > 595f) maxPtX else 1080f
                        }
                        val srcH = if (stroke.canvasHeight > 0f) stroke.canvasHeight else {
                            val maxPtY = stroke.points.maxOfOrNull { it.y } ?: 1528f
                            if (maxPtY > 842f) maxPtY else (srcW * (targetPdfHeight.toFloat() / targetPdfWidth))
                        }

                        val scaleRatioX = targetPdfWidth.toFloat() / srcW
                        val scaleRatioY = targetPdfHeight.toFloat() / srcH

                        val strokePaint = Paint().apply {
                            isAntiAlias = true
                            style = Paint.Style.STROKE
                            strokeCap = Paint.Cap.ROUND
                            strokeJoin = Paint.Join.ROUND
                            strokeWidth = (stroke.strokeWidth * scaleRatioX).coerceIn(1.5f, 24f)

                            val baseColor = stroke.color.toArgb()
                            if (stroke.isHighlighter) {
                                val alpha = 110
                                color = android.graphics.Color.argb(
                                    alpha,
                                    android.graphics.Color.red(baseColor),
                                    android.graphics.Color.green(baseColor),
                                    android.graphics.Color.blue(baseColor)
                                )
                            } else {
                                color = baseColor
                            }
                        }

                        val androidPath = android.graphics.Path()
                        val firstPt = stroke.points[0]
                        androidPath.moveTo(firstPt.x * scaleRatioX, firstPt.y * scaleRatioY)

                        for (i in 1 until stroke.points.size) {
                            val pt = stroke.points[i]
                            androidPath.lineTo(pt.x * scaleRatioX, pt.y * scaleRatioY)
                        }

                        canvas.drawPath(androidPath, strokePaint)
                    }
                }

                // Watermark footer: "Edited with StudyWell IGCSE"
                val footerPaint = Paint().apply {
                    color = android.graphics.Color.argb(160, 100, 116, 139)
                    textSize = 9f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    isAntiAlias = true
                }
                canvas.drawText("Edited Part • Page ${pageIndex + 1} of $totalPages • StudyWell IGCSE", 24f, targetPdfHeight - 14f, footerPaint)

                // Add the official watermark: App Logo & "study well app" text
                FileUtils.drawAppWatermark(context, canvas, targetPdfWidth.toFloat(), targetPdfHeight.toFloat())

                pdfDoc.finishPage(page)
            }

            // Save to device storage
            val outputFile = savePdfToDownloads(context, pdfDoc, fileName)
            pdfDoc.close()
            if (outputFile != null) {
                FileUtils.evictPdfCache()
                AnnotationPersistenceManager.saveStrokes(context, outputFile.absolutePath, pageStrokes)
                AnnotationPersistenceManager.saveStrokes(context, outputFile.name, pageStrokes)
            }
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Exports edited text/notes as a beautifully formatted PDF document.
     */
    suspend fun exportEditedNotesPdf(
        context: Context,
        material: StudyMaterialEntity,
        editedText: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val pdfDoc = PdfDocument()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val baseName = FileUtils.cleanFileNameForTitle(material.title).replace(" ", "_").ifBlank { "StudyNotes" }
            val fileName = "${baseName}_EditedNotes_$timeStamp.pdf"

            val pageWidth = 595
            val pageHeight = 842
            val margin = 40f
            val contentWidth = pageWidth - (margin * 2)

            val titlePaint = Paint().apply {
                color = android.graphics.Color.rgb(15, 23, 42)
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = android.graphics.Color.rgb(71, 85, 105)
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = android.graphics.Color.rgb(30, 41, 59)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val headerBoxPaint = Paint().apply {
                color = android.graphics.Color.rgb(241, 245, 249)
                style = Paint.Style.FILL
            }

            var currentPageNum = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNum).create()
            var page = pdfDoc.startPage(pageInfo)
            var canvas = page.canvas
            var currentY = margin

            // Draw Header
            canvas.drawRoundRect(margin, currentY, margin + contentWidth, currentY + 54f, 8f, 8f, headerBoxPaint)
            currentY += 22f
            canvas.drawText("STUDYWELL IGCSE — EDITED REVISION NOTES", margin + 14f, currentY, titlePaint)
            currentY += 16f
            canvas.drawText("Document: ${material.title}  •  Topic: ${material.topic}  •  Export Date: $timeStamp", margin + 14f, currentY, subtitlePaint)
            currentY += 34f

            // Split edited text into lines and wrap
            val paragraphs = editedText.split("\n")
            for (para in paragraphs) {
                if (para.isBlank()) {
                    currentY += 12f
                    continue
                }

                val words = para.split(" ")
                var lineBuffer = StringBuilder()

                for (word in words) {
                    val testLine = if (lineBuffer.isEmpty()) word else "$lineBuffer $word"
                    val measuredWidth = bodyPaint.measureText(testLine)

                    if (measuredWidth > contentWidth) {
                        if (currentY > pageHeight - 50f) {
                            // Footer
                            canvas.drawText("Page $currentPageNum", margin + contentWidth - 40f, pageHeight - 20f, subtitlePaint)
                            FileUtils.drawAppWatermark(context, canvas, pageWidth.toFloat(), pageHeight.toFloat())
                            pdfDoc.finishPage(page)

                            currentPageNum++
                            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNum).create()
                            page = pdfDoc.startPage(pageInfo)
                            canvas = page.canvas
                            currentY = margin + 20f
                        }

                        canvas.drawText(lineBuffer.toString(), margin, currentY, bodyPaint)
                        currentY += 16f
                        lineBuffer = StringBuilder(word)
                    } else {
                        lineBuffer = StringBuilder(testLine)
                    }
                }

                if (lineBuffer.isNotEmpty()) {
                    if (currentY > pageHeight - 50f) {
                        canvas.drawText("Page $currentPageNum", margin + contentWidth - 40f, pageHeight - 20f, subtitlePaint)
                        FileUtils.drawAppWatermark(context, canvas, pageWidth.toFloat(), pageHeight.toFloat())
                        pdfDoc.finishPage(page)

                        currentPageNum++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNum).create()
                        page = pdfDoc.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = margin + 20f
                    }
                    canvas.drawText(lineBuffer.toString(), margin, currentY, bodyPaint)
                    currentY += 18f
                }
            }

            // Footer on last page
            canvas.drawText("Page $currentPageNum  •  StudyWell IGCSE Notes Exporter", margin, pageHeight - 20f, subtitlePaint)
            FileUtils.drawAppWatermark(context, canvas, pageWidth.toFloat(), pageHeight.toFloat())
            pdfDoc.finishPage(page)

            val outputFile = savePdfToDownloads(context, pdfDoc, fileName)
            pdfDoc.close()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun savePdfToDownloads(context: Context, pdfDoc: PdfDocument, fileName: String): File? {
        return try {
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            if (!storageDir.exists()) storageDir.mkdirs()
            val pdfFile = File(storageDir, fileName)

            FileOutputStream(pdfFile).use { out ->
                pdfDoc.writeTo(out)
                out.flush()
            }

            // Also copy to system public Downloads if possible on Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { mediaOut ->
                            pdfFile.inputStream().use { input ->
                                input.copyTo(mediaOut)
                            }
                        }
                    }
                } catch (_: Exception) {}
            }

            // Automatically record into Student Library database
            try {
                val db = com.example.data.db.AppDatabase.getDatabase(context)
                val sizeKb = (pdfFile.length() / 1024.0)
                val formattedSize = if (sizeKb >= 1024) String.format(Locale.US, "%.1f MB", sizeKb / 1024.0) else String.format(Locale.US, "%.0f KB", sizeKb)
                val isEdited = fileName.contains("Edited", ignoreCase = true) || fileName.contains("Annotated", ignoreCase = true)
                val doc = com.example.data.model.LibraryDocumentEntity(
                    title = fileName.removeSuffix(".pdf").replace("_", " "),
                    filePath = pdfFile.absolutePath,
                    fileName = fileName,
                    docType = if (isEdited) "EDITED_PDF" else "DOWNLOADED_PDF",
                    fileSizeFormatted = formattedSize,
                    timestamp = System.currentTimeMillis(),
                    notes = if (isEdited) "Student Hand-Annotated Document" else "Downloaded Document"
                )
                db.libraryDocumentDao().insertDocument(doc)
            } catch (_: Exception) {}

            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileUri(context: Context, file: File): Uri {
        return try {
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (_: Exception) {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
    }

    fun openDownloadedPdf(context: Context, pdfFile: File) {
        try {
            val uri: Uri = getFileUri(context, pdfFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Open Downloaded PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            shareDownloadedPdf(context, pdfFile)
        }
    }

    fun shareDownloadedPdf(context: Context, pdfFile: File) {
        try {
            val uri: Uri = getFileUri(context, pdfFile)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, pdfFile.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Share Downloaded PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Saved to ${pdfFile.name}", Toast.LENGTH_SHORT).show()
        }
    }
}
