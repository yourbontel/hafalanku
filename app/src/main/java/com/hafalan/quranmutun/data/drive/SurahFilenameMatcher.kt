package com.hafalan.quranmutun.data.drive

import com.hafalan.quranmutun.data.seed.QuranCatalog

object SurahFilenameMatcher {
    private val leadingNumberRegex = Regex("""^\s*0*(\d{1,3})(?:[\s._\-:|)]+|$)""")

    private val normalizedSurahNames: List<Pair<Int, String>> by lazy {
        QuranCatalog.allSurahs
            .map { it.surahNumber to normalize(it.nameLatin) }
            .sortedByDescending { it.second.length }
    }

    private val extraAliases: Map<String, Int> = mapOf(
        "fatihah" to 1,
        "baqarah" to 2,
        "baqoroh" to 2,
        "imran" to 3,
        "aliimran" to 3,
        "nisa" to 4,
        "annisa" to 4,
        "maidah" to 5,
        "almaidah" to 5,
        "anam" to 6,
        "alanam" to 6,
        "araf" to 7,
        "alaraf" to 7,
        "anfal" to 8,
        "alanfal" to 8,
        "taubah" to 9,
        "attaubah" to 9,
        "atobah" to 9,
        "baraah" to 9
    )

    fun detectSurahNumber(titleOrFilename: String): Int? {
        val trimmed = titleOrFilename.trim()
        if (trimmed.isEmpty()) return null

        val normalized = normalize(trimmed.substringBeforeLast('.'))
        // Match explicit Surah name first to avoid false positives on track numbers
        for ((number, surahNorm) in normalizedSurahNames) {
            if (normalized.contains(surahNorm)) {
                return number
            }
        }
        for ((alias, number) in extraAliases) {
            if (normalized.contains(alias)) {
                return number
            }
        }

        // Fall back to leading number (e.g. "001.mp3" or "09 - Audio.mp3")
        leadingNumberRegex.find(trimmed)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { num ->
            if (num in 1..114) return num
        }

        return null
    }

    internal fun normalize(input: String): String =
        input.lowercase()
            .replace("'", "")
            .replace("`", "")
            .replace("-", "")
            .replace("_", "")
            .replace(" ", "")
            .replace(Regex("[^a-z0-9]"), "")
}
