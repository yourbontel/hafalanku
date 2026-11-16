package com.hafalan.quranmutun.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hafalan.quranmutun.data.local.DriveAudioEntity
import com.hafalan.quranmutun.data.local.MutunItemEntity
import com.hafalan.quranmutun.data.local.MutunSectionEntity
import com.hafalan.quranmutun.data.model.MemorizationStatus
import com.hafalan.quranmutun.player.AudioPlayerState
import com.hafalan.quranmutun.ui.components.DriveAudioRow
import com.hafalan.quranmutun.ui.components.StatusBadge
import com.hafalan.quranmutun.ui.components.formatRelativeDate
import com.hafalan.quranmutun.ui.components.statusColor
import kotlin.math.roundToInt

@Composable
fun MutunScreen(
    mutunItems: List<MutunItemEntity>,
    sectionsByMutunId: Map<Long, List<MutunSectionEntity>>,
    audios: List<DriveAudioEntity>,
    audioState: AudioPlayerState,
    selectedMutunForDetail: MutunItemEntity?,
    onSelectMutun: (MutunItemEntity?) -> Unit,
    onAddMutun: (String, String, String, String, Int, String) -> Unit,
    onSaveMutun: (MutunItemEntity, Int, MemorizationStatus, String) -> Unit,
    onMarkReviewedToday: (MutunItemEntity) -> Unit,
    onDeleteMutun: (MutunItemEntity) -> Unit,
    onAddSection: (Long, Int, String, String) -> Unit,
    onToggleSection: (MutunSectionEntity) -> Unit,
    onDeleteSection: (MutunSectionEntity) -> Unit,
    onOpenAddAudioForMutun: (MutunItemEntity) -> Unit,
    onPlayAudio: (DriveAudioEntity, List<DriveAudioEntity>) -> Unit,
    onDeleteAudio: (DriveAudioEntity) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = remember(mutunItems) {
        mutunItems.map { it.category }.distinct()
    }
    val filteredItems = remember(mutunItems, selectedCategory) {
        if (selectedCategory == null) mutunItems else mutunItems.filter { it.category == selectedCategory }
    }
    val audiosByMutun = remember(audios) {
        audios.filter { it.mutunId != null }.groupBy { it.mutunId!! }
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
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("Semua") }
                )
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label = { Text(cat) }
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Kitab")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredItems, key = { it.id }) { item ->
                val itemSections = sectionsByMutunId[item.id].orEmpty()
                val itemAudios = audiosByMutun[item.id].orEmpty()
                MutunCard(
                    item = item,
                    completedSections = itemSections.count { it.isCompleted },
                    totalSections = itemSections.size,
                    audioCount = itemAudios.size,
                    onClick = { onSelectMutun(item) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddMutunDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, author, category, unitLabel, totalUnits, sectionsRaw ->
                onAddMutun(title, author, category, unitLabel, totalUnits, sectionsRaw)
                showAddDialog = false
            }
        )
    }

    val liveSelected = selectedMutunForDetail?.let { target ->
        mutunItems.find { it.id == target.id }
    }
    if (liveSelected != null) {
        val sections = sectionsByMutunId[liveSelected.id].orEmpty()
        val itemAudios = audiosByMutun[liveSelected.id].orEmpty()
        MutunDetailBottomSheet(
            item = liveSelected,
            sections = sections,
            audios = itemAudios,
            audioState = audioState,
            onDismiss = { onSelectMutun(null) },
            onSave = { units, status, notes ->
                onSaveMutun(liveSelected, units, status, notes)
                onSelectMutun(null)
            },
            onMarkReviewedToday = { onMarkReviewedToday(liveSelected) },
            onDeleteMutun = {
                onDeleteMutun(liveSelected)
                onSelectMutun(null)
            },
            onAddSection = { title, range ->
                onAddSection(liveSelected.id, sections.size, title, range)
            },
            onToggleSection = onToggleSection,
            onDeleteSection = onDeleteSection,
            onAddAudio = { onOpenAddAudioForMutun(liveSelected) },
            onPlayAudio = { audio -> onPlayAudio(audio, itemAudios) },
            onDeleteAudio = onDeleteAudio
        )
    }
}

@Composable
private fun MutunCard(
    item: MutunItemEntity,
    completedSections: Int,
    totalSections: Int,
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.author.isNotBlank()) {
                        Text(
                            text = item.author,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = item.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { item.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = statusColor(item.status),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sectionInfo = if (totalSections > 0) " • Bab $completedSections/$totalSections" else ""
                Text(
                    text = "${item.memorizedUnits}/${item.totalUnits} ${item.unitLabel}$sectionInfo",
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

@Composable
private fun AddMutunDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Tajwid") }
    var unitLabel by remember { mutableStateOf("Bait") }
    var totalUnitsText by remember { mutableStateOf("50") }
    var sectionsRaw by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Kitab Mutun") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Kitab Mutun *") },
                    placeholder = { Text("Contoh: Mandzumah Al-Baiquniyah") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Penulis / Pengarang (opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Bidang Ilmu") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unitLabel,
                        onValueChange = { unitLabel = it },
                        label = { Text("Satuan") },
                        placeholder = { Text("Bait/Hadits/Bab") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = totalUnitsText,
                    onValueChange = { totalUnitsText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Total Jumlah (Bait / Hadits / Bab)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sectionsRaw,
                    onValueChange = { sectionsRaw = it },
                    label = { Text("Daftar Bab / Bagian (opsional, 1 per baris)") },
                    placeholder = { Text("Muqaddimah\nBab 1\nBab 2") },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = {
                    val total = totalUnitsText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    onSave(title, author, category, unitLabel, total, sectionsRaw)
                }
            ) {
                Text("Tambah Kitab")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MutunDetailBottomSheet(
    item: MutunItemEntity,
    sections: List<MutunSectionEntity>,
    audios: List<DriveAudioEntity>,
    audioState: AudioPlayerState,
    onDismiss: () -> Unit,
    onSave: (Int, MemorizationStatus, String) -> Unit,
    onMarkReviewedToday: () -> Unit,
    onDeleteMutun: () -> Unit,
    onAddSection: (String, String) -> Unit,
    onToggleSection: (MutunSectionEntity) -> Unit,
    onDeleteSection: (MutunSectionEntity) -> Unit,
    onAddAudio: () -> Unit,
    onPlayAudio: (DriveAudioEntity) -> Unit,
    onDeleteAudio: (DriveAudioEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var units by remember(item.id, item.memorizedUnits) { mutableIntStateOf(item.memorizedUnits) }
    var status by remember(item.id, item.status) { mutableStateOf(item.status) }
    var notes by remember(item.id, item.notes) { mutableStateOf(item.notes) }

    var newSectionTitle by remember { mutableStateOf("") }
    var newSectionRange by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }

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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = listOfNotNull(
                            item.category.takeIf { it.isNotBlank() },
                            item.author.takeIf { it.isNotBlank() }
                        ).joinToString(" • "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Hapus kitab",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(text = "Status Hafalan", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MemorizationStatus.entries.forEach { option ->
                    FilterChip(
                        selected = status == option,
                        onClick = {
                            status = option
                            if (option == MemorizationStatus.MUTQIN && units < item.totalUnits) {
                                units = item.totalUnits
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
                    text = "${item.unitLabel} Dihafal: $units / ${item.totalUnits}",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { units = (units - 1).coerceAtLeast(0) }) {
                        Icon(Icons.Rounded.Remove, contentDescription = "Kurangi")
                    }
                    IconButton(onClick = { units = (units + 1).coerceAtMost(item.totalUnits) }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Tambah")
                    }
                    TextButton(onClick = { units = item.totalUnits }) {
                        Text("Selesai")
                    }
                }
            }
            Slider(
                value = units.toFloat(),
                onValueChange = { units = it.roundToInt().coerceIn(0, item.totalUnits) },
                valueRange = 0f..item.totalUnits.toFloat()
            )

            OutlinedButton(
                onClick = onMarkReviewedToday,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Catat Muraja'ah Hari Ini (${item.reviewCount}x • ${formatRelativeDate(item.lastReviewedAt)})"
                )
            }

            Text(
                text = "Checklist Bab / Bagian (${sections.count { it.isCompleted }}/${sections.size})",
                style = MaterialTheme.typography.titleMedium
            )
            if (sections.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    sections.forEach { section ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleSection(section) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = section.isCompleted,
                                onCheckedChange = { onToggleSection(section) }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (section.unitRangeLabel.isNotBlank()) {
                                    Text(
                                        text = section.unitRangeLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { onDeleteSection(section) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Hapus bab",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newSectionTitle,
                    onValueChange = { newSectionTitle = it },
                    label = { Text("Tambah Bab Baru") },
                    singleLine = true,
                    modifier = Modifier.weight(1.5f)
                )
                OutlinedTextField(
                    value = newSectionRange,
                    onValueChange = { newSectionRange = it },
                    label = { Text("Bait/Hal") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    enabled = newSectionTitle.isNotBlank(),
                    onClick = {
                        onAddSection(newSectionTitle, newSectionRange)
                        newSectionTitle = ""
                        newSectionRange = ""
                    }
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Tambah bab")
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan Hafalan Mutun") },
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
                    text = "Belum ada audio Drive untuk kitab ini.",
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
                onClick = { onSave(units, status, notes) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan Progres Kitab")
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Hapus Kitab Mutun?") },
            text = { Text("Kitab \"${item.title}\" beserta daftar babnya akan dihapus.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        confirmDelete = false
                        onDeleteMutun()
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
