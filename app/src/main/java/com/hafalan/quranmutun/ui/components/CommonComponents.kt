package com.hafalan.quranmutun.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hafalan.quranmutun.data.local.DriveAudioEntity
import com.hafalan.quranmutun.data.model.MemorizationStatus
import com.hafalan.quranmutun.ui.theme.StatusMemorizing
import com.hafalan.quranmutun.ui.theme.StatusMutqin
import com.hafalan.quranmutun.ui.theme.StatusNeedsReview
import com.hafalan.quranmutun.ui.theme.StatusNotStarted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun statusColor(status: MemorizationStatus): Color = when (status) {
    MemorizationStatus.NOT_STARTED -> StatusNotStarted
    MemorizationStatus.MEMORIZING -> StatusMemorizing
    MemorizationStatus.NEEDS_REVIEW -> StatusNeedsReview
    MemorizationStatus.MUTQIN -> StatusMutqin
}

@Composable
fun StatusBadge(
    status: MemorizationStatus,
    modifier: Modifier = Modifier
) {
    val color = statusColor(status)
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = status.labelId,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ProgressBarWithLabel(
    progress: Float,
    leftLabel: String,
    rightLabel: String,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = leftLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = rightLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun DriveAudioRow(
    audio: DriveAudioEntity,
    isCurrentTrack: Boolean,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    subtitleOverride: String? = null,
    isLocked: Boolean = false,
    isCompleted: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCurrentTrack -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                isLocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isLocked) MaterialTheme.colorScheme.outlineVariant
                        else MaterialTheme.colorScheme.primary
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onPlayClick) {
                    Icon(
                        imageVector = when {
                            isLocked -> Icons.Rounded.Lock
                            isCurrentTrack && isPlaying -> Icons.Rounded.Pause
                            else -> Icons.Rounded.PlayArrow
                        },
                        contentDescription = if (isLocked) "Terkunci" else "Putar audio",
                        tint = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCurrentTrack) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    } else if (isCompleted) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Selesai",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = audio.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val sub = subtitleOverride ?: buildString {
                    if (audio.reciterOrSource.isNotBlank()) {
                        append(audio.reciterOrSource)
                        append(" • ")
                    }
                    append("Google Drive")
                }
                Text(
                    text = sub,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (onDeleteClick != null) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Hapus audio",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun formatRelativeDate(epochMillis: Long?): String {
    if (epochMillis == null) return "Belum pernah"
    val diffDays = ((System.currentTimeMillis() - epochMillis) / (24L * 60L * 60L * 1000L)).toInt()
    return when {
        diffDays <= 0 -> "Hari ini"
        diffDays == 1 -> "Kemarin"
        diffDays < 7 -> "$diffDays hari lalu"
        else -> SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(epochMillis))
    }
}

fun formatDuration(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSec / 60L
    val seconds = totalSec % 60L
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
