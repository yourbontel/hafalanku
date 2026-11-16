package com.hafalan.quranmutun.data.drive

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

sealed class FolderFetchResult {
    data class Success(val files: List<ParsedDriveFile>) : FolderFetchResult()
    data class Error(val message: String) : FolderFetchResult()
}

object DriveFolderFetcher {
    private val entryPattern = Regex(
        """href="https://drive\.google\.com/file/d/([a-zA-Z0-9_-]{10,})/[^"]*"[^>]*>.*?class="flip-entry-title">([^<]+)<""",
        setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
    )
    private val fallbackFilePattern = Regex(
        """https://drive\.google\.com/file/d/([a-zA-Z0-9_-]{10,})/"""
    )

    suspend fun fetchPublicFolder(folderId: String): FolderFetchResult = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL("https://drive.google.com/embeddedfolderview?id=$folderId#list")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12_000
                readTimeout = 12_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")
            }
            try {
                val code = conn.responseCode
                if (code !in 200..299) {
                    return@withContext FolderFetchResult.Error(
                        "Gagal membaca folder (HTTP $code). Pastikan akses folder diset ke 'Siapa saja yang memiliki link'."
                    )
                }
                val html = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val parsed = parseEmbeddedFolderHtml(html)
                if (parsed.isEmpty()) {
                    FolderFetchResult.Error(
                        "Tidak ditemukan file di folder ini, atau folder masih bersifat privat. " +
                            "Pastikan folder dibagikan publik ('Siapa saja yang memiliki link') atau tempel link file satu per satu."
                    )
                } else {
                    FolderFetchResult.Success(parsed)
                }
            } finally {
                conn.disconnect()
            }
        }.getOrElse { ex ->
            FolderFetchResult.Error(
                "Tidak dapat terhubung ke Google Drive: ${ex.localizedMessage ?: "Periksa koneksi internet Anda."}"
            )
        }
    }

    internal fun parseEmbeddedFolderHtml(html: String): List<ParsedDriveFile> {
        val seenIds = LinkedHashSet<String>()
        val results = mutableListOf<ParsedDriveFile>()

        entryPattern.findAll(html).forEach { match ->
            val fileId = match.groupValues[1]
            val rawTitle = unescapeHtml(match.groupValues[2].trim())
            if (seenIds.add(fileId)) {
                results.add(
                    ParsedDriveFile(
                        fileId = fileId,
                        originalUrl = "https://drive.google.com/file/d/$fileId/view",
                        streamUrl = GoogleDriveLinkParser.buildStreamUrl(fileId),
                        suggestedTitle = rawTitle.ifBlank { "Audio Drive" }
                    )
                )
            }
        }

        if (results.isEmpty()) {
            fallbackFilePattern.findAll(html).forEachIndexed { index, match ->
                val fileId = match.groupValues[1]
                if (seenIds.add(fileId)) {
                    results.add(
                        ParsedDriveFile(
                            fileId = fileId,
                            originalUrl = "https://drive.google.com/file/d/$fileId/view",
                            streamUrl = GoogleDriveLinkParser.buildStreamUrl(fileId),
                            suggestedTitle = "Audio Folder #${index + 1}"
                        )
                    )
                }
            }
        }

        return results
    }

    private fun unescapeHtml(text: String): String =
        text.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
}
