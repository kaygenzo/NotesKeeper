package com.telen.noteskeeper.presentation.common

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex

@Composable
fun rememberDragDropState(
    lazyListState: LazyListState,
    onMove: (Int, Int) -> Unit
): DragDropState {
    return remember(lazyListState) {
        DragDropState(
            state = lazyListState,
            onMove = onMove
        )
    }
}

class DragDropState(
    val state: LazyListState,
    private val onMove: (Int, Int) -> Unit
) {
    var draggedDistance by mutableFloatStateOf(0f)
    var draggingItemIndex by mutableIntStateOf(-1)

    private var initialDraggedItem: LazyListItemInfo? = null

    fun onDragStart(index: Int) {
        state.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == index }
            ?.also {
                draggingItemIndex = it.index
                initialDraggedItem = it
            }
    }

    fun onDrag(dragAmount: Float) {
        draggedDistance += dragAmount

        val currentDraggedItem = initialDraggedItem ?: return
        val startOffset = currentDraggedItem.offset + draggedDistance
        val endOffset = startOffset + currentDraggedItem.size

        val targetItem = state.layoutInfo.visibleItemsInfo.find { item ->
            val itemStart = item.offset
            val itemEnd = item.offset + item.size
            (startOffset.toInt() in itemStart..itemEnd || endOffset.toInt() in itemStart..itemEnd) &&
                    draggingItemIndex != item.index
        }

        if (targetItem != null) {
            onMove(draggingItemIndex, targetItem.index)
            draggingItemIndex = targetItem.index
            draggedDistance += initialDraggedItem!!.offset - targetItem.offset
            initialDraggedItem = targetItem
        }
    }

    fun onDragInterrupted() {
        draggingItemIndex = -1
        draggedDistance = 0f
        initialDraggedItem = null
    }
}

fun Modifier.dragHandle(
    index: Int,
    state: DragDropState
): Modifier = this.pointerInput(Unit) {
    detectDragGestures(
        onDragStart = { state.onDragStart(index) },
        onDrag = { change, dragAmount ->
            change.consume()
            state.onDrag(dragAmount.y)
        },
        onDragEnd = { state.onDragInterrupted() },
        onDragCancel = { state.onDragInterrupted() }
    )
}

fun Modifier.draggedItem(
    offset: Float,
): Modifier = this
    .zIndex(if (offset != 0f) 1f else 0f)
    .graphicsLayer {
        translationY = offset
    }
