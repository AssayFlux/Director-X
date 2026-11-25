package com.directorx.editor.engine

/**
 * Product-scope language matrix for Upgrade 2.
 *
 * This registry describes the 36 requested language/script targets separately
 * from runtime availability. It deliberately does not imply that a model's
 * published language list is an integrated or quality-accepted capability.
 *
 * Current Alpha runtime uses the English-only Whisper tiny.en path. Translation
 * has no integrated runtime. These facts are represented explicitly so UI and
 * orchestration code cannot mistake a roadmap target for a working feature.
 */
object SubtitleLanguageRegistry {
    enum class Priority { P0, P1 }
    enum class ScriptDirection { LEFT_TO_RIGHT, RIGHT_TO_LEFT }
    enum class AsrAvailability { ACTIVE_ENGLISH_ONLY, NOT_INTEGRATED }
    enum class TranslationAvailability { NOT_INTEGRATED }

    data class Language(
        val tag: String,
        val displayName: String,
        val priority: Priority,
        val direction: ScriptDirection = ScriptDirection.LEFT_TO_RIGHT,
        /** Whisper language token when a candidate mapping is known; not an availability claim. */
        val whisperCandidateCode: String? = null,
        val asrAvailability: AsrAvailability = AsrAvailability.NOT_INTEGRATED,
        val translationAvailability: TranslationAvailability = TranslationAvailability.NOT_INTEGRATED,
    )

    val all: List<Language> = listOf(
        Language("en", "English", Priority.P0, whisperCandidateCode = "en", asrAvailability = AsrAvailability.ACTIVE_ENGLISH_ONLY),
        Language("hi", "Hindi", Priority.P0, whisperCandidateCode = "hi"),
        Language("bn", "Bengali", Priority.P0, whisperCandidateCode = "bn"),
        Language("es", "Spanish", Priority.P0, whisperCandidateCode = "es"),
        Language("fr", "French", Priority.P0, whisperCandidateCode = "fr"),
        Language("de", "German", Priority.P0, whisperCandidateCode = "de"),
        Language("pt", "Portuguese", Priority.P0, whisperCandidateCode = "pt"),
        Language("ar", "Arabic", Priority.P0, ScriptDirection.RIGHT_TO_LEFT, "ar"),
        Language("ja", "Japanese", Priority.P0, whisperCandidateCode = "ja"),
        Language("ko", "Korean", Priority.P0, whisperCandidateCode = "ko"),
        Language("zh-Hans", "Chinese (Simplified)", Priority.P0, whisperCandidateCode = "zh"),
        Language("ru", "Russian", Priority.P0, whisperCandidateCode = "ru"),
        Language("zh-Hant", "Chinese (Traditional)", Priority.P1, whisperCandidateCode = "zh"),
        Language("it", "Italian", Priority.P1, whisperCandidateCode = "it"),
        Language("id", "Indonesian", Priority.P1, whisperCandidateCode = "id"),
        Language("ur", "Urdu", Priority.P1, ScriptDirection.RIGHT_TO_LEFT, "ur"),
        Language("tr", "Turkish", Priority.P1, whisperCandidateCode = "tr"),
        Language("vi", "Vietnamese", Priority.P1, whisperCandidateCode = "vi"),
        Language("th", "Thai", Priority.P1, whisperCandidateCode = "th"),
        Language("ta", "Tamil", Priority.P1, whisperCandidateCode = "ta"),
        Language("te", "Telugu", Priority.P1, whisperCandidateCode = "te"),
        Language("mr", "Marathi", Priority.P1),
        Language("gu", "Gujarati", Priority.P1),
        Language("pa", "Punjabi", Priority.P1),
        Language("ml", "Malayalam", Priority.P1, whisperCandidateCode = "ml"),
        Language("kn", "Kannada", Priority.P1, whisperCandidateCode = "kn"),
        Language("fa", "Persian", Priority.P1, ScriptDirection.RIGHT_TO_LEFT, "fa"),
        Language("pl", "Polish", Priority.P1, whisperCandidateCode = "pl"),
        Language("nl", "Dutch", Priority.P1, whisperCandidateCode = "nl"),
        Language("uk", "Ukrainian", Priority.P1, whisperCandidateCode = "uk"),
        Language("sw", "Swahili", Priority.P1),
        Language("fil", "Filipino", Priority.P1, whisperCandidateCode = "tl"),
        Language("ne", "Nepali", Priority.P1),
        Language("si", "Sinhala", Priority.P1),
        Language("el", "Greek", Priority.P1, whisperCandidateCode = "el"),
        Language("he", "Hebrew", Priority.P1, ScriptDirection.RIGHT_TO_LEFT, "he"),
    )

    private val byTag = all.associateBy { it.tag }

    init {
        require(all.size == 36) { "Upgrade 2 target language registry must contain exactly 36 entries." }
        require(byTag.size == all.size) { "Upgrade 2 language tags must be unique." }
    }

    fun find(tag: String): Language? = byTag[tag.trim()]

    fun byPriority(priority: Priority): List<Language> = all.filter { it.priority == priority }

    fun rightToLeft(): List<Language> = all.filter { it.direction == ScriptDirection.RIGHT_TO_LEFT }
}
