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
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
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
import com.hafalan.quranmutun.data.local.MutunItemEntity
import com.hafalan.quranmutun.data.local.SurahProgressEntity
import com.hafalan.quranmutun.data.model.AudioTargetType
import com.hafalan.quranmutun.ui.components.AddDriveAudioDialog
import com.hafalan.quranmutun.ui.components.FullAudioPlayerSheet
import com.hafalan.quranmutun.ui.components.MiniAudioPlayerBar
import com.hafalan.quranmutun.ui.screens.AudioLibraryScreen
import com.hafalan.quranmutun.ui.screens.DashboardScreen
import com.hafalan.quranmutun.ui.screens.MutunScreen
import com.hafalan.quranmutun.ui.screens.QuranScreen
import com.hafalan.quranmutun.ui.theme.HafalanTheme
import com.hafalan.quranmutun.ui.viewmodel.MainViewModel

private enum class AppTab(val label: String, val title: String, val icon: ImageVector) {
    DASHBOARD("Beranda", "Hafalan Qur'an & Mutun", Icons.Rounded.Dashboard),
    QURAN("Al-Qur'an", "Hafalan Al-Qur'an", Icons.Rounded.MenuBook),
    MUTUN("Mutun", "Hafalan Mutun Ilmiah", Icons.Rounded.AutoStories),
    AUDIO("Audio", "Audio Google Drive", Icons.Rounded.Headphones)
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
        MainViewModel.Factory(app.repository, app.audioPlayerManager)
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

    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    var selectedSurahForDetail by remember { mutableStateOf<SurahProgressEntity?>(null) }
    var selectedMutunForDetail by remember { mutableStateOf<MutunItemEntity?>(null) }
    var showFullPlayerSheet by remember { mutableStateOf(false) }
    var addAudioConfig by remember { mutableStateOf<AddAudioDialogConfig?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
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
                    AppTab.DASHBOARD -> DashboardScreen(
                        uiState = uiState,
                        onOpenQuranTab = { currentTab = AppTab.QURAN },
                        onOpenMutunTab = { currentTab = AppTab.MUTUN },
                        onOpenAudioTab = { currentTab = AppTab.AUDIO },
                        onSelectSurah = { surah ->
                            selectedSurahForDetail = surah
                            currentTab = AppTab.QURAN
                        },
                        onSelectMutun = { mutun ->
                            selectedMutunForDetail = mutun
                            currentTab = AppTab.MUTUN
                        },
                        onMarkSurahReviewed = viewModel::markSurahReviewedToday,
                        onMarkMutunReviewed = viewModel::markMutunReviewedToday,
                        onPlayAudio = { audio -> viewModel.playAudio(audio, uiState.audios) }
                    )

                    AppTab.QURAN -> QuranScreen(
                        surahs = uiState.surahs,
                        audios = uiState.audios,
                        audioState = audioState,
                        selectedSurahForDetail = selectedSurahForDetail,
                        onSelectSurah = { selectedSurahForDetail = it },
                        onSaveSurah = viewModel::updateSurahProgress,
                        onMarkReviewedToday = viewModel::markSurahReviewedToday,
                        onOpenAddAudioForSurah = { surah ->
                            addAudioConfig = AddAudioDialogConfig(
                                targetType = AudioTargetType.QURAN,
                                surahNumber = surah.surahNumber,
                                lockTarget = true
                            )
                        },
                        onPlayAudio = viewModel::playAudio,
                        onDeleteAudio = viewModel::deleteAudio
                    )

                    AppTab.MUTUN -> MutunScreen(
                        mutunItems = uiState.mutunItems,
                        sectionsByMutunId = uiState.sectionsByMutunId,
                        audios = uiState.audios,
                        audioState = audioState,
                        selectedMutunForDetail = selectedMutunForDetail,
                        onSelectMutun = { selectedMutunForDetail = it },
                        onAddMutun = viewModel::addMutun,
                        onSaveMutun = viewModel::updateMutunProgress,
                        onMarkReviewedToday = viewModel::markMutunReviewedToday,
                        onDeleteMutun = viewModel::deleteMutun,
                        onAddSection = viewModel::addMutunSection,
                        onToggleSection = viewModel::toggleMutunSection,
                        onDeleteSection = viewModel::deleteMutunSection,
                        onOpenAddAudioForMutun = { item ->
                            addAudioConfig = AddAudioDialogConfig(
                                targetType = AudioTargetType.MUTUN,
                                mutunId = item.id,
                                lockTarget = true
                            )
                        },
                        onPlayAudio = viewModel::playAudio,
                        onDeleteAudio = viewModel::deleteAudio
                    )

                    AppTab.AUDIO -> AudioLibraryScreen(
                        audios = uiState.audios,
                        surahs = uiState.surahs,
                        mutunItems = uiState.mutunItems,
                        audioState = audioState,
                        onOpenAddAudio = {
                            addAudioConfig = AddAudioDialogConfig()
                        },
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
            onDismiss = { showFullPlayerSheet = false },
            onTogglePlayPause = viewModel::togglePlayPause,
            onSeekTo = viewModel::seekTo,
            onSeekBy = viewModel::seekBy,
            onSetLoopMode = viewModel::setLoopMode,
            onSetSpeed = viewModel::setPlaybackSpeed,
            onPrevious = viewModel::playPreviousInPlaylist,
            onNext = viewModel::playNextInPlaylist
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
