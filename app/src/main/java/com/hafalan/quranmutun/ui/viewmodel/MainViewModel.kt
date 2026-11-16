package com.hafalan.quranmutun.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hafalan.quranmutun.data.drive.DriveFolderFetcher
import com.hafalan.quranmutun.data.drive.DriveUrlParseResult
import com.hafalan.quranmutun.data.drive.FolderFetchResult
import com.hafalan.quranmutun.data.drive.GoogleDriveLinkParser
import com.hafalan.quranmutun.data.drive.ParsedDriveFile
import com.hafalan.quranmutun.data.local.DriveAudioEntity
import com.hafalan.quranmutun.data.local.MutunItemEntity
import com.hafalan.quranmutun.data.local.MutunSectionEntity
import com.hafalan.quranmutun.data.local.SurahProgressEntity
import com.hafalan.quranmutun.data.model.AudioTargetType
import com.hafalan.quranmutun.data.model.LoopMode
import com.hafalan.quranmutun.data.model.MemorizationStatus
import com.hafalan.quranmutun.data.repository.HafalanRepository
import com.hafalan.quranmutun.player.AudioPlayerManager
import com.hafalan.quranmutun.player.AudioPlayerState
import com.hafalan.quranmutun.ui.state.DashboardSummary
import com.hafalan.quranmutun.ui.state.ProgressSummaryCalculator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoading: Boolean = true,
    val surahs: List<SurahProgressEntity> = emptyList(),
    val mutunItems: List<MutunItemEntity> = emptyList(),
    val sectionsByMutunId: Map<Long, List<MutunSectionEntity>> = emptyMap(),
    val audios: List<DriveAudioEntity> = emptyList(),
    val summary: DashboardSummary = ProgressSummaryCalculator.calculate(emptyList(), emptyList()),
    val isFetchingFolder: Boolean = false
)

class MainViewModel(
    private val repository: HafalanRepository,
    private val playerManager: AudioPlayerManager
) : ViewModel() {

    private val _isInitialized = MutableStateFlow(false)
    private val _isFetchingFolder = MutableStateFlow(false)

    private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val toastMessage = _toastMessage.asSharedFlow()

    val audioState: StateFlow<AudioPlayerState> = playerManager.state

    val uiState: StateFlow<MainUiState> = combine(
        repository.observeSurahs(),
        repository.observeMutunItems(),
        repository.observeAllMutunSections(),
        repository.observeAudios(),
        _isInitialized,
        _isFetchingFolder
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val surahs = values[0] as List<SurahProgressEntity>
        @Suppress("UNCHECKED_CAST")
        val mutun = values[1] as List<MutunItemEntity>
        @Suppress("UNCHECKED_CAST")
        val sections = values[2] as List<MutunSectionEntity>
        @Suppress("UNCHECKED_CAST")
        val audios = values[3] as List<DriveAudioEntity>
        val initialized = values[4] as Boolean
        val fetchingFolder = values[5] as Boolean

        MainUiState(
            isLoading = !initialized,
            surahs = surahs,
            mutunItems = mutun,
            sectionsByMutunId = sections.groupBy { it.mutunId },
            audios = audios,
            summary = ProgressSummaryCalculator.calculate(surahs, mutun),
            isFetchingFolder = fetchingFolder
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState()
    )

    init {
        viewModelScope.launch {
            repository.initialize()
            _isInitialized.value = true
        }
    }

    fun updateSurahProgress(
        surah: SurahProgressEntity,
        memorizedVerses: Int,
        status: MemorizationStatus,
        notes: String
    ) {
        viewModelScope.launch {
            val clamped = memorizedVerses.coerceIn(0, surah.totalVerses)
            val normalizedStatus = when {
                clamped == surah.totalVerses && status == MemorizationStatus.NOT_STARTED -> MemorizationStatus.MUTQIN
                clamped > 0 && status == MemorizationStatus.NOT_STARTED -> MemorizationStatus.MEMORIZING
                else -> status
            }
            repository.updateSurah(
                surah.copy(
                    memorizedVerses = clamped,
                    status = normalizedStatus,
                    notes = notes.trim(),
                    lastReviewedAt = if (clamped > 0 && surah.lastReviewedAt == null) {
                        System.currentTimeMillis()
                    } else {
                        surah.lastReviewedAt
                    }
                )
            )
            _toastMessage.tryEmit("Progres ${surah.nameLatin} disimpan")
        }
    }

    fun markSurahReviewedToday(surah: SurahProgressEntity) {
        viewModelScope.launch {
            repository.markSurahReviewedToday(surah)
            _toastMessage.tryEmit("Muraja'ah ${surah.nameLatin} tercatat")
        }
    }

    fun addMutun(
        title: String,
        author: String,
        category: String,
        unitLabel: String,
        totalUnits: Int,
        sectionNamesRaw: String
    ) {
        if (title.isBlank()) {
            _toastMessage.tryEmit("Nama kitab mutun wajib diisi")
            return
        }
        viewModelScope.launch {
            val sections = sectionNamesRaw
                .lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toList()
            val safeUnits = totalUnits.coerceAtLeast(1)
            repository.addMutun(
                item = MutunItemEntity(
                    title = title.trim(),
                    author = author.trim(),
                    category = category.trim().ifEmpty { "Umum" },
                    unitLabel = unitLabel.trim().ifEmpty { "Bait" },
                    totalUnits = safeUnits
                ),
                sectionTitles = sections
            )
            _toastMessage.tryEmit("Kitab \"$title\" ditambahkan")
        }
    }

    fun updateMutunProgress(
        item: MutunItemEntity,
        memorizedUnits: Int,
        status: MemorizationStatus,
        notes: String
    ) {
        viewModelScope.launch {
            val clamped = memorizedUnits.coerceIn(0, item.totalUnits)
            val normalizedStatus = when {
                clamped == item.totalUnits && status == MemorizationStatus.NOT_STARTED -> MemorizationStatus.MUTQIN
                clamped > 0 && status == MemorizationStatus.NOT_STARTED -> MemorizationStatus.MEMORIZING
                else -> status
            }
            repository.updateMutun(
                item.copy(
                    memorizedUnits = clamped,
                    status = normalizedStatus,
                    notes = notes.trim(),
                    lastReviewedAt = if (clamped > 0 && item.lastReviewedAt == null) {
                        System.currentTimeMillis()
                    } else {
                        item.lastReviewedAt
                    }
                )
            )
            _toastMessage.tryEmit("Progres ${item.title} disimpan")
        }
    }

    fun markMutunReviewedToday(item: MutunItemEntity) {
        viewModelScope.launch {
            repository.markMutunReviewedToday(item)
            _toastMessage.tryEmit("Muraja'ah ${item.title} tercatat")
        }
    }

    fun deleteMutun(item: MutunItemEntity) {
        viewModelScope.launch {
            repository.deleteMutun(item)
            _toastMessage.tryEmit("${item.title} dihapus")
        }
    }

    fun addMutunSection(mutunId: Long, nextOrderIndex: Int, title: String, unitRangeLabel: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addMutunSection(mutunId, nextOrderIndex, title, unitRangeLabel)
        }
    }

    fun toggleMutunSection(section: MutunSectionEntity) {
        viewModelScope.launch {
            repository.toggleMutunSection(section)
        }
    }

    fun deleteMutunSection(section: MutunSectionEntity) {
        viewModelScope.launch {
            repository.deleteMutunSection(section)
        }
    }

    fun addSingleOrBatchDriveLinks(
        rawInput: String,
        defaultTitle: String,
        reciterOrSource: String,
        targetType: AudioTargetType,
        surahNumber: Int?,
        mutunId: Long?,
        onDone: (Boolean) -> Unit
    ) {
        val parsedResults = GoogleDriveLinkParser.parseBatchLines(rawInput)
        if (parsedResults.isEmpty()) {
            _toastMessage.tryEmit("Tempelkan minimal satu link Google Drive")
            onDone(false)
            return
        }

        val folderResult = parsedResults.filterIsInstance<DriveUrlParseResult.Folder>().firstOrNull()
        if (folderResult != null && parsedResults.size == 1) {
            importDriveFolder(
                folderId = folderResult.folderId,
                reciterOrSource = reciterOrSource,
                targetType = targetType,
                surahNumber = surahNumber,
                mutunId = mutunId,
                onDone = onDone
            )
            return
        }

        val validFiles = parsedResults.filterIsInstance<DriveUrlParseResult.File>().map { it.parsed }
        if (validFiles.isEmpty()) {
            val firstError = parsedResults.filterIsInstance<DriveUrlParseResult.Invalid>().firstOrNull()?.reason
            _toastMessage.tryEmit(firstError ?: "Link Google Drive tidak valid")
            onDone(false)
            return
        }

        viewModelScope.launch {
            saveParsedDriveFiles(
                files = validFiles,
                defaultTitle = defaultTitle,
                reciterOrSource = reciterOrSource,
                targetType = targetType,
                surahNumber = surahNumber,
                mutunId = mutunId
            )
            onDone(true)
        }
    }

    fun importDriveFolder(
        folderInput: String,
        reciterOrSource: String,
        targetType: AudioTargetType,
        surahNumber: Int?,
        mutunId: Long?,
        onDone: (Boolean) -> Unit
    ) {
        when (val parsed = GoogleDriveLinkParser.parseSingle(folderInput)) {
            is DriveUrlParseResult.Folder -> {
                importDriveFolder(
                    folderId = parsed.folderId,
                    reciterOrSource = reciterOrSource,
                    targetType = targetType,
                    surahNumber = surahNumber,
                    mutunId = mutunId,
                    onDone = onDone
                )
            }
            is DriveUrlParseResult.File -> {
                viewModelScope.launch {
                    saveParsedDriveFiles(
                        files = listOf(parsed.parsed),
                        defaultTitle = "",
                        reciterOrSource = reciterOrSource,
                        targetType = targetType,
                        surahNumber = surahNumber,
                        mutunId = mutunId
                    )
                    onDone(true)
                }
            }
            is DriveUrlParseResult.Invalid -> {
                _toastMessage.tryEmit(parsed.reason)
                onDone(false)
            }
        }
    }

    private fun importDriveFolder(
        folderId: String,
        reciterOrSource: String,
        targetType: AudioTargetType,
        surahNumber: Int?,
        mutunId: Long?,
        onDone: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isFetchingFolder.value = true
            val result = DriveFolderFetcher.fetchPublicFolder(folderId)
            _isFetchingFolder.value = false
            when (result) {
                is FolderFetchResult.Success -> {
                    saveParsedDriveFiles(
                        files = result.files,
                        defaultTitle = "",
                        reciterOrSource = reciterOrSource,
                        targetType = targetType,
                        surahNumber = surahNumber,
                        mutunId = mutunId
                    )
                    onDone(true)
                }
                is FolderFetchResult.Error -> {
                    _toastMessage.tryEmit(result.message)
                    onDone(false)
                }
            }
        }
    }

    private suspend fun saveParsedDriveFiles(
        files: List<ParsedDriveFile>,
        defaultTitle: String,
        reciterOrSource: String,
        targetType: AudioTargetType,
        surahNumber: Int?,
        mutunId: Long?
    ) {
        val now = System.currentTimeMillis()
        val entities = files.mapIndexed { idx, file ->
            val resolvedTitle = file.suggestedTitle?.takeIf { it.isNotBlank() }
                ?: defaultTitle.trim().takeIf { it.isNotBlank() }
                    ?.let { if (files.size > 1) "$it (${idx + 1})" else it }
                ?: "Audio Drive #${idx + 1}"
            DriveAudioEntity(
                title = resolvedTitle,
                reciterOrSource = reciterOrSource.trim(),
                originalUrl = file.originalUrl,
                driveFileId = file.fileId,
                streamUrl = file.streamUrl,
                targetType = targetType,
                surahNumber = if (targetType == AudioTargetType.QURAN) surahNumber else null,
                mutunId = if (targetType == AudioTargetType.MUTUN) mutunId else null,
                createdAt = now + idx
            )
        }
        repository.addAudios(entities)
        _toastMessage.tryEmit("${entities.size} audio Google Drive ditambahkan")
    }

    fun updateAudioLink(audio: DriveAudioEntity) {
        viewModelScope.launch {
            repository.updateAudio(audio)
            _toastMessage.tryEmit("Audio diperbarui")
        }
    }

    fun deleteAudio(audio: DriveAudioEntity) {
        viewModelScope.launch {
            if (playerManager.state.value.currentAudio?.id == audio.id) {
                playerManager.stopAndDismiss()
            }
            repository.deleteAudio(audio)
            _toastMessage.tryEmit("Audio dihapus")
        }
    }

    fun playAudio(audio: DriveAudioEntity, playlist: List<DriveAudioEntity> = uiState.value.audios) {
        playerManager.playAudio(audio, playlist)
    }

    fun togglePlayPause() = playerManager.togglePlayPause()
    fun seekTo(positionMs: Long) = playerManager.seekTo(positionMs)
    fun seekBy(deltaMs: Long) = playerManager.seekBy(deltaMs)
    fun setPlaybackSpeed(speed: Float) = playerManager.setPlaybackSpeed(speed)
    fun setLoopMode(loopMode: LoopMode) = playerManager.setLoopMode(loopMode)
    fun cycleLoopMode() = playerManager.cycleLoopMode()
    fun playNextInPlaylist() = playerManager.playNextInPlaylist()
    fun playPreviousInPlaylist() = playerManager.playPreviousInPlaylist()
    fun stopPlayer() = playerManager.stopAndDismiss()
    fun clearPlayerError() = playerManager.clearError()

    class Factory(
        private val repository: HafalanRepository,
        private val playerManager: AudioPlayerManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository, playerManager) as T
        }
    }
}
