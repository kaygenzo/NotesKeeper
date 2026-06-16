package com.telen.noteskeeper.robot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft

/** Encapsulates all interactions with the SubNotes list screen and its creation dialog. */
class SubNotesRobot(private val rule: MainRule) {

    // --- Assertions ---

    fun assertScreenTitleVisible(noteTitle: String) =
        rule.onNodeWithText(noteTitle).assertIsDisplayed()

    fun assertEmptyStateVisible() =
        rule.onNodeWithText("No subnotes, tap + to create one").assertIsDisplayed()

    fun assertSubNoteVisible(name: String) =
        rule.onNodeWithText(name).assertIsDisplayed()

    fun assertSubNoteNotVisible(name: String) =
        rule.onNodeWithText(name).assertDoesNotExist()

    fun assertDeleteSnackbarVisible(name: String) =
        rule.onNodeWithText("\"$name\" deleted").assertIsDisplayed()

    fun assertUndoButtonVisible() =
        rule.onNodeWithText("UNDO").assertIsDisplayed()

    // --- Actions ---

    fun openCreateSubNoteDialog() =
        rule.onNodeWithContentDescription("Create a subnote").performClick()

    fun typeSubNoteName(name: String) =
        rule.onNodeWithText("Name").performTextInput(name)

    fun confirmSubNoteCreation() =
        rule.onNodeWithText("Create").performClick()

    fun dismissSubNoteDialog() =
        rule.onNodeWithText("Cancel").performClick()

    fun clickSubNote(name: String) =
        rule.onNodeWithText(name).performClick()

    fun clickBack() =
        rule.onNodeWithContentDescription("Back").performClick()

    fun swipeToDeleteSubNote(name: String) {
        rule.onNodeWithText(name).performTouchInput { swipeLeft() }
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Delete").performClick()
    }

    fun clickUndo() =
        rule.onNodeWithText("UNDO").performClick()
}

fun MainRule.subNotesRobot(block: SubNotesRobot.() -> Unit) = SubNotesRobot(this).block()
