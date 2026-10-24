package br.com.sosplus.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle

private val materialTypography = Typography()
private fun TextStyle.compact() = copy(fontSize = fontSize * 0.94f)

// Reduz a escala um pouco, preservando a fonte do sistema e a preferência de acessibilidade.
val Typography = materialTypography.copy(
    displayLarge = materialTypography.displayLarge.compact(),
    displayMedium = materialTypography.displayMedium.compact(),
    displaySmall = materialTypography.displaySmall.compact(),
    headlineLarge = materialTypography.headlineLarge.compact(),
    headlineMedium = materialTypography.headlineMedium.compact(),
    headlineSmall = materialTypography.headlineSmall.compact(),
    titleLarge = materialTypography.titleLarge.compact(),
    titleMedium = materialTypography.titleMedium.compact(),
    titleSmall = materialTypography.titleSmall.compact(),
    bodyLarge = materialTypography.bodyLarge.compact(),
    bodyMedium = materialTypography.bodyMedium.compact(),
    bodySmall = materialTypography.bodySmall.compact(),
    labelLarge = materialTypography.labelLarge.compact(),
    labelMedium = materialTypography.labelMedium.compact(),
    labelSmall = materialTypography.labelSmall.compact(),
)
