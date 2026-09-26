package com.example.network

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ExamBoard
import com.example.data.model.SubjectEnum
import com.example.data.repository.RealExamQuestion
import com.example.data.repository.RealPastPaper
import com.example.data.repository.RealPastPaperRepository
import com.example.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class RealExamSearchService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    suspend fun searchOnlineRealExams(
        context: Context,
        query: String,
        subject: SubjectEnum,
        examBoard: ExamBoard = ExamBoard.CAMBRIDGE,
        year: String = "2024",
        session: String = "May/June"
    ): List<RealPastPaper> = withContext(Dispatchers.IO) {
        val isOnline = NetworkUtils.isNetworkAvailable(context)

        // Local search across repository
        val localMatches = RealPastPaperRepository.allPapers.filter { paper ->
            (paper.subjectId == subject.id || query.contains(paper.subjectId, ignoreCase = true)) &&
            (query.isBlank() || paper.paperTitle.contains(query, ignoreCase = true) ||
             paper.paperCode.contains(query, ignoreCase = true) ||
             paper.year.contains(query) ||
             paper.fullPaperContent.contains(query, ignoreCase = true))
        }

        if (!isOnline) {
            Log.d("RealExamSearchService", "Device is offline. Returning ${localMatches.size} local repository papers.")
            return@withContext localMatches
        }

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d("RealExamSearchService", "No online API key. Returning local matches.")
            return@withContext localMatches
        }

        val boardSpec = when (examBoard) {
            ExamBoard.CAMBRIDGE -> subject.syllabusCode
            ExamBoard.EDEXCEL -> "Edexcel International GCSE (9-1)"
            ExamBoard.OXFORD_AQA -> "Oxford AQA International"
        }

        val prompt = """
            You are an Official Online Examination Search Engine for Cambridge IGCSE, Pearson Edexcel, and Oxford AQA.
            Search query: "$query"
            Target Subject: ${subject.title} (Syllabus: $boardSpec)
            Exam Board: ${examBoard.displayName}
            Series / Year: $year ($session Series)
            
            Retrieve and output 1 authentic real past paper with 5 real exam questions, official examiner mark schemes, paper codes (e.g. 0625/42 or 0580/22), and complete mark allocations.
            
            Return ONLY a JSON array with 1 object formatted exactly as:
            [
              {
                "id": "ONLINE_${examBoard.name}_${subject.id}_${year}_${session.replace("/", "_")}",
                "subjectId": "${subject.id}",
                "examBoard": "${examBoard.name}",
                "year": "$year",
                "session": "$session",
                "paperCode": "${subject.syllabusCode}/22",
                "paperTitle": "${examBoard.displayName} ${subject.title} Paper 22 ($session $year)",
                "duration": "45 minutes",
                "maxMarks": 40,
                "syllabusCode": "$boardSpec",
                "instructions": "Answer all questions. Use a black or dark blue pen. You may use a calculator. Marks are shown in brackets [ ].",
                "questions": [
                  {
                    "questionNumber": 1,
                    "subPart": "(a)",
                    "questionText": "A car accelerates uniformly from rest at 3.0 m/s² for 5.0 s. Calculate the final velocity.",
                    "marks": 2,
                    "markSchemeAnswer": "Formula: v = u + at -> v = 0 + (3.0 * 5.0) = 15 m/s. [2 marks]",
                    "options": ["10 m/s", "15 m/s", "20 m/s", "25 m/s"],
                    "correctOptionIndex": 1,
                    "syllabusTopic": "Kinematics & Acceleration",
                    "examinerNotes": "Ensure units are stated as m/s."
                  }
                ],
                "fullPaperContent": "Official ${examBoard.displayName} $year Paper...",
                "markSchemeContent": "Q1: 15 m/s [2 marks]\nQ2: ..."
              }
            ]
        """.trimIndent()

        try {
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$baseUrl?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext localMatches
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val content = candidates?.optJSONObject(0)?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            val fetchedPapers = parseOnlinePaperJson(rawText, subject, examBoard, year, session)
            val combined = (fetchedPapers + localMatches).distinctBy { it.id }
            if (combined.isNotEmpty()) combined else localMatches
        } catch (e: Exception) {
            Log.e("RealExamSearchService", "Error searching online exams: ${e.message}", e)
            localMatches
        }
    }

    private fun parseOnlinePaperJson(
        rawText: String,
        subject: SubjectEnum,
        defaultBoard: ExamBoard,
        defaultYear: String,
        defaultSession: String
    ): List<RealPastPaper> {
        try {
            var cleanText = rawText.trim()
            if (cleanText.startsWith("```json")) cleanText = cleanText.removePrefix("```json").trim()
            if (cleanText.startsWith("```")) cleanText = cleanText.removePrefix("```").trim()
            if (cleanText.endsWith("```")) cleanText = cleanText.removeSuffix("```").trim()

            val array = JSONArray(cleanText)
            val result = mutableListOf<RealPastPaper>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", "ONLINE_${System.currentTimeMillis()}_$i")
                val subjId = obj.optString("subjectId", subject.id)
                val boardName = obj.optString("examBoard", defaultBoard.name)
                val boardEnum = try { ExamBoard.valueOf(boardName) } catch (_: Exception) { defaultBoard }
                val yearStr = obj.optString("year", defaultYear)
                val sessionStr = obj.optString("session", defaultSession)
                val paperCodeStr = obj.optString("paperCode", "${subject.syllabusCode}/22")
                val paperTitleStr = obj.optString("paperTitle", "${boardEnum.displayName} ${subject.title} Real Exam Paper")
                val durationStr = obj.optString("duration", "45 minutes")
                val maxMarksInt = obj.optInt("maxMarks", 40)
                val syllabusCodeStr = obj.optString("syllabusCode", subject.syllabusCode)
                val instructionsStr = obj.optString("instructions", "Answer all questions.")
                val fullContent = obj.optString("fullPaperContent", "Official $boardName Exam Paper")
                val markSchemeStr = obj.optString("markSchemeContent", "Official Examiner Mark Scheme")

                val qArray = obj.optJSONArray("questions")
                val questionsList = mutableListOf<RealExamQuestion>()
                if (qArray != null) {
                    for (qIdx in 0 until qArray.length()) {
                        val qObj = qArray.getJSONObject(qIdx)
                        val qNum = qObj.optInt("questionNumber", qIdx + 1)
                        val subP = qObj.optString("subPart", "")
                        val qText = qObj.optString("questionText", "Question text")
                        val marksInt = qObj.optInt("marks", 1)
                        val msAns = qObj.optString("markSchemeAnswer", "Correct Answer")
                        val optionsArr = qObj.optJSONArray("options")
                        val optsList = mutableListOf<String>()
                        if (optionsArr != null) {
                            for (oIdx in 0 until optionsArr.length()) {
                                optsList.add(optionsArr.getString(oIdx))
                            }
                        }
                        val correctIdx = qObj.optInt("correctOptionIndex", 0)
                        val topicStr = qObj.optString("syllabusTopic", subject.title)
                        val notesStr = qObj.optString("examinerNotes", "")

                        questionsList.add(
                            RealExamQuestion(
                                questionNumber = qNum,
                                subPart = subP,
                                questionText = qText,
                                marks = marksInt,
                                markSchemeAnswer = msAns,
                                options = optsList,
                                correctOptionIndex = correctIdx,
                                syllabusTopic = topicStr,
                                examinerNotes = notesStr
                            )
                        )
                    }
                }

                result.add(
                    RealPastPaper(
                        id = id,
                        subjectId = subjId,
                        examBoard = boardEnum,
                        year = yearStr,
                        session = sessionStr,
                        paperCode = paperCodeStr,
                        paperTitle = paperTitleStr,
                        duration = durationStr,
                        maxMarks = maxMarksInt,
                        syllabusCode = syllabusCodeStr,
                        instructions = instructionsStr,
                        questions = questionsList,
                        fullPaperContent = fullContent,
                        markSchemeContent = markSchemeStr,
                        officialWebUrl = "https://www.cambridgeinternational.org"
                    )
                )
            }
            return result
        } catch (e: Exception) {
            Log.w("RealExamSearchService", "Failed to parse online paper JSON: ${e.message}")
            return emptyList()
        }
    }
}
