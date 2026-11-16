package com.hafalan.quranmutun.player

import com.hafalan.quranmutun.data.model.LoopMode

sealed class LoopNextAction {
    data class ReplayCurrent(val nextIteration: Int) : LoopNextAction()
    data object StopAtEnd : LoopNextAction()
}

object LoopRepeatTracker {
    fun onTrackEnded(loopMode: LoopMode, currentIteration: Int): LoopNextAction {
        val target = loopMode.targetCount ?: return LoopNextAction.ReplayCurrent(currentIteration + 1)
        return if (currentIteration < target) {
            LoopNextAction.ReplayCurrent(currentIteration + 1)
        } else {
            LoopNextAction.StopAtEnd
        }
    }
}
