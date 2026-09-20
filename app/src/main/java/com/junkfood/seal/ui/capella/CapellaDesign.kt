@file:OptIn(ExperimentalTextApi::class)

package com.junkfood.seal.ui.capella

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.seal.R

/**
 * Capella tasarım dizgesi. Değerler `Video Indirici.dc.html` prototipinden birebir alındı;
 * prototipteki 1 CSS px = 1 dp.
 */
object CapellaColors {
    val Accent = Color(0xFFE1447A)
    val AccentSoft = Color(0xFFFDEFF4)
    val AccentMuted = Color(0xFFEDA6C2)

    val Page = Color(0xFFF4F2F3)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceMuted = Color(0xFFFAF8F9)
    val NeutralBadge = Color(0xFFF3EFF1)

    val Border = Color(0xFFEFE3E8)
    val BorderNav = Color(0xFFF1E9EC)

    val TextPrimary = Color(0xFF221A1E)
    val TextSecondary = Color(0xFF8A7F84)
    val TextTertiary = Color(0xFF7C7176)

    val ProgressTrack = Color(0xFFF0E3E9)
    val DotInactive = Color(0xFFDDD2D7)

    /** Yer tutucu küçük resmin çizgili deseni */
    val PlaceholderLight = Color(0xFFF0E6EA)
    val PlaceholderDark = Color(0xFFE7DBE1)
}

private fun manrope(weight: Int) =
    Font(
        resId = R.font.manrope,
        weight = FontWeight(weight),
        variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
    )

/**
 * Manrope değişken font olarak paketlendi (tek dosya, 161 KB). Ağırlıklar `wght` ekseniyle
 * türetiliyor; bu yüzden minSdk 26.
 */
val Manrope =
    FontFamily(manrope(400), manrope(500), manrope(600), manrope(700), manrope(800))

object CapellaDimens {
    val ScreenPaddingHorizontal = 22.dp
    val ScreenPaddingTop = 12.dp

    /** Alt gezinme çubuğunun altında kalmaması için içerik alt boşluğu */
    val ContentPaddingBottom = 116.dp

    val CardRadius = 16.dp
    val CardPadding = 12.dp
    val CardGap = 10.dp
}

object CapellaType {
    val TitleSize = 29.sp
    val TitleLetterSpacing = (-0.9).sp

    val SubtitleSize = 14.sp
    val SectionLabelSize = 12.sp
    val SectionLabelSpacing = 1.4.sp

    val InputSize = 15.sp
    val RowTitleSize = 14.sp
    val RowMetaSize = 12.sp

    val FormatLabelSize = 16.sp
    val FormatSubSize = 12.sp

    val ButtonSize = 16.sp
    val TabLabelSize = 11.5.sp
    val TabLabelSpacing = 0.2.sp
}
