package com.hafalan.quranmutun.data.repository

import com.hafalan.quranmutun.data.local.AppDatabase
import com.hafalan.quranmutun.data.local.DriveAudioEntity
import com.hafalan.quranmutun.data.local.MutunItemEntity
import com.hafalan.quranmutun.data.local.MutunSectionEntity
import com.hafalan.quranmutun.data.local.SurahProgressEntity
import com.hafalan.quranmutun.data.model.MemorizationStatus
import kotlinx.coroutines.flow.Flow

class HafalanRepository(private val db: AppDatabase) {
    private val surahDao = db.surahDao()
    private val mutunDao = db.mutunDao()
    private val audioDao = db.driveAudioDao()

    suspend fun initialize() {
        db.ensureSeeded()
    }

    fun observeSurahs(): Flow<List<SurahProgressEntity>> = surahDao.observeAll()

    fun observeMutunItems(): Flow<List<MutunItemEntity>> = mutunDao.observeAllMutun()

    fun observeAllMutunSections(): Flow<List<MutunSectionEntity>> = mutunDao.observeAllSections()

    fun observeAudios(): Flow<List<DriveAudioEntity>> = audioDao.observeAll()

    suspend fun updateSurah(surah: SurahProgressEntity) {
        surahDao.update(surah)
    }

    suspend fun markSurahReviewedToday(surah: SurahProgressEntity) {
        val nextStatus = if (surah.status == MemorizationStatus.NOT_STARTED && surah.memorizedVerses > 0) {
            MemorizationStatus.MEMORIZING
        } else {
            surah.status
        }
        surahDao.update(
            surah.copy(
                status = nextStatus,
                lastReviewedAt = System.currentTimeMillis(),
                reviewCount = surah.reviewCount + 1
            )
        )
    }

    suspend fun addMutun(item: MutunItemEntity, sectionTitles: List<String> = emptyList()): Long {
        val id = mutunDao.insertMutun(item)
        if (sectionTitles.isNotEmpty()) {
            val sections = sectionTitles.mapIndexed { index, title ->
                MutunSectionEntity(
                    mutunId = id,
                    orderIndex = index,
                    title = title.trim()
                )
            }
            mutunDao.insertSections(sections)
        }
        return id
    }

    suspend fun updateMutun(item: MutunItemEntity) {
        mutunDao.updateMutun(item)
    }

    suspend fun deleteMutun(item: MutunItemEntity) {
        mutunDao.deleteMutun(item)
    }

    suspend fun markMutunReviewedToday(item: MutunItemEntity) {
        val nextStatus = if (item.status == MemorizationStatus.NOT_STARTED && item.memorizedUnits > 0) {
            MemorizationStatus.MEMORIZING
        } else {
            item.status
        }
        mutunDao.updateMutun(
            item.copy(
                status = nextStatus,
                lastReviewedAt = System.currentTimeMillis(),
                reviewCount = item.reviewCount + 1
            )
        )
    }

    suspend fun addMutunSection(mutunId: Long, orderIndex: Int, title: String, unitRangeLabel: String) {
        mutunDao.insertSection(
            MutunSectionEntity(
                mutunId = mutunId,
                orderIndex = orderIndex,
                title = title.trim(),
                unitRangeLabel = unitRangeLabel.trim()
            )
        )
    }

    suspend fun toggleMutunSection(section: MutunSectionEntity) {
        mutunDao.updateSection(section.copy(isCompleted = !section.isCompleted))
    }

    suspend fun deleteMutunSection(section: MutunSectionEntity) {
        mutunDao.deleteSection(section)
    }

    suspend fun addAudio(audio: DriveAudioEntity): Long = audioDao.insert(audio)

    suspend fun addAudios(audios: List<DriveAudioEntity>) {
        if (audios.isNotEmpty()) {
            audioDao.insertAll(audios)
        }
    }

    suspend fun updateAudio(audio: DriveAudioEntity) {
        audioDao.update(audio)
    }

    suspend fun deleteAudio(audio: DriveAudioEntity) {
        audioDao.delete(audio)
    }

    suspend fun saveAudioPosition(audioId: Long, positionMs: Long) {
        audioDao.updatePosition(audioId, positionMs)
    }
}
