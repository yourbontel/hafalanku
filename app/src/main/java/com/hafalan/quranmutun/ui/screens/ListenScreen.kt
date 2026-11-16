package com.hafalan.quranmutun.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hafalan.quranmutun.data.local.DriveAudioEntity
import com.hafalan.quranmutun.data.local.ListeningPreferences
import com.hafalan.quranmutun.data.local.MutunItemEntity
import com.hafalan.quranmutun.data.local.SurahProgressEntity
import com.hafalan.quranmutun.data.model.AudioTargetType
import com.hafalan.quranmutun.data.model.LoopMode
import com.hafalan.quranmutun.player.AudioPlayerState
import com.hafalan.quranmutun.player.SequentialLockPolicy
import com.hafalan.quranmutun.ui.components.DriveAudioRow
import com.hafalan.quranmutun.ui.components.formatDuration
import com.hafalan.quranmutun.ui.theme.EmeraldLight
import com.hafalan.quranmutun.ui.theme.EmeraldPrimary

@Composable
fun ListenScreen(
    audios: List<DriveAudioEntity>,
    surahs: List<SurahProgressEntity>,
    mutunItems: List<MutunItemEntity>,
    prefs: ListeningPreferences,
    audioState: AudioPlayerState,
    onPlayAudio: (DriveAudioEntity, List<DriveAudioEntity>) -> Unit,
    onPlayOrderedCategory: (AudioTargetType) -> Unit,
    onResetSequence: (AudioTargetType) -> Unit,
    onOpenDrivingMode: () -> Unit,
    onOpenAddAudio: () -> Unit,
    onSetAutoAdvance: (Boolean) -> Unit,
    onSetLoopMode: (LoopMode) -> Unit,
    onSetSpeed: (Float) -> Unit,
    onDeleteAudio: (DriveAudioEntity) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(AudioTargetType.QURAN) }

    val surahMap = remember(surahs) { surahs.associateBy { it.surahNumber } }
    val mutunMap = remember(mutunItems) { mutunItems.associateBy { it.id } }

    val minSurahName = surahMap[prefs.minHafalanSurah]?.nameLatin ?: "Surah ${prefs.minHafalanSurah}"
    val maxSurahName = surahMap[prefs.maxHafalanSurah]?.nameLatin ?: "Surah ${prefs.maxHafalanSurah}"

    val activePlaylist = remember(
        audios,
        selectedCategory,
        prefs.minHafalanSurah,
        prefs.maxHafalanSurah
    ) {
        SequentialLockPolicy.orderedPlaylistForCategory(
            allAudios = audios,
            targetType = selectedCategory,
            minHafalanSurah = prefs.minHafalanSurah,
            maxHafalanSurah = prefs.maxHafalanSurah
        )
    }

    val completedCount = remember(activePlaylist, prefs.completedAudioIds) {
        activePlaylist.count { it.id in prefs.completedAudioIds }
    }
    val totalQuranPlays = remember(audios, prefs.audioPlayCounts) {
        audios.filter { it.targetType == AudioTargetType.QURAN }
            .sumOf { prefs.audioPlayCounts[it.id] ?: 0 }
    }
    val totalMutunPlays = remember(audios, prefs.audioPlayCounts) {
        audios.filter { it.targetType == AudioTargetType.MUTUN }
            .sumOf { prefs.audioPlayCounts[it.id] ?: 0 }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(EmeraldPrimary, EmeraldLight)))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Muraja'ah Berurutan",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White
                            )
                            Text(
                                text = if (selectedCategory == AudioTargetType.QURAN) {
                                    "Al-Qur'an: $minSurahName – $maxSurahName ($completedCount/${activePlaylist.size} selesai)"
                                } else {
                                    "Mutun Ilmiah ($completedCount/${activePlaylist.size} selesai)"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                        }
                        Surface(
                            onClick = onOpenDrivingMode,
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DirectionsBike,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Mode Motor",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = { onPlayOrderedCategory(selectedCategory) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = EmeraldPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (completedCount in 1 until activePlaylist.size) {
                                    "Lanjutkan Urutan (${completedCount + 1}/${activePlaylist.size})"
                                } else {
                                    "Putar Berurutan (${selectedCategory.labelId})"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Catatan Pengulangan Muraja'ah",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Al-Qur'an",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${prefs.quranCycleCount}x khatam urutan",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${totalQuranPlays}x total putar surah",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Mutun Ilmiah",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${prefs.mutunCycleCount}x khatam urutan",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${totalMutunPlays}x total putar mutun",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Lanjut Setelah Tuntas",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Wajib selesai satu surah/mutun penuh sebelum lanjut berikutnya",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = prefs.autoAdvancePlaylist,
                            onCheckedChange = onSetAutoAdvance
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ulang:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LoopMode.entries.forEach { mode ->
                            FilterChip(
                                selected = audioState.loopMode == mode,
                                onClick = { onSetLoopMode(mode) },
                                label = { Text(mode.label) }
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kecepatan:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                            FilterChip(
                                selected = audioState.playbackSpeed == speed,
                                onClick = { onSetSpeed(speed) },
                                label = { Text("${speed}x") }
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedCategory == AudioTargetType.QURAN,
                        onClick = { selectedCategory = AudioTargetType.QURAN },
                        label = { Text("Al-Qur'an (${prefs.minHafalanSurah}–${prefs.maxHafalanSurah})") }
                    )
                    FilterChip(
                        selected = selectedCategory == AudioTargetType.MUTUN,
                        onClick = { selectedCategory = AudioTargetType.MUTUN },
                        label = { Text("Mutun") }
                    )
                }

                if (completedCount > 0) {
                    TextButton(onClick = { onResetSequence(selectedCategory) }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ulang Urutan")
                    }
                }
            }
        }

        if (activePlaylist.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Headphones,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp)
                        )
                        Text(
                            text = "Belum ada audio ${selectedCategory.labelId}",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (selectedCategory == AudioTargetType.QURAN) {
                                "Tempelkan link audio Google Drive untuk $minSurahName sampai $maxSurahName (atau impor 1 folder Google Drive sekaligus)."
                            } else {
                                "Tambahkan link audio Mutun Ilmiah dari Google Drive Anda."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = onOpenAddAudio) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tambah Link Google Drive")
                        }
                    }
                }
            }
        } else {
            itemsIndexed(activePlaylist, key = { _, audio -> audio.id }) { index, audio ->
                val isUnlocked = SequentialLockPolicy.isUnlocked(
                    activePlaylist,
                    index,
                    prefs.completedAudioIds
                )
                val isCompleted = audio.id in prefs.completedAudioIds
                val targetSubtitle = when (audio.targetType) {
                    AudioTargetType.QURAN -> audio.surahNumber?.let { num ->
                        surahMap[num]?.let { "Surah ${it.surahNumber}. ${it.nameLatin}" }
                    } ?: "Al-Qur'an"
                    AudioTargetType.MUTUN -> audio.mutunId?.let { id ->
                        mutunMap[id]?.title
                    } ?: "Mutun"
                    AudioTargetType.GENERAL -> "Audio Muraja'ah"
                }
                val playCount = prefs.audioPlayCounts[audio.id] ?: 0
                val lockStatus = when {
                    !isUnlocked -> "Terkunci (selesaikan #${index} dulu)"
                    isCompleted -> "Selesai"
                    else -> "Siap diputar"
                }
                val subtitle = buildString {
                    append("#${index + 1} • ")
                    append(targetSubtitle)
                    append(" • ")
                    append(lockStatus)
                    append(" • Diulang ${playCount}x")
                }
                DriveAudioRow(
                    audio = audio,
                    isCurrentTrack = audioState.currentAudio?.id == audio.id,
                    isPlaying = audioState.isPlaying,
                    onPlayClick = { onPlayAudio(audio, activePlaylist) },
                    onDeleteClick = { onDeleteAudio(audio) },
                    subtitleOverride = subtitle,
                    isLocked = !isUnlocked,
                    isCompleted = isCompleted
                )
            }
        }
    }
}

@Composable
fun DrivingModeOverlay(
    audioState: AudioPlayerState,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    keepScreenOn: Boolean,
    onToggleKeepScreenOn: (Boolean) -> Unit,
    onTogglePlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCycleLoop: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    DisposableEffect(keepScreenOn) {
        val window = (context as? Activity)?.window
        if (keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0C1814)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsBike,
                            contentDescription = null,
                            tint = Color(0xFF56D4A7)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mode Berkendara",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Tutup Mode Motor",
                            tint = Color.White
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val audio = audioState.currentAudio
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xFF1A342B)
                    ) {
                        val badgeText = when {
                            audio == null -> "Belum ada audio dipilih"
                            audioState.playlist.size > 1 -> {
                                val idx = audioState.playlist.indexOfFirst { it.id == audio.id }
                                "Urutan ${if (idx >= 0) idx + 1 else 1} dari ${audioState.playlist.size}"
                            }
                            else -> audio.targetType.labelId
                        }
                        Text(
                            text = badgeText,
                            color = Color(0xFF8BE5C3),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = audio?.title ?: "Pilih audio terlebih dahulu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!audio?.reciterOrSource.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = audio?.reciterOrSource.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }

                    if (audioState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = audioState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFFB4AB),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    val progress = if (audioState.durationMs > 0L) {
                        (audioState.positionMs.toFloat() / audioState.durationMs).coerceIn(0f, 1f)
                    } else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = Color(0xFF3FD29C),
                        trackColor = Color(0xFF1D332B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(audioState.positionMs),
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Putar penuh tanpa lewati",
                            color = Color(0xFF8BE5C3),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = if (audioState.durationMs > 0L) formatDuration(audioState.durationMs) else "--:--",
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    if (canSkipPrevious) Color(0xFF1C352C) else Color(0xFF12211C)
                                )
                                .clickable(enabled = canSkipPrevious, onClick = onPrevious),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = "Sebelumnya",
                                tint = if (canSkipPrevious) Color.White else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(112.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16986B))
                                .clickable(enabled = audioState.currentAudio != null, onClick = onTogglePlayPause),
                            contentAlignment = Alignment.Center
                        ) {
                            if (audioState.isBuffering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(44.dp),
                                    strokeWidth = 4.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = if (audioState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (audioState.isPlaying) "Jeda" else "Putar",
                                    tint = Color.White,
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    if (canSkipNext) Color(0xFF1C352C) else Color(0xFF12211C)
                                )
                                .clickable(enabled = canSkipNext, onClick = onNext),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "Berikutnya",
                                tint = if (canSkipNext) Color.White else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onCycleLoop,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Repeat, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ulang: ${audioState.loopMode.label} • Kecepatan: ${audioState.playbackSpeed}x")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Layar tetap menyala (di holder)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Switch(
                            checked = keepScreenOn,
                            onCheckedChange = onToggleKeepScreenOn
                        )
                    }
                }
            }
        }
    }
}
