package com.telen.noteskeeper.presentation.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.telen.noteskeeper.presentation.theme.NotesKeeperTheme
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Wraps [content] so the user can slide it left to reveal a delete button.
 * Dragging past half the button width snaps open; releasing before snaps closed.
 * The delete action fires only when the user explicitly taps the revealed bin icon.
 */
@Composable
fun SwipeToRevealDeleteBox(
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    onExpandedChanged: (Boolean) -> Unit = {},
    content: @Composable (isRevealed: Boolean) -> Unit,
) {
    val density = LocalDensity.current
    val deleteButtonWidth = 72.dp
    val deleteButtonWidthPx = with(density) { deleteButtonWidth.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val isRevealed = offsetX.value != 0f
    DisposableEffect(isRevealed) {
        onExpandedChanged(isRevealed)
        onDispose {
            if (isRevealed) onExpandedChanged(false)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
    ) {
        // Red background with delete icon, only visible when swiped.
        val alpha = if (offsetX.value == 0f) 0f else 1f
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(deleteButtonWidth)
                .fillMaxHeight()
                .graphicsLayer { this.alpha = alpha }
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.error)
                .clickable(enabled = alpha > 0f, onClick = onDeleteClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            offsetX.snapTo(
                                (offsetX.value + delta).coerceIn(-deleteButtonWidthPx, 0f),
                            )
                        }
                    },
                    onDragStopped = {
                        scope.launch {
                            offsetX.animateTo(
                                targetValue = if (offsetX.value < -deleteButtonWidthPx / 2f) {
                                    -deleteButtonWidthPx
                                } else {
                                    0f
                                },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            )
                        }
                    },
                ),
        ) {
            content(isRevealed)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SwipeToRevealDeleteBoxClosedPreview() {
    NotesKeeperTheme {
        SwipeToRevealDeleteBox(onDeleteClick = {}) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Swipe left to reveal delete",
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SwipeToRevealDeleteBoxOpenPreview() {
    NotesKeeperTheme {
        val offsetX = remember { Animatable(-72f * 3f) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(72.dp)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) },
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Swipe left to reveal delete",
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}
