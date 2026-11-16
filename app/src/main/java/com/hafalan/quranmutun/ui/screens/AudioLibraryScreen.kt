package com.hafalan.quranmutun.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hafalan.quranmutun.data.local.DriveAudioEntity
import com.hafalan.quranmutun.data.local.MutunItemEntity
import com.hafalan.quranmutun.data.local.SurahProgressEntity
import com.hafalan.quranmutun.data.model.AudioTargetType
import com.hafalan.quranmutun.player.AudioPlayerState
import com.hafalan.quranmutun.ui.components.DriveAudioRow

@Composable
fun AudioLibraryScreen(
    audios: List<DriveAudioEntity>,
    surahs: List<SurahProgressEntity>,
    mutunItems: List<MutunItemEntity>,
    audioState: AudioPlayerState,
    onOpenAddAudio: () -> Unit,
    onPlayAudio: (DriveAudioEntity, List<DriveAudioEntity>) -> Unit,
    onDeleteAudio: (DriveAudioEntity) -> Unit
) {
    var selectedType by remember { mutableStateOf<AudioTargetType?>(null) }

    val surahMap = remember(surahs) { surahs.associateBy { it.surahNumber } }
    val mutunMap = remember(mutunItems) { mutunItems.associateBy { it.id } }

    val filteredAudios = remember(audios, selectedType) {
        if (selectedType == null) audios else audios.filter { it.targetType == selectedType }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { selectedType = null },
                    label = { Text("Semua (${audios.size})") }
                )
                AudioTargetType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = if (selectedType == type) null else type },
                        label = { Text(type.labelId) }
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(onClick = onOpenAddAudio) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Link")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudQueue,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cara Memakai Audio dari Google Drive",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "1. Di Google Drive, pilih file/folder audio → Bagikan (Share).\n" +
                                    "2. Ubah akses menjadi \"Siapa saja yang memiliki link\".\n" +
                                    "3. Salin link-nya lalu tempel lewat tombol \"+ Link\" di atas.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (filteredAudios.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Headphones,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Belum ada audio tersimpan",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tambahkan link MP3/M4A murottal atau rekaman mutun dari Google Drive Anda.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredAudios, key = { it.id }) { audio ->
                    val targetName = when (audio.targetType) {
                        AudioTargetType.QURAN -> audio.surahNumber?.let { num ->
                            surahMap[num]?.let { "Surah ${it.surahNumber}. ${it.nameLatin}" }
                        } ?: "Al-Qur'an"
                        AudioTargetType.MUTUN -> audio.mutunId?.let { id ->
                            mutunMap[id]?.title
                        } ?: "Mutun"
                        AudioTargetType.GENERAL -> "Umum"
                    }
                    val subtitle = buildString {
                        append(targetName)
                        if (audio.reciterOrSource.isNotBlank()) {
                            append(" • ")
                            append(audio.reciterOrSource)
                        }
                    }
                    DriveAudioRow(
                        audio = audio,
                        isCurrentTrack = audioState.currentAudio?.id == audio.id,
                        isPlaying = audioState.isPlaying,
                        onPlayClick = { onPlayAudio(audio, filteredAudios) },
                        onDeleteClick = { onDeleteAudio(audio) },
                        subtitleOverride = subtitle
                    )
                }
            }
        }
    }
}
