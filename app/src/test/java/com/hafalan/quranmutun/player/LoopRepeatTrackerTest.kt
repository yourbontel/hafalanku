package com.hafalan.quranmutun.player

import com.hafalan.quranmutun.data.model.LoopMode
import org.junit.Assert.assertEquals
import org.junit.Test

class LoopRepeatTrackerTest {

    @Test
    fun `once mode stops when auto advance is disabled or no next track`() {
        assertEquals(LoopNextAction.StopAtEnd, LoopRepeatTracker.onTrackEnded(LoopMode.ONCE, 1))
        assertEquals(
            LoopNextAction.StopAtEnd,
            LoopRepeatTracker.onTrackEnded(
                loopMode = LoopMode.ONCE,
                currentIteration = 1,
                autoAdvancePlaylist = true,
                hasNextTrack = false
            )
        )
    }

    @Test
    fun `once mode advances to next track when auto advance enabled`() {
        assertEquals(
            LoopNextAction.AdvanceToNext,
            LoopRepeatTracker.onTrackEnded(
                loopMode = LoopMode.ONCE,
                currentIteration = 1,
                autoAdvancePlaylist = true,
                hasNextTrack = true
            )
        )
    }

    @Test
    fun `three mode replays twice then advances if auto advance enabled`() {
        assertEquals(
            LoopNextAction.ReplayCurrent(2),
            LoopRepeatTracker.onTrackEnded(LoopMode.THREE, 1, autoAdvancePlaylist = true, hasNextTrack = true)
        )
        assertEquals(
            LoopNextAction.ReplayCurrent(3),
            LoopRepeatTracker.onTrackEnded(LoopMode.THREE, 2, autoAdvancePlaylist = true, hasNextTrack = true)
        )
        assertEquals(
            LoopNextAction.AdvanceToNext,
            LoopRepeatTracker.onTrackEnded(LoopMode.THREE, 3, autoAdvancePlaylist = true, hasNextTrack = true)
        )
    }

    @Test
    fun `five mode replays up to five times`() {
        for (i in 1..4) {
            assertEquals(
                LoopNextAction.ReplayCurrent(i + 1),
                LoopRepeatTracker.onTrackEnded(LoopMode.FIVE, i)
            )
        }
        assertEquals(LoopNextAction.StopAtEnd, LoopRepeatTracker.onTrackEnded(LoopMode.FIVE, 5))
    }

    @Test
    fun `infinite mode always replays`() {
        assertEquals(
            LoopNextAction.ReplayCurrent(100),
            LoopRepeatTracker.onTrackEnded(LoopMode.INFINITE, 99, autoAdvancePlaylist = true, hasNextTrack = true)
        )
    }
}
