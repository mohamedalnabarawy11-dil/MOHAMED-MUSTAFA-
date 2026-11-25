package com.example.data.remote

import com.example.data.model.AppLanguage
import com.example.data.model.FlashcardItem
import com.example.data.model.LessonBreakdown
import com.example.data.model.QuestionType
import com.example.data.model.QuizDifficulty
import com.example.data.model.QuizQuestion
import java.util.UUID

object DemoStudyEngine {

    fun buildLessonBreakdown(
        sourceText: String,
        subjectTag: String,
        hasImage: Boolean,
        isCramMode: Boolean,
        language: AppLanguage
    ): LessonBreakdown {
        val cleanText = sourceText.trim()
        val topicLabel = subjectTag.ifBlank {
            if (language == AppLanguage.AR) "موضوع الدرس" else "Study Topic"
        }
        val sentences = extractKeySentences(cleanText)

        return if (language == AppLanguage.AR) {
            val intro = if (hasImage && cleanText.isBlank()) {
                "يا بطل! أنا الأستاذ الشاطر، وشفت الصورة اللي رفعتها في مادة ($topicLabel). خلّينا نمسك الفكرة الأساسية ونفصصها على الهادي:"
            } else {
                "أهلاً بك يا بطل في حصة ($topicLabel)! تعالَ نرتب كلام الدرس ده في دماغك كأننا بنحكي حكاية واضحة:"
            }
            val body = if (sentences.isNotEmpty()) {
                sentences.take(if (isCramMode) 3 else 5).joinToString("\n• ", prefix = "• ")
            } else {
                "• الفكرة الجوهرية في الدرس بتدور حوالين فهم العلاقة بين السبب والنتيجة بدل الحفظ الأعمى.\n" +
                    "• لو ركّزت على الكلمات المفتاحية في الصورة أو النص، هتلاقي إن كل مسألة بتتحل في خطوتين."
            }
            val cramPrefix = if (isCramMode) "⚡ [وضع الزنقة - الخلاصة في 60 ثانية]\n" else ""

            LessonBreakdown(
                id = UUID.randomUUID().toString(),
                subjectTag = topicLabel,
                sourceText = cleanText,
                simplifiedExplanation = "$cramPrefix$intro\n\n$body",
                outsideConnection = "تخيل الموضوع ده زي تخطيط رحلة بالقطر: قبل ما تتحرك لازم تعرف نقطة البداية (المعطيات) والمحطة اللي رايح لها (المطلوب)، وبكده تربط أي فكرة في ($topicLabel) بموقف حقيقي في حياتك اليومية ومش هتنساها في الامتحان.",
                importantQuestionsRadar = listOf(
                    "سؤال متكرر: اشرح الفكرة الأساسية في ($topicLabel) مع ذكر مثال تطبيقي واضح.",
                    "سؤال تريكّة: ماذا يحدث لو تغيّر الشرط الرئيسي المذكور في الدرس؟",
                    "قارن باختصار بين المفهوم الأول والنتيجة المترتبة عليه في هذا الجزء."
                ),
                mustMemorize = if (sentences.isNotEmpty()) {
                    sentences.take(3).mapIndexed { i, s -> "قاعدة ${i + 1}: ${s.take(90)}" }
                } else {
                    listOf(
                        "التعريف الأساسي ومصطلحات ($topicLabel) الرئيسية.",
                        "الترتيب المنطقي للخطوات أو القانون الأساسي.",
                        "وحدات القياس أو الكلمات الدلالية في السؤال."
                    )
                },
                isCramMode = isCramMode
            )
        } else {
            val intro = if (hasImage && cleanText.isBlank()) {
                "Hey champ! I've reviewed your attached study page for ($topicLabel). Here is the crystal-clear breakdown:"
            } else {
                "Welcome to ($topicLabel)! Let's break this down step by step so it clicks naturally:"
            }
            val body = if (sentences.isNotEmpty()) {
                sentences.take(if (isCramMode) 3 else 5).joinToString("\n• ", prefix = "• ")
            } else {
                "• Focus first on the core cause-and-effect relationship instead of rote memorization.\n" +
                    "• Once you anchor the main rule, every exam variation becomes predictable."
            }
            val cramPrefix = if (isCramMode) "⚡ [Cram Mode - High-Yield Brief]\n" else ""

            LessonBreakdown(
                id = UUID.randomUUID().toString(),
                subjectTag = topicLabel,
                sourceText = cleanText,
                simplifiedExplanation = "$cramPrefix$intro\n\n$body",
                outsideConnection = "Think of ($topicLabel) like assembling a puzzle: lock down the corner pieces (core definitions) first, and every real-world example snaps into place naturally.",
                importantQuestionsRadar = listOf(
                    "Classic Exam Prompt: Explain the main principle of ($topicLabel) in your own words.",
                    "Trap Question: What happens if the primary condition is reversed or removed?",
                    "Compare the key inputs and resulting outcome covered in this section."
                ),
                mustMemorize = if (sentences.isNotEmpty()) {
                    sentences.take(3).mapIndexed { i, s -> "Key Fact ${i + 1}: ${s.take(90)}" }
                } else {
                    listOf(
                        "Core terminology and definition of ($topicLabel).",
                        "Primary formula or ordered sequence of steps.",
                        "Common exam keywords and units."
                    )
                },
                isCramMode = isCramMode
            )
        }
    }

    fun clarifyPoint(point: String, lesson: LessonBreakdown, language: AppLanguage): String {
        val cleanPoint = point.trim()
        return if (language == AppLanguage.AR) {
            "بص يا سيدي على النقطة دي (\"$cleanPoint\"): ببساطة خالص، اربطها دايماً بسؤال: ليه بتحصل؟ وإزاي بنستخدمها؟ في درس (${lesson.subjectTag}) الجزء ده معناه إن كل خطوة بتسلم اللي بعدها عشان توصل للنتيجة الصح من غير لخبطة."
        } else {
            "Great question on \"$cleanPoint\"! In (${lesson.subjectTag}), think of it as a direct bridge between the given rule and the final outcome—once you spot the keyword, apply the rule one step at a time."
        }
    }

    fun evaluateTeachBack(
        studentExplanation: String,
        lesson: LessonBreakdown,
        language: AppLanguage
    ): String {
        val wordsCount = studentExplanation.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
        return if (language == AppLanguage.AR) {
            val praise = if (wordsCount >= 6) {
                "ممتاز يا بطل! شرحك بيوضح إنك لقطت الفكرة الرئيسية في (${lesson.subjectTag}) ورتبتها بدماغك صح."
            } else {
                "بداية جميلة يا بطل! الفكرة واصلة، بس حاول المرة الجاية تزود مثال صغير عشان تثبت المعلومة أكتر."
            }
            "$praise\nتأكد دايماً إنك حافظ النقاط الأساسية من كارت الحفظ.\n\nعايز تدريب كمان ولا تمام كده؟"
        } else {
            val praise = if (wordsCount >= 6) {
                "Awesome work! Your summary captures the main concept of (${lesson.subjectTag}) cleanly in your own words."
            } else {
                "Nice start! You've got the core idea—try adding one quick concrete example to lock it in completely."
            }
            "$praise\n\nWant more practice or are you good?"
        }
    }

    fun buildQuizFromContent(
        content: String,
        hasImage: Boolean,
        types: Set<QuestionType>,
        difficulty: QuizDifficulty,
        count: Int,
        totalScore: Int,
        language: AppLanguage
    ): List<QuizQuestion> {
        val sentences = extractKeySentences(content)
        val activeTypes = types.ifEmpty { setOf(QuestionType.MCQ) }.toList()
        val pointsPerQ = (totalScore / count.coerceAtLeast(1)).coerceAtLeast(1)

        return (0 until count).map { idx ->
            val qType = activeTypes[idx % activeTypes.size]
            val snippet = sentences.getOrNull(idx % sentences.size.coerceAtLeast(1))
                ?.take(85)
                ?: if (language == AppLanguage.AR) "الفكرة الأساسية في المحتوى المرفوع" else "the core concept in the uploaded content"

            when (qType) {
                QuestionType.MCQ -> if (language == AppLanguage.AR) {
                    val optA = snippet
                    val optB = "عكس ما ورد في النص المرفوع تماماً"
                    val optC = "معلومة خارجية غير مرتبطة بالمحتوى"
                    val optD = "افتراض عشوائي بدون دليل من الدرس"
                    QuizQuestion(
                        id = "demo_q_$idx",
                        type = QuestionType.MCQ,
                        prompt = "بناءً على المحتوى المرفوع (${idx + 1}): أي عبارة تعبر بدقة عن: «${snippet.take(50)}»؟",
                        options = listOf(optA, optB, optC, optD),
                        correctAnswer = optA,
                        explanation = "تم استخراج الإجابة مباشرة من المحتوى المرفوع: $snippet",
                        points = pointsPerQ
                    )
                } else {
                    val optA = snippet
                    val optB = "The opposite of what is stated in the uploaded content"
                    val optC = "An unrelated external assumption"
                    val optD = "None of the concepts in the provided material"
                    QuizQuestion(
                        id = "demo_q_$idx",
                        type = QuestionType.MCQ,
                        prompt = "Based strictly on your content (#${idx + 1}): Which statement accurately reflects \"${snippet.take(50)}\"?",
                        options = listOf(optA, optB, optC, optD),
                        correctAnswer = optA,
                        explanation = "Grounded directly in your provided content: $snippet",
                        points = pointsPerQ
                    )
                }

                QuestionType.TRUE_FALSE -> if (language == AppLanguage.AR) {
                    val trueOpt = "صح"
                    val falseOpt = "غلط"
                    val isTrueStatement = idx % 2 == 0
                    val prompt = if (isTrueStatement) {
                        "صح أم غلط: ورد في المحتوى المرفوع أن «$snippet»."
                    } else {
                        "صح أم غلط: لا توجد أي علاقة في المحتوى المرفوع بين الفكرة الأساسية وما ذُكر حول «${snippet.take(45)}»."
                    }
                    QuizQuestion(
                        id = "demo_q_$idx",
                        type = QuestionType.TRUE_FALSE,
                        prompt = prompt,
                        options = listOf(trueOpt, falseOpt),
                        correctAnswer = if (isTrueStatement) trueOpt else falseOpt,
                        explanation = "وفقاً للنص المرفوع: $snippet",
                        points = pointsPerQ
                    )
                } else {
                    val trueOpt = "True"
                    val falseOpt = "False"
                    val isTrueStatement = idx % 2 == 0
                    val prompt = if (isTrueStatement) {
                        "True or False: The uploaded material states that \"$snippet\"."
                    } else {
                        "True or False: The uploaded material completely contradicts \"$snippet\"."
                    }
                    QuizQuestion(
                        id = "demo_q_$idx",
                        type = QuestionType.TRUE_FALSE,
                        prompt = prompt,
                        options = listOf(trueOpt, falseOpt),
                        correctAnswer = if (isTrueStatement) trueOpt else falseOpt,
                        explanation = "Grounded in your study material: $snippet",
                        points = pointsPerQ
                    )
                }

                QuestionType.SHORT_ANSWER -> if (language == AppLanguage.AR) {
                    QuizQuestion(
                        id = "demo_q_$idx",
                        type = QuestionType.SHORT_ANSWER,
                        prompt = "باختصار من المحتوى المرفوع: اشرح المقصود بـ «${snippet.take(55)}»؟",
                        options = emptyList(),
                        correctAnswer = snippet,
                        explanation = "الإجابة النموذجية من المحتوى: $snippet",
                        points = pointsPerQ
                    )
                } else {
                    QuizQuestion(
                        id = "demo_q_$idx",
                        type = QuestionType.SHORT_ANSWER,
                        prompt = "Briefly from the uploaded content: Summarize what is meant by \"${snippet.take(55)}\".",
                        options = emptyList(),
                        correctAnswer = snippet,
                        explanation = "Reference answer from your notes: $snippet",
                        points = pointsPerQ
                    )
                }
            }
        }
    }

    fun buildFlashcardsFromContent(
        content: String,
        subject: String,
        language: AppLanguage
    ): List<FlashcardItem> {
        val sentences = extractKeySentences(content)
        val subj = subject.ifBlank { if (language == AppLanguage.AR) "مذاكرة عامة" else "General Study" }
        if (sentences.isEmpty()) {
            return if (language == AppLanguage.AR) {
                listOf(
                    FlashcardItem(UUID.randomUUID().toString(), "ما هي الفكرة الأساسية للدرس؟", "راجع كارت الشرح المبسط واربط السبب بالنتيجة.", subj),
                    FlashcardItem(UUID.randomUUID().toString(), "كيف تتجنب فخ الامتحان؟", "اقرأ المعطيات كاملة وتأكد من المطلوب قبل الحل.", subj),
                    FlashcardItem(UUID.randomUUID().toString(), "قاعدة المراجعة الذهبية", "اشرح المعلومة لنفسك في سطر واحد بدون النظر للكتاب.", subj)
                )
            } else {
                listOf(
                    FlashcardItem(UUID.randomUUID().toString(), "Core Lesson Takeaway", "Review the simplified breakdown and connect cause to effect.", subj),
                    FlashcardItem(UUID.randomUUID().toString(), "Exam Trap Check", "Read every given constraint carefully before answering.", subj),
                    FlashcardItem(UUID.randomUUID().toString(), "Feynman Rule", "Explain the concept in one plain sentence from memory.", subj)
                )
            }
        }

        return sentences.take(6).mapIndexed { i, sentence ->
            val words = sentence.split(Regex("\\s+")).filter { it.isNotBlank() }
            val front = if (language == AppLanguage.AR) {
                "نقطة ${i + 1} (${subj}): ما معنى «${words.take(5).joinToString(" ")}...»؟"
            } else {
                "Card ${i + 1} (${subj}): What does \"${words.take(5).joinToString(" ")}...\" mean?"
            }
            FlashcardItem(
                id = UUID.randomUUID().toString(),
                front = front,
                back = sentence,
                subject = subj
            )
        }
    }

    private fun extractKeySentences(text: String): List<String> {
        return text
            .split(Regex("[.\\n!؟?؛;]+"))
            .map { it.trim() }
            .filter { it.length >= 10 }
            .distinct()
    }
}
