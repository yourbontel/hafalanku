package com.hafalan.quranmutun.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hafalan.quranmutun.data.local.DriveAudioEntity
import com.hafalan.quranmutun.data.local.SurahProgressEntity
import com.hafalan.quranmutun.data.model.MemorizationStatus
import com.hafalan.quranmutun.player.AudioPlayerState
import com.hafalan.quranmutun.ui.components.DriveAudioRow
import com.hafalan.quranmutun.ui.components.StatusBadge
import com.hafalan.quranmutun.ui.components.formatRelativeDate
import com.hafalan.quranmutun.ui.components.statusColor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    surahs: List<SurahProgressEntity>,
    audios: List<DriveAudioEntity>,
    audioState: AudioPlayerState,
    selectedSurahForDetail: SurahProgressEntity?,
    onSelectSurah: (SurahProgressEntity?) -> Unit,
    onSaveSurah: (SurahProgressEntity, Int, MemorizationStatus, String) -> Unit,
    onMarkReviewedToday: (SurahProgressEntity) -> Unit,
    onOpenAddAudioForSurah: (SurahProgressEntity) -> Unit,
    onPlayAudio: (DriveAudioEntity, List<DriveAudioEntity>) -> Unit,
    onDeleteAudio: (DriveAudioEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<MemorizationStatus?>(null) }
    var selectedJuzFilter by remember { mutableStateOf<Int?>(null) }
    var juzMenuExpanded by remember { mutableStateOf(false) }

    val filteredSurahs = remember(surahs, searchQuery, selectedStatusFilter, selectedJuzFilter) {
        val q = searchQuery.trim().lowercase()
        surahs.filter { surah ->
            val matchesQuery = q.isEmpty() ||
                surah.nameLatin.lowercase().contains(q) ||
                surah.meaningId.lowercase().contains(q) ||
                surah.surahNumber.toString() == q
            val matchesStatus = selectedStatusFilter == null || surah.status == selectedStatusFilter
            val matchesJuz = selectedJuzFilter == null ||
                (selectedJuzFilter!! in surah.juzStart..surah.juzEnd)
            matchesQuery && matchesStatus && matchesJuz
        }
    }

    val audioCountBySurah = remember(audios) {
        audios.filter { it.surahNumber != null }.groupBy { it.surahNumber!! }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari surah, arti, atau nomor...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Hapus pencarian")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = juzMenuExpanded,
                    onExpandedChange = { juzMenuExpanded = !juzMenuExpanded },
                    modifier = Modifier.width(136.dp)
                ) {
                    OutlinedTextField(
                        value = selectedJuzFilter?.let { "Juz $it" } ?: "Semua Juz",
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = juzMenuExpanded)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = juzMenuExpanded,
                        onDismissRequest = { juzMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Semua Juz") },
                            onClick = {
                                selectedJuzFilter = null
                                juzMenuExpanded = false
                            }
                        )
                        (30 downTo 1).forEach { juz ->
                            DropdownMenuItem(
                                text = { Text("Juz $juz") },
                                onClick = {
                                    selectedJuzFilter = juz
                                    juzMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null },
                        label = { Text("Semua") }
                    )
                    MemorizationStatus.entries.forEach { status ->
                        FilterChip(
                            selected = selectedStatusFilter == status,
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == status) null else status
                            },
                            label = { Text(status.labelId) }
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredSurahs, key = { it.surahNumber }) { surah ->
                val surahAudios = audioCountBySurah[surah.surahNumber].orEmpty()
                SurahCard(
                    surah = surah,
                    audioCount = surahAudios.size,
                    onClick = { onSelectSurah(surah) }
                )
            }
        }
    }

    val liveSelectedSurah = selectedSurahForDetail?.let { target ->
        surahs.find { it.surahNumber == target.surahNumber } ?: target
    }
    if (liveSelectedSurah != null) {
        val surahAudios = audioCountBySurah[liveSelectedSurah.surahNumber].orEmpty()
        SurahDetailBottomSheet(
            surah = liveSelectedSurah,
            audios = surahAudios,
            audioState = audioState,
            onDismiss = { onSelectSurah(null) },
            onSave = { verses, status, notes ->
                onSaveSurah(liveSelectedSurah, verses, status, notes)
                onSelectSurah(null)
            },
            onMarkReviewedToday = { onMarkReviewedToday(liveSelectedSurah) },
            onAddAudio = { onOpenAddAudioForSurah(liveSelectedSurah) },
            onPlayAudio = { audio -> onPlayAudio(audio, surahAudios) },
            onDeleteAudio = onDeleteAudio
        )
    }
}

@Composable
private fun SurahCard(
    surah: SurahProgressEntity,
    audioCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
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
                    val juzText = if (surah.juzStart == surah.juzEnd) {
                        "Juz ${surah.juzStart}"
                    } else {
                        "Juz ${surah.juzStart}–${surah.juzEnd}"
                    }
                    Text(
                        text = "${surah.meaningId} • $juzText • Hal ${surah.pageStart}–${surah.pageEnd}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusBadge(status = surah.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { surah.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = statusColor(surah.status),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${surah.memorizedVerses}/${surah.totalVerses} ayat • Muraja'ah: ${formatRelativeDate(surah.lastReviewedAt)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (audioCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Headphones,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$audioCount",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SurahDetailBottomSheet(
    surah: SurahProgressEntity,
    audios: List<DriveAudioEntity>,
    audioState: AudioPlayerState,
    onDismiss: () -> Unit,
    onSave: (Int, MemorizationStatus, String) -> Unit,
    onMarkReviewedToday: () -> Unit,
    onAddAudio: () -> Unit,
    onPlayAudio: (DriveAudioEntity) -> Unit,
    onDeleteAudio: (DriveAudioEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var verses by remember(surah.surahNumber, surah.memorizedVerses) {
        mutableIntStateOf(surah.memorizedVerses)
    }
    var status by remember(surah.surahNumber, surah.status) {
        mutableStateOf(surah.status)
    }
    var notes by remember(surah.surahNumber, surah.notes) {
        mutableStateOf(surah.notes)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${surah.surahNumber}. ${surah.nameLatin} (${surah.nameArabic})",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "${surah.meaningId} • ${surah.totalVerses} ayat • Halaman ${surah.pageStart}–${surah.pageEnd}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(text = "Status Hafalan", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MemorizationStatus.entries.forEach { option ->
                    FilterChip(
                        selected = status == option,
                        onClick = {
                            status = option
                            if (option == MemorizationStatus.MUTQIN && verses < surah.totalVerses) {
                                verses = surah.totalVerses
                            }
                        },
                        label = { Text(option.labelId) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ayat Dihafal: $verses / ${surah.totalVerses}",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { verses = (verses - 1).coerceAtLeast(0) }) {
                        Icon(Icons.Rounded.Remove, contentDescription = "Kurangi ayat")
                    }
                    IconButton(onClick = { verses = (verses + 1).coerceAtMost(surah.totalVerses) }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Tambah ayat")
                    }
                    TextButton(onClick = { verses = surah.totalVerses }) {
                        Text("Khatam")
                    }
                }
            }
            Slider(
                value = verses.toFloat(),
                onValueChange = { verses = it.roundToInt().coerceIn(0, surah.totalVerses) },
                valueRange = 0f..surah.totalVerses.toFloat()
            )

            OutlinedButton(
                onClick = onMarkReviewedToday,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Catat Muraja'ah Hari Ini (Total: ${surah.reviewCount}x • Terakhir: ${formatRelativeDate(surah.lastReviewedAt)})"
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan Hafalan / Mutasyabihat") },
                placeholder = { Text("Contoh: Perhatikan waqaf ayat 12, mirip dengan Surah...") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Audio Google Drive (${audios.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onAddAudio) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Link Drive")
                }
            }

            if (audios.isEmpty()) {
                Text(
                    text = "Belum ada audio Drive untuk surah ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                audios.forEach { audio ->
                    DriveAudioRow(
                        audio = audio,
                        isCurrentTrack = audioState.currentAudio?.id == audio.id,
                        isPlaying = audioState.isPlaying,
                        onPlayClick = { onPlayAudio(audio) },
                        onDeleteClick = { onDeleteAudio(audio) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = { onSave(verses, status, notes) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan Progres Surah")
            }
        }
    }
}
