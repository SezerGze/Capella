package com.junkfood.seal.ui.capella

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeTab(viewModel: CapellaViewModel) {
    var showMaintenance by remember { mutableStateOf(false) }
    val resolve = viewModel.resolveState
    val busy = resolve is ResolveState.Loading

    Column(
        modifier =
            Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = CapellaDimens.ScreenPaddingHorizontal,
                    end = CapellaDimens.ScreenPaddingHorizontal,
                    top = CapellaDimens.ScreenPaddingTop,
                    bottom = CapellaDimens.ContentPaddingBottom,
                )
    ) {
        Row(
            modifier =
                Modifier.padding(top = 8.dp, bottom = 2.dp)
                    .combinedClickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {},
                        onLongClick = { showMaintenance = true },
                    ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            CapellaMark()
            Text(
                text = "capella",
                fontFamily = Manrope,
                fontSize = CapellaType.TitleSize,
                fontWeight = FontWeight.W800,
                letterSpacing = CapellaType.TitleLetterSpacing,
                color = CapellaColors.TextPrimary,
            )
        }

        Text(
            text = "Bağlantıyı yapıştır, biçimi seç.",
            modifier = Modifier.padding(top = 6.dp, bottom = 26.dp),
            fontFamily = Manrope,
            fontSize = CapellaType.SubtitleSize,
            fontWeight = FontWeight.W500,
            color = CapellaColors.TextSecondary,
        )

        UrlField(
            url = viewModel.url,
            onUrlChange = viewModel::onUrlChange,
            onPaste = viewModel::pasteFromClipboard,
        )

        if (resolve !is ResolveState.Idle) {
            Box(modifier = Modifier.padding(top = 18.dp)) { PreviewCard(resolve) }
        }

        Text(
            text = "BİÇİM",
            modifier = Modifier.padding(top = 28.dp, bottom = 12.dp),
            fontFamily = Manrope,
            fontSize = CapellaType.SectionLabelSize,
            fontWeight = FontWeight.W700,
            letterSpacing = CapellaType.SectionLabelSpacing,
            color = CapellaColors.TextSecondary,
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CapellaFormat.entries.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEach { entry ->
                        FormatButton(
                            modifier = Modifier.weight(1f),
                            format = entry,
                            selected = viewModel.format == entry,
                            onClick = { viewModel.selectFormat(entry) },
                        )
                    }
                }
            }
        }

        DownloadButton(
            modifier = Modifier.padding(top = 30.dp),
            busy = busy,
            enabled = viewModel.url.isNotBlank(),
            onClick = viewModel::startDownload,
        )
    }

    if (showMaintenance) {
        MaintenanceDialog(onDismiss = { showMaintenance = false })
    }
}

/** Logo: iç içe üç daire. Prototipteki 24 birimlik SVG'nin birebir karşılığı. */
@Composable
private fun CapellaMark() {
    Canvas(modifier = Modifier.size(26.dp)) {
        val unit = size.minDimension / 24f
        val center = Offset(12f * unit, 12f * unit)
        val stroke = Stroke(width = 1.8f * unit)
        drawCircle(CapellaColors.Accent, radius = 9.2f * unit, center = center, style = stroke)
        drawCircle(CapellaColors.Accent, radius = 5.4f * unit, center = center, style = stroke)
        drawCircle(CapellaColors.Accent, radius = 1.9f * unit, center = center)
    }
}

@Composable
private fun UrlField(url: String, onUrlChange: (String) -> Unit, onPaste: () -> Unit) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CapellaColors.Surface)
                .border(1.5.dp, CapellaColors.Border, RoundedCornerShape(16.dp))
                .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BasicTextField(
            value = url,
            onValueChange = onUrlChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            cursorBrush = SolidColor(CapellaColors.Accent),
            textStyle =
                TextStyle(
                    fontFamily = Manrope,
                    fontSize = CapellaType.InputSize,
                    fontWeight = FontWeight.W500,
                    color = CapellaColors.TextPrimary,
                ),
            decorationBox = { inner ->
                Box(modifier = Modifier.padding(vertical = 12.dp)) {
                    if (url.isEmpty()) {
                        Text(
                            text = "https://...",
                            fontFamily = Manrope,
                            fontSize = CapellaType.InputSize,
                            fontWeight = FontWeight.W500,
                            color = CapellaColors.TextSecondary,
                        )
                    }
                    inner()
                }
            },
        )
        Box(
            modifier =
                Modifier.clip(RoundedCornerShape(11.dp))
                    .background(CapellaColors.AccentSoft)
                    .clickable(onClick = onPaste)
                    .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            Text(
                text = "Yapıştır",
                fontFamily = Manrope,
                fontSize = 13.sp,
                fontWeight = FontWeight.W700,
                color = CapellaColors.Accent,
            )
        }
    }
}

@Composable
private fun PreviewCard(state: ResolveState) {
    val info = (state as? ResolveState.Ready)?.info

    val title =
        when (state) {
            is ResolveState.Ready -> info?.title?.takeIf { it.isNotBlank() } ?: "Bağlantı çözümlendi"
            ResolveState.Loading -> "Bağlantı çözümleniyor…"
            ResolveState.Failed -> "Bağlantı çözümlenemedi"
            ResolveState.Idle -> ""
        }

    val meta =
        when (state) {
            is ResolveState.Ready -> info?.previewMeta().orEmpty()
            ResolveState.Loading -> "Bilgiler alınıyor"
            ResolveState.Failed -> "Yine de indirmeyi deneyebilirsin"
            ResolveState.Idle -> ""
        }

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CapellaColors.SurfaceMuted)
                .padding(CapellaDimens.CardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        val thumbModifier = Modifier.width(88.dp).height(58.dp).clip(RoundedCornerShape(10.dp))
        val thumbnail = info?.thumbnail
        if (thumbnail.isNullOrBlank()) {
            StripedPlaceholder(thumbModifier)
        } else {
            AsyncImage(
                model = thumbnail,
                contentDescription = null,
                modifier = thumbModifier,
                contentScale = ContentScale.Crop,
            )
        }

        Column {
            Text(
                text = title,
                fontFamily = Manrope,
                fontSize = CapellaType.RowTitleSize,
                fontWeight = FontWeight.W700,
                color = CapellaColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (meta.isNotEmpty()) {
                Text(
                    text = meta,
                    modifier = Modifier.padding(top = 5.dp),
                    fontFamily = Manrope,
                    fontSize = CapellaType.RowMetaSize,
                    fontWeight = FontWeight.W500,
                    color = CapellaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Prototipteki 135° çizgili yer tutucu. */
@Composable
private fun StripedPlaceholder(modifier: Modifier) {
    Canvas(modifier = modifier) {
        drawRect(CapellaColors.PlaceholderLight)
        val band = 6.dp.toPx()
        val step = band * 2f * 1.4142f
        var x = -size.height
        while (x < size.width + size.height) {
            drawLine(
                color = CapellaColors.PlaceholderDark,
                start = Offset(x, 0f),
                end = Offset(x + size.height, size.height),
                strokeWidth = band,
            )
            x += step
        }
    }
}

@Composable
private fun FormatButton(
    modifier: Modifier,
    format: CapellaFormat,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val content = if (selected) Color.White else CapellaColors.TextPrimary
    Column(
        modifier =
            modifier
                .clip(shape)
                .background(if (selected) CapellaColors.Accent else CapellaColors.Surface)
                .border(1.5.dp, if (selected) CapellaColors.Accent else CapellaColors.Border, shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = format.label,
            fontFamily = Manrope,
            fontSize = CapellaType.FormatLabelSize,
            fontWeight = FontWeight.W800,
            letterSpacing = (-0.2).sp,
            color = content,
        )
        Text(
            text = format.sub,
            fontFamily = Manrope,
            fontSize = CapellaType.FormatSubSize,
            fontWeight = FontWeight.W600,
            color = content.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun DownloadButton(
    modifier: Modifier,
    busy: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (busy || !enabled) CapellaColors.AccentMuted else CapellaColors.Accent)
                .clickable(enabled = enabled && !busy, onClick = onClick)
                .padding(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (busy) "Hazırlanıyor…" else "İndir",
            fontFamily = Manrope,
            fontSize = CapellaType.ButtonSize,
            fontWeight = FontWeight.W800,
            letterSpacing = (-0.2).sp,
            color = Color.White,
        )
    }
}

/** Önizleme kartının alt satırı: "4:32 · 1920×1080 · 48 MB" */
private fun com.junkfood.seal.util.VideoInfo.previewMeta(): String {
    val parts = mutableListOf<String>()
    duration?.roundToInt()?.let { formatDuration(it).takeIf(String::isNotEmpty)?.let(parts::add) }
    val w = width?.roundToInt() ?: 0
    val h = height?.roundToInt() ?: 0
    if (w > 0 && h > 0) parts.add("${w}×${h}")
    (fileSize ?: fileSizeApprox)?.takeIf { it > 0 }?.let { parts.add(formatSize(it.toLong())) }
    return parts.joinToString(" · ")
}
