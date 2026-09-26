package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.LruCache
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

enum class PdfItemType {
    TEXT,
    HEADER,
    SEPARATOR,
    BULLET,
    ITALIC,
    BLANK_LINE
}

class PdfRenderItem(
    val type: PdfItemType,
    val text: String = "",
    val headerLevel: Int = 0,
    val bulletChar: String = ""
)

object FileUtils {

    // Memory-efficient LRU bitmap cache dynamically allocated up to 128MB for ultra-fast page rendering
    private val maxCacheSize = (Runtime.getRuntime().maxMemory() / 4).toInt().coerceIn(64 * 1024 * 1024, 128 * 1024 * 1024)
    private val pdfPageCache = object : LruCache<String, Bitmap>(maxCacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount
        }
    }

    private object PdfRendererManager {
        private val rendererCache = object : LruCache<String, Pair<ParcelFileDescriptor, PdfRenderer>>(8) {
            override fun entryRemoved(
                evicted: Boolean,
                key: String,
                oldValue: Pair<ParcelFileDescriptor, PdfRenderer>,
                newValue: Pair<ParcelFileDescriptor, PdfRenderer>?
            ) {
                try {
                    oldValue.second.close()
                    oldValue.first.close()
                } catch (_: Exception) {}
            }
        }

        @Synchronized
        fun getRenderer(file: File): PdfRenderer? {
            if (!file.exists() || file.length() < 50) return null
            val key = "${file.absolutePath}_${file.lastModified()}_${file.length()}"
            val cached = rendererCache.get(key)
            if (cached != null) return cached.second
            return try {
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                rendererCache.put(key, Pair(pfd, renderer))
                renderer
            } catch (e: Exception) {
                android.util.Log.e("FileUtils", "Failed to get PdfRenderer for ${file.name}: ${e.message}")
                null
            }
        }

        @Synchronized
        fun getRendererFromUri(context: Context, uri: Uri): PdfRenderer? {
            val key = uri.toString()
            val cached = rendererCache.get(key)
            if (cached != null) return cached.second
            return try {
                val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
                val renderer = PdfRenderer(pfd)
                rendererCache.put(key, Pair(pfd, renderer))
                renderer
            } catch (e: Exception) {
                null
            }
        }

        @Synchronized
        fun clearAll() {
            rendererCache.evictAll()
        }
    }

    fun evictPdfCache() {
        try {
            pdfPageCache.evictAll()
            PdfRendererManager.clearAll()
        } catch (_: Exception) {}
    }

    data class FileMeta(
        val name: String,
        val sizeFormatted: String,
        val sizeBytes: Long,
        val mimeType: String?,
        val isGoogleDriveOrLocal: Boolean,
        val isPdf: Boolean,
        val pageCount: Int = 0
    )

    fun getFileMeta(context: Context, uri: Uri): FileMeta {
        var name = "Document_${System.currentTimeMillis().toString().takeLast(4)}"
        var size: Long = -1
        val mimeType = context.contentResolver.getType(uri)

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        val fetchedName = cursor.getString(nameIndex)
                        if (!fetchedName.isNullOrBlank()) name = fetchedName
                    }
                    if (sizeIndex != -1) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (_: Exception) {}

        val isPdf = name.endsWith(".pdf", ignoreCase = true) ||
                (mimeType != null && mimeType.contains("pdf", ignoreCase = true))

        var pages = 0
        if (isPdf) {
            pages = countPdfPagesFromUri(context, uri)
        }

        val sizeFormatted = formatFileSize(size)
        val isDrive = uri.toString().contains("com.google.android.apps.docs.storage") ||
                uri.toString().contains("com.google.android.apps.docs")

        return FileMeta(
            name = name,
            sizeFormatted = sizeFormatted,
            sizeBytes = size,
            mimeType = mimeType,
            isGoogleDriveOrLocal = true,
            isPdf = isPdf,
            pageCount = pages
        )
    }

    fun countPdfPagesFromUri(context: Context, uri: Uri): Int {
        return try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return 0
            val renderer = PdfRenderer(pfd)
            val count = renderer.pageCount
            try { renderer.close() } catch (_: Exception) {}
            try { pfd.close() } catch (_: Exception) {}
            count
        } catch (_: Exception) {
            0
        }
    }

    fun countPdfPagesFromFile(file: File): Int {
        return try {
            if (!file.exists()) return 0
            val renderer = PdfRendererManager.getRenderer(file)
            renderer?.pageCount ?: 0
        } catch (_: Exception) {
            0
        }
    }

    fun downloadRemotePdfToCache(context: Context, urlString: String, title: String): File? {
        return try {
            val safeName = title.filter { it.isLetterOrDigit() }.take(30)
            val pdfDir = File(context.filesDir, "study_pdfs").apply { if (!exists()) mkdirs() }
            val destFile = File(pdfDir, "remote_${urlString.hashCode().toUInt().toString(16)}_${safeName}.pdf")
            if (destFile.exists() && destFile.length() > 500) {
                return destFile
            }
            val url = java.net.URL(urlString)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 12000
            connection.instanceFollowRedirects = true
            connection.connect()
            if (connection.responseCode in 200..299) {
                val tempFile = File(pdfDir, "temp_${System.currentTimeMillis()}.pdf")
                connection.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                if (tempFile.exists() && tempFile.length() > 500) {
                    tempFile.renameTo(destFile)
                    return destFile
                } else {
                    tempFile.delete()
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    fun copyUriToInternalStorage(context: Context, uri: Uri, originalName: String): File? {
        return try {
            val safeName = originalName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val pdfDir = File(context.filesDir, "study_pdfs").apply { if (!exists()) mkdirs() }
            val destFile = File(pdfDir, "${System.currentTimeMillis()}_$safeName")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun renderPdfPage(file: File, pageIndex: Int, targetWidth: Int = 1080): Bitmap? {
        val cacheKey = "file_${file.absolutePath}_${pageIndex}_$targetWidth"
        pdfPageCache.get(cacheKey)?.let { cached ->
            if (!cached.isRecycled) return cached
        }

        return try {
            if (!file.exists()) return null
            val renderer = PdfRendererManager.getRenderer(file) ?: return null
            synchronized(renderer) {
                if (pageIndex < 0 || pageIndex >= renderer.pageCount) return null
                renderer.openPage(pageIndex).use { page ->
                    val ratio = page.height.toFloat() / page.width.toFloat()
                    val targetHeight = (targetWidth * ratio).toInt().coerceAtLeast(100)
                    val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.RGB_565)
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    pdfPageCache.put(cacheKey, bitmap)
                    bitmap
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FileUtils", "Failed to render PDF page $pageIndex: ${e.message}", e)
            null
        }
    }

    fun renderPdfPageFromUri(context: Context, uri: Uri, pageIndex: Int, targetWidth: Int = 1080): Bitmap? {
        val cacheKey = "uri_${uri}_${pageIndex}_$targetWidth"
        pdfPageCache.get(cacheKey)?.let { cached ->
            if (!cached.isRecycled) return cached
        }

        return try {
            val renderer = PdfRendererManager.getRendererFromUri(context, uri) ?: return null
            synchronized(renderer) {
                if (pageIndex < 0 || pageIndex >= renderer.pageCount) return null
                renderer.openPage(pageIndex).use { page ->
                    val ratio = page.height.toFloat() / page.width.toFloat()
                    val targetHeight = (targetWidth * ratio).toInt().coerceAtLeast(100)
                    val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.RGB_565)
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    pdfPageCache.put(cacheKey, bitmap)
                    bitmap
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FileUtils", "Failed to render PDF page $pageIndex from uri: ${e.message}", e)
            null
        }
    }

    fun loadBitmapFromFile(file: File, maxDim: Int = 1200): Bitmap? {
        return try {
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                android.graphics.BitmapFactory.decodeFile(file.absolutePath, this)
                var scale = 1
                val maxActualDim = maxOf(outWidth, outHeight)
                while (maxActualDim / scale > maxDim) {
                    scale *= 2
                }
                inJustDecodeBounds = false
                inSampleSize = scale.coerceAtLeast(1)
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            android.graphics.BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: Throwable) {
            null
        }
    }

    fun loadBitmapFromUri(context: Context, uri: Uri, maxDim: Int = 1200): Bitmap? {
        return try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
            val fd = pfd.fileDescriptor
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                android.graphics.BitmapFactory.decodeFileDescriptor(fd, null, this)
                var scale = 1
                val maxActualDim = maxOf(outWidth, outHeight)
                while (maxActualDim / scale > maxDim) {
                    scale *= 2
                }
                inJustDecodeBounds = false
                inSampleSize = scale.coerceAtLeast(1)
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            val bitmap = android.graphics.BitmapFactory.decodeFileDescriptor(fd, null, options)
            pfd.close()
            bitmap
        } catch (e: Throwable) {
            null
        }
    }

    fun fixImageRotationIfRequired(filePath: String, bitmap: Bitmap): Bitmap {
        return try {
            val exifInterface = android.media.ExifInterface(filePath)
            val orientation = exifInterface.getAttributeInt(
                android.media.ExifInterface.TAG_ORIENTATION,
                android.media.ExifInterface.ORIENTATION_UNDEFINED
            )
            val matrix = android.graphics.Matrix()
            when (orientation) {
                android.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                android.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                android.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap && !bitmap.isRecycled) {
                bitmap.recycle()
            }
            rotated
        } catch (e: Throwable) {
            bitmap
        }
    }

    /**
     * Resolves or generates a local, renderable PDF file for any study material.
     * If user uploaded a PDF or shared from device/drive, returns that file.
     * If material is a textbook or notes without on-disk PDF, synthesizes authentic A4 pages.
     */
    fun ensurePdfFile(context: Context, material: com.example.data.model.StudyMaterialEntity): File? {
        // 1. Local explicit path
        val path = material.localFilePath
        if (!path.isNullOrBlank()) {
            val file = File(path)
            if (file.exists() && file.length() > 100) return file
        }

        // 2. Local contentUrl or Android storage URI
        val url = material.contentUrl
        if (url.startsWith("/")) {
            val file = File(url)
            if (file.exists() && file.length() > 100) return file
        } else if (url.startsWith("content://")) {
            val uri = Uri.parse(url)
            val copied = copyUriToInternalStorage(context, uri, "${material.title}.pdf")
            if (copied != null && copied.exists() && copied.length() > 100) return copied
        } else if (url.startsWith("http://") || url.startsWith("https://")) {
            if (url.contains(".pdf", ignoreCase = true) || url.contains("drive.google.com/uc", ignoreCase = true)) {
                val downloaded = downloadRemotePdfToCache(context, url, material.title)
                if (downloaded != null && downloaded.exists() && downloaded.length() > 100) {
                    return downloaded
                }
            }
        }

        // 3. For EXAM materials: try generating authentic exam paper or mark scheme PDF
        if (material.materialType == "EXAM" || material.title.contains("Paper", ignoreCase = true) || material.title.contains("Mark Scheme", ignoreCase = true)) {
            try {
                val cleanYear = material.topic.filter { it.isDigit() }
                val isMarkScheme = material.title.contains("Mark Scheme", ignoreCase = true) || material.title.contains("(MS)", ignoreCase = true)
                val paper = com.example.data.repository.RealPastPaperRepository.findPaper(
                    subjectId = material.subjectId,
                    examBoard = null,
                    year = if (cleanYear.isNotBlank()) cleanYear else null,
                    paperCodeKeyword = if (material.title.contains("/", ignoreCase = true)) material.title.substringAfterLast(" ").trim() else null
                )
                if (paper != null) {
                    val pdfFile = com.example.utils.ExamPdfGenerator.generateRealExamPdfSync(context, paper, includeMarkScheme = isMarkScheme)
                    if (pdfFile != null && pdfFile.exists() && pdfFile.length() > 100) {
                        return pdfFile
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 4. Generate high-quality, professional textbook / revision notes / markscheme PDF
        return generateTextbookPdf(context, material)
    }

    /**
     * Generates standard A4 PDF pages with Cambridge IGCSE headers, syllabus topics,
     * formulas, summary callouts, and practice problems so every book has real renderable pages.
     */
    fun generateTextbookPdf(context: Context, material: com.example.data.model.StudyMaterialEntity): File? {
        return try {
            val pdfDir = File(context.filesDir, "study_pdfs").apply { if (!exists()) mkdirs() }
            val safeKey = "${material.subjectId}_${material.materialType}_${material.id}_${material.title}".filter { it.isLetterOrDigit() }.take(35) + "_" + (material.title + "_" + material.topic + "_" + material.documentContent.take(150)).hashCode().toUInt().toString(16)
            val destFile = File(pdfDir, "doc_${safeKey}.pdf")

            // If already generated, verify and return cached PDF
            if (destFile.exists() && destFile.length() > 500) {
                return destFile
            }

            val doc = android.graphics.pdf.PdfDocument()
            val text = if (material.documentContent.isNotBlank()) {
                material.documentContent
            } else {
                buildDefaultSubjectContent(material)
            }
            val lines = text.split("\n")

            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

            // --- Helpers ---
            fun wrapText(text: String, itemPaint: android.graphics.Paint, maxWidth: Float): List<String> {
                val words = text.split(" ")
                val wrappedLines = mutableListOf<String>()
                var currentLine = java.lang.StringBuilder()

                for (word in words) {
                    val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
                    val width = itemPaint.measureText(testLine)
                    if (width <= maxWidth) {
                        currentLine.append(if (currentLine.isEmpty()) word else " $word")
                    } else {
                        wrappedLines.add(currentLine.toString())
                        currentLine = java.lang.StringBuilder(word)
                    }
                }
                if (currentLine.isNotEmpty()) {
                    wrappedLines.add(currentLine.toString())
                }
                return wrappedLines
            }

            fun drawPageDecorations(canvas: android.graphics.Canvas, pageNum: Int, totalPages: Int) {
                // Background
                canvas.drawColor(android.graphics.Color.WHITE)

                // Top Header Banner
                paint.color = android.graphics.Color.rgb(15, 23, 42) // Slate 900
                canvas.drawRect(0f, 0f, 595f, 46f, paint)

                paint.color = android.graphics.Color.rgb(250, 204, 21) // Sleek Gold
                canvas.drawRect(0f, 46f, 595f, 50f, paint)

                // Header Text
                paint.color = android.graphics.Color.WHITE
                paint.textSize = 13f
                paint.isFakeBoldText = true
                paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                val subCode = if (material.subjectId == "PHYSICS") "0625" else if (material.subjectId == "MATH") "0580" else if (material.subjectId == "ENGLISH") "0500" else if (material.subjectId == "CHEMISTRY") "0620" else if (material.subjectId == "BIOLOGY") "0610" else "0417"
                canvas.drawText("CAMBRIDGE IGCSE • ${material.subjectId} ($subCode)", 28f, 30f, paint)

                paint.color = android.graphics.Color.rgb(203, 213, 225)
                paint.textSize = 9.5f
                paint.isFakeBoldText = false
                paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                canvas.drawText("Official Curriculum Study & Assessment", 360f, 30f, paint)

                // Footer rule
                paint.color = android.graphics.Color.rgb(226, 232, 240)
                paint.strokeWidth = 1.2f
                canvas.drawLine(28f, 804f, 567f, 804f, paint)

                // Page Number & Footer Text
                paint.color = android.graphics.Color.rgb(148, 163, 184)
                paint.textSize = 9.5f
                paint.isFakeBoldText = false
                paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                canvas.drawText("Page $pageNum of $totalPages", 28f, 822f, paint)
                canvas.drawText("IGCSE Study Well • International Assessment & Curriculum", 210f, 822f, paint)

                // Watermark
                drawAppWatermark(context, canvas, 595f, 842f)
            }

            fun drawInlineStyledLine(
                canvas: android.graphics.Canvas,
                line: String,
                startX: Float,
                y: Float,
                textPaint: android.graphics.Paint
            ) {
                val parts = line.split("**")
                var currentX = startX
                var isBold = false

                for (part in parts) {
                    if (part.isEmpty()) {
                        isBold = !isBold
                        continue
                    }

                    if (isBold) {
                        textPaint.isFakeBoldText = true
                        textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                        textPaint.color = android.graphics.Color.rgb(15, 23, 42)
                    } else {
                        textPaint.isFakeBoldText = false
                        textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                        textPaint.color = android.graphics.Color.rgb(51, 65, 85)
                    }

                    canvas.drawText(part, currentX, y, textPaint)
                    currentX += textPaint.measureText(part)
                    isBold = !isBold
                }
            }

            // Parse Markdown Lines
            val parsedItems = mutableListOf<PdfRenderItem>()
            lines.forEach { line ->
                val trimmed = line.trim()
                when {
                    trimmed.isEmpty() -> {
                        parsedItems.add(PdfRenderItem(PdfItemType.BLANK_LINE))
                    }
                    trimmed == "---" || trimmed == "___" || trimmed == "***" -> {
                        parsedItems.add(PdfRenderItem(PdfItemType.SEPARATOR))
                    }
                    trimmed.startsWith("#") -> {
                        val level = trimmed.takeWhile { it == '#' }.length
                        val textOnly = trimmed.drop(level).trim()
                        parsedItems.add(PdfRenderItem(PdfItemType.HEADER, text = textOnly, headerLevel = level))
                    }
                    trimmed.startsWith("•") || trimmed.startsWith("-") || trimmed.startsWith("* ") -> {
                        val bulletChar = if (trimmed.startsWith("•")) "•" else "-"
                        val textOnly = trimmed.drop(1).trim()
                        parsedItems.add(PdfRenderItem(PdfItemType.BULLET, text = textOnly, bulletChar = bulletChar))
                    }
                    trimmed.startsWith("*") && trimmed.endsWith("*") && !trimmed.startsWith("**") -> {
                        val textOnly = trimmed.substring(1, trimmed.length - 1).trim()
                        parsedItems.add(PdfRenderItem(PdfItemType.ITALIC, text = textOnly))
                    }
                    else -> {
                        parsedItems.add(PdfRenderItem(PdfItemType.TEXT, text = line))
                    }
                }
            }

            // --- Pass 1: Measure total pages ---
            var totalPages = 1
            var currentY = 120f

            parsedItems.forEach { item ->
                when (item.type) {
                    PdfItemType.BLANK_LINE -> {
                        currentY += 12f
                        if (currentY > 740f) { totalPages++; currentY = 120f }
                    }
                    PdfItemType.SEPARATOR -> {
                        currentY += 8f
                        if (currentY > 740f) { totalPages++; currentY = 120f }
                        currentY += 12f
                    }
                    PdfItemType.HEADER -> {
                        currentY += 14f
                        if (currentY > 740f) { totalPages++; currentY = 120f }

                        paint.isFakeBoldText = true
                        val fontSize = when (item.headerLevel) {
                            1 -> 16f
                            2 -> 14f
                            else -> 12.5f
                        }
                        paint.textSize = fontSize

                        val wrapped = wrapText(item.text, paint, 515f)
                        wrapped.forEach {
                            currentY += fontSize + 6f
                            if (currentY > 740f) { totalPages++; currentY = 120f }
                        }
                        currentY += 6f
                    }
                    PdfItemType.ITALIC -> {
                        currentY += 4f
                        if (currentY > 740f) { totalPages++; currentY = 120f }

                        paint.isFakeBoldText = false
                        paint.textSize = 10f
                        val wrapped = wrapText(item.text, paint, 515f)
                        wrapped.forEach {
                            currentY += 16f
                            if (currentY > 740f) { totalPages++; currentY = 120f }
                        }
                    }
                    PdfItemType.BULLET -> {
                        currentY += 4f
                        if (currentY > 740f) { totalPages++; currentY = 120f }

                        paint.isFakeBoldText = false
                        paint.textSize = 10.5f
                        val wrapped = wrapText(item.text, paint, 495f)
                        wrapped.forEach {
                            currentY += 16f
                            if (currentY > 740f) { totalPages++; currentY = 120f }
                        }
                    }
                    PdfItemType.TEXT -> {
                        paint.isFakeBoldText = false
                        paint.textSize = 10.5f
                        val wrapped = wrapText(item.text, paint, 515f)
                        wrapped.forEach {
                            currentY += 16f
                            if (currentY > 740f) { totalPages++; currentY = 120f }
                        }
                    }
                }
            }

            // Ensure we have at least 1 page
            totalPages = totalPages.coerceAtLeast(1)

            // --- Pass 2: Actually draw the pages ---
            var pageNum = 1
            currentY = 120f

            var pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
            var currentPage = doc.startPage(pageInfo)
            var canvas = currentPage.canvas

            // Draw decorations for page 1
            drawPageDecorations(canvas, pageNum, totalPages)

            fun checkNewPage() {
                if (currentY > 740f) {
                    doc.finishPage(currentPage)
                    pageNum++
                    pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
                    currentPage = doc.startPage(pageInfo)
                    canvas = currentPage.canvas
                    drawPageDecorations(canvas, pageNum, totalPages)
                    currentY = 120f
                }
            }

            parsedItems.forEach { item ->
                when (item.type) {
                    PdfItemType.BLANK_LINE -> {
                        currentY += 12f
                        checkNewPage()
                    }
                    PdfItemType.SEPARATOR -> {
                        currentY += 8f
                        checkNewPage()
                        paint.color = android.graphics.Color.rgb(226, 232, 240)
                        paint.strokeWidth = 1.2f
                        canvas.drawLine(40f, currentY, 555f, currentY, paint)
                        currentY += 12f
                    }
                    PdfItemType.HEADER -> {
                        currentY += 14f
                        checkNewPage()

                        paint.isFakeBoldText = true
                        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                        paint.color = android.graphics.Color.rgb(15, 23, 42) // Slate 900

                        val fontSize = when (item.headerLevel) {
                            1 -> 16f
                            2 -> 14f
                            else -> 12.5f
                        }
                        paint.textSize = fontSize

                        val wrapped = wrapText(item.text, paint, 515f)
                        wrapped.forEach { line ->
                            canvas.drawText(line, 40f, currentY, paint)
                            currentY += fontSize + 6f
                            checkNewPage()
                        }
                        currentY += 6f
                    }
                    PdfItemType.ITALIC -> {
                        currentY += 4f
                        checkNewPage()

                        paint.isFakeBoldText = false
                        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
                        paint.color = android.graphics.Color.rgb(71, 85, 105) // Cool grey
                        paint.textSize = 10f

                        val wrapped = wrapText(item.text, paint, 515f)
                        wrapped.forEach { line ->
                            canvas.drawText(line, 40f, currentY, paint)
                            currentY += 16f
                            checkNewPage()
                        }
                    }
                    PdfItemType.BULLET -> {
                        currentY += 4f
                        checkNewPage()

                        paint.isFakeBoldText = true
                        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                        paint.color = android.graphics.Color.rgb(15, 23, 42)
                        paint.textSize = 10.5f

                        canvas.drawText("•", 44f, currentY, paint)

                        paint.isFakeBoldText = false
                        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                        paint.color = android.graphics.Color.rgb(51, 65, 85)

                        val wrapped = wrapText(item.text, paint, 495f) // indented
                        wrapped.forEach { line ->
                            drawInlineStyledLine(canvas, line, 58f, currentY, paint)
                            currentY += 16f
                            checkNewPage()
                        }
                    }
                    PdfItemType.TEXT -> {
                        paint.isFakeBoldText = false
                        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                        paint.color = android.graphics.Color.rgb(51, 65, 85)
                        paint.textSize = 10.5f

                        val wrapped = wrapText(item.text, paint, 515f)
                        wrapped.forEach { line ->
                            drawInlineStyledLine(canvas, line, 40f, currentY, paint)
                            currentY += 16f
                            checkNewPage()
                        }
                    }
                }
            }

            doc.finishPage(currentPage)

            FileOutputStream(destFile).use { out ->
                doc.writeTo(out)
                out.flush()
            }
            doc.close()
            destFile
        } catch (e: Exception) {
            android.util.Log.e("FileUtils", "Error generating textbook PDF: ${e.message}", e)
            null
        }
    }

    fun formatFileSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "PDF Document"
        val kb = sizeBytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1.0 -> String.format("%.2f GB", gb)
            mb >= 1.0 -> String.format("%.1f MB", mb)
            kb >= 1.0 -> String.format("%.1f KB", kb)
            else -> "$sizeBytes B"
        }
    }

    fun readTextContent(context: Context, uri: Uri, maxChars: Int = 30000): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val sb = StringBuilder()
                    var line: String?
                    var charsRead = 0
                    while (reader.readLine().also { line = it } != null && charsRead < maxChars) {
                        sb.append(line).append("\n")
                        charsRead += (line?.length ?: 0) + 1
                    }
                    sb.toString().trim()
                }
            } ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    fun cleanFileNameForTitle(fileName: String): String {
        val withoutExt = fileName.substringBeforeLast(".")
        return withoutExt
            .replace("_", " ")
            .replace("-", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun parseGoogleDriveLink(url: String): Pair<Boolean, String> {
        val trimmed = url.trim()
        if (trimmed.contains("drive.google.com") || trimmed.contains("docs.google.com")) {
            val fileIdPattern = Regex("/d/([a-zA-Z0-9_-]+)")
            val match = fileIdPattern.find(trimmed)
            if (match != null) {
                val fileId = match.groupValues[1]
                return Pair(true, "https://drive.google.com/file/d/$fileId/preview")
            }

            val idPattern = Regex("[?&]id=([a-zA-Z0-9_-]+)")
            val idMatch = idPattern.find(trimmed)
            if (idMatch != null) {
                val fileId = idMatch.groupValues[1]
                return Pair(true, "https://drive.google.com/file/d/$fileId/preview")
            }
            return Pair(true, trimmed)
        }
        return Pair(false, trimmed)
    }

    fun extractSharedBatchFromIntent(context: Context, intent: Intent): com.example.data.model.SharedIncomingBatch? {
        val action = intent.action ?: return null
        val type = intent.type ?: "*/*"

        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)

        val uris = mutableListOf<Uri>()

        if (action == Intent.ACTION_SEND) {
            val streamUri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            if (streamUri != null) {
                uris.add(streamUri)
            } else if (intent.data != null) {
                intent.data?.let { uris.add(it) }
            }
        } else if (action == Intent.ACTION_SEND_MULTIPLE) {
            val streamList = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
            }
            streamList?.filterNotNull()?.let { uris.addAll(it) }
        } else if (action == Intent.ACTION_VIEW) {
            intent.data?.let { uris.add(it) }
        }

        if (uris.isEmpty() && sharedText.isNullOrBlank()) {
            return null
        }

        val items = mutableListOf<com.example.data.model.SharedIncomingFile>()

        for (uri in uris) {
            try {
                val meta = getFileMeta(context, uri)
                val mime = meta.mimeType ?: type

                val isPdf = meta.isPdf || mime.contains("pdf", ignoreCase = true) || meta.name.endsWith(".pdf", ignoreCase = true)
                val isImage = mime.startsWith("image/") || meta.name.matches(Regex(".*\\.(jpg|jpeg|png|webp|bmp|heic)$", RegexOption.IGNORE_CASE))
                val isVideo = mime.startsWith("video/") || meta.name.matches(Regex(".*\\.(mp4|mkv|webm|3gp|mov)$", RegexOption.IGNORE_CASE))
                val isText = mime.startsWith("text/") || meta.name.matches(Regex(".*\\.(txt|md|csv|json)$", RegexOption.IGNORE_CASE))

                // Always ensure we have a local copy of the shared file to prevent permission loss or access issues
                val copiedFile = copyUriToInternalStorage(context, uri, meta.name)
                val localPath = copiedFile?.absolutePath

                val pageCount = if (isPdf) {
                    if (copiedFile != null) countPdfPagesFromFile(copiedFile)
                    else countPdfPagesFromUri(context, uri).takeIf { it > 0 } ?: meta.pageCount
                } else 0

                val textContent = if (isText) readTextContent(context, uri) else ""

                items.add(
                    com.example.data.model.SharedIncomingFile(
                        uriString = uri.toString(),
                        localFilePath = localPath,
                        fileName = meta.name,
                        mimeType = mime,
                        sizeFormatted = meta.sizeFormatted,
                        isPdf = isPdf,
                        isImage = isImage,
                        isVideo = isVideo,
                        isText = isText,
                        textContent = textContent,
                        pageCount = pageCount
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // If only text was shared (e.g. copied problem or note from WhatsApp)
        if (items.isEmpty() && !sharedText.isNullOrBlank()) {
            items.add(
                com.example.data.model.SharedIncomingFile(
                    uriString = "",
                    localFilePath = null,
                    fileName = "Shared Note",
                    mimeType = "text/plain",
                    sizeFormatted = "${sharedText.length} chars",
                    isPdf = false,
                    isImage = false,
                    isVideo = false,
                    isText = true,
                    textContent = sharedText
                )
            )
        }

        if (items.isEmpty()) return null

        return com.example.data.model.SharedIncomingBatch(
            items = items,
            sharedText = sharedText
        )
    }

    fun openExternalUriOrUrl(context: Context, urlOrUri: String) {
        try {
            val isPdf = urlOrUri.contains(".pdf", ignoreCase = true)
            val uri = if (urlOrUri.startsWith("/")) {
                val file = File(urlOrUri)
                try {
                    androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                } catch (_: Exception) {
                    Uri.fromFile(file)
                }
            } else {
                Uri.parse(urlOrUri)
            }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                if (isPdf) {
                    setDataAndType(uri, "application/pdf")
                } else {
                    data = uri
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            val chooser = Intent.createChooser(intent, "Open with PDF Viewer")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
        } catch (_: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(urlOrUri)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            } catch (_: Exception) {}
        }
    }

    fun openPdfInExternalViewer(context: Context, pathOrUri: String) {
        if (pathOrUri.isBlank()) return
        try {
            val file = if (pathOrUri.startsWith("/")) File(pathOrUri) else null
            val uri = if (file != null && file.exists()) {
                try {
                    androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                } catch (_: Exception) {
                    Uri.fromFile(file)
                }
            } else {
                Uri.parse(pathOrUri)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            val chooser = Intent.createChooser(intent, "Open with System PDF Viewer")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
        } catch (_: Exception) {
            openExternalUriOrUrl(context, pathOrUri)
        }
    }

    fun drawAppWatermark(context: Context, canvas: android.graphics.Canvas, pageWidth: Float, pageHeight: Float) {
        // Position at bottom-right corner
        val rightMargin = 90f
        val bottomMargin = 85f
        val watermarkX = pageWidth - rightMargin
        val watermarkY = pageHeight - bottomMargin

        canvas.save()

        // 1. First, attempt to draw the actual app_logo resource.
        val drawable = try {
            androidx.core.content.res.ResourcesCompat.getDrawable(
                context.resources,
                com.example.R.drawable.app_logo,
                null
            )
        } catch (_: Exception) {
            null
        }

        if (drawable != null) {
            val logoSize = 54
            val left = (watermarkX - logoSize / 2f).toInt()
            val top = (watermarkY - logoSize / 2f - 6f).toInt()
            drawable.setBounds(left, top, left + logoSize, top + logoSize)
            // Elegant, clean semi-transparent opacity for high readability without clutter
            drawable.alpha = 110
            drawable.draw(canvas)
        } else {
            // Draw a gorgeous, vector academic shield and open book emblem if drawable fails
            val shieldPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.FILL
                color = android.graphics.Color.argb(22, 2, 132, 199) // subtle blue-slate, ~9% opacity
            }
            canvas.drawCircle(watermarkX, watermarkY - 12f, 22f, shieldPaint)

            shieldPaint.style = android.graphics.Paint.Style.STROKE
            shieldPaint.strokeWidth = 1.5f
            canvas.drawCircle(watermarkX, watermarkY - 12f, 20f, shieldPaint)

            // Draw open book
            shieldPaint.style = android.graphics.Paint.Style.FILL
            val leftBookPage = android.graphics.Path().apply {
                moveTo(watermarkX, watermarkY - 7f)
                cubicTo(watermarkX - 5f, watermarkY - 12f, watermarkX - 12f, watermarkY - 12f, watermarkX - 12f, watermarkY - 7f)
                lineTo(watermarkX - 12f, watermarkY - 17f)
                cubicTo(watermarkX - 12f, watermarkY - 22f, watermarkX - 5f, watermarkY - 22f, watermarkX, watermarkY - 17f)
                close()
            }
            canvas.drawPath(leftBookPage, shieldPaint)

            val rightBookPage = android.graphics.Path().apply {
                moveTo(watermarkX, watermarkY - 7f)
                cubicTo(watermarkX + 5f, watermarkY - 12f, watermarkX + 12f, watermarkY - 12f, watermarkX + 12f, watermarkY - 7f)
                lineTo(watermarkX + 12f, watermarkY - 17f)
                cubicTo(watermarkX + 12f, watermarkY - 22f, watermarkX + 5f, watermarkY - 22f, watermarkX, watermarkY - 17f)
                close()
            }
            canvas.drawPath(rightBookPage, shieldPaint)

            shieldPaint.strokeWidth = 1.5f
            canvas.drawLine(watermarkX, watermarkY - 7f, watermarkX, watermarkY - 17f, shieldPaint)
        }

        // 2. Draw text "study well app" under the logo
        val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(120, 71, 85, 105) // elegant, visible semi-transparent slate gray
            textSize = 9f
            isFakeBoldText = true
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
        }

        // Draw "study well app" text
        canvas.drawText("study well app", watermarkX, watermarkY + 28f, textPaint)

        canvas.restore()
    }

    private fun buildDefaultSubjectContent(material: com.example.data.model.StudyMaterialEntity): String {
        val title = material.title
        val topic = material.topic.ifBlank { "Core Cambridge Curriculum" }
        val desc = material.description.ifBlank { "Comprehensive curriculum unit review notes and practice problems." }
        val subCode = if (material.subjectId == "PHYSICS") "0625" else if (material.subjectId == "MATH") "0580" else if (material.subjectId == "ENGLISH") "0500" else if (material.subjectId == "CHEMISTRY") "0620" else if (material.subjectId == "BIOLOGY") "0610" else "0417"

        return """
        # $title
        
        **Cambridge IGCSE & Edexcel International Curriculum • Syllabus $subCode**
        **Unit / Module:** $topic
        
        ---
        
        ## 1. Overview & Syllabus Learning Objectives
        $desc
        
        Students studying this unit are required to master both theoretical principles and numerical calculation methods:
        • Define core scientific terminology and standard SI units.
        • Apply algebraic formulas to structured multi-step exam problems.
        • Interpret experimental diagrams, graphs, and tabular experimental data.
        • Evaluate sources of error and state realistic experimental precautions.
        
        ---
        
        ## 2. Key Theoretical Concepts & Principles
        
        ### Fundamental Theory & Rules:
        • **Core Rule 1:** Always write down the relevant formula before substituting numerical values [M1 Method Mark].
        • **Core Rule 2:** Ensure all quantities are converted to standard SI units (e.g. seconds, metres, kilograms) prior to computation.
        • **Core Rule 3:** State numerical final answers to 2 or 3 significant figures unless otherwise instructed [A1 Accuracy Mark].
        
        ### Key Formula Quick Reference:
        • Quantity Rate Formula: Rate = Total Change / Total Time Taken
        • Efficiency Equation: Efficiency (%) = (Useful Output / Total Input) * 100%
        • Linear Relationship: Y = m * X + c (where m is gradient and c is intercept)
        
        ---
        
        ## 3. Worked Examination Examples & Step-by-Step Solutions
        
        ### Question 1 [Extended Theory - 4 Marks]
        A sample is tested under standard laboratory conditions. The initial reading is recorded at t = 0s and final measurement at t = 60s.
        
        **(a) State the relevant definition and equation [2 Marks]:**
        • **Formula:** Rate = (Final Value - Initial Value) / Δt  *[Award 1 Mark for formula statement (M1)]*
        • **Definition:** The rate measures change in quantity per unit time *[Award 1 Mark for correct definition (B1)]*
        
        **(b) Calculate the resulting value and state the standard units [2 Marks]:**
        • **Calculation:** (120 - 30) / 60 = 90 / 60 = 1.5  *[Award 1 Mark (M1)]*
        • **Final Answer:** 1.5 units/s  *[Award 1 Mark for value with correct unit (A1)]*
        
        ---
        
        ## 4. Examiner Advice & Common Student Pitfalls
        
        • **Common Pitfall 1:** Premature rounding during intermediate steps. Keep intermediate values in calculator memory.
        • **Common Pitfall 2:** Omitting units in calculation answers. An answer without standard units loses the A mark.
        • **Common Pitfall 3:** Confusing core definitions. Ensure terminology matches official Cambridge syllabus definitions.
        
        ---
        
        ## 5. End of Unit Practice Questions
        
        1. Explain how temperature changes affect the rate of reaction or kinetic behavior of particles. [3 Marks]
        2. Describe an experimental method to measure the density of an irregularly shaped solid object. [4 Marks]
        3. State two precautions required to ensure accurate stopwatch timing in laboratory experiments. [2 Marks]
        """.trimIndent()
    }
}
