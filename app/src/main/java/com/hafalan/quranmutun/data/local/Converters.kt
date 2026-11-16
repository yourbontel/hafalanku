package com.hafalan.quranmutun.data.local

import androidx.room.TypeConverter
import com.hafalan.quranmutun.data.model.AudioTargetType
import com.hafalan.quranmutun.data.model.MemorizationStatus

class Converters {
    @TypeConverter
    fun fromMemorizationStatus(value: MemorizationStatus): String = value.name

    @TypeConverter
    fun toMemorizationStatus(value: String): MemorizationStatus =
        runCatching { MemorizationStatus.valueOf(value) }.getOrDefault(MemorizationStatus.NOT_STARTED)

    @TypeConverter
    fun fromAudioTargetType(value: AudioTargetType): String = value.name

    @TypeConverter
    fun toAudioTargetType(value: String): AudioTargetType =
        runCatching { AudioTargetType.valueOf(value) }.getOrDefault(AudioTargetType.GENERAL)
}
