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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.hafalan.quranmutun.player.AudioPlayerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetHafalanScreen(
    surahs: List<SurahProgressEntity>,
    mutunItems: List<MutunItemEntity>,
    audios: List<DriveAudioEntity>,
    prefs: ListeningPreferences,
    audioState: AudioPlayerState,
    onSetHafalanRange: (Int, Int) -> Unit,
    onAddAudioForSurah: (SurahProgressEntity) -> Unit,
    onAddAudioForMutun: (MutunItemEntity) -> Unit,
    onPlayAudio: (DriveAudioEntity, List<DriveAudioEntity>) -> Unit
) {
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    val targetSurahs = remember(surahs, prefs.minHafalanSurah, prefs.maxHafalanSurah) {
        surahs.filter { it.surahNumber in prefs.minHafalanSurah..prefs.maxHafalanSurah }
    }
    val audiosBySurah = remember(audios) {
        audios.filter { it.surahNumber != null }.groupBy { it.surahNumber!! }
    }
    val audiosByMutun = remember(audios) {
        audios.filter { it.mutunId != null }.groupBy { it.mutunId!! }
    }

    val fromSurah = surahs.find { it.surahNumber == prefs.minHafalanSurah }
    val toSurah = surahs.find { it.surahNumber == prefs.maxHafalanSurah }

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
                        ExposedDropdownMenuBox(
                            expanded = fromExpanded,
                            onExpandedChange = { fromExpanded = !fromExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = fromSurah?.let { "${it.surahNumber}. ${it.nameLatin}" }
                                    ?: "Surah ${prefs.minHafalanSurah}",
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                label = { Text("Dari Surah") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = fromExpanded,
                                onDismissRequest = { fromExpanded = false }
                            ) {
                                surahs.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text("${s.surahNumber}. ${s.nameLatin}") },
                                        onClick = {
                                            val newMax = maxOf(s.surahNumber, prefs.maxHafalanSurah)
                                            onSetHafalanRange(s.surahNumber, newMax)
                                            fromExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = toExpanded,
                            onExpandedChange = { toExpanded = !toExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = toSurah?.let { "${it.surahNumber}. ${it.nameLatin}" }
                                    ?: "Surah ${prefs.maxHafalanSurah}",
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                label = { Text("Sampai Surah") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = toExpanded,
                                onDismissRequest = { toExpanded = false }
                            ) {
                                surahs.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text("${s.surahNumber}. ${s.nameLatin}") },
                                        onClick = {
                                            val newMin = minOf(prefs.minHafalanSurah, s.surahNumber)
                                            onSetHafalanRange(newMin, s.surahNumber)
                                            toExpanded = false
                                        }
                                    )
                                }
                            }
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

        items(targetSurahs, key = { "target-surah-${it.surahNumber}" }) { surah ->
            val surahAudios = audiosBySurah[surah.surahNumber].orEmpty()
            val firstAudio = surahAudios.firstOrNull()
            val isPlayingThis = firstAudio != null && audioState.currentAudio?.id == firstAudio.id && audioState.isPlaying

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
                        Text(
                            text = if (surahAudios.isEmpty()) {
                                "${surah.totalVerses} ayat • Belum ada audio Drive"
                            } else {
                                "${surahAudios.size} audio Google Drive siap diputar"
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
                        IconButton(onClick = { onPlayAudio(firstAudio, surahAudios) }) {
                            Icon(
                                imageVector = if (isPlayingThis) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = "Putar ${surah.nameLatin}",
                                tint = MaterialTheme.colorScheme.primary
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
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
        }

        items(mutunItems, key = { "target-mutun-${it.id}" }) { mutun ->
            val mutunAudios = audiosByMutun[mutun.id].orEmpty()
            val firstAudio = mutunAudios.firstOrNull()
            val isPlayingThis = firstAudio != null && audioState.currentAudio?.id == firstAudio.id && audioState.isPlaying

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (firstAudio != null) {
                            onPlayAudio(firstAudio, mutunAudios)
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
                        Text(
                            text = if (mutunAudios.isEmpty()) {
                                "${mutun.category} • Belum ada audio Drive"
                            } else {
                                "${mutun.category} • ${mutunAudios.size} audio siap diputar"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (firstAudio != null) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            IconButton(onClick = { onPlayAudio(firstAudio, mutunAudios) }) {
                                Icon(
                                    imageVector = if (isPlayingThis) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = "Putar ${mutun.title}",
                                    tint = Color.White
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
}
