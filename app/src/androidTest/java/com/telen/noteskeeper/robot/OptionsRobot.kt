package com.telen.noteskeeper.robot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.telen.noteskeeper.BuildConfig

/** Encapsulates all interactions with the Options screen. */
class OptionsRobot(private val rule: MainRule) {

    // --- Assertions ---

    fun assertScreenTitleVisible() =
        rule.onNodeWithText("Options").assertIsDisplayed()

    fun assertVersionVisible() =
        rule.onNodeWithText(BuildConfig.VERSION_NAME).assertIsDisplayed()

    fun assertBackupSectionVisible() =
        rule.onNodeWithText("Backup & Restore").assertIsDisplayed()

    fun assertExportOptionVisible() =
        rule.onNodeWithText("Export data").assertIsDisplayed()

    fun assertImportOptionVisible() =
        rule.onNodeWithText("Import data").assertIsDisplayed()

    fun assertDeleteAllOptionVisible() =
        rule.onNodeWithText("Delete all data").assertIsDisplayed()

    fun assertConfirmDeleteDialogVisible() =
        rule.onNodeWithText("Delete all data?").assertIsDisplayed()

    // --- Actions ---

    fun clickBack() =
        rule.onNodeWithContentDescription("Back").performClick()

    fun clickDeleteAllData() =
        rule.onNodeWithText("Delete all data").performClick()

    fun confirmDeleteAll() =
        rule.onNodeWithText("DELETE EVERYTHING").performClick()

    fun dismissDialog() =
        rule.onNodeWithText("CANCEL").performClick()
}

fun MainRule.optionsRobot(block: OptionsRobot.() -> Unit) = OptionsRobot(this).block()
