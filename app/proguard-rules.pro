# Keep Room entities, DAOs, enums, and Media3 service under R8 release optimization
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep class com.hafalan.quranmutun.data.local.** { *; }
-keep class com.hafalan.quranmutun.data.model.** { *; }
-keep class com.hafalan.quranmutun.player.HafalanPlaybackService { *; }
-dontwarn androidx.room.paging.**
