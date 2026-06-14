package com.telen.noteskeeper.presentation.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Empty state illustration for the sub notes list:
 * stacked empty cards with a "+" hint.
 */
val EmptySubNotesVector: ImageVector by lazy {
    ImageVector.Builder(
        name = "EmptySubNotes",
        defaultWidth = 160.dp,
        defaultHeight = 160.dp,
        viewportWidth = 160f,
        viewportHeight = 160f,
    ).apply {
        // Back card
        path(fill = SolidColor(Color(0xFFBFD0F7))) {
            moveTo(44f, 34f)
            horizontalLineTo(124f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 132f, 42f)
            verticalLineTo(86f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 124f, 94f)
            horizontalLineTo(44f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 36f, 86f)
            verticalLineTo(42f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 44f, 34f)
            close()
        }
        // Front card
        path(fill = SolidColor(Color(0xFFD9E3FC))) {
            moveTo(36f, 62f)
            horizontalLineTo(116f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 124f, 70f)
            verticalLineTo(118f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 116f, 126f)
            horizontalLineTo(36f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28f, 118f)
            verticalLineTo(70f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 36f, 62f)
            close()
        }
        // Avatar circle on the front card
        path(fill = SolidColor(Color(0xFF1E5AE8))) {
            moveTo(52f, 82f)
            arcTo(10f, 10f, 0f, isMoreThanHalf = true, isPositiveArc = true, 52.01f, 82f)
            close()
        }
        // Name lines
        path(
            stroke = SolidColor(Color(0xFF8FA9F0)),
            strokeLineWidth = 6f,
            strokeLineCap = StrokeCap.Round,
        ) {
            moveTo(72f, 86f)
            horizontalLineTo(108f)
            moveTo(72f, 102f)
            horizontalLineTo(96f)
        }
        // Plus hint
        path(fill = SolidColor(Color(0xFFFFC107))) {
            moveTo(118f, 104f)
            arcTo(18f, 18f, 0f, isMoreThanHalf = true, isPositiveArc = true, 118.01f, 104f)
            close()
        }
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 5f,
            strokeLineCap = StrokeCap.Round,
        ) {
            moveTo(136f, 113f)
            lineTo(136f, 131f)
            moveTo(127f, 122f)
            lineTo(145f, 122f)
        }
    }.build()
}
