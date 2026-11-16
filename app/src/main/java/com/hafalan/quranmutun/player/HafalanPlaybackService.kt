package com.hafalan.quranmutun.player

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.hafalan.quranmutun.HafalanApp

@OptIn(UnstableApi::class)
class HafalanPlaybackService : MediaSessionService() {
    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val app = application as HafalanApp

        val httpFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(15_000)
            .setUserAgent("Mozilla/5.0 (Android) HafalanQuranMutun/1.0")

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(httpFactory))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()

        // Lock seeking forward/backward and only allow Next when sequential rule permits
        val forwardingPlayer = object : ForwardingPlayer(player) {
            override fun getAvailableCommands(): Player.Commands {
                val builder = super.getAvailableCommands().buildUpon()
                    .remove(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
                    .remove(Player.COMMAND_SEEK_TO_DEFAULT_POSITION)
                    .remove(Player.COMMAND_SEEK_BACK)
                    .remove(Player.COMMAND_SEEK_FORWARD)
                if (app.audioPlayerManager.hasNextInPlaylist()) {
                    builder.add(Player.COMMAND_SEEK_TO_NEXT)
                    builder.add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                } else {
                    builder.remove(Player.COMMAND_SEEK_TO_NEXT)
                    builder.remove(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                }
                if (app.audioPlayerManager.hasPreviousInPlaylist()) {
                    builder.add(Player.COMMAND_SEEK_TO_PREVIOUS)
                    builder.add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                } else {
                    builder.remove(Player.COMMAND_SEEK_TO_PREVIOUS)
                    builder.remove(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                }
                return builder.build()
            }

            override fun isCommandAvailable(command: Int): Boolean {
                return when (command) {
                    Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM,
                    Player.COMMAND_SEEK_TO_DEFAULT_POSITION,
                    Player.COMMAND_SEEK_BACK,
                    Player.COMMAND_SEEK_FORWARD -> false
                    Player.COMMAND_SEEK_TO_NEXT,
                    Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> app.audioPlayerManager.hasNextInPlaylist()
                    Player.COMMAND_SEEK_TO_PREVIOUS,
                    Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> app.audioPlayerManager.hasPreviousInPlaylist()
                    else -> super.isCommandAvailable(command)
                }
            }

            override fun seekTo(positionMs: Long) {
                // Disabled: audio cannot be skipped forward or backward
            }

            override fun seekTo(mediaItemIndex: Int, positionMs: Long) {
                // Disabled: audio cannot be skipped forward or backward
            }

            override fun seekToDefaultPosition() {
                // Disabled
            }

            override fun seekToDefaultPosition(mediaItemIndex: Int) {
                // Disabled
            }

            override fun seekBack() {
                // Disabled
            }

            override fun seekForward() {
                // Disabled
            }

            override fun hasNextMediaItem(): Boolean = app.audioPlayerManager.hasNextInPlaylist()

            override fun hasPreviousMediaItem(): Boolean = app.audioPlayerManager.hasPreviousInPlaylist()

            override fun seekToNext() {
                app.audioPlayerManager.playNextInPlaylist()
            }

            override fun seekToNextMediaItem() {
                app.audioPlayerManager.playNextInPlaylist()
            }

            override fun seekToPrevious() {
                app.audioPlayerManager.playPreviousInPlaylist()
            }

            override fun seekToPreviousMediaItem() {
                app.audioPlayerManager.playPreviousInPlaylist()
            }
        }

        exoPlayer = player
        mediaSession = MediaSession.Builder(this, forwardingPlayer).build()
        app.audioPlayerManager.attachServicePlayer(player)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val app = application as HafalanApp
        app.audioPlayerManager.persistCurrentPositionNow()
        val player = exoPlayer
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        val app = application as HafalanApp
        app.audioPlayerManager.persistCurrentPositionNow()
        app.audioPlayerManager.detachServicePlayer()
        mediaSession?.release()
        mediaSession = null
        exoPlayer?.release()
        exoPlayer = null
        super.onDestroy()
    }
}
