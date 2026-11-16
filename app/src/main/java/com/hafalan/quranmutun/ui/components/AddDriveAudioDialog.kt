package com.hafalan.quranmutun.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hafalan.quranmutun.data.local.MutunItemEntity
import com.hafalan.quranmutun.data.local.SurahProgressEntity
import com.hafalan.quranmutun.data.model.AudioTargetType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDriveAudioDialog(
    surahs: List<SurahProgressEntity>,
    mutunItems: List<MutunItemEntity>,
    isFetchingFolder: Boolean,
    initialTargetType: AudioTargetType = AudioTargetType.QURAN,
    initialSurahNumber: Int? = null,
    initialMutunId: Long? = null,
    lockTarget: Boolean = false,
    onDismiss: () -> Unit,
    onSaveLinks: (
        rawInput: String,
        title: String,
        reciterOrSource: String,
        targetType: AudioTargetType,
        surahNumber: Int?,
        mutunId: Long?,
        onDone: (Boolean) -> Unit
    ) -> Unit
) {
    var linkInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var reciterInput by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf(initialTargetType) }
    var selectedSurahNumber by remember { mutableStateOf(initialSurahNumber) }
    var selectedMutunId by remember { mutableStateOf(initialMutunId) }

    var surahMenuExpanded by remember { mutableStateOf(false) }
    var mutunMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isFetchingFolder) onDismiss() },
        title = { Text("Tambah Audio Google Drive") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Pastikan akses file/folder di Google Drive sudah diset ke \"Siapa saja yang memiliki link\".\n" +
                        "Anda bisa menempel 1 link file, link folder publik, atau banyak link (1 baris = 1 link atau format: Judul | Link).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = linkInput,
                    onValueChange = { linkInput = it },
                    label = { Text("Link File / Folder Google Drive *") },
                    placeholder = { Text("https://drive.google.com/file/d/.../view") },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Judul Audio (opsional jika folder / Judul | Link)") },
                    placeholder = { Text("Contoh: Al-Mulk - Syaikh Mishary") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reciterInput,
                    onValueChange = { reciterInput = it },
                    label = { Text("Qari / Ustadz / Sumber (opsional)") },
                    placeholder = { Text("Contoh: Syaikh Mishary Rashid / Rekaman Setoran") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!lockTarget) {
                    Text(
                        text = "Tautkan ke:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AudioTargetType.entries.forEach { type ->
                            FilterChip(
                                selected = targetType == type,
                                onClick = { targetType = type },
                                label = { Text(type.labelId) }
                            )
                        }
                    }

                    if (targetType == AudioTargetType.QURAN && surahs.isNotEmpty()) {
                        val selectedSurah = surahs.find { it.surahNumber == selectedSurahNumber }
                        ExposedDropdownMenuBox(
                            expanded = surahMenuExpanded,
                            onExpandedChange = { surahMenuExpanded = !surahMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedSurah?.let { "${it.surahNumber}. ${it.nameLatin}" }
                                    ?: "Pilih Surah (opsional)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Surah Al-Qur'an") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = surahMenuExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = surahMenuExpanded,
                                onDismissRequest = { surahMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Tanpa surah tertentu") },
                                    onClick = {
                                        selectedSurahNumber = null
                                        surahMenuExpanded = false
                                    }
                                )
                                surahs.forEach { surah ->
                                    DropdownMenuItem(
                                        text = { Text("${surah.surahNumber}. ${surah.nameLatin} (${surah.totalVerses} ayat)") },
                                        onClick = {
                                            selectedSurahNumber = surah.surahNumber
                                            surahMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (targetType == AudioTargetType.MUTUN && mutunItems.isNotEmpty()) {
                        val selectedMutun = mutunItems.find { it.id == selectedMutunId }
                        ExposedDropdownMenuBox(
                            expanded = mutunMenuExpanded,
                            onExpandedChange = { mutunMenuExpanded = !mutunMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedMutun?.title ?: "Pilih Kitab Mutun (opsional)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Kitab Mutun") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = mutunMenuExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = mutunMenuExpanded,
                                onDismissRequest = { mutunMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Tanpa kitab tertentu") },
                                    onClick = {
                                        selectedMutunId = null
                                        mutunMenuExpanded = false
                                    }
                                )
                                mutunItems.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item.title) },
                                        onClick = {
                                            selectedMutunId = item.id
                                            mutunMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isFetchingFolder && linkInput.isNotBlank(),
                onClick = {
                    onSaveLinks(
                        linkInput,
                        titleInput,
                        reciterInput,
                        targetType,
                        selectedSurahNumber,
                        selectedMutunId
                    ) { success ->
                        if (success) onDismiss()
                    }
                }
            ) {
                if (isFetchingFolder) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Mengambil Folder...")
                } else {
                    Text("Simpan Audio")
                }
            }
        },
        dismissButton = {
            TextButton(enabled = !isFetchingFolder, onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
