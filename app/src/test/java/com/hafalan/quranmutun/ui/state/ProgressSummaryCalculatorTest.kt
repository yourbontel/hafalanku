package com.hafalan.quranmutun.ui.state

import com.hafalan.quranmutun.data.local.MutunItemEntity
import com.hafalan.quranmutun.data.local.SurahProgressEntity
import com.hafalan.quranmutun.data.model.MemorizationStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressSummaryCalculatorTest {

    @Test
    fun `calculates quran, mutun, and review queue accurately`() {
        val now = 1_700_000_000_000L
        val fourDaysAgo = now - (4L * 24L * 60L * 60L * 1000L)
        val oneHourAgo = now - (60L * 60L * 1000L)

        val surahs = listOf(
            SurahProgressEntity(
                surahNumber = 1,
                nameLatin = "Al-Fatihah",
                nameArabic = "الفاتحة",
                meaningId = "Pembukaan",
                totalVerses = 7,
                juzStart = 1,
                juzEnd = 1,
                pageStart = 1,
                pageEnd = 1,
                memorizedVerses = 7,
                status = MemorizationStatus.MUTQIN,
                lastReviewedAt = oneHourAgo
            ),
            SurahProgressEntity(
                surahNumber = 67,
                nameLatin = "Al-Mulk",
                nameArabic = "الملك",
                meaningId = "Kerajaan",
                totalVerses = 30,
                juzStart = 29,
                juzEnd = 29,
                pageStart = 562,
                pageEnd = 564,
                memorizedVerses = 20,
                status = MemorizationStatus.MEMORIZING,
                lastReviewedAt = fourDaysAgo
            )
        )

        val mutun = listOf(
            MutunItemEntity(
                id = 1L,
                title = "Tuhfatul Athfal",
                author = "Al-Jamzuri",
                category = "Tajwid",
                unitLabel = "Bait",
                totalUnits = 61,
                memorizedUnits = 30,
                status = MemorizationStatus.NEEDS_REVIEW,
                lastReviewedAt = oneHourAgo
            )
        )

        val summary = ProgressSummaryCalculator.calculate(surahs, mutun, nowMillis = now)
        assertEquals(37, summary.totalQuranVerses)
        assertEquals(27, summary.memorizedQuranVerses)
        assertEquals(1, summary.mutqinSurahCount)
        assertEquals(2, summary.activeSurahCount)
        assertEquals(30, summary.memorizedMutunUnits)
        assertEquals(61, summary.totalMutunUnits)
        assertEquals(listOf(67), summary.surahsNeedingReview.map { it.surahNumber })
        assertEquals(listOf(1L), summary.mutunNeedingReview.map { it.id })
    }
}
