package com.hafalan.quranmutun.data.seed

import com.hafalan.quranmutun.data.local.DriveAudioEntity
import com.hafalan.quranmutun.data.model.AudioTargetType

object QuranicAudioCatalog {
    const val QARI_5_NAME = "Mishari Rashid al-'Afasy"
    const val QARI_5_SOURCE_URL = "https://quranicaudio.com/quran/5"
    private const val QARI_5_CDN_BASE = "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/"

    fun buildQari5StreamUrl(surahNumber: Int): String {
        val padded = surahNumber.toString().padStart(3, '0')
        return "$QARI_5_CDN_BASE$padded.mp3"
    }

    fun qari5Entries(): List<DriveAudioEntity> {
        return QuranCatalog.allSurahs.map { surah ->
            val padded = surah.surahNumber.toString().padStart(3, '0')
            DriveAudioEntity(
                title = "$padded ${surah.nameLatin}",
                reciterOrSource = "$QARI_5_NAME (QuranicAudio)",
                originalUrl = QARI_5_SOURCE_URL,
                driveFileId = "quranicaudio_5_$padded",
                streamUrl = buildQari5StreamUrl(surah.surahNumber),
                targetType = AudioTargetType.QURAN,
                surahNumber = surah.surahNumber,
                mutunId = null,
                createdAt = 1_700_000_000_000L + surah.surahNumber
            )
        }
    }
}
