package com.hafalan.quranmutun.data.seed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranCatalogTest {

    @Test
    fun `contains all 114 surahs with total 6236 verses`() {
        val surahs = QuranCatalog.allSurahs
        assertEquals(114, surahs.size)
        assertEquals((1..114).toList(), surahs.map { it.surahNumber })
        assertEquals(6236, surahs.sumOf { it.totalVerses })
    }

    @Test
    fun `all surahs have valid juz and mushaf page ranges`() {
        QuranCatalog.allSurahs.forEach { surah ->
            assertTrue("Invalid juz for ${surah.nameLatin}", surah.juzStart in 1..30)
            assertTrue("Invalid juzEnd for ${surah.nameLatin}", surah.juzEnd in surah.juzStart..30)
            assertTrue("Invalid pageStart for ${surah.nameLatin}", surah.pageStart in 1..604)
            assertTrue("Invalid pageEnd for ${surah.nameLatin}", surah.pageEnd in surah.pageStart..604)
        }
    }
}
