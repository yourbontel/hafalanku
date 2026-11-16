package com.hafalan.quranmutun.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SurahDao {
    @Query("SELECT * FROM surah_progress ORDER BY surahNumber ASC")
    fun observeAll(): Flow<List<SurahProgressEntity>>

    @Query("SELECT COUNT(*) FROM surah_progress")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<SurahProgressEntity>)

    @Update
    suspend fun update(item: SurahProgressEntity)

    @Query("SELECT * FROM surah_progress WHERE surahNumber = :surahNumber LIMIT 1")
    suspend fun getByNumber(surahNumber: Int): SurahProgressEntity?
}

@Dao
interface MutunDao {
    @Query("SELECT * FROM mutun_items ORDER BY createdAt ASC, id ASC")
    fun observeAllMutun(): Flow<List<MutunItemEntity>>

    @Query("SELECT * FROM mutun_sections WHERE mutunId = :mutunId ORDER BY orderIndex ASC, id ASC")
    fun observeSectionsForMutun(mutunId: Long): Flow<List<MutunSectionEntity>>

    @Query("SELECT * FROM mutun_sections ORDER BY mutunId ASC, orderIndex ASC, id ASC")
    fun observeAllSections(): Flow<List<MutunSectionEntity>>

    @Query("SELECT COUNT(*) FROM mutun_items")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMutun(item: MutunItemEntity): Long

    @Update
    suspend fun updateMutun(item: MutunItemEntity)

    @Delete
    suspend fun deleteMutun(item: MutunItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<MutunSectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: MutunSectionEntity): Long

    @Update
    suspend fun updateSection(section: MutunSectionEntity)

    @Delete
    suspend fun deleteSection(section: MutunSectionEntity)
}

@Dao
interface DriveAudioDao {
    @Query("SELECT * FROM drive_audios ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<DriveAudioEntity>>

    @Query("SELECT * FROM drive_audios")
    suspend fun getAllOnce(): List<DriveAudioEntity>

    @Query("SELECT COUNT(*) FROM drive_audios WHERE driveFileId LIKE 'quranicaudio_5_%'")
    suspend fun countQuranicAudio5(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(audio: DriveAudioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(audios: List<DriveAudioEntity>)

    @Update
    suspend fun update(audio: DriveAudioEntity)

    @Delete
    suspend fun delete(audio: DriveAudioEntity)

    @Query("UPDATE drive_audios SET lastPlayedPositionMs = :positionMs WHERE id = :audioId")
    suspend fun updatePosition(audioId: Long, positionMs: Long)
}
