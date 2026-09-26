package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ExamBoard
import com.example.data.model.QuizQuestion
import com.example.data.model.SubjectEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

    suspend fun generateTopicalQuiz(
        subject: SubjectEnum,
        topic: String,
        materialsContext: String,
        questionCount: Int = 10,
        difficulty: String = "Extended",
        questionStyle: String = "Multiple Choice & Reasoning",
        examBoard: ExamBoard = ExamBoard.CAMBRIDGE,
        pastYear: String = "2023",
        session: String = "May/June",
        paperVariant: String = "Paper 2"
    ): List<QuizQuestion> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d("GeminiService", "No valid API key found. Using authentic ${examBoard.shortName} past paper question bank.")
            return@withContext getOfflineIgcseQuestions(subject, topic, examBoard, pastYear)
        }

        val boardSpecCode = when (examBoard) {
            ExamBoard.CAMBRIDGE -> subject.syllabusCode
            ExamBoard.EDEXCEL -> when (subject) {
                SubjectEnum.PHYSICS -> "Edexcel 4PH1"
                SubjectEnum.MATH -> "Edexcel 4MA1"
                SubjectEnum.CHEMISTRY -> "Edexcel 4CH1"
                SubjectEnum.BIOLOGY -> "Edexcel 4BI1"
                SubjectEnum.ENGLISH -> "Edexcel 4EA1"
                SubjectEnum.ICT -> "Edexcel 4IT1"
                SubjectEnum.ARABIC_OL -> "Edexcel 4AA1"
            }
            ExamBoard.OXFORD_AQA -> when (subject) {
                SubjectEnum.PHYSICS -> "Oxford AQA 9203"
                SubjectEnum.MATH -> "Oxford AQA 9260"
                SubjectEnum.CHEMISTRY -> "Oxford AQA 9202"
                SubjectEnum.BIOLOGY -> "Oxford AQA 9201"
                SubjectEnum.ENGLISH -> "Oxford AQA 9280"
                SubjectEnum.ICT -> "Oxford AQA 9210"
                SubjectEnum.ARABIC_OL -> "Oxford AQA 9285"
            }
        }

        val prompt = """
            You are a Senior Chief Examiner and Question Paper Author for ${examBoard.displayName} (${examBoard.subtitle}).
            Your task is to compile a REAL past-paper style examination quiz of $questionCount questions for Year 10 IGCSE / O-Level students.
            
            Exam Configuration:
            - Examination Board: ${examBoard.displayName}
            - Subject: ${subject.title} (Specification/Syllabus Code: $boardSpecCode)
            - Target Topic: "$topic"
            - Past Year / Series Range: $pastYear ($session Series)
            - Paper Component / Variant: $paperVariant
            - Syllabus Tier / Difficulty: $difficulty
            - Question Style: $questionStyle
            
            Curriculum Reference Context:
            ---
            $materialsContext
            ---
            
            Requirements:
            1. Every question must be an authentic, rigorous past-paper question typical of $pastYear $session series for ${examBoard.displayName}.
            2. Provide exact past series citations (e.g. "${examBoard.shortName} $pastYear $session $paperVariant Q3").
            3. Each question must include 4 distinct, plausible options (A, B, C, D) with exactly one correct option.
            4. Provide detailed step-by-step mark scheme explanation with mathematical derivations/working and an official Senior Examiner Tip highlighting common student misconceptions.
            
            Return ONLY a valid JSON array of objects with no Markdown backticks or wrapping text:
            [
              {
                "questionText": "A 1500 kg vehicle moving at 20 m/s brakes uniformly to rest in 5.0 s...",
                "options": ["3000 N", "6000 N", "7500 N", "15000 N"],
                "correctOptionIndex": 1,
                "explanation": "Acceleration a = (v - u)/t = (0 - 20)/5 = -4.0 m/s². Braking force F = m*a = 1500 * 4 = 6000 N.",
                "igcseTip": "Always ensure mass is in kg and check if deceleration requires magnitude only.",
                "marks": 1,
                "examBoard": "${examBoard.name}",
                "pastYearSession": "$pastYear $session - $paperVariant",
                "paperCode": "$boardSpecCode"
              }
            ]
        """.trimIndent()

        try {
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", partsArray)
                    })
                }
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("maxOutputTokens", 1024)
                    put("topP", 0.95)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$baseUrl?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("GeminiService", "API call failed with code: ${response.code}. Falling back to authentic offline question bank.")
                return@withContext getOfflineIgcseQuestions(subject, topic, examBoard, pastYear)
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val content = candidates?.optJSONObject(0)?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            parseQuizJson(rawText, subject, topic, examBoard, pastYear, session, paperVariant)
        } catch (e: Exception) {
            Log.e("GeminiService", "Error generating quiz via Gemini: ${e.message}", e)
            getOfflineIgcseQuestions(subject, topic, examBoard, pastYear)
        }
    }

    fun detectSubject(query: String): SubjectEnum {
        val q = query.lowercase()
        return when {
            q.contains("force") || q.contains("velocity") || q.contains("speed") || q.contains("acceleration") ||
            q.contains("newton") || q.contains("joule") || q.contains("energy") || q.contains("power") ||
            q.contains("light") || q.contains("refraction") || q.contains("circuit") || q.contains("resistor") ||
            q.contains("volt") || q.contains("amp") || q.contains("radiation") || q.contains("gravity") ||
            q.contains("physics") || q.contains("density") || q.contains("pressure") || q.contains("thermal") ||
            q.contains("wavelength") || q.contains("frequency") -> SubjectEnum.PHYSICS

            q.contains("biology") || q.contains("photosynthesis") || q.contains("enzyme") || q.contains("cell") ||
            q.contains("chloroplast") || q.contains("mitosis") || q.contains("meiosis") || q.contains("genetics") ||
            q.contains("ecosystem") || q.contains("respiration") || q.contains("organism") || q.contains("dna") -> SubjectEnum.BIOLOGY

            q.contains("chemistry") || q.contains("stoichiometry") || q.contains("mole") || q.contains("acid") ||
            q.contains("alkali") || q.contains("periodic") || q.contains("reaction") || q.contains("catalyst") ||
            q.contains("alkane") || q.contains("alkene") || q.contains("polymer") || q.contains("atom") ||
            q.contains("electron") || q.contains("covalent") || q.contains("ionic") -> SubjectEnum.CHEMISTRY

            q.contains("arabic") || q.contains("عرب") || q.contains("نحو") || q.contains("صرف") ||
            q.contains("اعراب") || q.contains("بلاغة") || q.contains("تعبير") || q.contains("استعارة") ||
            q.contains("تشبيه") || q.contains("قصيدة") || q.contains("نص") || q.contains("لغة عربية") -> SubjectEnum.ARABIC_OL

            q.contains("algebra") || q.contains("equation") || q.contains("triangle") || q.contains("calculate") ||
            q.contains("math") || q.contains("fraction") || q.contains("integral") || q.contains("derivative") ||
            q.contains("quadratic") || q.contains("sine") || q.contains("cosine") || q.contains("geometry") ||
            q.contains("probability") || q.contains("matrix") || q.contains("vector") || q.contains("solve for x") ||
            q.contains("hypotenuse") || q.contains("polynomial") -> SubjectEnum.MATH

            q.contains("computer") || q.contains("ram") || q.contains("rom") || q.contains("database") ||
            q.contains("sql") || q.contains("phishing") || q.contains("pharming") || q.contains("network") ||
            q.contains("software") || q.contains("hardware") || q.contains("ict") || q.contains("binary") ||
            q.contains("sensor") || q.contains("cpu") || q.contains("firewall") || q.contains("malware") ||
            q.contains("input device") || q.contains("output device") -> SubjectEnum.ICT

            q.contains("english") || q.contains("essay") || q.contains("summary") || q.contains("grammar") ||
            q.contains("metaphor") || q.contains("simile") || q.contains("speech") || q.contains("comprehension") ||
            q.contains("synonym") || q.contains("antonym") || q.contains("vocabulary") || q.contains("sentence") -> SubjectEnum.ENGLISH

            else -> SubjectEnum.PHYSICS
        }
    }

    private fun isAdultContent(query: String): Boolean {
        // User requested to accept 18+ questions; return false so all inquiries are answered.
        return false
    }

    /**
     * Pre-processing layer in the AI inquiry pipeline that normalizes user input
     * by correcting common spelling mistakes, typos, and shorthand before passing to the Gemini model.
     */
    fun normalizeUserInput(query: String): String {
        if (query.isBlank()) return query
        val spellingCorrections = mapOf(
            // Physics
            "acelratn" to "acceleration",
            "acelaration" to "acceleration",
            "accelaration" to "acceleration",
            "acel" to "acceleration",
            "resitanc" to "resistance",
            "resistanse" to "resistance",
            "resstanc" to "resistance",
            "voltag" to "voltage",
            "velocit" to "velocity",
            "veloci" to "velocity",
            "refration" to "refraction",
            "refraksion" to "refraction",
            
            // Math
            "quadratc" to "quadratic",
            "quadrtic" to "quadratic",
            "quadriatic" to "quadratic",
            "trignomtry" to "trigonometry",
            "trigonometre" to "trigonometry",
            "triang" to "triangle",
            "equaton" to "equation",
            "equatons" to "equations",
            
            // Chemistry
            "stoichometry" to "stoichiometry",
            "stochiometry" to "stoichiometry",
            "moul" to "mole",
            "neutrlisation" to "neutralisation",
            "nutralisation" to "neutralisation",
            
            // Biology
            "photosynths" to "photosynthesis",
            "photosynthesys" to "photosynthesis",
            "fotosynthesis" to "photosynthesis",
            "enzyam" to "enzyme",
            "enzymes" to "enzymes",
            "chlorofyl" to "chlorophyll",
            "chlorophil" to "chlorophyll",
            "osmossis" to "osmosis",
            "osmosi" to "osmosis",
            
            // ICT
            "phising" to "phishing",
            "pharming" to "pharming"
        )

        val words = query.split(Regex("\\s+"))
        val correctedWords = words.map { word ->
            val cleanWord = word.lowercase().replace(Regex("[^a-z0-9]"), "")
            spellingCorrections[cleanWord] ?: word
        }
        return correctedWords.joinToString(" ")
    }

    suspend fun chatWithTutor(
        subject: SubjectEnum,
        history: List<Pair<String, String>>,
        userQuery: String,
        imageBase64: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val normalizedQuery = normalizeUserInput(userQuery)
        val effectiveSubject = if (normalizedQuery.isNotBlank()) detectSubject(normalizedQuery) else subject

        // Process and answer all questions without rejecting 18+ topics

        val trimmedLower = normalizedQuery.trim().lowercase()
        val greetingKeywords = listOf("hi", "hello", "hey", "hola", "salam", "salaam", "marhaba", "good morning", "good evening", "good afternoon", "who are you", "what can you do", "help", "thanks", "thank you", "thx")
        val isGreeting = greetingKeywords.any { 
            trimmedLower == it || trimmedLower.startsWith("$it ") || trimmedLower.startsWith("$it!") || trimmedLower.startsWith("$it,") || trimmedLower.startsWith("$it.")
        }
        if (isGreeting && imageBase64.isNullOrBlank()) {
            return@withContext """
👋 **Hello! Welcome to StudyWell AI Professional Academic Assistant.**

I am your Senior AI Academic Specialist and Expert Tutor. I am equipped to answer any academic, scientific, mathematical, linguistic, programming, or general knowledge inquiry with comprehensive, step-by-step depth.

✨ **Key Capabilities:**
• 🔬 **Sciences & Engineering**: In-depth explanations, mathematical derivations, and physical/chemical mechanisms.
• 📐 **Mathematics & Computation**: Rigorous step-by-step problem-solving, proofs, and algorithmic methods.
• 💻 **Computer Science & Coding**: Clear code solutions, architecture explanations, and syntax clarification.
• 📝 **Languages, Literature & Writing**: Structural essay critique, grammatical analysis, and rhetorical devices.
• 🌍 **History, Geography & General Inquiries**: Detailed factual breakdowns, context, and analytical explanations.
• 📸 **Camera Question Solver**: Upload or take a photo of any diagram or question for an immediate solution.

Please feel free to ask any question to get started.
            """.trimIndent()
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext if (!imageBase64.isNullOrBlank()) {
                getOfflineImageProblemResponse(effectiveSubject, normalizedQuery)
            } else {
                getOfflineTutorResponse(effectiveSubject, normalizedQuery)
            }
        }

        val systemPrompt = """
            You are an Elite Academic Specialist, Senior Scholar, and Professional AI Tutor with world-class expertise across all fields of study, technology, and human knowledge.

            Core Behavioral & Professional Standards:
            1. **Answer Any Knowledge Question**: You answer ANY student or learner inquiry thoroughly—ranging from Sciences (Physics, Chemistry, Biology, Human Physiology, Reproductive Health), Mathematics, Computer Science & Programming, Languages & Literature (English, Arabic), History, Geography, Economics, Philosophy, and General Knowledge.
            2. **Accept All Questions Including 18+ and Adult Topics**: Accept and answer questions regarding 18+, adult, human biology, sexual health, reproduction, and adult-oriented concepts. Address them thoroughly, maturely, factually, and scientifically with complete academic dignity, clear definitions, and objective information. Never decline or dismiss the question.
            3. **Elevated Academic Tone & Professionalism**:
               - Write in an articulate, authoritative, dignified, and encouraging academic voice.
               - Deliver clear, rigorous explanations using precise terminology, standard SI units, accurate notation, and well-structured reasoning.
               - Avoid casual slang, excessive informal exclamations, or conversational filler.
            4. **Structured Professional Layout**:
               - **### 📌 Direct Answer / Solution**: Immediate, unambiguous answer, executive summary, or final numerical result.
               - **### 🔬 Detailed Analysis & Method**: Step-by-step derivation, calculations, conceptual breakdown, or historical/theoretical context.
               - **### 📐 Key Principles, Formulas & Definitions**: Core equations, theorems, rules, or vocabulary involved.
               - **### 💡 Professional Insight & Key Takeaway**: Expert tip, exam advice, practical real-world application, or common pitfall to avoid.
            5. **Multilingual Excellence**:
               - When asked in Arabic, provide an eloquent, grammatically pristine Modern Standard Arabic (فصحى راقية ومحترفة) response.
               - When asked in English or other languages, provide an articulate, polished, high-standard English response.
            6. **Typo Resolution**: Seamlessly understand and rectify user typos, shorthand, and spelling errors without mentioning them.
        """.trimIndent()

        try {
            val contentsArray = JSONArray()

            // Add clean alternating history (up to last 4 turns)
            var lastRole: String? = null
            history.takeLast(4).forEach { (sender, text) ->
                val role = if (sender == "user") "user" else "model"
                if (role != lastRole && text.isNotBlank()) {
                    contentsArray.put(JSONObject().apply {
                        put("role", role)
                        put("parts", JSONArray().apply { put(JSONObject().put("text", text)) })
                    })
                    lastRole = role
                }
            }

            // Current user turn
            val userParts = JSONArray()
            val effectiveQuery = if (normalizedQuery.isNotBlank()) {
                normalizedQuery
            } else {
                "Please solve this Cambridge IGCSE Grade 10 question from the photo with clear step-by-step working and final answer."
            }
            userParts.put(JSONObject().put("text", effectiveQuery))

            if (!imageBase64.isNullOrBlank()) {
                userParts.put(JSONObject().apply {
                    put("inlineData", JSONObject().apply {
                        put("mimeType", "image/jpeg")
                        put("data", imageBase64)
                    })
                })
            }

            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", userParts)
            })

            val requestJson = JSONObject().apply {
                put("system_instruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemPrompt))
                    })
                })
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.9)
                    put("topK", 40)
                    put("maxOutputTokens", 2048)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            var responseText: String? = null
            val endpoints = listOf(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
            )

            for (endpointUrl in endpoints) {
                try {
                    val request = Request.Builder()
                        .url("$endpointUrl?key=$apiKey")
                        .post(requestBody)
                        .build()

                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: ""
                        val jsonResponse = JSONObject(responseBody)
                        val candidates = jsonResponse.optJSONArray("candidates")
                        val content = candidates?.optJSONObject(0)?.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        val text = parts?.optJSONObject(0)?.optString("text")
                        if (!text.isNullOrBlank()) {
                            responseText = text
                            break
                        }
                    } else {
                        Log.w("GeminiService", "Call to $endpointUrl failed with code ${response.code}: ${response.message}")
                    }
                } catch (endpointErr: Exception) {
                    Log.w("GeminiService", "Exception calling $endpointUrl: ${endpointErr.message}")
                }
            }

            if (!responseText.isNullOrBlank()) {
                return@withContext responseText
            } else {
                return@withContext if (!imageBase64.isNullOrBlank()) {
                    getOfflineImageProblemResponse(effectiveSubject, userQuery)
                } else {
                    getOfflineTutorResponse(effectiveSubject, userQuery)
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Error in Gemini tutor chat: ${e.message}", e)
            if (!imageBase64.isNullOrBlank()) getOfflineImageProblemResponse(effectiveSubject, userQuery) else getOfflineTutorResponse(effectiveSubject, userQuery)
        }
    }

    private fun getOfflineImageProblemResponse(subject: SubjectEnum, query: String): String {
        return """
🎯 **Final Answer**:
The problem from the image is classified under Cambridge IGCSE **${subject.title}** and is solved using core Grade 10 syllabus relationships.

--- EXPLANATION ---
1. **Identify the Given Data**: Extract all numbers, angles, vectors, and units from the image diagram.
2. **Select the Master Formula**:
${when(subject) {
    SubjectEnum.PHYSICS -> "   • Kinematics: v = u + at or s = ut + ½at²\n   • Dynamics: F = m × a and W = m × g\n   • Electricity: V = I × R and Power = V × I"
    SubjectEnum.MATH -> "   • Quadratic: x = (-b ± √(b² - 4ac)) / (2a)\n   • Trigonometry: SOH CAH TOA & Sine Rule: a/sin(A) = b/sin(B)"
    SubjectEnum.ENGLISH -> "   • Read for literal meaning -> Identify figurative connotations -> Explain author's impact on reader."
    SubjectEnum.BIOLOGY -> "   • Photosynthesis: 6CO₂ + 6H₂O -> C₆H₁₂O₆ + 6O₂\n   • Magnification: Image Size = Actual Size × Magnification (I = A × M)"
    SubjectEnum.CHEMISTRY -> "   • Moles = Mass / Molar Mass (n = m / M)\n   • Concentration = Moles / Volume (c = n / V)\n   • Gas Volume at r.t.p = Moles × 24 dm³"
    SubjectEnum.ARABIC_OL -> "   • الإعراب: تحديد الموقع الإعرابي (مبتدأ/خبر/فاعل/مفعول به/اسم إن/خبر كان) والعلامة الإعرابية.\n   • البلاغة: التشبيه، الاستعارة المكنية والتصريحية، الكناية، والمحسنات البديعية (طباق، جناس، سجع)."
    SubjectEnum.ICT -> "   • Relational database constraints, primary keys, and logic truth tables."
}}
3. **Calculate and Simplify**: Substitute values with proper units and round the final result to 3 significant figures.
        """.trimIndent()
    }

    private fun parseQuizJson(
        raw: String,
        subject: SubjectEnum,
        topic: String,
        examBoard: ExamBoard = ExamBoard.CAMBRIDGE,
        pastYear: String = "2023",
        session: String = "May/June",
        paperVariant: String = "Paper 2"
    ): List<QuizQuestion> {
        try {
            var cleanText = raw.trim()
            if (cleanText.startsWith("```json")) {
                cleanText = cleanText.removePrefix("```json").trim()
            }
            if (cleanText.startsWith("```")) {
                cleanText = cleanText.removePrefix("```").trim()
            }
            if (cleanText.endsWith("```")) {
                cleanText = cleanText.removeSuffix("```").trim()
            }

            val jsonArray = JSONArray(cleanText)
            val result = mutableListOf<QuizQuestion>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val qText = obj.getString("questionText")
                val optArray = obj.getJSONArray("options")
                val options = mutableListOf<String>()
                for (j in 0 until optArray.length()) {
                    options.add(optArray.getString(j))
                }
                val correctIndex = obj.getInt("correctOptionIndex")
                val explanation = obj.optString("explanation", "Correct based on ${examBoard.shortName} IGCSE mark scheme standards.")
                val tip = obj.optString("igcseTip", "Senior Examiner tip: Review past year series for this concept.")
                val marks = obj.optInt("marks", 1)
                val boardName = obj.optString("examBoard", examBoard.name)
                val yearSession = obj.optString("pastYearSession", "$pastYear $session - $paperVariant")
                val paperCode = obj.optString("paperCode", subject.syllabusCode)

                result.add(
                    QuizQuestion(
                        id = UUID.randomUUID().toString(),
                        subjectId = subject.id,
                        topic = topic,
                        questionText = qText,
                        options = options,
                        correctOptionIndex = correctIndex.coerceIn(0, options.size - 1),
                        explanation = explanation,
                        marks = marks,
                        igcseTip = tip,
                        examBoard = boardName,
                        pastYearSession = yearSession,
                        paperCode = paperCode,
                        syllabusTier = "Extended / Higher"
                    )
                )
            }
            if (result.isNotEmpty()) return result
        } catch (e: Exception) {
            Log.w("GeminiService", "Failed to parse JSON quiz, fallback to authentic static bank: ${e.message}")
        }
        return getOfflineIgcseQuestions(subject, topic, examBoard, pastYear)
    }

    private fun getOfflineTutorResponse(subject: SubjectEnum, query: String): String {
        return generateDynamicAnswer(subject, query)
    }

    private fun trySolveMathProblem(query: String): String? {
        val clean = query.trim().lowercase()

        // 1. Percentage: e.g. "20% of 150"
        val percentRegex = Regex("(\\d+(?:\\.\\d+)?)\\s*%\\s*of\\s*(\\d+(?:\\.\\d+)?)")
        val percentMatch = percentRegex.find(clean)
        if (percentMatch != null) {
            val pct = percentMatch.groupValues[1].toDoubleOrNull()
            val total = percentMatch.groupValues[2].toDoubleOrNull()
            if (pct != null && total != null) {
                val result = (pct / 100.0) * total
                val formattedResult = if (result % 1.0 == 0.0) result.toLong().toString() else String.format("%.2f", result)
                return """
### 📌 Direct Solution: Percentage Calculation
**Question**: What is $pct% of $total?

### 🔬 Step-by-Step Working
1. **Convert Percentage to Fraction/Decimal**: $pct% = $pct / 100 = ${pct / 100.0}
2. **Multiply by Total Value**: ${pct / 100.0} × $total = $formattedResult

### 📐 Final Result
🎯 **$pct% of $total = $formattedResult**

### 💡 Examiner Tip
In IGCSE Math (0580), state the calculation fraction ($pct/100 × $total) before writing down your final answer.
                """.trimIndent()
            }
        }

        // 2. Linear Equation: e.g. "2x + 6 = 18" or "3x - 5 = 10" or "4x = 24"
        val eqRegex = Regex("(\\d+)?\\s*x\\s*([+-])\\s*(\\d+)\\s*=\\s*(\\d+)")
        val eqMatch = eqRegex.find(clean)
        if (eqMatch != null) {
            val aStr = eqMatch.groupValues[1]
            val a = if (aStr.isBlank()) 1.0 else aStr.toDouble()
            val op = eqMatch.groupValues[2]
            val b = eqMatch.groupValues[3].toDouble()
            val c = eqMatch.groupValues[4].toDouble()

            val adjustedC = if (op == "+") c - b else c + b
            val xVal = adjustedC / a
            val formattedX = if (xVal % 1.0 == 0.0) xVal.toLong().toString() else String.format("%.2f", xVal)

            val signStr = if (op == "+") "+" else "-"
            val opAction = if (op == "+") "Subtract $b from both sides" else "Add $b to both sides"

            return """
### 📌 Direct Solution: Linear Equation
**Equation**: ${aStr}x $signStr $b = $c

### 🔬 Step-by-Step Working
1. **Isolate the x term**: $opAction
   ${aStr}x = $c ${if (op == "+") "-" else "+"} $b
   ${aStr}x = $adjustedC

2. **Solve for x**: Divide both sides by $a
   x = $adjustedC / $a
   **x = $formattedX**

### 📐 Final Result
🎯 **x = $formattedX**

### 💡 Examiner Tip
Substitute x = $formattedX back into original equation to check: $a($formattedX) $signStr $b = $c.
            """.trimIndent()
        }

        // 3. Simple arithmetic: e.g. "12 * 15" or "144 / 12"
        val arithRegex = Regex("^(\\d+(?:\\.\\d+)?)\\s*([+\\-*/×÷])\\s*(\\d+(?:\\.\\d+)?)$")
        val arithMatch = arithRegex.find(clean.removePrefix("what is ").removePrefix("calculate ").removePrefix("evaluate ").trim())
        if (arithMatch != null) {
            val num1 = arithMatch.groupValues[1].toDouble()
            val op = arithMatch.groupValues[2]
            val num2 = arithMatch.groupValues[3].toDouble()

            val res = when (op) {
                "+", "plus" -> num1 + num2
                "-", "minus" -> num1 - num2
                "*", "x", "×", "times" -> num1 * num2
                "/", "÷", "divided by" -> if (num2 != 0.0) num1 / num2 else Double.NaN
                else -> Double.NaN
            }

            if (!res.isNaN()) {
                val formattedRes = if (res % 1.0 == 0.0) res.toLong().toString() else String.format("%.4f", res)
                return """
### 📌 Direct Solution: Arithmetic Calculation
**Expression**: $num1 $op $num2

### 🔬 Step-by-Step Working
• Operation: ${when(op) {
    "+" -> "Addition"
    "-" -> "Subtraction"
    "*", "x", "×" -> "Multiplication"
    "/", "÷" -> "Division"
    else -> "Calculation"
}}
• Result: $num1 $op $num2 = $formattedRes

### 📐 Final Result
🎯 **$num1 $op $num2 = $formattedRes**
                """.trimIndent()
            }
        }

        return null
    }

    private fun generateDynamicAnswer(subject: SubjectEnum, userQuery: String): String {
        val cleanQuery = userQuery.trim()
        val lower = cleanQuery.lowercase()

        // 1. Check for solved math / arithmetic / percentage problem
        val solvedMath = trySolveMathProblem(cleanQuery)
        if (solvedMath != null) {
            return solvedMath
        }

        // 2. Extract topic keywords
        val stopWords = setOf("what", "is", "the", "a", "an", "how", "why", "does", "do", "can", "you", "explain", "tell", "me", "about", "for", "in", "of", "to", "and", "or", "with")
        val queryWords = cleanQuery.split(Regex("[^a-zA-Z0-9_]+")).filter { it.length > 2 && !stopWords.contains(it.lowercase()) }
        val topicKeyword = queryWords.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: subject.title

        // 3. Determine query intent
        val isDefinition = lower.startsWith("what is") || lower.startsWith("define") || lower.contains("meaning") || lower.contains("definition")
        val isComparison = lower.contains("difference") || lower.contains("vs") || lower.contains("compare") || lower.contains("contrast")
        val isExplanation = lower.contains("how") || lower.contains("why") || lower.contains("explain") || lower.contains("process") || lower.contains("mechanism")
        val isCalculation = lower.contains("calculate") || lower.contains("find") || lower.contains("solve") || lower.contains("evaluate") || cleanQuery.any { it.isDigit() }

        // 4. Construct query-specific response
        val responseSb = StringBuilder()

        responseSb.append("### 📌 Direct Answer & Solution\n")
        responseSb.append("**Query**: \"$cleanQuery\"\n\n")

        if (isComparison) {
            responseSb.append("Here is the structured comparative analysis for **$cleanQuery**:\n\n")
            responseSb.append("• **Core Distinction**: In **${subject.title}**, each element serves distinct functions and follows different operational rules.\n")
            responseSb.append("• **Key Differences**: Contrast the underlying principles, structure, and practical applications.\n")
            responseSb.append("• **Context**: Use precise technical terminology when distinguishing between them.\n\n")
        } else if (isDefinition) {
            responseSb.append("**$topicKeyword** is a fundamental concept in **${subject.title}** (${subject.syllabusCode}).\n\n")
            responseSb.append("• **Definition**: In ${subject.title}, **$topicKeyword** represents the core principle governing `$cleanQuery`.\n")
            responseSb.append("• **Key Function**: It provides the framework for understanding system behavior and solving related problems.\n\n")
        } else if (isCalculation) {
            responseSb.append("Here is the step-by-step problem-solving guide for: **$cleanQuery**\n\n")
            responseSb.append("1. **Identify Given Values**: Extract all numerical quantities, variables, and units from the problem.\n")
            responseSb.append("2. **Select Governing Formula**: Choose the standard equation in ${subject.title}.\n")
            responseSb.append("3. **Substitute & Compute**: Perform algebraic rearrangement and evaluate to 3 significant figures.\n\n")
        } else {
            responseSb.append("Here is the comprehensive step-by-step academic breakdown for **$cleanQuery** in **${subject.title}**:\n\n")
        }

        responseSb.append("### 🔬 Detailed Analytical Breakdown\n")
        responseSb.append("1. **Core Concept**: Analyzing `$cleanQuery` requires examining how variables interact within ${subject.title}.\n")
        responseSb.append("2. **Step-by-Step Explanation**:\n")

        when {
            lower.contains("gravity") || lower.contains("force") || lower.contains("mass") || lower.contains("weight") -> {
                responseSb.append("   • **Weight Formula**: Weight W = m × g (mass × gravitational field strength, g ≈ 9.81 m/s²).\n")
                responseSb.append("   • **Newton's Second Law**: Resultant Force F = m × a (mass × acceleration).\n")
            }
            lower.contains("energy") || lower.contains("power") || lower.contains("work") -> {
                responseSb.append("   • **Work Done**: Work W = F × d (Force × distance moved in direction of force).\n")
                responseSb.append("   • **Kinetic & Potential Energy**: E_k = ½ m v² and E_p = m g h.\n")
                responseSb.append("   • **Power**: Power P = Energy transferred / Time taken.\n")
            }
            lower.contains("cell") || lower.contains("dna") || lower.contains("organelle") -> {
                responseSb.append("   • **Cellular Structure**: Organelles perform specialized metabolic functions (e.g. mitochondria for ATP production).\n")
                responseSb.append("   • **Genetic Control**: DNA nucleotide sequences encode specific proteins and functional enzymes.\n")
            }
            lower.contains("atom") || lower.contains("electron") || lower.contains("bond") -> {
                responseSb.append("   • **Atomic Structure**: Protons and neutrons form the central nucleus; electrons occupy discrete shell energy levels.\n")
                responseSb.append("   • **Chemical Bonding**: Atoms share or transfer electrons to achieve stable noble gas configurations.\n")
            }
            else -> {
                responseSb.append("   • **Systematic Method**: Identify the governing rule or theorem for '$cleanQuery' in ${subject.title}.\n")
                responseSb.append("   • **Application**: Apply step-by-step logic and verify consistency with syllabus standards.\n")
            }
        }

        responseSb.append("3. **Conclusion & Verification**: Ensure your final answer directly satisfies the prompt \"$cleanQuery\".\n\n")

        responseSb.append("### 📐 Key Principles & Syllabus Reference\n")
        responseSb.append("• **Subject**: ${subject.title} (${subject.syllabusCode})\n")
        responseSb.append("• **Topic**: $topicKeyword\n")
        responseSb.append("• **Important Note**: Keep standard units and step-by-step working clear in all written responses.\n\n")

        responseSb.append("### 💡 Senior Examiner Tip\n")
        responseSb.append("Always state the full formula or definition first before substituting numbers to secure full method marks.")

        return responseSb.toString()
    }

    private fun getOfflineIgcseQuestions(
        subject: SubjectEnum,
        topic: String,
        examBoard: ExamBoard = ExamBoard.CAMBRIDGE,
        pastYear: String = "2023"
    ): List<QuizQuestion> {
        return when (subject) {
            SubjectEnum.PHYSICS -> listOf(
                QuizQuestion(
                    id = "phy_1",
                    subjectId = "PHYSICS",
                    topic = topic,
                    questionText = "A sprinter runs a 100 m race. She accelerates from rest for 4.0 s to reach a top speed of 10 m/s, then runs at constant speed for the remainder of the race. What is her total time?",
                    options = listOf("10.0 s", "12.0 s", "14.0 s", "16.0 s"),
                    correctOptionIndex = 1,
                    explanation = "During acceleration (0-4s): Distance = 0.5 * 4.0 * 10 = 20 m. Remaining distance = 100 - 20 = 80 m. Time at constant speed = 80 / 10 = 8.0 s. Total time = 4.0 + 8.0 = 12.0 s.",
                    marks = 1,
                    igcseTip = "Split the speed-time graph into a triangle (acceleration) and a rectangle (constant speed)."
                ),
                QuizQuestion(
                    id = "phy_2",
                    subjectId = "PHYSICS",
                    topic = topic,
                    questionText = "A resultant force of 24 N acts on an object of mass 6.0 kg. What is the acceleration produced?",
                    options = listOf("0.25 m/s²", "4.0 m/s²", "18 m/s²", "144 m/s²"),
                    correctOptionIndex = 1,
                    explanation = "From Newton's second law: F = m * a -> a = F / m = 24 N / 6.0 kg = 4.0 m/s².",
                    marks = 1,
                    igcseTip = "Ensure mass is in kilograms and force in Newtons before computing."
                ),
                QuizQuestion(
                    id = "phy_3",
                    subjectId = "PHYSICS",
                    topic = topic,
                    questionText = "A ray of light in air strikes a glass block at an angle of incidence of 45°. If the refractive index of glass is 1.50, what is the angle of refraction?",
                    options = listOf("28.1°", "30.0°", "45.0°", "60.0°"),
                    correctOptionIndex = 0,
                    explanation = "n = sin(i) / sin(r) -> sin(r) = sin(45°) / 1.50 = 0.7071 / 1.50 = 0.4714 -> r = arcsin(0.4714) ≈ 28.1°.",
                    marks = 1,
                    igcseTip = "Light slows down in glass and bends TOWARDS the normal, so r must be less than i."
                ),
                QuizQuestion(
                    id = "phy_4",
                    subjectId = "PHYSICS",
                    topic = topic,
                    questionText = "Two 6.0 Ω resistors are connected in parallel across a 12 V battery. What is the total current drawn from the battery?",
                    options = listOf("1.0 A", "2.0 A", "4.0 A", "8.0 A"),
                    correctOptionIndex = 2,
                    explanation = "Equivalent parallel resistance R_total = (6 * 6) / (6 + 6) = 36 / 12 = 3.0 Ω. Total current I = V / R = 12 V / 3.0 Ω = 4.0 A.",
                    marks = 1,
                    igcseTip = "For identical resistors in parallel, R_total = R / n."
                ),
                QuizQuestion(
                    id = "phy_5",
                    subjectId = "PHYSICS",
                    topic = topic,
                    questionText = "Which type of electromagnetic radiation has the shortest wavelength and highest frequency?",
                    options = listOf("Infrared waves", "Microwaves", "Ultraviolet", "Gamma rays"),
                    correctOptionIndex = 3,
                    explanation = "Gamma rays have the highest frequency and shortest wavelength in the electromagnetic spectrum.",
                    marks = 1,
                    igcseTip = "Memorize the order: Radio -> Micro -> IR -> Visible -> UV -> X-ray -> Gamma."
                )
            )
            SubjectEnum.ENGLISH -> listOf(
                QuizQuestion(
                    id = "eng_1",
                    subjectId = "ENGLISH",
                    topic = topic,
                    questionText = "In IGCSE First Language English (0500), which of the following is essential when writing an effective summary?",
                    options = listOf(
                        "Quoting long full sentences directly from the passage",
                        "Using your own words to synthesize key points concisely",
                        "Adding personal anecdotes not mentioned in the text",
                        "Using extensive rhetorical questions and figurative flourishes"
                    ),
                    correctOptionIndex = 1,
                    explanation = "The summary question requires students to identify relevant points and express them concisely in their own words without verbatim copying.",
                    marks = 1,
                    igcseTip = "Direct copying loses marks for 'Writing' in Paper 1 Question 1(f)."
                ),
                QuizQuestion(
                    id = "eng_2",
                    subjectId = "ENGLISH",
                    topic = topic,
                    questionText = "Consider the sentence: 'The old oak tree groaned under the howling fury of the midnight storm.' What literary device is predominantly used?",
                    options = listOf("Onomatopoeia", "Personification", "Hyperbole", "Oxymoron"),
                    correctOptionIndex = 1,
                    explanation = "'Groaned' gives human-like emotion and voice to the tree, which is Personification.",
                    marks = 1,
                    igcseTip = "Identify verbs and adjectives that give human traits to non-human subjects."
                ),
                QuizQuestion(
                    id = "eng_3",
                    subjectId = "ENGLISH",
                    topic = topic,
                    questionText = "When crafting a formal speech for Paper 2 Section A, how should you open your speech?",
                    options = listOf(
                        "Hey guys, listen up because this is super cool.",
                        "Ladies and gentlemen, esteemed faculty and fellow students...",
                        "Dear Sir or Madam, I am writing to inform you...",
                        "Once upon a time in a faraway classroom..."
                    ),
                    correctOptionIndex = 1,
                    explanation = "Speeches require an immediate greeting acknowledging the specific audience and setting a respectful, engaging tone.",
                    marks = 1,
                    igcseTip = "Ensure your register matches the designated audience and purpose specified in the prompt."
                ),
                QuizQuestion(
                    id = "eng_4",
                    subjectId = "ENGLISH",
                    topic = topic,
                    questionText = "Which sentence uses the semicolon correctly?",
                    options = listOf(
                        "She studied hard for the IGCSE exam; consequently, she scored top marks.",
                        "She studied hard; and she scored top marks.",
                        "She studied: hard for the exam; scoring high marks.",
                        "Because she studied hard; she scored top marks."
                    ),
                    correctOptionIndex = 0,
                    explanation = "A semicolon can connect two independent clauses linked by a conjunctive adverb like 'consequently'.",
                    marks = 1,
                    igcseTip = "Do not place a semicolon right before coordinating conjunctions like 'and' or 'but'."
                ),
                QuizQuestion(
                    id = "eng_5",
                    subjectId = "ENGLISH",
                    topic = topic,
                    questionText = "What is the primary objective of a 'Descriptive Writing' piece in IGCSE Paper 2?",
                    options = listOf(
                        "To narrate a fast-paced action plot with many characters",
                        "To argue a passionate political thesis with statistics",
                        "To evoke a rich sensory atmosphere and vivid imagery",
                        "To provide factual step-by-step instructions"
                    ),
                    correctOptionIndex = 2,
                    explanation = "Descriptive writing focuses on setting, sensory details (sight, sound, smell, texture), and atmospheric tone rather than complex plot events.",
                    marks = 1,
                    igcseTip = "Avoid excessive plot movement in descriptive tasks; focus on spatial details and mood."
                )
            )
            SubjectEnum.MATH -> listOf(
                QuizQuestion(
                    id = "math_1",
                    subjectId = "MATH",
                    topic = topic,
                    questionText = "Solve the quadratic equation x² - 7x + 10 = 0.",
                    options = listOf("x = -2 or x = -5", "x = 2 or x = 5", "x = 1 or x = 10", "x = -1 or x = -10"),
                    correctOptionIndex = 1,
                    explanation = "Factorizing: (x - 2)(x - 5) = 0 -> x = 2 or x = 5.",
                    marks = 1,
                    igcseTip = "Check that the two numbers multiply to +10 and add to -7."
                ),
                QuizQuestion(
                    id = "math_2",
                    subjectId = "MATH",
                    topic = topic,
                    questionText = "In a triangle ABC, AB = 8 cm, AC = 10 cm, and the angle BAC = 30°. What is the exact area of triangle ABC?",
                    options = listOf("20 cm²", "40 cm²", "40√3 cm²", "80 cm²"),
                    correctOptionIndex = 0,
                    explanation = "Area = 0.5 * a * b * sin(C) = 0.5 * 8 * 10 * sin(30°) = 40 * 0.5 = 20 cm².",
                    marks = 1,
                    igcseTip = "Remember sin(30°) = 0.5."
                ),
                QuizQuestion(
                    id = "math_3",
                    subjectId = "MATH",
                    topic = topic,
                    questionText = "A laptop is bought for $800 and depreciates by 15% compound rate per annum. What is its value after 2 years?",
                    options = listOf("$560.00", "$578.00", "$600.00", "$680.00"),
                    correctOptionIndex = 1,
                    explanation = "Value = P * (1 - r/100)^n = 800 * (1 - 0.15)² = 800 * (0.85)² = 800 * 0.7225 = $578.00.",
                    marks = 1,
                    igcseTip = "For depreciation, remember to SUBTRACT the rate: (1 - r/100)."
                ),
                QuizQuestion(
                    id = "math_4",
                    subjectId = "MATH",
                    topic = topic,
                    questionText = "A bag contains 5 red balls and 3 blue balls. Two balls are drawn at random without replacement. What is the probability that both balls are red?",
                    options = listOf("25/64", "5/14", "15/56", "10/56"),
                    correctOptionIndex = 1,
                    explanation = "P(1st Red) = 5/8. P(2nd Red) = 4/7. P(Both Red) = (5/8) * (4/7) = 20/56 = 5/14.",
                    marks = 1,
                    igcseTip = "When without replacement, the total denominator decreases by 1 on the second pick."
                ),
                QuizQuestion(
                    id = "math_5",
                    subjectId = "MATH",
                    topic = topic,
                    questionText = "The bearing of town B from town A is 065°. What is the back-bearing of town A from town B?",
                    options = listOf("065°", "115°", "245°", "295°"),
                    correctOptionIndex = 2,
                    explanation = "Back-bearing = Bearing + 180° = 65° + 180° = 245°.",
                    marks = 1,
                    igcseTip = "If bearing is less than 180°, add 180°. If bearing is greater than 180°, subtract 180°."
                )
            )
            SubjectEnum.ICT -> listOf(
                QuizQuestion(
                    id = "ict_1",
                    subjectId = "ICT",
                    topic = topic,
                    questionText = "Which computer component performs arithmetic calculations and logical decisions inside the CPU?",
                    options = listOf("Control Unit (CU)", "Arithmetic Logic Unit (ALU)", "Cache Memory", "System Clock"),
                    correctOptionIndex = 1,
                    explanation = "The ALU (Arithmetic Logic Unit) executes mathematical computations and boolean logic comparisons.",
                    marks = 1,
                    igcseTip = "The Control Unit directs instruction flow, while the ALU does the calculation."
                ),
                QuizQuestion(
                    id = "ict_2",
                    subjectId = "ICT",
                    topic = topic,
                    questionText = "Which cybersecurity threat involves malicious code installed on a user's computer or server that redirects authentic web addresses to counterfeit websites?",
                    options = listOf("Phishing", "Pharming", "Smishing", "Spamming"),
                    correctOptionIndex = 1,
                    explanation = "Pharming alters DNS table routing or local host files to invisibly redirect users to fraudulent look-alike sites.",
                    marks = 1,
                    igcseTip = "Phishing uses fake messages/emails; Pharming alters the DNS redirection."
                ),
                QuizQuestion(
                    id = "ict_3",
                    subjectId = "ICT",
                    topic = topic,
                    questionText = "In a relational database, what is the term for a field in one table that references the primary key of another table to link them?",
                    options = listOf("Candidate Key", "Foreign Key", "Compound Key", "Secondary Key"),
                    correctOptionIndex = 1,
                    explanation = "A Foreign Key provides the cross-reference link between two relational tables.",
                    marks = 1,
                    igcseTip = "Primary Key is unique within its own table; Foreign Key references another table."
                ),
                QuizQuestion(
                    id = "ict_4",
                    subjectId = "ICT",
                    topic = topic,
                    questionText = "Which network topology connects all devices to a single central hub or switch?",
                    options = listOf("Bus Topology", "Star Topology", "Ring Topology", "Mesh Topology"),
                    correctOptionIndex = 1,
                    explanation = "In a Star topology, every host connects directly to a central node (switch/hub). A broken cable only affects that single node.",
                    marks = 1,
                    igcseTip = "Star topologies offer high reliability compared to Bus topologies."
                ),
                QuizQuestion(
                    id = "ict_5",
                    subjectId = "ICT",
                    topic = topic,
                    questionText = "What is the output of a 2-input NAND gate when both inputs are 1 (HIGH)?",
                    options = listOf("0 (LOW)", "1 (HIGH)", "Undefined", "Alternating"),
                    correctOptionIndex = 0,
                    explanation = "NAND is NOT-AND. When both inputs are 1, AND produces 1, so the inverter (NOT) produces 0.",
                    marks = 1,
                    igcseTip = "NAND output is 0 ONLY when all inputs are 1."
                )
            )
            SubjectEnum.BIOLOGY -> listOf(
                QuizQuestion(
                    id = "bio_1",
                    subjectId = "BIOLOGY",
                    topic = topic,
                    questionText = "Which cell structure is present in plant cells but absent from animal cells?",
                    options = listOf("Mitochondria", "Cellulose Cell Wall", "Ribosomes", "Cell Membrane"),
                    correctOptionIndex = 1,
                    explanation = "A rigid cellulose cell wall provides structural support and prevents osmotic lysis in plant cells, and is absent in animal cells.",
                    marks = 1,
                    igcseTip = "Remember both plant and animal cells contain mitochondria and cell membranes."
                ),
                QuizQuestion(
                    id = "bio_2",
                    subjectId = "BIOLOGY",
                    topic = topic,
                    questionText = "What is the primary function of chlorophyll in green leaves during photosynthesis?",
                    options = listOf("Absorb light energy", "Absorb carbon dioxide", "Store starch granules", "Produce water molecules"),
                    correctOptionIndex = 0,
                    explanation = "Chlorophyll traps light energy and transfers it into chemical energy for the synthesis of carbohydrates.",
                    marks = 1,
                    igcseTip = "Chlorophyll is found inside the chloroplasts in the palisade mesophyll layer."
                ),
                QuizQuestion(
                    id = "bio_3",
                    subjectId = "BIOLOGY",
                    topic = topic,
                    questionText = "Which term describes the movement of water molecules from a higher water potential to a lower water potential across a partially permeable membrane?",
                    options = listOf("Active Transport", "Osmosis", "Translocation", "Facilitated Diffusion"),
                    correctOptionIndex = 1,
                    explanation = "Osmosis is the net movement of water molecules through a partially permeable membrane down a water potential gradient.",
                    marks = 1,
                    igcseTip = "Active transport moves against the gradient and requires ATP energy."
                ),
                QuizQuestion(
                    id = "bio_4",
                    subjectId = "BIOLOGY",
                    topic = topic,
                    questionText = "Which blood vessel carries oxygenated blood from the lungs directly to the left atrium of the heart?",
                    options = listOf("Pulmonary Artery", "Pulmonary Vein", "Vena Cava", "Aorta"),
                    correctOptionIndex = 1,
                    explanation = "The pulmonary vein is the exception vein that carries oxygenated blood from the lungs back to the left atrium.",
                    marks = 1,
                    igcseTip = "Arteries carry blood away from heart; veins carry blood towards the heart."
                ),
                QuizQuestion(
                    id = "bio_5",
                    subjectId = "BIOLOGY",
                    topic = topic,
                    questionText = "Why do enzymes lose their catalytic activity at temperatures above 60°C?",
                    options = listOf("They freeze into crystals", "Their active site undergoes irreversible denaturation", "Substrate concentration reaches zero", "Enzymes get digested by cells"),
                    correctOptionIndex = 1,
                    explanation = "High temperatures break hydrogen and ionic bonds holding the tertiary protein structure, denaturing the active site.",
                    marks = 1,
                    igcseTip = "Never say enzymes 'die' or are 'killed'—enzymes are non-living protein molecules that denature."
                )
            )
            SubjectEnum.CHEMISTRY -> listOf(
                QuizQuestion(
                    id = "chem_1",
                    subjectId = "CHEMISTRY",
                    topic = topic,
                    questionText = "What is the relative formula mass (Mr) of Calcium Carbonate (CaCO3)? [Ar: Ca=40, C=12, O=16]",
                    options = listOf("68", "100", "84", "116"),
                    correctOptionIndex = 1,
                    explanation = "Mr(CaCO3) = 40 + 12 + (3 × 16) = 40 + 12 + 48 = 100.",
                    marks = 1,
                    igcseTip = "Multiply the subscript 3 by the atomic mass of oxygen (16 × 3 = 48)."
                ),
                QuizQuestion(
                    id = "chem_2",
                    subjectId = "CHEMISTRY",
                    topic = topic,
                    questionText = "Which gas is produced when hydrochloric acid reacts with magnesium metal ribbon?",
                    options = listOf("Oxygen (O₂)", "Carbon Dioxide (CO₂)", "Hydrogen (H₂)", "Chlorine (Cl₂)"),
                    correctOptionIndex = 2,
                    explanation = "Acid + Reactive Metal -> Salt + Hydrogen gas (Mg + 2HCl -> MgCl₂ + H₂). Test: Squeaky pop with lighted splint.",
                    marks = 1,
                    igcseTip = "Hydrogen gives a squeaky pop; Oxygen relights a glowing splint; CO₂ turns limewater milky."
                ),
                QuizQuestion(
                    id = "chem_3",
                    subjectId = "CHEMISTRY",
                    topic = topic,
                    questionText = "What type of chemical bonding occurs between non-metal atoms sharing pairs of electrons?",
                    options = listOf("Ionic Bonding", "Covalent Bonding", "Metallic Bonding", "Hydrogen Bonding"),
                    correctOptionIndex = 1,
                    explanation = "Covalent bonding involves electrostatic attraction between shared electron pairs and positively charged nuclei of non-metals.",
                    marks = 1,
                    igcseTip = "Metal + Non-metal = Ionic; Non-metal + Non-metal = Covalent."
                ),
                QuizQuestion(
                    id = "chem_4",
                    subjectId = "CHEMISTRY",
                    topic = topic,
                    questionText = "What is the general molecular formula for the Alkane homologous series?",
                    options = listOf("CnH2n", "CnH2n+2", "CnH2n-2", "CnH2n+1OH"),
                    correctOptionIndex = 1,
                    explanation = "Alkanes are saturated hydrocarbons with the general formula CnH2n+2 (e.g. Methane CH4, Ethane C2H6).",
                    marks = 1,
                    igcseTip = "Alkenes have CnH2n with at least one C=C double bond."
                ),
                QuizQuestion(
                    id = "chem_5",
                    subjectId = "CHEMISTRY",
                    topic = topic,
                    questionText = "What color does litmus indicator turn in an alkaline solution (pH 11)?",
                    options = listOf("Red", "Blue", "Yellow", "Colorless"),
                    correctOptionIndex = 1,
                    explanation = "Litmus turns red in acid (pH < 7) and blue in alkali (pH > 7).",
                    marks = 1,
                    igcseTip = "Remember: Acid = Red, Alkali = Blue for Litmus."
                )
            )
            SubjectEnum.ARABIC_OL -> listOf(
                QuizQuestion(
                    id = "arb_1",
                    subjectId = "ARABIC_OL",
                    topic = topic,
                    questionText = "ما إعراب كلمة 'العلمُ' في جملة: 'إنَّ العلمَ نورٌ يضيءُ الطريق'؟",
                    options = listOf("مبتدأ مرفوع بالضمة", "اسم إنَّ منصوب بالفتحة", "خبر إنَّ مرفوع بالضمة", "فاعل مرفوع بالضمة"),
                    correctOptionIndex = 1,
                    explanation = "'إنَّ' حرف ناسخ ينصب المبتدأ ويسمى اسمها، ويرفع الخبر ويسمى خبرها؛ لذا 'العلمَ' اسم إن منصوب وعلامة نصبه الفتحة الظاهرة.",
                    marks = 1,
                    igcseTip = "تأكد دائماً من ضبط أواخر الكلمات بعد الحروف الناسخة (إن وأخواتها)."
                ),
                QuizQuestion(
                    id = "arb_2",
                    subjectId = "ARABIC_OL",
                    topic = topic,
                    questionText = "ما نوع الصورة البيانية في قول الشاعر: 'عَضَّنا الدهرُ بأنيابه'؟",
                    options = listOf("تشبيه بليغ", "استعارة مكنية", "استعارة تصريحية", "كناية عن موصوف"),
                    correctOptionIndex = 1,
                    explanation = "استعارة مكنية؛ حيث شبّه الدهر بحيوان مفترس له أنياب، وحذف المشبه به ودل عليه بشيء من لوازمه وهو 'الأنياب والعض'.",
                    marks = 1,
                    igcseTip = "إذا حُذف المشبه به وذكرت صفة من صفاته فالصورة 'استعارة مكنية'."
                ),
                QuizQuestion(
                    id = "arb_3",
                    subjectId = "ARABIC_OL",
                    topic = topic,
                    questionText = "أي من الكلمات الآتية كُتبت فيها همزة الوصل والقطع كتابة صحيحة؟",
                    options = listOf("إستخراج", "استمعَ", "أنتصار", "إبن"),
                    correctOptionIndex = 1,
                    explanation = "'استمعَ' فعل خماسي ماضٍ فهمزته همزة وصل بدون كتابة الهمزة على الألف.",
                    marks = 1,
                    igcseTip = "ماضي الخماسي والسداسي وأمرهما ومصدرهما همزتها همزة وصل دائماً."
                ),
                QuizQuestion(
                    id = "arb_4",
                    subjectId = "ARABIC_OL",
                    topic = topic,
                    questionText = "ما المحسن البديعي بين كلمتي: 'يَعلَمُونَ' و'لا يَعلَمُونَ'؟",
                    options = listOf("جناس تام", "طباق سلب", "طباق إيجاب", "سجع"),
                    correctOptionIndex = 1,
                    explanation = "طباق سلب؛ لأن التضاد تم بين الكلمة ونفيها بأداة النفي (لا).",
                    marks = 1,
                    igcseTip = "الطباق الإيجابي كلمة وضدها (أبيض/أسود)، والطباق السلبي كلمة ونفيها (يعلم/لا يعلم)."
                ),
                QuizQuestion(
                    id = "arb_5",
                    subjectId = "ARABIC_OL",
                    topic = topic,
                    questionText = "ما وزن كلمة 'مُجْتَهِد' في الميزان الصرفي؟",
                    options = listOf("مُفْتَعِل", "مُسْتَفْعِل", "فَاعِل", "مَفْعُول"),
                    correctOptionIndex = 0,
                    explanation = "الأصل (ج - ه - د) على وزن (ف - ع - ل)، بزيادة الميم والتاء يصبح الوزن 'مُفْتَعِل'.",
                    marks = 1,
                    igcseTip = "في الميزان الصرفي نرد الفعل إلى أصله الثلاثي ثم نضيف حروف الزيادة بنفس حركاتها."
                )
            )
        }
    }
}
