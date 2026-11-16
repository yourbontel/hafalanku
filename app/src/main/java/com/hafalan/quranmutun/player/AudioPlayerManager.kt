package com.hafalan.quranmutun.player

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
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
    val autoAdvancePlaylist: Boolean = true,
    val errorMessage: String? = null
)

class AudioPlayerManager(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var positionTickerJob: Job? = null

    private var exoPlayer: ExoPlayer? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var pendingPlayRequest: Boolean = false

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
            val player = exoPlayer ?: return
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    _state.update { it.copy(isBuffering = true, errorMessage = null) }
                }
                Player.STATE_READY -> {
                    val dur = player.duration.takeIf { it != C.TIME_UNSET } ?: 0L
                    _state.update {
                        it.copy(
                            isBuffering = false,
                            durationMs = dur,
                            positionMs = player.currentPosition.coerceAtLeast(0L)
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

    internal fun attachServicePlayer(player: ExoPlayer) {
        exoPlayer?.removeListener(playerListener)
        exoPlayer = player
        player.addListener(playerListener)
        if (pendingPlayRequest) {
            pendingPlayRequest = false
            _state.value.currentAudio?.let { audio ->
                loadAndPlayOnAttachedPlayer(player, audio)
            }
        }
    }

    internal fun detachServicePlayer() {
        stopPositionTicker()
        exoPlayer?.removeListener(playerListener)
        exoPlayer = null
    }

    private fun ensureServiceConnected() {
        if (exoPlayer != null || controllerFuture != null) return
        val token = SessionToken(appContext, ComponentName(appContext, HafalanPlaybackService::class.java))
        val future = MediaController.Builder(appContext, token).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                runCatching {
                    mediaController = future.get()
                }
            },
            ContextCompat.getMainExecutor(appContext)
        )
    }

    fun playAudio(audio: DriveAudioEntity, playlist: List<DriveAudioEntity> = listOf(audio)) {
        val current = _state.value.currentAudio
        if (current?.id == audio.id && current.streamUrl == audio.streamUrl && exoPlayer != null) {
            if (_state.value.errorMessage != null) {
                startNewTrack(audio, playlist)
            } else {
                _state.update { it.copy(playlist = playlist) }
                togglePlayPause()
            }
            return
        }
        startNewTrack(audio, playlist)
    }

    private fun startNewTrack(audio: DriveAudioEntity, playlist: List<DriveAudioEntity>) {
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
        ensureServiceConnected()
        val player = exoPlayer
        if (player != null) {
            loadAndPlayOnAttachedPlayer(player, audio)
        } else {
            pendingPlayRequest = true
        }
    }

    private fun loadAndPlayOnAttachedPlayer(player: ExoPlayer, audio: DriveAudioEntity) {
        applyLoopToExoPlayer(player, _state.value.loopMode)
        player.setPlaybackParameters(PlaybackParameters(_state.value.playbackSpeed))
        val metadata = MediaMetadata.Builder()
            .setTitle(audio.title)
            .setArtist(audio.reciterOrSource.ifBlank { "Muraja'ah • Google Drive" })
            .build()
        val item = MediaItem.Builder()
            .setMediaId(audio.id.toString())
            .setUri(audio.streamUrl)
            .setMediaMetadata(metadata)
            .build()
        player.setMediaItem(item)
        player.prepare()
        player.playWhenReady = true
    }

    fun togglePlayPause() {
        val current = _state.value.currentAudio ?: return
        val player = exoPlayer
        if (player == null) {
            startNewTrack(current, _state.value.playlist)
            return
        }
        if (player.playbackState == Player.STATE_ENDED) {
            _state.update { it.copy(currentLoopIteration = 1) }
            player.seekTo(0L)
            player.play()
            return
        }
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _state.value.durationMs.coerceAtLeast(0L))
        exoPlayer?.seekTo(clamped)
        _state.update { it.copy(positionMs = clamped) }
    }

    fun seekBy(deltaMs: Long) {
        val currentPos = exoPlayer?.currentPosition ?: _state.value.positionMs
        val target = (currentPos + deltaMs).coerceIn(0L, _state.value.durationMs.coerceAtLeast(0L))
        seekTo(target)
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.5f, 2.0f)
        exoPlayer?.setPlaybackParameters(PlaybackParameters(clamped))
        _state.update { it.copy(playbackSpeed = clamped) }
    }

    fun setLoopMode(loopMode: LoopMode) {
        exoPlayer?.let { applyLoopToExoPlayer(it, loopMode) }
        _state.update {
            it.copy(
                loopMode = loopMode,
                currentLoopIteration = 1
            )
        }
    }

    fun setAutoAdvancePlaylist(enabled: Boolean) {
        _state.update { it.copy(autoAdvancePlaylist = enabled) }
    }

    fun cycleLoopMode() {
        val modes = LoopMode.entries
        val nextIndex = (modes.indexOf(_state.value.loopMode) + 1) % modes.size
        setLoopMode(modes[nextIndex])
    }

    fun hasNextInPlaylist(): Boolean {
        val list = _state.value.playlist
        val current = _state.value.currentAudio ?: return false
        if (list.size <= 1) return false
        val idx = list.indexOfFirst { it.id == current.id }
        return idx >= 0 && idx + 1 < list.size
    }

    fun hasPreviousInPlaylist(): Boolean {
        val list = _state.value.playlist
        val current = _state.value.currentAudio ?: return false
        if (list.size <= 1) return false
        val idx = list.indexOfFirst { it.id == current.id }
        return idx > 0
    }

    fun playNextInPlaylist() {
        val list = _state.value.playlist
        val current = _state.value.currentAudio ?: return
        if (list.isEmpty()) return
        val idx = list.indexOfFirst { it.id == current.id }
        val nextIdx = if (idx < 0 || idx + 1 >= list.size) 0 else idx + 1
        startNewTrack(list[nextIdx], list)
    }

    fun playPreviousInPlaylist() {
        val list = _state.value.playlist
        val current = _state.value.currentAudio ?: return
        if (list.isEmpty()) return
        val idx = list.indexOfFirst { it.id == current.id }
        val prevIdx = if (idx <= 0) list.lastIndex else idx - 1
        startNewTrack(list[prevIdx], list)
    }

    fun stopAndDismiss() {
        stopPositionTicker()
        pendingPlayRequest = false
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        _state.update {
            AudioPlayerState(
                playbackSpeed = it.playbackSpeed,
                loopMode = it.loopMode,
                autoAdvancePlaylist = it.autoAdvancePlaylist
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun applyLoopToExoPlayer(player: ExoPlayer, loopMode: LoopMode) {
        player.repeatMode = if (loopMode == LoopMode.INFINITE) {
            Player.REPEAT_MODE_ONE
        } else {
            Player.REPEAT_MODE_OFF
        }
    }

    private fun handleTrackEnded() {
        val snapshot = _state.value
        val player = exoPlayer ?: return
        val action = LoopRepeatTracker.onTrackEnded(
            loopMode = snapshot.loopMode,
            currentIteration = snapshot.currentLoopIteration,
            autoAdvancePlaylist = snapshot.autoAdvancePlaylist,
            hasNextTrack = hasNextInPlaylist()
        )
        when (action) {
            is LoopNextAction.ReplayCurrent -> {
                _state.update { it.copy(currentLoopIteration = action.nextIteration) }
                player.seekTo(0L)
                player.play()
            }
            LoopNextAction.AdvanceToNext -> {
                playNextInPlaylist()
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
                val player = exoPlayer
                if (player != null) {
                    val dur = player.duration.takeIf { it != C.TIME_UNSET } ?: _state.value.durationMs
                    _state.update {
                        it.copy(
                            positionMs = player.currentPosition.coerceAtLeast(0L),
                            durationMs = dur
                        )
                    }
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
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        mediaController = null
        detachServicePlayer()
    }
}
