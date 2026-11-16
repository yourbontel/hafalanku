package com.hafalan.quranmutun.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hafalan.quranmutun.data.model.AudioTargetType
import com.hafalan.quranmutun.data.model.MemorizationStatus

@Entity(tableName = "surah_progress")
data class SurahProgressEntity(
    @PrimaryKey val surahNumber: Int,
    val nameLatin: String,
    val nameArabic: String,
    val meaningId: String,
    val totalVerses: Int,
    val juzStart: Int,
    val juzEnd: Int,
    val pageStart: Int,
    val pageEnd: Int,
    val memorizedVerses: Int = 0,
    val status: MemorizationStatus = MemorizationStatus.NOT_STARTED,
    val lastReviewedAt: Long? = null,
    val reviewCount: Int = 0,
    val notes: String = ""
) {
    val progressFraction: Float
        get() = if (totalVerses <= 0) 0f else (memorizedVerses.toFloat() / totalVerses).coerceIn(0f, 1f)
}

@Entity(tableName = "mutun_items")
data class MutunItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val author: String,
    val category: String,
    val unitLabel: String,
    val totalUnits: Int,
    val memorizedUnits: Int = 0,
    val status: MemorizationStatus = MemorizationStatus.NOT_STARTED,
    val lastReviewedAt: Long? = null,
    val reviewCount: Int = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val progressFraction: Float
        get() = if (totalUnits <= 0) 0f else (memorizedUnits.toFloat() / totalUnits).coerceIn(0f, 1f)
}

@Entity(
    tableName = "mutun_sections",
    foreignKeys = [
        ForeignKey(
            entity = MutunItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["mutunId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mutunId")]
)
data class MutunSectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val mutunId: Long,
    val orderIndex: Int,
    val title: String,
    val unitRangeLabel: String = "",
    val isCompleted: Boolean = false,
    val notes: String = ""
)

@Entity(
    tableName = "drive_audios",
    indices = [Index("surahNumber"), Index("mutunId")]
)
data class DriveAudioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val reciterOrSource: String = "",
    val originalUrl: String,
    val driveFileId: String,
    val streamUrl: String,
    val targetType: AudioTargetType = AudioTargetType.GENERAL,
    val surahNumber: Int? = null,
    val mutunId: Long? = null,
    val lastPlayedPositionMs: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
