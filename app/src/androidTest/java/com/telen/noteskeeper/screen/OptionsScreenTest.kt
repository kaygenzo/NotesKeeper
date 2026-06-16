package com.telen.noteskeeper.screen

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.telen.noteskeeper.domain.usecase.ClearAllDataUseCase
import com.telen.noteskeeper.robot.MainRule
import com.telen.noteskeeper.robot.notesRobot
import com.telen.noteskeeper.robot.optionsRobot
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OptionsScreenTest : KoinComponent {

    @get:Rule
    val rule: MainRule = createAndroidComposeRule()

    private val clearAllData: ClearAllDataUseCase by inject()

    @Before
    fun setUp() = runBlocking { clearAllData() }

    private fun openOptions() = rule.notesRobot { clickOptions() }

    @Test
    fun optionsScreen_showsTitle() {
        openOptions()
        rule.optionsRobot { assertScreenTitleVisible() }
    }

    @Test
    fun optionsScreen_showsVersionName() {
        openOptions()
        rule.optionsRobot { assertVersionVisible() }
    }

    @Test
    fun optionsScreen_showsBackupSection() {
        openOptions()
        rule.optionsRobot {
            assertBackupSectionVisible()
            assertExportOptionVisible()
            assertImportOptionVisible()
        }
    }

    @Test
    fun optionsScreen_showsDeleteAllOption() {
        openOptions()
        rule.optionsRobot { assertDeleteAllOptionVisible() }
    }

    @Test
    fun deleteAll_showsConfirmationDialog() {
        openOptions()
        rule.optionsRobot {
            clickDeleteAllData()
            assertConfirmDeleteDialogVisible()
        }
    }

    @Test
    fun deleteAll_dialogDismissed_onCancel() {
        openOptions()
        rule.optionsRobot {
            clickDeleteAllData()
            assertConfirmDeleteDialogVisible()
            dismissDialog()
            assertScreenTitleVisible() // Still on options screen
        }
    }

    @Test
    fun deleteAll_confirmed_widesAllData() {
        // Create a note first, then delete all from options
        rule.notesRobot {
            openCreateNoteDialog()
            typeNoteTitle("Note to wipe")
            confirmNoteCreation()
        }
        openOptions()
        rule.optionsRobot {
            clickDeleteAllData()
            confirmDeleteAll()
        }
        // Navigate back and verify list is empty
        rule.optionsRobot { assertScreenTitleVisible() }
        rule.optionsRobot { clickBack() }
        rule.notesRobot { assertNoteNotVisible("Note to wipe") }
    }

    @Test
    fun backButton_navigatesBackToNotesList() {
        openOptions()
        rule.optionsRobot { clickBack() }
        // Notes screen FAB is present — confirms we're back
        rule.notesRobot { assertEmptyStateVisible() }
    }
}
