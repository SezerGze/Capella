package com.junkfood.seal.ui.capella

import android.media.MediaMetadataRetriever
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junkfood.seal.App
import com.junkfood.seal.download.DownloaderV2
import com.junkfood.seal.download.Task
import com.junkfood.seal.util.CONVERT_MP3
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FORMAT_COMPATIBILITY
import com.junkfood.seal.util.M4A
import com.junkfood.seal.util.RES_1080P
import com.junkfood.seal.util.RES_720P
import com.junkfood.seal.util.VideoInfo
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Prototipteki dört biçim. Sıra ve etiketler tasarımdan. */
enum class CapellaFormat(val label: String, val sub: String) {
    Mp3("MP3", "Ses · uyumlu"),
    M4a("M4A", "Ses · AAC"),
    Mp4("MP4", "Video · 720p"),
    Mp4Hd("MP4 HD", "Video · 1080p"),
}

/**
 * Seçilen biçimi motorun beklediği tercihlere çevirir. Kullanıcının kayıtlı ayarlarından
 * başlayıp yalnızca biçimle ilgili alanları ezer.
 */
fun CapellaFormat.toPreferences(): DownloadUtil.DownloadPreferences =
    DownloadUtil.DownloadPreferences.createFromPreferences().run {
        val base = copy(formatIdString = "", formatSorting = false, downloadPlaylist = false)
        when (this@toPreferences) {
            CapellaFormat.Mp3 ->
                base.copy(extractAudio = true, convertAudio = true, audioConvertFormat = CONVERT_MP3)
            CapellaFormat.M4a ->
                base.copy(
                    extractAudio = true,
                    convertAudio = false,
                    useCustomAudioPreset = true,
                    audioFormat = M4A,
                )
            CapellaFormat.Mp4 ->
                base.copy(
                    extractAudio = false,
                    videoFormat = FORMAT_COMPATIBILITY,
                    videoResolution = RES_720P,
                )
            CapellaFormat.Mp4Hd ->
                base.copy(
                    extractAudio = false,
                    videoFormat = FORMAT_COMPATIBILITY,
                    videoResolution = RES_1080P,
                )
        }
    }

enum class CapellaTab {
    Home,
    History,
}

sealed interface ResolveState {
    data object Idle : ResolveState

    data object Loading : ResolveState

    data class Ready(val info: VideoInfo) : ResolveState

    data object Failed : ResolveState
}

/** Geçmiş sekmesindeki tek satır. Hem süren indirmeler hem biten dosyalar buraya düşer. */
data class HistoryRow(
    val key: String,
    val title: String,
    val kind: String,
    val meta: String,
    val right: String,
    val progress: Float?,
    val path: String?,
    val sizeBytes: Long,
)

private val URL_PATTERN = Regex("""^\s*(https?://|www\.)\S{4,}""", RegexOption.IGNORE_CASE)

class CapellaViewModel(private val downloader: DownloaderV2) : ViewModel() {

    var tab by mutableStateOf(CapellaTab.Home)
        private set

    var url by mutableStateOf("")
        private set

    var format by mutableStateOf(CapellaFormat.Mp4)
        private set

    var resolveState by mutableStateOf<ResolveState>(ResolveState.Idle)
        private set

    private var resolveJob: Job? = null

    /** Biten indirmeler; dosya bilgisi diskten okunarak zenginleştirilir. */
    val completed =
        DatabaseUtil.getDownloadHistoryFlow()
            .map { list -> withContext(Dispatchers.IO) { list.toRows() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectTab(value: CapellaTab) {
        tab = value
    }

    fun selectFormat(value: CapellaFormat) {
        format = value
    }

    fun onUrlChange(value: String) {
        url = value
        scheduleResolve(value)
    }

    fun pasteFromClipboard() {
        val text =
            runCatching { App.clipboard.primaryClip?.getItemAt(0)?.text?.toString() }.getOrNull()
        if (!text.isNullOrBlank()) onUrlChange(text.trim())
    }

    /** Bağlantı geçerli görünüyorsa kısa bir beklemeden sonra bilgi çeker. */
    private fun scheduleResolve(value: String) {
        resolveJob?.cancel()
        if (!URL_PATTERN.containsMatchIn(value)) {
            resolveState = ResolveState.Idle
            return
        }
        resolveState = ResolveState.Loading
        resolveJob =
            viewModelScope.launch {
                delay(400)
                val result =
                    withContext(Dispatchers.IO) {
                        DownloadUtil.fetchVideoInfoFromUrl(
                            url = value.trim(),
                            taskKey = "Capella_resolve",
                        )
                    }
                resolveState =
                    result.fold(
                        onSuccess = { ResolveState.Ready(it) },
                        onFailure = { ResolveState.Failed },
                    )
            }
    }

    fun startDownload() {
        val target = url.trim()
        if (target.isEmpty()) return
        downloader.enqueue(Task(url = target, preferences = format.toPreferences()))
        tab = CapellaTab.History
    }

    fun cancel(task: Task) {
        downloader.cancel(task)
    }

    /** Süren indirmeler. Compose'un gözlemlediği bir harita, doğrudan okunabilir. */
    fun taskStates(): SnapshotStateMap<Task, Task.State> = downloader.getTaskStateMap()
}

// ---------------------------------------------------------------------------
// Biçimlendirme yardımcıları
// ---------------------------------------------------------------------------

private val trLocale = Locale("tr", "TR")

fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "—"
    val mb = bytes / 1_048_576.0
    return if (mb < 1.0) {
        String.format(trLocale, "%.0f KB", bytes / 1024.0)
    } else if (mb < 10.0) {
        String.format(trLocale, "%.1f MB", mb)
    } else if (mb < 1024.0) {
        String.format(trLocale, "%.0f MB", mb)
    } else {
        String.format(trLocale, "%.1f GB", mb / 1024.0)
    }
}

fun formatDuration(seconds: Int): String {
    if (seconds <= 0) return ""
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format(trLocale, "%d:%02d:%02d", h, m, s)
    else String.format(trLocale, "%d:%02d", m, s)
}

private fun relativeDay(millis: Long): String {
    if (millis <= 0) return ""
    val days =
        TimeUnit.MILLISECONDS.toDays(
            startOfDay(System.currentTimeMillis()) - startOfDay(millis)
        )
    return when {
        days <= 0L -> "Bugün"
        days == 1L -> "Dün"
        days < 7L -> "$days gün"
        else -> java.text.SimpleDateFormat("d MMM", trLocale).format(java.util.Date(millis))
    }
}

private fun startOfDay(millis: Long): Long {
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = millis
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

/**
 * Veritabanı yalnızca başlık ve dosya yolu tutuyor; kalite ve boyut dosyanın kendisinden
 * okunuyor. Bu yüzden motor tarafına dokunmaya gerek kalmıyor.
 */
private fun List<com.junkfood.seal.database.objects.DownloadedVideoInfo>.toRows(): List<HistoryRow> =
    mapNotNull { info ->
            val file = File(info.videoPath)
            if (!file.exists()) return@mapNotNull null

            val kind = file.extension.uppercase(trLocale).ifEmpty { "DOSYA" }
            val quality = probeQuality(info.videoPath)
            val size = formatSize(file.length())

            HistoryRow(
                key = "db_${info.id}",
                title = info.videoTitle.ifBlank { file.nameWithoutExtension },
                kind = kind,
                meta = listOfNotNull(quality, size).joinToString(" · "),
                right = relativeDay(file.lastModified()),
                progress = null,
                path = info.videoPath,
                sizeBytes = file.length(),
            )
        }
        .sortedByDescending { it.path?.let { p -> File(p).lastModified() } ?: 0L }

/** Video için yükseklik ("1080p"), ses için bit hızı ("256 kbps"). */
private fun probeQuality(path: String): String? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(path)
        val height =
            retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull()
        if (height != null && height > 0) {
            "${height}p"
        } else {
            retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                ?.toIntOrNull()
                ?.let { "${it / 1000} kbps" }
        }
    } catch (_: Throwable) {
        null
    } finally {
        runCatching { retriever.release() }
    }
}
