package com.telen.noteskeeper.screen

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.telen.noteskeeper.domain.usecase.ClearAllDataUseCase
import com.telen.noteskeeper.robot.MainRule
import com.telen.noteskeeper.robot.notesRobot
import com.telen.noteskeeper.robot.subNotesRobot
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SubNotesScreenTest : KoinComponent {

    @get:Rule
    val rule: MainRule = createAndroidComposeRule()

    private val clearAllData: ClearAllDataUseCase by inject()

    @Before
    fun setUp() = runBlocking {
        clearAllData()
    }

    /** Helper: creates a note and opens its sub-notes screen. */
    private fun openSubNotesFor(noteTitle: String) {
        rule.notesRobot {
            openCreateNoteDialog()
            typeNoteTitle(noteTitle)
            confirmNoteCreation()
            clickNote(noteTitle)
        }
    }

    @Test
    fun emptyState_isDisplayed_whenNoSubNotes() {
        openSubNotesFor("My note")
        rule.subNotesRobot { assertEmptyStateVisible() }
    }

    @Test
    fun noteTitle_isShown_inToolbar() {
        openSubNotesFor("Catan session")
        rule.subNotesRobot { assertScreenTitleVisible("Catan session") }
    }

    @Test
    fun subNote_isCreated_andAppearsInList() {
        openSubNotesFor("D&D campaign")
        rule.subNotesRobot {
            openCreateSubNoteDialog()
            typeSubNoteName("Player 1")
            confirmSubNoteCreation()
            assertSubNoteVisible("Player 1")
        }
    }

    @Test
    fun subNoteCreationDialog_isDismissed_onCancel() {
        openSubNotesFor("My note")
        rule.subNotesRobot {
            openCreateSubNoteDialog()
            typeSubNoteName("Draft player")
            dismissSubNoteDialog()
            assertSubNoteNotVisible("Draft player")
        }
    }

    @Test
    fun multipleSubNotes_allAppearInList() {
        openSubNotesFor("Game night")
        rule.subNotesRobot {
            openCreateSubNoteDialog()
            typeSubNoteName("Alice")
            confirmSubNoteCreation()

            openCreateSubNoteDialog()
            typeSubNoteName("Bob")
            confirmSubNoteCreation()

            assertSubNoteVisible("Alice")
            assertSubNoteVisible("Bob")
        }
    }

    @Test
    fun tappingSubNote_navigatesToDetailScreen() {
        openSubNotesFor("Campaign")
        rule.subNotesRobot {
            openCreateSubNoteDialog()
            typeSubNoteName("Gandalf")
            confirmSubNoteCreation()
            clickSubNote("Gandalf")
        }
        rule.subNotesRobot { assertScreenTitleVisible("Gandalf") }
    }

    @Test
    fun backButton_navigatesBackToNotesList() {
        openSubNotesFor("Session")
        rule.subNotesRobot { clickBack() }
        rule.notesRobot { assertNoteVisible("Session") }
    }

    @Test
    fun deletingSubNote_showsSnackbarWithName() {
        openSubNotesFor("Campaign")
        rule.subNotesRobot {
            openCreateSubNoteDialog()
            typeSubNoteName("Aragorn")
            confirmSubNoteCreation()

            swipeToDeleteSubNote("Aragorn")

            assertSubNoteNotVisible("Aragorn")
            assertDeleteSnackbarVisible("Aragorn")
            assertUndoButtonVisible()
        }
    }

    @Test
    fun undoDeleteSubNote_restoresSubNoteInList() {
        openSubNotesFor("Campaign")
        rule.subNotesRobot {
            openCreateSubNoteDialog()
            typeSubNoteName("Legolas")
            confirmSubNoteCreation()

            swipeToDeleteSubNote("Legolas")
            assertSubNoteNotVisible("Legolas")

            clickUndo()
            assertSubNoteVisible("Legolas")
        }
    }
}
