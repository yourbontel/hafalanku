package com.hafalan.quranmutun

import android.app.Application
import com.hafalan.quranmutun.data.local.AppDatabase
import com.hafalan.quranmutun.data.repository.HafalanRepository
import com.hafalan.quranmutun.player.AudioPlayerManager

class HafalanApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: HafalanRepository by lazy { HafalanRepository(database) }
    val audioPlayerManager: AudioPlayerManager by lazy { AudioPlayerManager(this) }

    override fun onTerminate() {
        super.onTerminate()
        audioPlayerManager.release()
    }
}
