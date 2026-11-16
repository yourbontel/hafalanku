package com.hafalan.quranmutun.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.hafalan.quranmutun.data.local.DriveAudioEntity
import com.hafalan.quranmutun.data.model.LoopMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AudioPlayerState(
    val currentAudio: DriveAudioEntity? = null,
    val playlist: List<DriveAudioEntity> = emptyList(),
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val loopMode: LoopMode = LoopMode.ONCE,
    val currentLoopIteration: Int = 1,
    val errorMessage: String? = null
)

@OptIn(UnstableApi::class)
class AudioPlayerManager(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var positionTickerJob: Job? = null

    private val httpFactory = DefaultHttpDataSource.Factory()
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(15_000)
        .setReadTimeoutMs(15_000)
        .setUserAgent("Mozilla/5.0 (Android) HafalanQuranMutun/1.0")

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context.applicationContext)
        .setMediaSourceFactory(DefaultMediaSourceFactory(httpFactory))
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .build(),
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    private val _state = MutableStateFlow(AudioPlayerState())
    val state: StateFlow<AudioPlayerState> = _state.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) {
                startPositionTicker()
            } else {
                stopPositionTicker()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    _state.update { it.copy(isBuffering = true, errorMessage = null) }
                }
                Player.STATE_READY -> {
                    val dur = exoPlayer.duration.takeIf { it != C.TIME_UNSET } ?: 0L
                    _state.update {
                        it.copy(
                            isBuffering = false,
                            durationMs = dur,
                            positionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                        )
                    }
                }
                Player.STATE_ENDED -> {
                    handleTrackEnded()
                }
                Player.STATE_IDLE -> {
                    _state.update { it.copy(isBuffering = false) }
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _state.update {
                it.copy(
                    isPlaying = false,
                    isBuffering = false,
                    errorMessage = "Audio gagal diputar (${error.errorCodeName}). " +
                        "Pastikan koneksi internet aktif dan akses file Google Drive sudah diset ke 'Siapa saja yang memiliki link'."
                )
            }
        }
    }

    init {
        exoPlayer.addListener(playerListener)
    }

    fun playAudio(audio: DriveAudioEntity, playlist: List<DriveAudioEntity> = listOf(audio)) {
        val current = _state.value.currentAudio
        if (current?.id == audio.id && current.streamUrl == audio.streamUrl) {
            if (_state.value.errorMessage != null) {
                loadAndPlay(audio, playlist)
            } else {
                togglePlayPause()
            }
            return
        }
        loadAndPlay(audio, playlist)
    }

    private fun loadAndPlay(audio: DriveAudioEntity, playlist: List<DriveAudioEntity>) {
        _state.update {
            it.copy(
                currentAudio = audio,
                playlist = playlist,
                isBuffering = true,
                positionMs = 0L,
                durationMs = 0L,
                currentLoopIteration = 1,
                errorMessage = null
            )
        }
        applyLoopToExoPlayer(_state.value.loopMode)
        exoPlayer.setPlaybackParameters(PlaybackParameters(_state.value.playbackSpeed))
        exoPlayer.setMediaItem(MediaItem.fromUri(audio.streamUrl))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    fun togglePlayPause() {
        if (_state.value.currentAudio == null) return
        if (exoPlayer.playbackState == Player.STATE_ENDED) {
            _state.update { it.copy(currentLoopIteration = 1) }
            exoPlayer.seekTo(0L)
            exoPlayer.play()
            return
        }
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _state.value.durationMs.coerceAtLeast(0L))
        exoPlayer.seekTo(clamped)
        _state.update { it.copy(positionMs = clamped) }
    }

    fun seekBy(deltaMs: Long) {
        val target = (exoPlayer.currentPosition + deltaMs).coerceIn(
            0L,
            _state.value.durationMs.coerceAtLeast(0L)
        )
        seekTo(target)
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.5f, 2.0f)
        exoPlayer.setPlaybackParameters(PlaybackParameters(clamped))
        _state.update { it.copy(playbackSpeed = clamped) }
    }

    fun setLoopMode(loopMode: LoopMode) {
        applyLoopToExoPlayer(loopMode)
        _state.update {
            it.copy(
                loopMode = loopMode,
                currentLoopIteration = 1
            )
        }
    }

    fun cycleLoopMode() {
        val modes = LoopMode.entries
        val nextIndex = (modes.indexOf(_state.value.loopMode) + 1) % modes.size
        setLoopMode(modes[nextIndex])
    }

    fun playNextInPlaylist() {
        val list = _state.value.playlist
        val current = _state.value.currentAudio ?: return
        if (list.size <= 1) return
        val idx = list.indexOfFirst { it.id == current.id }
        val nextIdx = if (idx < 0 || idx + 1 >= list.size) 0 else idx + 1
        loadAndPlay(list[nextIdx], list)
    }

    fun playPreviousInPlaylist() {
        val list = _state.value.playlist
        val current = _state.value.currentAudio ?: return
        if (list.size <= 1) return
        val idx = list.indexOfFirst { it.id == current.id }
        val prevIdx = if (idx <= 0) list.lastIndex else idx - 1
        loadAndPlay(list[prevIdx], list)
    }

    fun stopAndDismiss() {
        stopPositionTicker()
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _state.update {
            AudioPlayerState(
                playbackSpeed = it.playbackSpeed,
                loopMode = it.loopMode
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun applyLoopToExoPlayer(loopMode: LoopMode) {
        exoPlayer.repeatMode = if (loopMode == LoopMode.INFINITE) {
            Player.REPEAT_MODE_ONE
        } else {
            Player.REPEAT_MODE_OFF
        }
    }

    private fun handleTrackEnded() {
        val snapshot = _state.value
        when (val action = LoopRepeatTracker.onTrackEnded(snapshot.loopMode, snapshot.currentLoopIteration)) {
            is LoopNextAction.ReplayCurrent -> {
                _state.update { it.copy(currentLoopIteration = action.nextIteration) }
                exoPlayer.seekTo(0L)
                exoPlayer.play()
            }
            LoopNextAction.StopAtEnd -> {
                _state.update {
                    it.copy(
                        isPlaying = false,
                        isBuffering = false,
                        currentLoopIteration = 1,
                        positionMs = snapshot.durationMs
                    )
                }
            }
        }
    }

    private fun startPositionTicker() {
        if (positionTickerJob?.isActive == true) return
        positionTickerJob = scope.launch {
            while (isActive) {
                val dur = exoPlayer.duration.takeIf { it != C.TIME_UNSET } ?: _state.value.durationMs
                _state.update {
                    it.copy(
                        positionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                        durationMs = dur
                    )
                }
                delay(400L)
            }
        }
    }

    private fun stopPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = null
    }

    fun release() {
        stopPositionTicker()
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }
}
