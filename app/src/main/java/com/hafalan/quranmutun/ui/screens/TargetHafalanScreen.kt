package com.hafalan.quranmutun.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hafalan.quranmutun.data.local.DriveAudioEntity
import com.hafalan.quranmutun.data.local.ListeningPreferences
import com.hafalan.quranmutun.data.local.MutunItemEntity
import com.hafalan.quranmutun.data.local.SurahProgressEntity
import com.hafalan.quranmutun.data.model.AudioTargetType
import com.hafalan.quranmutun.player.SequentialLockPolicy
import com.hafalan.quranmutun.ui.components.formatDuration

@Composable
fun TargetHafalanScreen(
    surahs: List<SurahProgressEntity>,
    mutunItems: List<MutunItemEntity>,
    audios: List<DriveAudioEntity>,
    prefs: ListeningPreferences,
    activeAudioId: Long?,
    isAudioPlaying: Boolean,
    onSetHafalanRange: (Int, Int) -> Unit,
    onAddAudioForSurah: (SurahProgressEntity) -> Unit,
    onAddAudioForMutun: (MutunItemEntity) -> Unit,
    onUpdateMutunDetails: (MutunItemEntity, String, String, String) -> Unit,
    onAddCustomMutun: (String, String, String) -> Unit,
    onDeleteMutun: (MutunItemEntity) -> Unit,
    onResetStats: () -> Unit,
    onPlayAudio: (DriveAudioEntity, List<DriveAudioEntity>) -> Unit
) {
    var pickingFromSurah by remember { mutableStateOf(false) }
    var pickingToSurah by remember { mutableStateOf(false) }
    var editingMutun by remember { mutableStateOf<MutunItemEntity?>(null) }
    var showAddMutunDialog by remember { mutableStateOf(false) }
    var showResetStatsDialog by remember { mutableStateOf(false) }

    val targetSurahs = remember(surahs, prefs.minHafalanSurah, prefs.maxHafalanSurah) {
        surahs.filter { it.surahNumber in prefs.minHafalanSurah..prefs.maxHafalanSurah }
    }
    val audiosBySurah = remember(audios) {
        audios.filter { it.surahNumber != null }.groupBy { it.surahNumber!! }
    }
    val audiosByMutun = remember(audios) {
        audios.filter { it.mutunId != null }.groupBy { it.mutunId!! }
    }
    val orderedQuran = remember(audios, prefs.minHafalanSurah, prefs.maxHafalanSurah) {
        SequentialLockPolicy.orderedPlaylistForCategory(
            allAudios = audios,
            targetType = AudioTargetType.QURAN,
            minHafalanSurah = prefs.minHafalanSurah,
            maxHafalanSurah = prefs.maxHafalanSurah
        )
    }
    val orderedMutun = remember(audios, prefs.minHafalanSurah, prefs.maxHafalanSurah) {
        SequentialLockPolicy.orderedPlaylistForCategory(
            allAudios = audios,
            targetType = AudioTargetType.MUTUN,
            minHafalanSurah = prefs.minHafalanSurah,
            maxHafalanSurah = prefs.maxHafalanSurah
        )
    }

    val fromSurah = surahs.find { it.surahNumber == prefs.minHafalanSurah }
    val toSurah = surahs.find { it.surahNumber == prefs.maxHafalanSurah }
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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rekap Pengulangan Saya",
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(onClick = { showResetStatsDialog = true }) {
                            Text("Reset")
                        }
                    }
                    Text(
                        text = "Al-Qur'an: ${prefs.quranCycleCount}x khatam urutan (${totalQuranPlays}x putar surah)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Mutun Ilmiah: ${prefs.mutunCycleCount}x khatam urutan (${totalMutunPlays}x putar mutun)",
                        style = MaterialTheme.typography.bodyMedium
                    )
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Batas Hafalan Al-Qur'an Saya",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        text = "Atur rentang surah yang ingin Anda muroja'ah. Saat ini: ${fromSurah?.nameLatin ?: "Surah ${prefs.minHafalanSurah}"} sampai ${toSurah?.nameLatin ?: "Surah ${prefs.maxHafalanSurah}"}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { pickingFromSurah = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Dari Surah",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = fromSurah?.let { "${it.surahNumber}. ${it.nameLatin}" }
                                        ?: "Surah ${prefs.minHafalanSurah}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                        }

                        OutlinedButton(
                            onClick = { pickingToSurah = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sampai Surah",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = toSurah?.let { "${it.surahNumber}. ${it.nameLatin}" }
                                        ?: "Surah ${prefs.maxHafalanSurah}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                        }
                    }
                }
            }
        }

        item {
            val readyCount = targetSurahs.count { audiosBySurah[it.surahNumber].orEmpty().isNotEmpty() }
            Text(
                text = "Surah Hafalan ($readyCount/${targetSurahs.size} sudah punya audio)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(
            items = targetSurahs,
            key = { "target-surah-${it.surahNumber}" },
            contentType = { "target_surah" }
        ) { surah ->
            val surahAudios = audiosBySurah[surah.surahNumber].orEmpty()
            val firstAudio = surahAudios.firstOrNull()
            val firstIdx = firstAudio?.let { audio -> orderedQuran.indexOfFirst { it.id == audio.id } } ?: -1
            val isUnlocked = firstIdx >= 0 && SequentialLockPolicy.isUnlocked(orderedQuran, firstIdx, prefs.completedAudioIds)
            val isCompleted = firstAudio != null && firstAudio.id in prefs.completedAudioIds
            val isPlayingThis = firstAudio != null && activeAudioId == firstAudio.id && isAudioPlaying

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = surah.surahNumber.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = surah.nameLatin,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = surah.nameArabic,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        val surahPlayCount = surahAudios.sumOf { prefs.audioPlayCounts[it.id] ?: 0 }
                        val savedSurahPos = surahAudios.firstOrNull()?.let { prefs.audioPositionsMs[it.id] ?: 0L } ?: 0L
                        Text(
                            text = if (surahAudios.isEmpty()) {
                                "${surah.totalVerses} ayat • Diulang ${surahPlayCount}x"
                            } else if (savedSurahPos > 0L) {
                                "${surahAudios.size} audio • Lanjut ${formatDuration(savedSurahPos)} • Diulang ${surahPlayCount}x"
                            } else {
                                "${surahAudios.size} audio Drive • Diulang ${surahPlayCount}x"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (surahAudios.isEmpty()) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }

                    if (firstAudio != null) {
                        IconButton(onClick = { onPlayAudio(firstAudio, orderedQuran) }) {
                            Icon(
                                imageVector = when {
                                    !isUnlocked -> Icons.Rounded.Lock
                                    isPlayingThis -> Icons.Rounded.Pause
                                    isCompleted -> Icons.Rounded.CheckCircle
                                    else -> Icons.Rounded.PlayArrow
                                },
                                contentDescription = "Putar ${surah.nameLatin}",
                                tint = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onAddAudioForSurah(surah) }) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Tambah audio lagi",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        FilledTonalButton(onClick = { onAddAudioForSurah(surah) }) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Link Drive")
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoStories,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Audio Mutun Ilmiah",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                TextButton(onClick = { showAddMutunDialog = true }) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Mutun")
                }
            }
        }

        items(
            items = mutunItems,
            key = { "target-mutun-${it.id}" },
            contentType = { "target_mutun" }
        ) { mutun ->
            val mutunAudios = audiosByMutun[mutun.id].orEmpty()
            val firstAudio = mutunAudios.firstOrNull()
            val firstIdx = firstAudio?.let { audio -> orderedMutun.indexOfFirst { it.id == audio.id } } ?: -1
            val isUnlocked = firstIdx >= 0 && SequentialLockPolicy.isUnlocked(orderedMutun, firstIdx, prefs.completedAudioIds)
            val isCompleted = firstAudio != null && firstAudio.id in prefs.completedAudioIds
            val isPlayingThis = firstAudio != null && activeAudioId == firstAudio.id && isAudioPlaying

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (firstAudio != null) {
                            onPlayAudio(firstAudio, orderedMutun)
                        } else {
                            onAddAudioForMutun(mutun)
                        }
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mutun.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val mutunPlayCount = mutunAudios.sumOf { prefs.audioPlayCounts[it.id] ?: 0 }
                        val savedMutunPos = mutunAudios.firstOrNull()?.let { prefs.audioPositionsMs[it.id] ?: 0L } ?: 0L
                        Text(
                            text = if (mutunAudios.isEmpty()) {
                                "${mutun.category} • Diulang ${mutunPlayCount}x"
                            } else if (savedMutunPos > 0L) {
                                "${mutun.category} • Lanjut ${formatDuration(savedMutunPos)} • Diulang ${mutunPlayCount}x"
                            } else {
                                "${mutun.category} • ${mutunAudios.size} audio • Diulang ${mutunPlayCount}x"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { editingMutun = mutun }) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit judul mutun",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (firstAudio != null) {
                        Surface(
                            shape = CircleShape,
                            color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ) {
                            IconButton(onClick = { onPlayAudio(firstAudio, orderedMutun) }) {
                                Icon(
                                    imageVector = when {
                                        !isUnlocked -> Icons.Rounded.Lock
                                        isPlayingThis -> Icons.Rounded.Pause
                                        isCompleted -> Icons.Rounded.CheckCircle
                                        else -> Icons.Rounded.PlayArrow
                                    },
                                    contentDescription = "Putar ${mutun.title}",
                                    tint = if (isUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { onAddAudioForMutun(mutun) }) {
                            Icon(Icons.Rounded.Add, contentDescription = "Tambah audio")
                        }
                    } else {
                        FilledTonalButton(onClick = { onAddAudioForMutun(mutun) }) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Link Drive")
                        }
                    }
                }
            }
        }
    }

    editingMutun?.let { target ->
        EditMutunTitleDialog(
            initialTitle = target.title,
            initialCategory = target.category,
            initialAuthor = target.author,
            isNew = false,
            onDismiss = { editingMutun = null },
            onSave = { title, category, author ->
                onUpdateMutunDetails(target, title, category, author)
                editingMutun = null
            },
            onDelete = {
                onDeleteMutun(target)
                editingMutun = null
            }
        )
    }

    if (showAddMutunDialog) {
        EditMutunTitleDialog(
            initialTitle = "",
            initialCategory = "Umum",
            initialAuthor = "",
            isNew = true,
            onDismiss = { showAddMutunDialog = false },
            onSave = { title, category, author ->
                onAddCustomMutun(title, category, author)
                showAddMutunDialog = false
            },
            onDelete = null
        )
    }

    if (showResetStatsDialog) {
        AlertDialog(
            onDismissRequest = { showResetStatsDialog = false },
            title = { Text("Reset Catatan Pengulangan?") },
            text = { Text("Jumlah putar per surah/mutun dan jumlah khatam urutan akan kembali ke 0.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        showResetStatsDialog = false
                        onResetStats()
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetStatsDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (pickingFromSurah) {
        SurahPickerDialog(
            title = "Pilih Dari Surah",
            surahs = surahs,
            selectedNumber = prefs.minHafalanSurah,
            onDismiss = { pickingFromSurah = false },
            onSelect = { num ->
                val newMax = maxOf(num, prefs.maxHafalanSurah)
                onSetHafalanRange(num, newMax)
                pickingFromSurah = false
            }
        )
    }

    if (pickingToSurah) {
        SurahPickerDialog(
            title = "Pilih Sampai Surah",
            surahs = surahs,
            selectedNumber = prefs.maxHafalanSurah,
            onDismiss = { pickingToSurah = false },
            onSelect = { num ->
                val newMin = minOf(prefs.minHafalanSurah, num)
                onSetHafalanRange(newMin, num)
                pickingToSurah = false
            }
        )
    }
}

@Composable
private fun SurahPickerDialog(
    title: String,
    surahs: List<SurahProgressEntity>,
    selectedNumber: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
            ) {
                items(
                    items = surahs,
                    key = { it.surahNumber },
                    contentType = { "surah_picker" }
                ) { s ->
                    val isSelected = s.surahNumber == selectedNumber
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(s.surahNumber) }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${s.surahNumber}. ${s.nameLatin}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = s.nameArabic,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun EditMutunTitleDialog(
    initialTitle: String,
    initialCategory: String,
    initialAuthor: String,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
    onDelete: (() -> Unit)?
) {
    var title by remember(initialTitle) { mutableStateOf(initialTitle) }
    var category by remember(initialCategory) { mutableStateOf(initialCategory) }
    var author by remember(initialAuthor) { mutableStateOf(initialAuthor) }
    var confirmDelete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Tambah Mutun Ilmiah" else "Edit Judul Mutun") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Mutun *") },
                    placeholder = { Text("Contoh: Mandzumah Al-Baiquniyah") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Bidang / Kategori") },
                    placeholder = { Text("Contoh: Tajwid, Hadits, Aqidah") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Penulis / Keterangan (opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (onDelete != null) {
                    TextButton(
                        onClick = { confirmDelete = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Hapus Mutun Ini")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = { onSave(title, category, author) }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Hapus Mutun?") },
            text = { Text("Kitab \"$initialTitle\" akan dihapus dari daftar hafalan Anda.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    }
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
