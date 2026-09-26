package com.example.utils

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.ui.player.HandStroke
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

object AnnotationPersistenceManager {
    private const val PREF_NAME = "study_pdf_annotations_pref"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun saveStrokes(context: Context, docKey: String, pageStrokes: Map<Int, List<HandStroke>>) {
        if (docKey.isBlank()) return
        val snapshotMap = pageStrokes.mapValues { entry -> entry.value.toList() }
        val appContext = context.applicationContext
        scope.launch {
            try {
                val pref = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                val rootArray = JSONArray()

                snapshotMap.forEach { (pageIdx, strokes) ->
                    strokes.forEach { stroke ->
                        val strokeObj = JSONObject()
                        strokeObj.put("pageIndex", pageIdx)
                        strokeObj.put("color", stroke.color.toArgb())
                        strokeObj.put("strokeWidth", stroke.strokeWidth.toDouble())
                        strokeObj.put("isHighlighter", stroke.isHighlighter)
                        strokeObj.put("canvasWidth", stroke.canvasWidth.toDouble())
                        strokeObj.put("canvasHeight", stroke.canvasHeight.toDouble())

                        val pointsArray = JSONArray()
                        stroke.points.forEach { pt ->
                            val ptObj = JSONObject()
                            ptObj.put("x", pt.x.toDouble())
                            ptObj.put("y", pt.y.toDouble())
                            pointsArray.put(ptObj)
                        }
                        strokeObj.put("points", pointsArray)
                        rootArray.put(strokeObj)
                    }
                }

                val sanitized = sanitizeKey(docKey)
                pref.edit().putString(sanitized, rootArray.toString()).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadStrokes(context: Context, docKey: String): Map<Int, List<HandStroke>> {
        val resultMap = mutableMapOf<Int, MutableList<HandStroke>>()
        try {
            if (docKey.isBlank()) return resultMap
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val sanitized = sanitizeKey(docKey)
            val jsonStr = pref.getString(sanitized, null) ?: return resultMap
            val rootArray = JSONArray(jsonStr)

            for (i in 0 until rootArray.length()) {
                val strokeObj = rootArray.getJSONObject(i)
                val pageIdx = strokeObj.getInt("pageIndex")
                val colorInt = strokeObj.getInt("color")
                val width = strokeObj.getDouble("strokeWidth").toFloat()
                val isHighlighter = strokeObj.optBoolean("isHighlighter", false)
                val canvasW = strokeObj.optDouble("canvasWidth", 850.0).toFloat()
                val canvasH = strokeObj.optDouble("canvasHeight", 1200.0).toFloat()

                val pointsArray = strokeObj.getJSONArray("points")
                val points = mutableListOf<Offset>()
                for (j in 0 until pointsArray.length()) {
                    val ptObj = pointsArray.getJSONObject(j)
                    val x = ptObj.getDouble("x").toFloat()
                    val y = ptObj.getDouble("y").toFloat()
                    points.add(Offset(x, y))
                }

                if (points.isNotEmpty()) {
                    val stroke = HandStroke(
                        points = points,
                        color = Color(colorInt),
                        strokeWidth = width,
                        isHighlighter = isHighlighter,
                        canvasWidth = canvasW,
                        canvasHeight = canvasH
                    )
                    resultMap.getOrPut(pageIdx) { mutableListOf() }.add(stroke)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultMap
    }

    private fun sanitizeKey(key: String): String {
        return key.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    }
}
