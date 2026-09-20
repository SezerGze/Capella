package com.junkfood.seal.ui.capella

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.UpdateUtil
import com.junkfood.seal.util.YT_DLP_VERSION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Gizli bakım penceresi — ana sayfadaki logoya uzun basınca açılır.
 *
 * yt-dlp zaten kendini günlük olarak güncelliyor; buradaki düğme yalnızca bir şey bozulduğunda
 * planlı kontrolü beklemeden zorlamak için var. Kullanıcının kazara bulmaması adına arayüzde
 * görünür bir girişi yok.
 */
@Composable
fun MaintenanceDialog(onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var version by remember { mutableStateOf(YT_DLP_VERSION.getString()) }
    var updating by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CapellaColors.Surface)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Bakım",
                fontFamily = Manrope,
                fontSize = 18.sp,
                fontWeight = FontWeight.W800,
                color = CapellaColors.TextPrimary,
            )
            Text(
                text = "yt-dlp: " + version.ifEmpty { "bilinmiyor" },
                fontFamily = Manrope,
                fontSize = 13.sp,
                fontWeight = FontWeight.W500,
                color = CapellaColors.TextSecondary,
            )
            result?.let {
                Text(
                    text = it,
                    fontFamily = Manrope,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W600,
                    color = CapellaColors.Accent,
                )
            }

            Box(
                modifier =
                    Modifier.padding(top = 10.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (updating) CapellaColors.AccentMuted else CapellaColors.Accent
                        )
                        .clickable(enabled = !updating) {
                            scope.launch {
                                updating = true
                                result = null
                                val status =
                                    withContext(Dispatchers.IO) {
                                        runCatching { UpdateUtil.updateYtDlp() }
                                    }
                                version = YT_DLP_VERSION.getString()
                                result =
                                    if (status.isSuccess) "Güncelleme kontrolü bitti"
                                    else "Güncellenemedi, bağlantıyı kontrol et"
                                updating = false
                            }
                        }
                        .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (updating) "Güncelleniyor…" else "yt-dlp'yi şimdi güncelle",
                    fontFamily = Manrope,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W800,
                    color = Color.White,
                )
            }
        }
    }
}
