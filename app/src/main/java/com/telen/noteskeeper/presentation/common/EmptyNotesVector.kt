package com.telen.noteskeeper.presentation.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Empty state illustration for the notes list:
 * an open notebook with a pencil and a d20 die as a board game wink.
 */
val EmptyNotesVector: ImageVector by lazy {
    ImageVector.Builder(
        name = "EmptyNotes",
        defaultWidth = 160.dp,
        defaultHeight = 160.dp,
        viewportWidth = 160f,
        viewportHeight = 160f,
    ).apply {
        // Notebook body
        path(fill = SolidColor(Color(0xFFD9E3FC))) {
            moveTo(34f, 28f)
            horizontalLineTo(110f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 118f, 36f)
            verticalLineTo(124f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 110f, 132f)
            horizontalLineTo(34f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 26f, 124f)
            verticalLineTo(36f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 34f, 28f)
            close()
        }
        // Notebook spine
        path(fill = SolidColor(Color(0xFF1E5AE8))) {
            moveTo(26f, 36f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 34f, 28f)
            horizontalLineTo(42f)
            verticalLineTo(132f)
            horizontalLineTo(34f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 26f, 124f)
            close()
        }
        // Text lines
        path(
            stroke = SolidColor(Color(0xFF8FA9F0)),
            strokeLineWidth = 6f,
            strokeLineCap = StrokeCap.Round,
        ) {
            moveTo(54f, 52f)
            horizontalLineTo(104f)
            moveTo(54f, 70f)
            horizontalLineTo(104f)
            moveTo(54f, 88f)
            horizontalLineTo(84f)
        }
        // Pencil
        path(fill = SolidColor(Color(0xFFFFC107))) {
            moveTo(96f, 110f)
            lineTo(126f, 80f)
            lineTo(138f, 92f)
            lineTo(108f, 122f)
            lineTo(92f, 126f)
            close()
        }
        path(fill = SolidColor(Color(0xFF6D4C41))) {
            moveTo(92f, 126f)
            lineTo(96f, 110f)
            lineTo(108f, 122f)
            close()
        }
        // d20 die (hexagon with inner triangle)
        path(fill = SolidColor(Color(0xFF1E5AE8))) {
            moveTo(132f, 30f)
            lineTo(148f, 39f)
            lineTo(148f, 57f)
            lineTo(132f, 66f)
            lineTo(116f, 57f)
            lineTo(116f, 39f)
            close()
        }
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2.5f,
            strokeLineCap = StrokeCap.Round,
        ) {
            moveTo(132f, 36f)
            lineTo(143f, 54f)
            lineTo(121f, 54f)
            close()
        }
    }.build()
}
