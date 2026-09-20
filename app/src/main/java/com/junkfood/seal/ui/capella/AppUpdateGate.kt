package com.junkfood.seal.ui.capella

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.UpdateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface UpdateState {
    data object Idle : UpdateState

    data class Available(val release: UpdateUtil.Release) : UpdateState

    data class Downloading(val percent: Int) : UpdateState

    data object Failed : UpdateState
}

/**
 * Açılışta Capella'nın kendi deposunda yeni sürüm var mı diye bakar, varsa tek dokunuşluk bir
 * pencere gösterir.
 *
 * Android, mağaza dışından kurulan uygulamalarda sessiz güncellemeye izin vermiyor: APK'yı
 * indirmek otomatik, kurulum onayını sistem mutlaka soruyor. Buradaki akış da o yüzden
 * "indirmeyi biz yapalım, kullanıcı tek kez onaylasın" şeklinde.
 */
@Composable
fun AppUpdateGate() {
    var state by remember { mutableStateOf<UpdateState>(UpdateState.Idle) }
    var dismissed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!PreferenceUtil.isAutoUpdateEnabled()) return@LaunchedEffect
        if (!PreferenceUtil.isNetworkAvailableForDownload()) return@LaunchedEffect

        val release =
            withContext(Dispatchers.IO) { runCatching { UpdateUtil.checkForUpdate() }.getOrNull() }
        if (release != null) state = UpdateState.Available(release)
    }

    val current = state
    if (dismissed || current is UpdateState.Idle) return

    Dialog(onDismissRequest = { if (current !is UpdateState.Downloading) dismissed = true }) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CapellaColors.Surface)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Yeni sürüm hazır",
                fontFamily = Manrope,
                fontSize = 18.sp,
                fontWeight = FontWeight.W800,
                color = CapellaColors.TextPrimary,
            )

            when (current) {
                is UpdateState.Available ->
                    Text(
                        text = "Capella " + (current.release.name ?: "") + " yüklenebilir.",
                        fontFamily = Manrope,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W500,
                        color = CapellaColors.TextSecondary,
                    )

                is UpdateState.Downloading -> {
                    Text(
                        text = "İndiriliyor · %" + current.percent,
                        fontFamily = Manrope,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W500,
                        color = CapellaColors.TextSecondary,
                    )
                    Box(
                        modifier =
                            Modifier.padding(top = 4.dp)
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(CapellaColors.ProgressTrack)
                    ) {
                        Box(
                            modifier =
                                Modifier.fillMaxWidth(current.percent / 100f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(CapellaColors.Accent)
                        )
                    }
                }

                UpdateState.Failed ->
                    Text(
                        text = "Güncelleme indirilemedi. Sonra tekrar denenecek.",
                        fontFamily = Manrope,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W500,
                        color = CapellaColors.TextSecondary,
                    )

                UpdateState.Idle -> Unit
            }

            if (current !is UpdateState.Downloading) {
                Box(
                    modifier =
                        Modifier.padding(top = 10.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CapellaColors.Accent)
                            .clickable {
                                val release = (current as? UpdateState.Available)?.release
                                if (release == null) {
                                    dismissed = true
                                    return@clickable
                                }
                                scope.launch {
                                    state = UpdateState.Downloading(0)
                                    runCatching {
                                            UpdateUtil.downloadApk(release = release).collect {
                                                when (it) {
                                                    is UpdateUtil.DownloadStatus.Progress ->
                                                        state = UpdateState.Downloading(it.percent)

                                                    is UpdateUtil.DownloadStatus.Finished -> {
                                                        UpdateUtil.installLatestApk()
                                                        dismissed = true
                                                    }

                                                    UpdateUtil.DownloadStatus.NotYet -> Unit
                                                }
                                            }
                                        }
                                        .onFailure { state = UpdateState.Failed }
                                }
                            }
                            .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (current is UpdateState.Failed) "Kapat" else "Güncelle",
                        fontFamily = Manrope,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W800,
                        color = Color.White,
                    )
                }

                if (current is UpdateState.Available) {
                    Box(
                        modifier =
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { dismissed = true }
                                .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Sonra",
                            fontFamily = Manrope,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W700,
                            color = CapellaColors.TextSecondary,
                        )
                    }
                }
            }
        }
    }
}
