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
 * Capella tasarım dizgesi. Palet uzay moru; Değerler `Video Indirici.dc.html` prototipinden birebir alındı;
 * prototipteki 1 CSS px = 1 dp.
 */
object CapellaColors {
    val Accent = Color(0xFF6D3FD4)
    val AccentSoft = Color(0xFFEFEAFD)
    val AccentMuted = Color(0xFFB7A6EC)

    val Page = Color(0xFFF4F2F8)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceMuted = Color(0xFFF9F8FD)
    val NeutralBadge = Color(0xFFF1EEF7)

    val Border = Color(0xFFE5DFF3)
    val BorderNav = Color(0xFFEBE7F5)

    val TextPrimary = Color(0xFF1D1733)
    val TextSecondary = Color(0xFF7C7690)
    val TextTertiary = Color(0xFF6F6983)

    val ProgressTrack = Color(0xFFE7E0F6)
    val DotInactive = Color(0xFFD2CBE4)

    /** Yer tutucu küçük resmin çizgili deseni */
    val PlaceholderLight = Color(0xFFE9E3F5)
    val PlaceholderDark = Color(0xFFDCD4EE)
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
    val ContentPaddingBottom = 100.dp

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
