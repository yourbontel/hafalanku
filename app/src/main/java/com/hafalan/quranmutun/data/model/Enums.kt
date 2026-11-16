package com.hafalan.quranmutun.data.model

enum class MemorizationStatus(val labelId: String) {
    NOT_STARTED("Belum"),
    MEMORIZING("Menghafal"),
    NEEDS_REVIEW("Muraja'ah"),
    MUTQIN("Mutqin")
}

enum class AudioTargetType(val labelId: String) {
    QURAN("Al-Qur'an"),
    MUTUN("Mutun"),
    GENERAL("Umum")
}

enum class LoopMode(val label: String, val targetCount: Int?) {
    ONCE("1x", 1),
    THREE("3x", 3),
    FIVE("5x", 5),
    INFINITE("∞", null)
}
