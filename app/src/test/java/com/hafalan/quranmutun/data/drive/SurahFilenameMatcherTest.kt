package com.hafalan.quranmutun.data.drive

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SurahFilenameMatcherTest {

    @Test
    fun `detects surah from standard filenames`() {
        assertEquals(1, SurahFilenameMatcher.detectSurahNumber("001 Al-Fatihah.mp3"))
        assertEquals(2, SurahFilenameMatcher.detectSurahNumber("02_Al_Baqarah.m4a"))
        assertEquals(3, SurahFilenameMatcher.detectSurahNumber("Ali Imran - Mishary.mp3"))
        assertEquals(9, SurahFilenameMatcher.detectSurahNumber("009 At-Taubah.mp3"))
        assertEquals(9, SurahFilenameMatcher.detectSurahNumber("at_taubah_murajaah.ogg"))
    }

    @Test
    fun `falls back to leading track number`() {
        assertEquals(5, SurahFilenameMatcher.detectSurahNumber("005.mp3"))
        assertEquals(8, SurahFilenameMatcher.detectSurahNumber("08 - Audio.mp3"))
    }

    @Test
    fun `returns null for unrelated names`() {
        assertNull(SurahFilenameMatcher.detectSurahNumber("Tuhfatul Athfal Lengkap.mp3"))
    }
}
