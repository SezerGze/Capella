package com.junkfood.seal.ui.capella

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.download.DownloaderV2
import com.junkfood.seal.download.Task
import com.junkfood.seal.download.Task.DownloadState
import com.junkfood.seal.ui.page.YtdlpUpdater
import com.junkfood.seal.util.CONVERT_MP3
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FORMAT_COMPATIBILITY
import com.junkfood.seal.util.M4A
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.RES_1080P
import com.junkfood.seal.util.RES_720P
import com.junkfood.seal.util.UpdateUtil
import com.junkfood.seal.util.YT_DLP_VERSION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

/**
 * Geçici iskelet arayüz. Tek amacı: indirme motorunun (yt-dlp / youtubedl-android) yeni bir Compose
 * arayüzünden uçtan uca çalıştığını doğrulamak. Tasarım bilinçli olarak yapılmadı; gerçek Capella
 * arayüzü bunun yerine geçecek.
 */
private enum class SkeletonFormat(val label: String) {
    Mp4("MP4 720p"),
    Mp4Hd("MP4 1080p"),
    M4a("M4A"),
    Mp3("MP3"),
}

/**
 * Seçilen formatı motorun beklediği [DownloadUtil.DownloadPreferences] haline getirir. Kullanıcının
 * kayıtlı ayarlarından başlayıp yalnızca format alanlarını ezer.
 */
private fun SkeletonFormat.toPreferences(): DownloadUtil.DownloadPreferences =
    DownloadUtil.DownloadPreferences.createFromPreferences().run {
        val base = copy(formatIdString = "", formatSorting = false, downloadPlaylist = false)
        when (this@toPreferences) {
            SkeletonFormat.Mp4 ->
                base.copy(
                    extractAudio = false,
                    videoFormat = FORMAT_COMPATIBILITY,
                    videoResolution = RES_720P,
                )
            SkeletonFormat.Mp4Hd ->
                base.copy(
                    extractAudio = false,
                    videoFormat = FORMAT_COMPATIBILITY,
                    videoResolution = RES_1080P,
                )
            SkeletonFormat.M4a ->
                base.copy(
                    extractAudio = true,
                    convertAudio = false,
                    useCustomAudioPreset = true,
                    audioFormat = M4A,
                )
            SkeletonFormat.Mp3 ->
                base.copy(
                    extractAudio = true,
                    convertAudio = true,
                    audioConvertFormat = CONVERT_MP3,
                )
        }
    }

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CapellaSkeletonScreen() {
    // Açılışta yt-dlp otomatik güncelleme kontrolü (motor özelliği, korunuyor).
    YtdlpUpdater()

    val downloader: DownloaderV2 = koinInject()
    val taskStateMap = downloader.getTaskStateMap()
    val scope = rememberCoroutineScope()

    var url by remember { mutableStateOf("") }
    var format by remember { mutableStateOf(SkeletonFormat.Mp4) }
    var ytdlpVersion by remember { mutableStateOf(YT_DLP_VERSION.getString()) }
    var updating by remember { mutableStateOf(false) }

    val notificationPermission =
        if (Build.VERSION.SDK_INT >= 33) {
            rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        } else null

    LaunchedEffect(Unit) {
        notificationPermission?.let { if (!it.status.isGranted) it.launchPermissionRequest() }
    }

    Scaffold { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Capella — iskelet", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Bağlantı") },
                placeholder = { Text("https://...") },
                singleLine = true,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonFormat.entries.forEach { entry ->
                    FilterChip(
                        selected = format == entry,
                        onClick = { format = entry },
                        label = { Text(entry.label) },
                    )
                }
            }

            Button(
                onClick = {
                    val trimmed = url.trim()
                    if (trimmed.isNotEmpty()) {
                        downloader.enqueue(Task(url = trimmed, preferences = format.toPreferences()))
                    }
                },
                enabled = url.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("İndir")
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "yt-dlp: " + ytdlpVersion.ifEmpty { "bilinmiyor" },
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedButton(
                    enabled = !updating,
                    onClick = {
                        scope.launch {
                            updating = true
                            withContext(Dispatchers.IO) { runCatching { UpdateUtil.updateYtDlp() } }
                            ytdlpVersion = YT_DLP_VERSION.getString()
                            updating = false
                        }
                    },
                ) {
                    Text(if (updating) "Güncelleniyor…" else "yt-dlp güncelle")
                }
            }

            HorizontalDivider()

            Text("Görevler", style = MaterialTheme.typography.titleMedium)

            if (taskStateMap.isEmpty()) {
                Text("Henüz görev yok.", style = MaterialTheme.typography.bodySmall)
            }

            taskStateMap.entries
                .sortedByDescending { it.key.timeCreated }
                .forEach { (task, state) ->
                    TaskRow(task = task, state = state, onCancel = { downloader.cancel(task) })
                }
        }
    }
}

@Composable
private fun TaskRow(task: Task, state: Task.State, onCancel: () -> Unit) {
    val downloadState = state.downloadState
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = state.viewState.title.ifEmpty { task.url },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(text = downloadState.describe(), style = MaterialTheme.typography.bodySmall)

            if (downloadState is DownloadState.Running && downloadState.progress >= 0f) {
                LinearProgressIndicator(
                    progress = { downloadState.progress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (downloadState is DownloadState.Cancelable) {
                OutlinedButton(onClick = onCancel) { Text("İptal") }
            }
        }
    }
}

private fun DownloadState.describe(): String =
    when (this) {
        DownloadState.Idle -> "Sırada"
        is DownloadState.FetchingInfo -> "Bağlantı çözülüyor…"
        DownloadState.ReadyWithInfo -> "Hazır, indirme başlıyor…"
        is DownloadState.Running ->
            if (progress < 0f) "İndiriliyor… " + progressText
            else "İndiriliyor %" + (progress * 100).toInt() + " " + progressText
        is DownloadState.Canceled -> "İptal edildi"
        is DownloadState.Error -> "Hata: " + (throwable.message ?: throwable.toString())
        is DownloadState.Completed -> "Bitti: " + (filePath ?: "dosya yolu yok")
    }
