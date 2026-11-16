package com.hafalan.quranmutun.player

import com.hafalan.quranmutun.data.model.LoopMode
import org.junit.Assert.assertEquals
import org.junit.Test

class LoopRepeatTrackerTest {

    @Test
    fun `once mode stops immediately after first playback`() {
        assertEquals(LoopNextAction.StopAtEnd, LoopRepeatTracker.onTrackEnded(LoopMode.ONCE, 1))
    }

    @Test
    fun `three mode replays twice then stops on third`() {
        assertEquals(LoopNextAction.ReplayCurrent(2), LoopRepeatTracker.onTrackEnded(LoopMode.THREE, 1))
        assertEquals(LoopNextAction.ReplayCurrent(3), LoopRepeatTracker.onTrackEnded(LoopMode.THREE, 2))
        assertEquals(LoopNextAction.StopAtEnd, LoopRepeatTracker.onTrackEnded(LoopMode.THREE, 3))
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
        assertEquals(LoopNextAction.ReplayCurrent(100), LoopRepeatTracker.onTrackEnded(LoopMode.INFINITE, 99))
    }
}
