package com.hafalan.quranmutun

import android.app.Application
import com.hafalan.quranmutun.data.local.AppDatabase
import com.hafalan.quranmutun.data.local.UserPreferences
import com.hafalan.quranmutun.data.repository.HafalanRepository
import com.hafalan.quranmutun.player.AudioPlayerManager
import com.hafalan.quranmutun.player.SequentialLockPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HafalanApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val userPreferences: UserPreferences by lazy { UserPreferences(this) }
    val repository: HafalanRepository by lazy { HafalanRepository(database) }
    val audioPlayerManager: AudioPlayerManager by lazy {
        AudioPlayerManager(
            context = this,
            completedAudioIdsProvider = { userPreferences.state.value.completedAudioIds },
            onAudioPlayFinished = { audioId ->
                userPreferences.incrementAudioPlayCount(audioId)
            },
            onAudioSequenceCompleted = { audio ->
                val prefsBefore = userPreferences.state.value
                val completedBefore = prefsBefore.completedAudioIds
                userPreferences.markAudioCompleted(audio.id)

                appScope.launch {
                    val allAudios = database.driveAudioDao().getAllOnce()
                    val ordered = SequentialLockPolicy.orderedPlaylistForCategory(
                        allAudios = allAudios,
                        targetType = audio.targetType,
                        minHafalanSurah = prefsBefore.minHafalanSurah,
                        maxHafalanSurah = prefsBefore.maxHafalanSurah
                    )
                    if (SequentialLockPolicy.shouldIncrementCycleOnCompletion(
                            orderedPlaylist = ordered,
                            completedBefore = completedBefore,
                            justCompletedAudioId = audio.id
                        )
                    ) {
                        userPreferences.incrementCycleCount(audio.targetType)
                    }
                }
            }
        )
    }

    override fun onTerminate() {
        super.onTerminate()
        audioPlayerManager.release()
    }
}
