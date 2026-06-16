package com.telen.noteskeeper.robot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.telen.noteskeeper.MainActivity

typealias MainRule = AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>

/** Encapsulates all interactions with the Notes list screen and its creation dialog. */
class NotesRobot(private val rule: MainRule) {

    // --- Assertions ---

    fun assertEmptyStateVisible() =
        rule.onNodeWithText("No notes yet, tap + to create one").assertIsDisplayed()

    fun assertNoteVisible(title: String) =
        rule.onNodeWithText(title).assertIsDisplayed()

    fun assertNoteNotVisible(title: String) =
        rule.onNodeWithText(title).assertDoesNotExist()

    fun assertDeleteSnackbarVisible(title: String) =
        rule.onNodeWithText("\"$title\" deleted").assertIsDisplayed()

    fun assertUndoButtonVisible() =
        rule.onNodeWithText("UNDO").assertIsDisplayed()

    // --- Actions ---

    fun openCreateNoteDialog() =
        rule.onNodeWithContentDescription("Create a note").performClick()

    fun typeNoteTitle(title: String) =
        rule.onNodeWithText("Title").performTextInput(title)

    fun confirmNoteCreation() =
        rule.onNodeWithText("Create").performClick()

    fun dismissNoteDialog() =
        rule.onNodeWithText("Cancel").performClick()

    fun clickNote(title: String) =
        rule.onNodeWithText(title).performClick()

    fun clickOptions() =
        rule.onNodeWithContentDescription("Options").performClick()

    fun swipeToDeleteNote(title: String) {
        rule.onNodeWithText(title).performTouchInput { swipeLeft() }
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Delete").performClick()
    }

    fun clickUndo() =
        rule.onNodeWithText("UNDO").performClick()
}

fun MainRule.notesRobot(block: NotesRobot.() -> Unit) = NotesRobot(this).block()
