package com.hafalan.quranmutun.data.drive

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleDriveLinkParserTest {

    @Test
    fun `parses standard share file link`() {
        val url = "https://drive.google.com/file/d/1AbCdEfGhIjKlMnOpQrStUvWxYz12345/view?usp=sharing"
        val result = GoogleDriveLinkParser.parseSingle(url)
        assertTrue(result is DriveUrlParseResult.File)
        val file = (result as DriveUrlParseResult.File).parsed
        assertEquals("1AbCdEfGhIjKlMnOpQrStUvWxYz12345", file.fileId)
        assertEquals(
            "https://drive.google.com/uc?export=download&id=1AbCdEfGhIjKlMnOpQrStUvWxYz12345",
            file.streamUrl
        )
    }

    @Test
    fun `parses open id and uc download links`() {
        val openRes = GoogleDriveLinkParser.parseSingle(
            "https://drive.google.com/open?id=1AbCdEfGhIjKlMnOpQrStUvWxYz12345"
        )
        assertEquals(
            "1AbCdEfGhIjKlMnOpQrStUvWxYz12345",
            (openRes as DriveUrlParseResult.File).parsed.fileId
        )

        val ucRes = GoogleDriveLinkParser.parseSingle(
            "https://drive.google.com/uc?export=download&id=9ZyXwVuTsRqPoNmLkJiHgFeDcBa54321"
        )
        assertEquals(
            "9ZyXwVuTsRqPoNmLkJiHgFeDcBa54321",
            (ucRes as DriveUrlParseResult.File).parsed.fileId
        )
    }

    @Test
    fun `parses public folder link`() {
        val url = "https://drive.google.com/drive/folders/1FolderIdExample_987654321?usp=drive_link"
        val result = GoogleDriveLinkParser.parseSingle(url)
        assertTrue(result is DriveUrlParseResult.Folder)
        assertEquals("1FolderIdExample_987654321", (result as DriveUrlParseResult.Folder).folderId)
    }

    @Test
    fun `parses batch lines with custom titles`() {
        val input = """
            Al-Mulk Mishary | https://drive.google.com/file/d/1AbCdEfGhIjKlMnOpQrStUvWxYz12345/view
            https://drive.google.com/open?id=9ZyXwVuTsRqPoNmLkJiHgFeDcBa54321
        """.trimIndent()

        val results = GoogleDriveLinkParser.parseBatchLines(input)
        assertEquals(2, results.size)
        val first = (results[0] as DriveUrlParseResult.File).parsed
        assertEquals("Al-Mulk Mishary", first.suggestedTitle)
        assertEquals("1AbCdEfGhIjKlMnOpQrStUvWxYz12345", first.fileId)

        val second = (results[1] as DriveUrlParseResult.File).parsed
        assertEquals(null, second.suggestedTitle)
        assertEquals("9ZyXwVuTsRqPoNmLkJiHgFeDcBa54321", second.fileId)
    }

    @Test
    fun `parses direct https audio url`() {
        val url = "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/001.mp3"
        val result = GoogleDriveLinkParser.parseSingle(url)
        assertTrue(result is DriveUrlParseResult.File)
        val file = (result as DriveUrlParseResult.File).parsed
        assertEquals(url, file.streamUrl)
        assertEquals("001", file.suggestedTitle)
    }

    @Test
    fun `returns invalid for non-url text`() {
        val result = GoogleDriveLinkParser.parseSingle("bukan link audio")
        assertTrue(result is DriveUrlParseResult.Invalid)
    }

    @Test
    fun `parses embedded folder html entries`() {
        val sampleHtml = """
            <div class="flip-entry">
              <a href="https://drive.google.com/file/d/1FileAlpha1234567890/view?usp=drive_web">
                <div class="flip-entry-title">01 Al-Fatihah.mp3</div>
              </a>
            </div>
            <div class="flip-entry">
              <a href="https://drive.google.com/file/d/1FileBeta0987654321/view?usp=drive_web">
                <div class="flip-entry-title">02 Tuhfatul Athfal &amp; Syarh.m4a</div>
              </a>
            </div>
        """.trimIndent()

        val files = DriveFolderFetcher.parseEmbeddedFolderHtml(sampleHtml)
        assertEquals(2, files.size)
        assertEquals("01 Al-Fatihah.mp3", files[0].suggestedTitle)
        assertEquals("1FileAlpha1234567890", files[0].fileId)
        assertEquals("02 Tuhfatul Athfal & Syarh.m4a", files[1].suggestedTitle)
    }
}
