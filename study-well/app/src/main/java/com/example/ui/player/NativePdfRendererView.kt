package com.example.ui.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun NativePdfRendererView(
    pdfFile: File,
    readerTheme: ReaderTheme,
    modifier: Modifier = Modifier,
    targetPage: Int = 0,
    onPageChanged: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val adapter = remember(pdfFile, readerTheme) {
        NativePdfAdapter(context, pdfFile, readerTheme, coroutineScope)
    }

    DisposableEffect(adapter) {
        onDispose {
            adapter.close()
        }
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    var isZooming = false
                    do {
                        val event = awaitPointerEvent()
                        val changes = event.changes
                        
                        // Detect pinch gesture (two or more fingers)
                        if (changes.size > 1) {
                            isZooming = true
                        }
                        
                        // If we are actively zooming, or if we are already zoomed in (>1f),
                        // consume touch events to perform custom scale and pan
                        if (isZooming || scale > 1.05f) {
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()
                            
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            if (scale > 1f) {
                                offsetX += pan.x * scale
                                offsetY += pan.y * scale
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                            
                            // Consume the touch changes to prevent them from scrolling the RecyclerView underneath
                            changes.forEach { it.consume() }
                        }
                    } while (changes.any { it.pressed })
                }
            }
    ) {
        AndroidView<RecyclerView>(
            factory = { ctx ->
                RecyclerView(ctx).apply {
                    layoutManager = LinearLayoutManager(ctx)
                    this.adapter = adapter
                    
                    addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            val lm = recyclerView.layoutManager as? LinearLayoutManager
                            val firstVisible = lm?.findFirstVisibleItemPosition() ?: 0
                            if (firstVisible >= 0) {
                                onPageChanged(firstVisible)
                            }
                        }
                    })
                }
            },
            update = { recyclerView ->
                if (recyclerView.adapter != adapter) {
                    recyclerView.adapter = adapter
                }
                val lm = recyclerView.layoutManager as? LinearLayoutManager
                if (lm != null) {
                    val currentVisible = lm.findFirstVisibleItemPosition()
                    if (currentVisible != targetPage && targetPage >= 0 && targetPage < adapter.itemCount) {
                        recyclerView.scrollToPosition(targetPage)
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    
                    // Bound translations based on the zoomed scale to prevent paging completely off-screen
                    val maxTx = (size.width * (scale - 1f)) / 2f
                    val maxTy = (size.height * (scale - 1f)) / 2f
                    
                    translationX = offsetX.coerceIn(-maxTx, maxTx)
                    translationY = offsetY.coerceIn(-maxTy, maxTy)
                    
                    offsetX = offsetX.coerceIn(-maxTx, maxTx)
                    offsetY = offsetY.coerceIn(-maxTy, maxTy)
                }
        )

        // Floating Zoom Controls Panel
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 32.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloatingActionButton(
                onClick = {
                    scale = (scale + 0.25f).coerceAtMost(4f)
                },
                containerColor = android.graphics.Color.parseColor("#1E293B").let { androidx.compose.ui.graphics.Color(it) }.copy(alpha = 0.85f),
                contentColor = androidx.compose.ui.graphics.Color.White,
                shape = CircleShape,
                modifier = Modifier.size(44.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            FloatingActionButton(
                onClick = {
                    scale = (scale - 0.25f).coerceAtLeast(1f)
                    if (scale == 1f) {
                        offsetX = 0f
                        offsetY = 0f
                    }
                },
                containerColor = android.graphics.Color.parseColor("#1E293B").let { androidx.compose.ui.graphics.Color(it) }.copy(alpha = 0.85f),
                contentColor = androidx.compose.ui.graphics.Color.White,
                shape = CircleShape,
                modifier = Modifier.size(44.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    modifier = Modifier.size(22.dp)
                )
            }

            if (scale > 1.05f) {
                Spacer(modifier = Modifier.height(10.dp))
                
                FloatingActionButton(
                    onClick = {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    },
                    containerColor = android.graphics.Color.parseColor("#E2E8F0").let { androidx.compose.ui.graphics.Color(it) }.copy(alpha = 0.95f),
                    contentColor = android.graphics.Color.parseColor("#1E293B").let { androidx.compose.ui.graphics.Color(it) },
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp),
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Zoom",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

class NativePdfAdapter(
    private val context: Context,
    private val file: File,
    private val readerTheme: ReaderTheme,
    private val scope: CoroutineScope
) : RecyclerView.Adapter<NativePdfAdapter.PageViewHolder>() {

    private var pdfRenderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var pageCount = 0
    private val activeJobs = mutableMapOf<Int, Job>()

    init {
        try {
            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            fileDescriptor?.let {
                pdfRenderer = PdfRenderer(it)
                pageCount = pdfRenderer?.pageCount ?: 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val imageView = ImageView(context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 16)
            }
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.WHITE)
            elevation = 4f
        }
        return PageViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val imageView = holder.itemView as ImageView
        
        activeJobs[position]?.cancel()
        imageView.setImageBitmap(null)
        
        val job = scope.launch {
            val renderer = pdfRenderer ?: return@launch
            val bmp = withContext(Dispatchers.IO) {
                try {
                    synchronized(renderer) {
                        if (position >= 0 && position < renderer.pageCount) {
                            renderer.openPage(position).use { page ->
                                val displayWidth = context.resources.displayMetrics.widthPixels
                                val targetWidth = (displayWidth - 64).coerceAtLeast(300)
                                val ratio = page.height.toFloat() / page.width.toFloat()
                                val targetHeight = (targetWidth * ratio).toInt()

                                val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(bitmap)
                                canvas.drawColor(Color.WHITE)
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                bitmap
                            }
                        } else null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            
            if (bmp != null) {
                imageView.setImageBitmap(bmp)
                
                when (readerTheme) {
                    ReaderTheme.DARK -> {
                        val matrix = ColorMatrix(floatArrayOf(
                            -1f, 0f, 0f, 0f, 255f,
                            0f, -1f, 0f, 0f, 255f,
                            0f, 0f, -1f, 0f, 255f,
                            0f, 0f, 0f, 1f, 0f
                        ))
                        imageView.colorFilter = ColorMatrixColorFilter(matrix)
                    }
                    ReaderTheme.SEPIA -> {
                        val matrix = ColorMatrix(floatArrayOf(
                            0.90f, 0.05f, 0.05f, 0f, 0f,
                            0.05f, 0.85f, 0.05f, 0f, 0f,
                            0.05f, 0.05f, 0.70f, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        ))
                        imageView.colorFilter = ColorMatrixColorFilter(matrix)
                    }
                    else -> {
                        imageView.clearColorFilter()
                    }
                }
            }
        }
        activeJobs[position] = job
    }

    override fun onViewRecycled(holder: PageViewHolder) {
        super.onViewRecycled(holder)
        val position = holder.bindingAdapterPosition
        activeJobs[position]?.cancel()
        activeJobs.remove(position)
        val imageView = holder.itemView as ImageView
        imageView.setImageBitmap(null)
    }

    override fun getItemCount(): Int = pageCount

    fun close() {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
        try {
            pdfRenderer?.close()
            fileDescriptor?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
