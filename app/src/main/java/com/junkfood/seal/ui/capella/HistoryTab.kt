package com.junkfood.seal.ui.capella

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.download.Task
import com.junkfood.seal.download.Task.DownloadState
import com.junkfood.seal.util.CONVERT_MP3

@Composable
fun HistoryTab(viewModel: CapellaViewModel) {
    val context = LocalContext.current
    val completed by viewModel.completed.collectAsStateWithLifecycle()
    val active = viewModel.taskStates().toActiveRows()
    val rows = active + completed

    val note =
        if (completed.isEmpty()) {
            "Henüz indirilen dosya yok"
        } else {
            "${completed.size} dosya · ${formatSize(completed.sumOf { it.sizeBytes })}"
        }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding =
            PaddingValues(
                start = CapellaDimens.ScreenPaddingHorizontal,
                end = CapellaDimens.ScreenPaddingHorizontal,
                top = CapellaDimens.ScreenPaddingTop,
                bottom = CapellaDimens.ContentPaddingBottom,
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column {
                Text(
                    text = "Geçmiş",
                    modifier = Modifier.padding(top = 14.dp, bottom = 4.dp),
                    fontFamily = Manrope,
                    fontSize = CapellaType.TitleSize,
                    fontWeight = FontWeight.W800,
                    letterSpacing = CapellaType.TitleLetterSpacing,
                    color = CapellaColors.TextPrimary,
                )
                Text(
                    text = note,
                    modifier = Modifier.padding(top = 6.dp, bottom = 14.dp),
                    fontFamily = Manrope,
                    fontSize = CapellaType.SubtitleSize,
                    fontWeight = FontWeight.W500,
                    color = CapellaColors.TextSecondary,
                )
            }
        }

        items(items = rows, key = { it.key }) { row ->
            HistoryItem(
                row = row,
                onCancel = { row.task?.let(viewModel::cancel) },
                onOpen = {
                    row.path?.let { path ->
                        viewModel.openFile(path) {
                            Toast.makeText(context, "Dosya açılamadı", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun HistoryItem(row: HistoryRow, onCancel: () -> Unit, onOpen: () -> Unit) {
    val isVideo = row.kind.equals("MP4", ignoreCase = true)
    val acilabilir = row.task == null && row.path != null
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CapellaColors.SurfaceMuted)
                .then(if (acilabilir) Modifier.clickable(onClick = onOpen) else Modifier)
                .padding(CapellaDimens.CardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            modifier =
                Modifier.size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        if (isVideo) CapellaColors.AccentSoft else CapellaColors.NeutralBadge
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = row.kind,
                fontFamily = Manrope,
                fontSize = 11.sp,
                fontWeight = FontWeight.W800,
                letterSpacing = 0.3.sp,
                color = if (isVideo) CapellaColors.Accent else CapellaColors.TextSecondary,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.title,
                fontFamily = Manrope,
                fontSize = CapellaType.RowTitleSize,
                fontWeight = FontWeight.W700,
                color = CapellaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = row.meta,
                modifier = Modifier.padding(top = 5.dp),
                fontFamily = Manrope,
                fontSize = CapellaType.RowMetaSize,
                fontWeight = FontWeight.W500,
                color = CapellaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            row.progress?.let { value ->
                Box(
                    modifier =
                        Modifier.padding(top = 9.dp)
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(CapellaColors.ProgressTrack)
                ) {
                    Box(
                        modifier =
                            Modifier.fillMaxWidth(value.coerceIn(0f, 1f))
                                .height(4.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(CapellaColors.Accent)
                    )
                }
            }
        }

        if (row.task != null) {
            Box(
                modifier =
                    Modifier.size(30.dp)
                        .clip(CircleShape)
                        .background(CapellaColors.AccentSoft)
                        .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "İptal",
                    modifier = Modifier.size(17.dp),
                    tint = CapellaColors.Accent,
                )
            }
        } else {
            Text(
                text = row.right,
                fontFamily = Manrope,
                fontSize = CapellaType.RowMetaSize,
                fontWeight = FontWeight.W700,
                color = CapellaColors.TextTertiary,
            )
        }
    }
}

/** Süren görevleri geçmiş satırına çevirir. Biten görevler veritabanından geldiği için atlanır. */
private fun Map<Task, Task.State>.toActiveRows(): List<HistoryRow> =
    entries
        .filter { (_, state) -> state.downloadState !is DownloadState.Completed }
        .sortedByDescending { (task, _) -> task.timeCreated }
        .map { (task, state) ->
            val downloadState = state.downloadState
            HistoryRow(
                key = "task_${task.id}",
                title = state.viewState.title.ifBlank { task.url },
                kind = task.kindLabel(),
                meta = downloadState.statusText(),
                right = "Şimdi",
                progress =
                    (downloadState as? DownloadState.Running)?.progress?.takeIf { it >= 0f },
                path = null,
                sizeBytes = 0L,
                task = task,
            )
        }

private fun Task.kindLabel(): String =
    with(preferences) {
        when {
            extractAudio && convertAudio && audioConvertFormat == CONVERT_MP3 -> "MP3"
            extractAudio -> "M4A"
            else -> "MP4"
        }
    }

private fun DownloadState.statusText(): String =
    when (this) {
        DownloadState.Idle -> "Sırada"
        is DownloadState.FetchingInfo -> "Bağlantı çözülüyor"
        DownloadState.ReadyWithInfo -> "Başlıyor"
        is DownloadState.Running ->
            if (progress < 0f) "İndiriliyor" else "%${(progress * 100).toInt()} indirildi"
        is DownloadState.Canceled -> "İptal edildi"
        is DownloadState.Error -> "İndirilemedi"
        is DownloadState.Completed -> "Tamamlandı"
    }
