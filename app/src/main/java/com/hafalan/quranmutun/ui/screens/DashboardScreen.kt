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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hafalan.quranmutun.data.local.DriveAudioEntity
import com.hafalan.quranmutun.data.local.MutunItemEntity
import com.hafalan.quranmutun.data.local.SurahProgressEntity
import com.hafalan.quranmutun.ui.components.ProgressBarWithLabel
import com.hafalan.quranmutun.ui.components.StatusBadge
import com.hafalan.quranmutun.ui.components.formatRelativeDate
import com.hafalan.quranmutun.ui.theme.EmeraldLight
import com.hafalan.quranmutun.ui.theme.EmeraldPrimary
import com.hafalan.quranmutun.ui.theme.GoldAccent
import com.hafalan.quranmutun.ui.viewmodel.MainUiState
import java.util.Locale

@Composable
fun DashboardScreen(
    uiState: MainUiState,
    onOpenQuranTab: () -> Unit,
    onOpenMutunTab: () -> Unit,
    onOpenAudioTab: () -> Unit,
    onSelectSurah: (SurahProgressEntity) -> Unit,
    onSelectMutun: (MutunItemEntity) -> Unit,
    onMarkSurahReviewed: (SurahProgressEntity) -> Unit,
    onMarkMutunReviewed: (MutunItemEntity) -> Unit,
    onPlayAudio: (DriveAudioEntity) -> Unit
) {
    val summary = uiState.summary

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(listOf(EmeraldPrimary, EmeraldLight))
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        text = "Assalamu'alaikum",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Progres Hafalan Anda",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HeroStatBox(
                            title = "Ayat Al-Qur'an",
                            value = "${summary.memorizedQuranVerses}",
                            subtitle = String.format(Locale.US, "≈ %.1f Juz", summary.estimatedJuzMemorized),
                            modifier = Modifier.weight(1f)
                        )
                        HeroStatBox(
                            title = "Surah Mutqin",
                            value = "${summary.mutqinSurahCount}/114",
                            subtitle = "${summary.activeSurahCount} surah aktif",
                            modifier = Modifier.weight(1f)
                        )
                        HeroStatBox(
                            title = "Mutun",
                            value = "${summary.memorizedMutunUnits}",
                            subtitle = "${summary.mutqinMutunCount}/${summary.totalMutunKits} mutqin",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenQuranTab),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hafalan Al-Qur'an",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Lihat 114 Surah →",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    ProgressBarWithLabel(
                        progress = summary.quranProgressFraction,
                        leftLabel = "${summary.memorizedQuranVerses} dari ${summary.totalQuranVerses} ayat",
                        rightLabel = String.format(Locale.US, "%.1f%%", summary.quranProgressFraction * 100f)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenMutunTab),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AutoStories,
                            contentDescription = null,
                            tint = GoldAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hafalan Mutun Ilmiah",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${summary.totalMutunKits} Kitab →",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    ProgressBarWithLabel(
                        progress = summary.mutunProgressFraction,
                        leftLabel = "${summary.memorizedMutunUnits} dari ${summary.totalMutunUnits} bait/bagian",
                        rightLabel = String.format(Locale.US, "%.1f%%", summary.mutunProgressFraction * 100f),
                        barColor = GoldAccent
                    )
                }
            }
        }

        if (uiState.audios.isNotEmpty()) {
            item {
                val latestAudio = uiState.audios.first()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Headphones,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pustaka Audio Google Drive (${uiState.audios.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = latestAudio.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { onPlayAudio(latestAudio) }) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Putar audio",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        FilledTonalButton(onClick = onOpenAudioTab) {
                            Text("Semua")
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenAudioTab),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Headphones,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Belum Ada Audio Google Drive",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Ketuk untuk menempel link audio murottal atau kajian mutun dari Google Drive.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Prioritas Muraja'ah",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        val reviewSurahs = summary.surahsNeedingReview.take(5)
        val reviewMutun = summary.mutunNeedingReview.take(5)

        if (reviewSurahs.isEmpty() && reviewMutun.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Belum ada antrean muraja'ah",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mulai catat ayat Surah atau bait Mutun yang sedang Anda hafal. " +
                                "Item yang berstatus Muraja'ah atau belum diulang ≥ 3 hari akan muncul di sini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(reviewSurahs, key = { "rev-surah-${it.surahNumber}" }) { surah ->
                ReviewQueueCard(
                    title = "${surah.surahNumber}. ${surah.nameLatin}",
                    subtitle = "${surah.memorizedVerses}/${surah.totalVerses} ayat • Terakhir: ${formatRelativeDate(surah.lastReviewedAt)}",
                    status = surah.status,
                    onClick = { onSelectSurah(surah) },
                    onMarkReviewed = { onMarkSurahReviewed(surah) }
                )
            }
            items(reviewMutun, key = { "rev-mutun-${it.id}" }) { mutun ->
                ReviewQueueCard(
                    title = mutun.title,
                    subtitle = "${mutun.memorizedUnits}/${mutun.totalUnits} ${mutun.unitLabel} • Terakhir: ${formatRelativeDate(mutun.lastReviewedAt)}",
                    status = mutun.status,
                    onClick = { onSelectMutun(mutun) },
                    onMarkReviewed = { onMarkMutunReviewed(mutun) }
                )
            }
        }
    }
}

@Composable
private fun HeroStatBox(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReviewQueueCard(
    title: String,
    subtitle: String,
    status: com.hafalan.quranmutun.data.model.MemorizationStatus,
    onClick: () -> Unit,
    onMarkReviewed: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(status = status)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onMarkReviewed) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Tandai muraja'ah hari ini",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
