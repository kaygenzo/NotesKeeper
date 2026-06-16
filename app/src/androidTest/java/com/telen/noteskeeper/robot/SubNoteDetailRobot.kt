package com.telen.noteskeeper.robot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput

/** Encapsulates all interactions with the SubNote detail screen. */
class SubNoteDetailRobot(private val rule: MainRule) {

    // --- Assertions ---

    fun assertScreenTitleVisible(subNoteName: String) =
        rule.onNodeWithText(subNoteName).assertIsDisplayed()

    fun assertEmptyTextPlaceholderVisible() =
        rule.onNodeWithText("No text yet, tap the pencil to edit").assertIsDisplayed()

    fun assertTextVisible(text: String) =
        rule.onNodeWithText(text).assertIsDisplayed()

    fun assertEditButtonVisible() =
        rule.onNodeWithContentDescription("Edit").assertIsDisplayed()

    fun assertSaveButtonVisible() =
        rule.onNodeWithContentDescription("Save").assertIsDisplayed()

    fun assertCameraButtonVisible() =
        rule.onNodeWithContentDescription("Take a photo").assertIsDisplayed()

    // --- Actions ---

    fun clickEdit() =
        rule.onNodeWithContentDescription("Edit").performClick()

    fun clickSave() =
        rule.onNodeWithContentDescription("Save").performClick()

    fun clearText() =
        rule.onNodeWithText("Write your notes here...").performTextClearance()

    fun typeText(text: String) =
        rule.onNodeWithText("Write your notes here...").performTextInput(text)

    fun clickBack() =
        rule.onNodeWithContentDescription("Back").performClick()
}

fun MainRule.subNoteDetailRobot(block: SubNoteDetailRobot.() -> Unit) =
    SubNoteDetailRobot(this).block()
