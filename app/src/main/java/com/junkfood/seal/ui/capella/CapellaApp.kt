package com.junkfood.seal.ui.capella

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.ui.page.YtdlpUpdater

/**
 * Capella'nın kökü: üstte seçili sekmenin içeriği, altta iki sekmeli gezinme çubuğu.
 * Yerleşim `Video Indirici.dc.html` prototipinden.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CapellaApp(viewModel: CapellaViewModel) {
    // Açılışta yt-dlp otomatik güncelleme kontrolü. Görünmez çalışır, motor özelliğidir.
    YtdlpUpdater()

    // Capella'nın kendi sürüm kontrolü. Yeni sürüm varsa tek dokunuşluk pencere gösterir.
    AppUpdateGate()

    // Platform gereği izinler: indirme bildirimi (Android 13+) ve dosya yazma (Android 9 ve altı).
    val required =
        when {
            Build.VERSION.SDK_INT >= 33 -> Manifest.permission.POST_NOTIFICATIONS
            Build.VERSION.SDK_INT <= 28 -> Manifest.permission.WRITE_EXTERNAL_STORAGE
            else -> null
        }
    if (required != null) {
        val permission = rememberPermissionState(required)
        LaunchedEffect(Unit) {
            if (!permission.status.isGranted) permission.launchPermissionRequest()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(CapellaColors.Surface)) {
        Box(modifier = Modifier.weight(1f).statusBarsPadding()) {
            when (viewModel.tab) {
                CapellaTab.Home -> HomeTab(viewModel)
                CapellaTab.History -> HistoryTab(viewModel)
            }
        }
        CapellaBottomNav(
            selected = viewModel.tab,
            onSelect = viewModel::selectTab,
        )
    }
}

@Composable
private fun CapellaBottomNav(selected: CapellaTab, onSelect: (CapellaTab) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(CapellaColors.Surface)) {
        HorizontalDivider(thickness = 1.dp, color = CapellaColors.BorderNav)
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 26.dp, end = 26.dp, top = 10.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NavTab(
                modifier = Modifier.weight(1f),
                label = "İndir",
                icon = Icons.Outlined.FileDownload,
                active = selected == CapellaTab.Home,
                onClick = { onSelect(CapellaTab.Home) },
            )
            NavTab(
                modifier = Modifier.weight(1f),
                label = "Geçmiş",
                icon = Icons.Outlined.History,
                active = selected == CapellaTab.History,
                onClick = { onSelect(CapellaTab.History) },
            )
        }
    }
}

@Composable
private fun NavTab(
    modifier: Modifier,
    label: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (active) CapellaColors.Accent else CapellaColors.TextTertiary
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                )
                .padding(vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = tint,
        )
        Text(
            text = label,
            fontFamily = Manrope,
            fontSize = CapellaType.TabLabelSize,
            fontWeight = FontWeight.W700,
            letterSpacing = CapellaType.TabLabelSpacing,
            color = tint,
        )
    }
}
