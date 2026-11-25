package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.AppLanguage
import com.example.data.model.FlashcardItem
import com.example.data.model.LessonBreakdown
import com.example.data.model.QuestionType
import com.example.data.model.QuizDifficulty
import com.example.data.model.QuizQuestion
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

sealed class AiServiceOutcome<out T> {
    data class LiveSuccess<T>(val data: T) : AiServiceOutcome<T>()
    data class DemoFallback<T>(val data: T, val reason: String) : AiServiceOutcome<T>()
}

@JsonClass(generateAdapter = true)
data class LessonJsonDto(
    val simplifiedExplanation: String = "",
    val outsideConnection: String = "",
    val importantQuestionsRadar: List<String> = emptyList(),
    val mustMemorize: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class QuizQuestionJsonDto(
    val type: String = "MCQ",
    val prompt: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val explanation: String = ""
)

@JsonClass(generateAdapter = true)
data class QuizResponseJsonDto(
    val questions: List<QuizQuestionJsonDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class FlashcardJsonDto(
    val front: String = "",
    val back: String = ""
)

@JsonClass(generateAdapter = true)
data class FlashcardResponseJsonDto(
    val flashcards: List<FlashcardJsonDto> = emptyList()
)

class OpenRouterService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val lessonAdapter = moshi.adapter(LessonJsonDto::class.java)
    private val quizAdapter = moshi.adapter(QuizResponseJsonDto::class.java)
    private val flashcardAdapter = moshi.adapter(FlashcardResponseJsonDto::class.java)

    private fun resolveActiveKey(customKey: String): String {
        val custom = customKey.trim()
        if (custom.isNotEmpty()) return custom
        val buildKey = BuildConfig.OPENROUTER_API_KEY.trim()
        return if (buildKey.isEmpty() || buildKey == "MY_OPENROUTER_API_KEY") "" else buildKey
    }

    private fun resolveEndpointAndAuth(customKey: String): Pair<String, String?>? {
        val proxyUrl = BuildConfig.OPENROUTER_PROXY_URL.trim()
        if (proxyUrl.startsWith("https://")) {
            return proxyUrl to null
        }
        val key = resolveActiveKey(customKey)
        if (key.isEmpty()) return null
        return "https://openrouter.ai/api/v1/chat/completions" to key
    }

    suspend fun explainLesson(
        customKey: String,
        sourceText: String,
        subjectTag: String,
        imageDataUrl: String?,
        isCramMode: Boolean,
        language: AppLanguage,
        onRetryStatus: (String) -> Unit = {}
    ): AiServiceOutcome<LessonBreakdown> {
        val endpointAuth = resolveEndpointAndAuth(customKey)
        if (endpointAuth == null) {
            return AiServiceOutcome.DemoFallback(
                DemoStudyEngine.buildLessonBreakdown(
                    sourceText, subjectTag, imageDataUrl != null, isCramMode, language
                ),
                reason = "empty_key"
            )
        }

        val langInstr = if (language == AppLanguage.AR) {
            "Respond strictly in natural, warm, encouraging Egyptian Arabic ('الأستاذ الشاطر' persona)."
        } else {
            "Respond strictly in clear, friendly, encouraging English ('Smart Teacher' persona)."
        }
        val cramInstr = if (isCramMode) {
            "CRAM MODE IS ON: Be ultra-concise, bullet-pointed, high-yield for night-before-exam review."
        } else {
            "Explain deeply yet intuitively with clear analogies."
        }

        val systemPrompt = """
            You are Nabraawy's Smart Teacher ("الأستاذ الشاطر").
            $langInstr
            $cramInstr
            Return ONLY valid JSON with these exact keys:
            {
              "simplifiedExplanation": "string",
              "outsideConnection": "string (real-world / cross-topic link outside the lesson)",
              "importantQuestionsRadar": ["string", "string", "string"],
              "mustMemorize": ["string", "string", "string"]
            }
        """.trimIndent()

        val userPrompt = buildString {
            if (subjectTag.isNotBlank()) append("Subject: $subjectTag\n")
            if (sourceText.isNotBlank()) append("Lesson Content:\n$sourceText\n")
            else append("Analyze the attached lesson image thoroughly and teach it.")
        }

        val raw = executeChatWithRetry(
            endpointUrl = endpointAuth.first,
            apiKey = endpointAuth.second,
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
            imageDataUrl = imageDataUrl,
            onRetryStatus = onRetryStatus
        )

        if (raw != null) {
            val parsed = parseJsonObject(raw) { lessonAdapter.fromJson(it) }
            if (parsed != null && parsed.simplifiedExplanation.isNotBlank()) {
                return AiServiceOutcome.LiveSuccess(
                    LessonBreakdown(
                        id = UUID.randomUUID().toString(),
                        subjectTag = subjectTag.ifBlank {
                            if (language == AppLanguage.AR) "الدرس" else "Lesson"
                        },
                        sourceText = sourceText,
                        simplifiedExplanation = parsed.simplifiedExplanation,
                        outsideConnection = parsed.outsideConnection,
                        importantQuestionsRadar = parsed.importantQuestionsRadar,
                        mustMemorize = parsed.mustMemorize,
                        isCramMode = isCramMode
                    )
                )
            }
        }

        return AiServiceOutcome.DemoFallback(
            DemoStudyEngine.buildLessonBreakdown(
                sourceText, subjectTag, imageDataUrl != null, isCramMode, language
            ),
            reason = "connection_or_parse_fallback"
        )
    }

    suspend fun clarifyPoint(
        customKey: String,
        point: String,
        lesson: LessonBreakdown,
        language: AppLanguage,
        onRetryStatus: (String) -> Unit = {}
    ): AiServiceOutcome<String> {
        val endpointAuth = resolveEndpointAndAuth(customKey)
        if (endpointAuth == null) {
            return AiServiceOutcome.DemoFallback(
                DemoStudyEngine.clarifyPoint(point, lesson, language),
                reason = "empty_key"
            )
        }
        val langInstr = if (language == AppLanguage.AR) "Answer warmly in Egyptian Arabic ('الأستاذ الشاطر')." else "Answer clearly in English."
        val raw = executeChatWithRetry(
            endpointUrl = endpointAuth.first,
            apiKey = endpointAuth.second,
            systemPrompt = "You are Nabraawy's Smart Teacher. $langInstr Keep it clear, focused, and encouraging.",
            userPrompt = "Lesson context: ${lesson.simplifiedExplanation.take(900)}\nStudent asks to clarify: $point",
            imageDataUrl = null,
            onRetryStatus = onRetryStatus
        )
        return if (!raw.isNullOrBlank()) {
            AiServiceOutcome.LiveSuccess(raw.trim())
        } else {
            AiServiceOutcome.DemoFallback(
                DemoStudyEngine.clarifyPoint(point, lesson, language),
                reason = "fallback"
            )
        }
    }

    suspend fun evaluateTeachBack(
        customKey: String,
        studentExplanation: String,
        lesson: LessonBreakdown,
        language: AppLanguage,
        onRetryStatus: (String) -> Unit = {}
    ): AiServiceOutcome<String> {
        val endpointAuth = resolveEndpointAndAuth(customKey)
        if (endpointAuth == null) {
            return AiServiceOutcome.DemoFallback(
                DemoStudyEngine.evaluateTeachBack(studentExplanation, lesson, language),
                reason = "empty_key"
            )
        }
        val closingLine = if (language == AppLanguage.AR) {
            "عايز تدريب كمان ولا تمام كده؟"
        } else {
            "Want more practice or are you good?"
        }
        val langInstr = if (language == AppLanguage.AR) "Respond in warm Egyptian Arabic ('الأستاذ الشاطر')." else "Respond in warm, clear English."
        val raw = executeChatWithRetry(
            endpointUrl = endpointAuth.first,
            apiKey = endpointAuth.second,
            systemPrompt = "You are Nabraawy's Smart Teacher. $langInstr Evaluate the student's understanding gently, praise what is right, correct any gap, and ALWAYS end with the exact question: \"$closingLine\"",
            userPrompt = "Lesson reference:\n${lesson.simplifiedExplanation.take(900)}\n\nStudent's teach-back explanation:\n$studentExplanation",
            imageDataUrl = null,
            onRetryStatus = onRetryStatus
        )
        return if (!raw.isNullOrBlank()) {
            val cleaned = raw.trim()
            val withClosing = if (cleaned.contains(closingLine)) cleaned else "$cleaned\n\n$closingLine"
            AiServiceOutcome.LiveSuccess(withClosing)
        } else {
            AiServiceOutcome.DemoFallback(
                DemoStudyEngine.evaluateTeachBack(studentExplanation, lesson, language),
                reason = "fallback"
            )
        }
    }

    suspend fun generateQuiz(
        customKey: String,
        content: String,
        imageDataUrl: String?,
        types: Set<QuestionType>,
        difficulty: QuizDifficulty,
        count: Int,
        totalScore: Int,
        language: AppLanguage,
        onRetryStatus: (String) -> Unit = {}
    ): AiServiceOutcome<List<QuizQuestion>> {
        val endpointAuth = resolveEndpointAndAuth(customKey)
        if (endpointAuth == null) {
            return AiServiceOutcome.DemoFallback(
                DemoStudyEngine.buildQuizFromContent(
                    content, imageDataUrl != null, types, difficulty, count, totalScore, language
                ),
                reason = "empty_key"
            )
        }

        val activeTypes = types.ifEmpty { setOf(QuestionType.MCQ) }.joinToString(", ") { it.name }
        val langInstr = if (language == AppLanguage.AR) "All questions, options, and explanations MUST be in Arabic." else "All questions, options, and explanations MUST be in English."
        val pointsPerQ = (totalScore / count.coerceAtLeast(1)).coerceAtLeast(1)

        val systemPrompt = """
            You are an exam generator for Nabraawy Study.
            CRITICAL RULE: Generate questions STRICTLY AND ONLY from the uploaded/provided lesson content. Never invent facts outside the provided content.
            $langInstr
            Requested count: $count
            Requested difficulty: ${difficulty.name}
            Allowed types: $activeTypes (valid values for "type" field: "MCQ", "TRUE_FALSE", "SHORT_ANSWER")
            For MCQ provide 4 options. For TRUE_FALSE provide 2 options ("صح", "غلط" in Arabic or "True", "False" in English). For SHORT_ANSWER provide empty options [].
            Return ONLY valid JSON:
            {
              "questions": [
                {
                  "type": "MCQ",
                  "prompt": "...",
                  "options": ["..."],
                  "correctAnswer": "...",
                  "explanation": "..."
                }
              ]
            }
        """.trimIndent()

        val userPrompt = buildString {
            if (content.isNotBlank()) append("Uploaded Content:\n$content")
            else append("Generate questions strictly from the attached image.")
        }

        val raw = executeChatWithRetry(
            endpointUrl = endpointAuth.first,
            apiKey = endpointAuth.second,
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
            imageDataUrl = imageDataUrl,
            onRetryStatus = onRetryStatus
        )

        if (raw != null) {
            val parsed = parseJsonObject(raw) { quizAdapter.fromJson(it) }
            if (parsed != null && parsed.questions.isNotEmpty()) {
                val mapped = parsed.questions.take(count).mapIndexed { idx, dto ->
                    val qType = runCatching { QuestionType.valueOf(dto.type.uppercase()) }
                        .getOrDefault(QuestionType.MCQ)
                    QuizQuestion(
                        id = "live_q_${idx}_${UUID.randomUUID().toString().take(6)}",
                        type = qType,
                        prompt = dto.prompt,
                        options = dto.options,
                        correctAnswer = dto.correctAnswer,
                        explanation = dto.explanation,
                        points = pointsPerQ
                    )
                }
                return AiServiceOutcome.LiveSuccess(mapped)
            }
        }

        return AiServiceOutcome.DemoFallback(
            DemoStudyEngine.buildQuizFromContent(
                content, imageDataUrl != null, types, difficulty, count, totalScore, language
            ),
            reason = "fallback"
        )
    }

    suspend fun generateFlashcards(
        customKey: String,
        content: String,
        subject: String,
        language: AppLanguage,
        onRetryStatus: (String) -> Unit = {}
    ): AiServiceOutcome<List<FlashcardItem>> {
        val endpointAuth = resolveEndpointAndAuth(customKey)
        if (endpointAuth == null) {
            return AiServiceOutcome.DemoFallback(
                DemoStudyEngine.buildFlashcardsFromContent(content, subject, language),
                reason = "empty_key"
            )
        }
        val langInstr = if (language == AppLanguage.AR) "Write flashcards in Arabic." else "Write flashcards in English."
        val raw = executeChatWithRetry(
            endpointUrl = endpointAuth.first,
            apiKey = endpointAuth.second,
            systemPrompt = """
                Create 5 concise study flashcards ONLY from the provided content. $langInstr
                Return ONLY JSON: {"flashcards": [{"front": "...", "back": "..."}]}
            """.trimIndent(),
            userPrompt = content,
            imageDataUrl = null,
            onRetryStatus = onRetryStatus
        )
        if (raw != null) {
            val parsed = parseJsonObject(raw) { flashcardAdapter.fromJson(it) }
            if (parsed != null && parsed.flashcards.isNotEmpty()) {
                val subj = subject.ifBlank { if (language == AppLanguage.AR) "مذاكرة" else "Study" }
                return AiServiceOutcome.LiveSuccess(
                    parsed.flashcards.map {
                        FlashcardItem(UUID.randomUUID().toString(), it.front, it.back, subj)
                    }
                )
            }
        }
        return AiServiceOutcome.DemoFallback(
            DemoStudyEngine.buildFlashcardsFromContent(content, subject, language),
            reason = "fallback"
        )
    }

    private suspend fun executeChatWithRetry(
        endpointUrl: String,
        apiKey: String?,
        systemPrompt: String,
        userPrompt: String,
        imageDataUrl: String?,
        onRetryStatus: (String) -> Unit
    ): String? = withContext(Dispatchers.IO) {
        var useImage = imageDataUrl != null
        val maxAttempts = 3
        for (attempt in 1..maxAttempts) {
            try {
                val bodyJson = buildRequestBodyJson(systemPrompt, userPrompt, if (useImage) imageDataUrl else null)
                val reqBuilder = Request.Builder()
                    .url(endpointUrl)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("HTTP-Referer", "https://nabraawy.study")
                    .addHeader("X-Title", "Nabraawy Study")
                if (!apiKey.isNullOrBlank()) {
                    reqBuilder.addHeader("Authorization", "Bearer $apiKey")
                }
                val request = reqBuilder
                    .post(bodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        val content = extractMessageContent(responseBody)
                        if (!content.isNullOrBlank()) return@withContext content
                    } else if (useImage && response.code in listOf(400, 404, 422)) {
                        // Free model routed may not support vision; retry text-only if prompt has context
                        useImage = false
                    } else if (response.code == 401 || response.code == 403) {
                        return@withContext null
                    }
                }
            } catch (_: IOException) {
                // Transient network error -> retry
            } catch (_: Exception) {
                return@withContext null
            }

            if (attempt < maxAttempts) {
                onRetryStatus("retry_$attempt")
                delay(750L * attempt)
            }
        }
        null
    }

    private fun buildRequestBodyJson(
        systemPrompt: String,
        userPrompt: String,
        imageDataUrl: String?
    ): String {
        val messages = JSONArray()
        messages.put(JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
        })
        if (imageDataUrl != null) {
            val contentArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("type", "text")
                    put("text", userPrompt)
                })
                put(JSONObject().apply {
                    put("type", "image_url")
                    put("image_url", JSONObject().apply {
                        put("url", imageDataUrl)
                    })
                })
            }
            messages.put(JSONObject().apply {
                put("role", "user")
                put("content", contentArray)
            })
        } else {
            messages.put(JSONObject().apply {
                put("role", "user")
                put("content", userPrompt)
            })
        }

        return JSONObject().apply {
            put("model", "openrouter/free")
            put("messages", messages)
            put("temperature", 0.4)
        }.toString()
    }

    private fun extractMessageContent(jsonString: String): String? = runCatching {
        val root = JSONObject(jsonString)
        val choices = root.optJSONArray("choices") ?: return@runCatching null
        if (choices.length() == 0) return@runCatching null
        val msg = choices.getJSONObject(0).optJSONObject("message") ?: return@runCatching null
        msg.optString("content", "").takeIf { it.isNotBlank() }
    }.getOrNull()

    private fun <T> parseJsonObject(raw: String, decoder: (String) -> T?): T? {
        val trimmed = raw.trim()
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start == -1 || end <= start) return null
        val slice = trimmed.substring(start, end + 1)
        return runCatching { decoder(slice) }.getOrNull()
    }
}
