package com.hafalan.quranmutun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hafalan.quranmutun.data.model.AudioTargetType
import com.hafalan.quranmutun.player.SequentialLockPolicy
import com.hafalan.quranmutun.ui.components.AddDriveAudioDialog
import com.hafalan.quranmutun.ui.components.FullAudioPlayerSheet
import com.hafalan.quranmutun.ui.components.MiniAudioPlayerBar
import com.hafalan.quranmutun.ui.screens.AudioLibraryScreen
import com.hafalan.quranmutun.ui.screens.DrivingModeOverlay
import com.hafalan.quranmutun.ui.screens.ListenScreen
import com.hafalan.quranmutun.ui.screens.TargetHafalanScreen
import com.hafalan.quranmutun.ui.theme.HafalanTheme
import com.hafalan.quranmutun.ui.viewmodel.MainViewModel

private enum class AppTab(val label: String, val title: String, val icon: ImageVector) {
    LISTEN("Dengarkan", "Muraja'ah Berurutan", Icons.Rounded.Headphones),
    TARGET("Hafalan Saya", "Target Hafalan & Mutun", Icons.Rounded.MenuBook),
    LIBRARY("Pustaka Drive", "Pustaka Google Drive", Icons.Rounded.CloudQueue)
}

private data class AddAudioDialogConfig(
    val targetType: AudioTargetType = AudioTargetType.QURAN,
    val surahNumber: Int? = null,
    val mutunId: Long? = null,
    val lockTarget: Boolean = false
)

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        val app = application as HafalanApp
        MainViewModel.Factory(app.repository, app.audioPlayerManager, app.userPreferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HafalanTheme {
                HafalanRootScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HafalanRootScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val audioState by viewModel.audioState.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(AppTab.LISTEN) }
    var showFullPlayerSheet by remember { mutableStateOf(false) }
    var showDrivingMode by remember { mutableStateOf(false) }
    var addAudioConfig by remember { mutableStateOf<AddAudioDialogConfig?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val currentIndexInPlaylist = remember(audioState.currentAudio, audioState.playlist) {
        val id = audioState.currentAudio?.id ?: return@remember -1
        audioState.playlist.indexOfFirst { it.id == id }
    }
    val canSkipPrevious = remember(
        audioState.playlist,
        currentIndexInPlaylist,
        uiState.listeningPrefs.completedAudioIds
    ) {
        SequentialLockPolicy.canGoToPrevious(
            audioState.playlist,
            currentIndexInPlaylist,
            uiState.listeningPrefs.completedAudioIds
        )
    }
    val canSkipNext = remember(
        audioState.playlist,
        currentIndexInPlaylist,
        uiState.listeningPrefs.completedAudioIds
    ) {
        SequentialLockPolicy.canAdvanceToNext(
            audioState.playlist,
            currentIndexInPlaylist,
            uiState.listeningPrefs.completedAudioIds
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTab.title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Column {
                if (audioState.currentAudio != null) {
                    MiniAudioPlayerBar(
                        state = audioState,
                        onExpand = { showFullPlayerSheet = true },
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onCycleLoop = viewModel::cycleLoopMode,
                        onDismiss = viewModel::stopPlayer
                    )
                }
                NavigationBar {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                when (currentTab) {
                    AppTab.LISTEN -> ListenScreen(
                        audios = uiState.audios,
                        surahs = uiState.surahs,
                        mutunItems = uiState.mutunItems,
                        prefs = uiState.listeningPrefs,
                        audioState = audioState,
                        onPlayAudio = viewModel::playAudio,
                        onPlayOrderedCategory = viewModel::playOrderedCategory,
                        onResetSequence = viewModel::resetSequenceProgress,
                        onOpenDrivingMode = { showDrivingMode = true },
                        onOpenAddAudio = { addAudioConfig = AddAudioDialogConfig() },
                        onSetAutoAdvance = viewModel::setAutoAdvancePlaylist,
                        onSetLoopMode = viewModel::setLoopMode,
                        onSetSpeed = viewModel::setPlaybackSpeed,
                        onDeleteAudio = viewModel::deleteAudio
                    )

                    AppTab.TARGET -> TargetHafalanScreen(
                        surahs = uiState.surahs,
                        mutunItems = uiState.mutunItems,
                        audios = uiState.audios,
                        prefs = uiState.listeningPrefs,
                        audioState = audioState,
                        onSetHafalanRange = viewModel::setHafalanRange,
                        onAddAudioForSurah = { surah ->
                            addAudioConfig = AddAudioDialogConfig(
                                targetType = AudioTargetType.QURAN,
                                surahNumber = surah.surahNumber,
                                lockTarget = true
                            )
                        },
                        onAddAudioForMutun = { mutun ->
                            addAudioConfig = AddAudioDialogConfig(
                                targetType = AudioTargetType.MUTUN,
                                mutunId = mutun.id,
                                lockTarget = true
                            )
                        },
                        onUpdateMutunDetails = viewModel::updateMutunDetails,
                        onAddCustomMutun = { title, category, author ->
                            viewModel.addMutun(
                                title = title,
                                author = author,
                                category = category,
                                unitLabel = "Bagian",
                                totalUnits = 1,
                                sectionNamesRaw = ""
                            )
                        },
                        onDeleteMutun = viewModel::deleteMutun,
                        onResetStats = viewModel::resetRepetitionStats,
                        onPlayAudio = viewModel::playAudio
                    )

                    AppTab.LIBRARY -> AudioLibraryScreen(
                        audios = uiState.audios,
                        surahs = uiState.surahs,
                        mutunItems = uiState.mutunItems,
                        prefs = uiState.listeningPrefs,
                        audioState = audioState,
                        onOpenAddAudio = { addAudioConfig = AddAudioDialogConfig() },
                        onPlayAudio = viewModel::playAudio,
                        onDeleteAudio = viewModel::deleteAudio
                    )
                }
            }
        }
    }

    if (showFullPlayerSheet && audioState.currentAudio != null) {
        FullAudioPlayerSheet(
            state = audioState,
            canSkipPrevious = canSkipPrevious,
            canSkipNext = canSkipNext,
            onDismiss = { showFullPlayerSheet = false },
            onTogglePlayPause = viewModel::togglePlayPause,
            onSetLoopMode = viewModel::setLoopMode,
            onSetSpeed = viewModel::setPlaybackSpeed,
            onPrevious = viewModel::playPreviousInPlaylist,
            onNext = viewModel::playNextInPlaylist
        )
    }

    if (showDrivingMode) {
        DrivingModeOverlay(
            audioState = audioState,
            canSkipPrevious = canSkipPrevious,
            canSkipNext = canSkipNext,
            keepScreenOn = uiState.listeningPrefs.keepScreenOnInDrivingMode,
            onToggleKeepScreenOn = viewModel::setKeepScreenOnInDrivingMode,
            onTogglePlayPause = viewModel::togglePlayPause,
            onPrevious = viewModel::playPreviousInPlaylist,
            onNext = viewModel::playNextInPlaylist,
            onCycleLoop = viewModel::cycleLoopMode,
            onDismiss = { showDrivingMode = false }
        )
    }

    addAudioConfig?.let { config ->
        AddDriveAudioDialog(
            surahs = uiState.surahs,
            mutunItems = uiState.mutunItems,
            isFetchingFolder = uiState.isFetchingFolder,
            initialTargetType = config.targetType,
            initialSurahNumber = config.surahNumber,
            initialMutunId = config.mutunId,
            lockTarget = config.lockTarget,
            onDismiss = { addAudioConfig = null },
            onSaveLinks = viewModel::addSingleOrBatchDriveLinks
        )
    }
}
