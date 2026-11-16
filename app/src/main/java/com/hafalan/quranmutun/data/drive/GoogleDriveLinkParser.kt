package com.hafalan.quranmutun.data.drive

data class ParsedDriveFile(
    val fileId: String,
    val originalUrl: String,
    val streamUrl: String,
    val suggestedTitle: String? = null
)

sealed class DriveUrlParseResult {
    data class File(val parsed: ParsedDriveFile) : DriveUrlParseResult()
    data class Folder(val folderId: String, val originalUrl: String) : DriveUrlParseResult()
    data class Invalid(val reason: String) : DriveUrlParseResult()
}

object GoogleDriveLinkParser {
    private val folderRegex = Regex(
        """drive\.google\.com/(?:drive/(?:u/\d+/)?folders/|embeddedfolderview\?id=)([a-zA-Z0-9_-]{10,})"""
    )
    private val filePathRegex = Regex(
        """drive\.google\.com/file/d/([a-zA-Z0-9_-]{10,})"""
    )
    private val queryIdRegex = Regex(
        """(?:drive|docs)\.google\.com/(?:open|uc)\?[^#\s]*\bid=([a-zA-Z0-9_-]{10,})"""
    )
    private val rawIdRegex = Regex("""^[a-zA-Z0-9_-]{20,80}$""")

    fun buildStreamUrl(fileId: String): String =
        "https://drive.google.com/uc?export=download&id=$fileId"

    fun parseSingle(input: String, suggestedTitle: String? = null): DriveUrlParseResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return DriveUrlParseResult.Invalid("Link Google Drive tidak boleh kosong.")
        }

        folderRegex.find(trimmed)?.groupValues?.getOrNull(1)?.let { folderId ->
            return DriveUrlParseResult.Folder(folderId = folderId, originalUrl = trimmed)
        }

        filePathRegex.find(trimmed)?.groupValues?.getOrNull(1)?.let { fileId ->
            return DriveUrlParseResult.File(
                ParsedDriveFile(
                    fileId = fileId,
                    originalUrl = trimmed,
                    streamUrl = buildStreamUrl(fileId),
                    suggestedTitle = suggestedTitle
                )
            )
        }

        queryIdRegex.find(trimmed)?.groupValues?.getOrNull(1)?.let { fileId ->
            return DriveUrlParseResult.File(
                ParsedDriveFile(
                    fileId = fileId,
                    originalUrl = trimmed,
                    streamUrl = buildStreamUrl(fileId),
                    suggestedTitle = suggestedTitle
                )
            )
        }

        if (rawIdRegex.matches(trimmed)) {
            return DriveUrlParseResult.File(
                ParsedDriveFile(
                    fileId = trimmed,
                    originalUrl = "https://drive.google.com/file/d/$trimmed/view",
                    streamUrl = buildStreamUrl(trimmed),
                    suggestedTitle = suggestedTitle
                )
            )
        }

        if (trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("http://", ignoreCase = true)
        ) {
            val cleanName = trimmed.substringAfterLast('/')
                .substringBefore('?')
                .substringBeforeLast('.')
                .takeIf { it.isNotBlank() }
            return DriveUrlParseResult.File(
                ParsedDriveFile(
                    fileId = "direct_${trimmed.hashCode().toUInt()}",
                    originalUrl = trimmed,
                    streamUrl = trimmed,
                    suggestedTitle = suggestedTitle ?: cleanName
                )
            )
        }

        return DriveUrlParseResult.Invalid(
            "Link tidak dikenali sebagai URL audio atau link Google Drive yang valid."
        )
    }

    /**
     * Parses multi-line input. Each line can be:
     * - https://drive.google.com/file/d/.../view
     * - Judul Audio | https://drive.google.com/file/d/.../view
     */
    fun parseBatchLines(rawInput: String): List<DriveUrlParseResult> {
        return rawInput
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { line ->
                val pipeParts = line.split("|", limit = 2)
                if (pipeParts.size == 2) {
                    val title = pipeParts[0].trim().takeIf { it.isNotEmpty() }
                    val url = pipeParts[1].trim()
                    parseSingle(url, suggestedTitle = title)
                } else {
                    parseSingle(line)
                }
            }
            .toList()
    }
}
