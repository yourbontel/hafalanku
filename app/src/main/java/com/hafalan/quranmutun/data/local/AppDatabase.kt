package com.hafalan.quranmutun.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.withTransaction
import com.hafalan.quranmutun.data.seed.MutunSeedCatalog
import com.hafalan.quranmutun.data.seed.QuranCatalog

@Database(
    entities = [
        SurahProgressEntity::class,
        MutunItemEntity::class,
        MutunSectionEntity::class,
        DriveAudioEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun surahDao(): SurahDao
    abstract fun mutunDao(): MutunDao
    abstract fun driveAudioDao(): DriveAudioDao

    suspend fun ensureSeeded() {
        withTransaction {
            val surahCount = surahDao().count()
            if (surahCount == 0) {
                surahDao().insertAll(QuranCatalog.allSurahs)
            }
            val mutunCount = mutunDao().count()
            if (mutunCount == 0) {
                MutunSeedCatalog.defaults.forEach { template ->
                    val id = mutunDao().insertMutun(template.item)
                    val sections = MutunSeedCatalog.buildSections(id, template)
                    if (sections.isNotEmpty()) {
                        mutunDao().insertSections(sections)
                    }
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hafalan_quran_mutun.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
