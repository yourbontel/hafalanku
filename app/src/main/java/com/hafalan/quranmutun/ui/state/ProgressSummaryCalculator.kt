package com.hafalan.quranmutun.ui.state

import com.hafalan.quranmutun.data.local.MutunItemEntity
import com.hafalan.quranmutun.data.local.SurahProgressEntity
import com.hafalan.quranmutun.data.model.MemorizationStatus

data class DashboardSummary(
    val totalQuranVerses: Int,
    val memorizedQuranVerses: Int,
    val mutqinSurahCount: Int,
    val activeSurahCount: Int,
    val estimatedJuzMemorized: Float,
    val totalMutunKits: Int,
    val mutqinMutunCount: Int,
    val memorizedMutunUnits: Int,
    val totalMutunUnits: Int,
    val surahsNeedingReview: List<SurahProgressEntity>,
    val mutunNeedingReview: List<MutunItemEntity>
) {
    val quranProgressFraction: Float
        get() = if (totalQuranVerses <= 0) 0f else (memorizedQuranVerses.toFloat() / totalQuranVerses).coerceIn(0f, 1f)

    val mutunProgressFraction: Float
        get() = if (totalMutunUnits <= 0) 0f else (memorizedMutunUnits.toFloat() / totalMutunUnits).coerceIn(0f, 1f)
}

object ProgressSummaryCalculator {
    private const val THREE_DAYS_MILLIS = 3L * 24L * 60L * 60L * 1000L

    fun calculate(
        surahs: List<SurahProgressEntity>,
        mutunItems: List<MutunItemEntity>,
        nowMillis: Long = System.currentTimeMillis()
    ): DashboardSummary {
        val totalVerses = surahs.sumOf { it.totalVerses }
        val memorizedVerses = surahs.sumOf { it.memorizedVerses.coerceIn(0, it.totalVerses) }
        val mutqinSurahs = surahs.count { it.status == MemorizationStatus.MUTQIN }
        val activeSurahs = surahs.count {
            it.status != MemorizationStatus.NOT_STARTED || it.memorizedVerses > 0
        }
        val estimatedJuz = if (totalVerses <= 0) 0f else (memorizedVerses.toFloat() / totalVerses) * 30f

        val totalMutunUnits = mutunItems.sumOf { it.totalUnits }
        val memorizedMutunUnits = mutunItems.sumOf { it.memorizedUnits.coerceIn(0, it.totalUnits) }
        val mutqinMutun = mutunItems.count { it.status == MemorizationStatus.MUTQIN }

        val surahsForReview = surahs.filter { surah ->
            surah.status == MemorizationStatus.NEEDS_REVIEW ||
                (surah.memorizedVerses > 0 &&
                    (surah.lastReviewedAt == null || (nowMillis - surah.lastReviewedAt) >= THREE_DAYS_MILLIS))
        }.sortedWith(
            compareByDescending<SurahProgressEntity> { it.status == MemorizationStatus.NEEDS_REVIEW }
                .thenBy { it.lastReviewedAt ?: 0L }
                .thenBy { it.surahNumber }
        )

        val mutunForReview = mutunItems.filter { item ->
            item.status == MemorizationStatus.NEEDS_REVIEW ||
                (item.memorizedUnits > 0 &&
                    (item.lastReviewedAt == null || (nowMillis - item.lastReviewedAt) >= THREE_DAYS_MILLIS))
        }.sortedWith(
            compareByDescending<MutunItemEntity> { it.status == MemorizationStatus.NEEDS_REVIEW }
                .thenBy { it.lastReviewedAt ?: 0L }
                .thenBy { it.id }
        )

        return DashboardSummary(
            totalQuranVerses = totalVerses,
            memorizedQuranVerses = memorizedVerses,
            mutqinSurahCount = mutqinSurahs,
            activeSurahCount = activeSurahs,
            estimatedJuzMemorized = estimatedJuz,
            totalMutunKits = mutunItems.size,
            mutqinMutunCount = mutqinMutun,
            memorizedMutunUnits = memorizedMutunUnits,
            totalMutunUnits = totalMutunUnits,
            surahsNeedingReview = surahsForReview,
            mutunNeedingReview = mutunForReview
        )
    }
}
