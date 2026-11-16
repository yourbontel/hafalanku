package com.hafalan.quranmutun.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val EmeraldPrimary = Color(0xFF0F6B52)
val EmeraldLight = Color(0xFF3E9B7E)
val EmeraldContainer = Color(0xFFD6F2E7)
val GoldAccent = Color(0xFFC99738)
val GoldContainer = Color(0xFFF9EED5)
val CreamBackground = Color(0xFFFAF8F3)
val CardSurface = Color(0xFFFFFFFF)
val SoftSurfaceVariant = Color(0xFFEFF3F0)
val DarkText = Color(0xFF19231F)
val MutedText = Color(0xFF5A6862)

val StatusNotStarted = Color(0xFF7D8A84)
val StatusMemorizing = Color(0xFF1E70EB)
val StatusNeedsReview = Color(0xFFD97706)
val StatusMutqin = Color(0xFF0F8A5F)

private val LightColors = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = Color(0xFF063829),
    secondary = GoldAccent,
    onSecondary = Color.White,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = Color(0xFF473103),
    background = CreamBackground,
    onBackground = DarkText,
    surface = CardSurface,
    onSurface = DarkText,
    surfaceVariant = SoftSurfaceVariant,
    onSurfaceVariant = MutedText,
    outlineVariant = Color(0xFFD8E2DC)
)

private val DarkColors = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Color(0xFF02261C),
    primaryContainer = Color(0xFF0E4D3B),
    onPrimaryContainer = Color(0xFFD6F2E7),
    secondary = Color(0xFFE0B45C),
    onSecondary = Color(0xFF362400),
    secondaryContainer = Color(0xFF4C3508),
    onSecondaryContainer = Color(0xFFF9EED5),
    background = Color(0xFF0E1613),
    onBackground = Color(0xFFE8EFEA),
    surface = Color(0xFF16211D),
    onSurface = Color(0xFFE8EFEA),
    surfaceVariant = Color(0xFF21302A),
    onSurfaceVariant = Color(0xFFB3C2BA),
    outlineVariant = Color(0xFF2C3F37)
)

private val AppTypography = Typography(
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp),
    titleLarge = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.SemiBold, lineHeight = 25.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.Normal, lineHeight = 19.sp),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp)
)

@Composable
fun HafalanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
