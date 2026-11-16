package com.hafalan.quranmutun.player

import com.hafalan.quranmutun.data.model.LoopMode

sealed class LoopNextAction {
    data class ReplayCurrent(val nextIteration: Int) : LoopNextAction()
    data object AdvanceToNext : LoopNextAction()
    data object StopAtEnd : LoopNextAction()
}

object LoopRepeatTracker {
    fun onTrackEnded(
        loopMode: LoopMode,
        currentIteration: Int,
        autoAdvancePlaylist: Boolean = false,
        hasNextTrack: Boolean = false
    ): LoopNextAction {
        val target = loopMode.targetCount ?: return LoopNextAction.ReplayCurrent(currentIteration + 1)
        return when {
            currentIteration < target -> LoopNextAction.ReplayCurrent(currentIteration + 1)
            autoAdvancePlaylist && hasNextTrack -> LoopNextAction.AdvanceToNext
            else -> LoopNextAction.StopAtEnd
        }
    }
}
