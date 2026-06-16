package com.telen.noteskeeper.screen

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
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

class NotesScreenTest : KoinComponent {

    @get:Rule
    val rule: MainRule = createAndroidComposeRule()

    private val clearAllData: ClearAllDataUseCase by inject()

    @Before
    fun clearDatabase() = runBlocking { clearAllData() }

    @Test
    fun emptyState_isDisplayed_whenNoNotes() {
        rule.notesRobot { assertEmptyStateVisible() }
    }

    @Test
    fun note_isCreated_andAppearsInList() {
        rule.notesRobot {
            openCreateNoteDialog()
            typeNoteTitle("Board game session")
            confirmNoteCreation()
            assertNoteVisible("Board game session")
        }
    }

    @Test
    fun creationDialog_isDismissed_onCancel() {
        rule.notesRobot {
            openCreateNoteDialog()
            typeNoteTitle("Draft")
            dismissNoteDialog()
            assertNoteNotVisible("Draft")
        }
    }

    @Test
    fun multipleNotes_allAppearInList() {
        rule.notesRobot {
            openCreateNoteDialog()
            typeNoteTitle("Session 1")
            confirmNoteCreation()

            openCreateNoteDialog()
            typeNoteTitle("Session 2")
            confirmNoteCreation()

            assertNoteVisible("Session 1")
            assertNoteVisible("Session 2")
        }
    }

    @Test
    fun tappingNote_navigatesToSubNotesScreen() {
        rule.notesRobot {
            openCreateNoteDialog()
            typeNoteTitle("Catan night")
            confirmNoteCreation()
            clickNote("Catan night")
        }
        // Sub-notes screen is identified by its own title matching the note name
        rule.notesRobot { assertNoteVisible("Catan night") }
    }

    @Test
    fun tappingOptions_navigatesToOptionsScreen() {
        rule.notesRobot { clickOptions() }
        rule.optionsRobot { assertScreenTitleVisible() }
    }

    @Test
    fun deletingNote_showsSnackbarWithTitle() {
        rule.notesRobot {
            openCreateNoteDialog()
            typeNoteTitle("D&D campaign")
            confirmNoteCreation()

            swipeToDeleteNote("D&D campaign")

            assertNoteNotVisible("D&D campaign")
            assertDeleteSnackbarVisible("D&D campaign")
            assertUndoButtonVisible()
        }
    }

    @Test
    fun undoDelete_restoresNoteInList() {
        rule.notesRobot {
            openCreateNoteDialog()
            typeNoteTitle("Catan session")
            confirmNoteCreation()

            swipeToDeleteNote("Catan session")
            assertNoteNotVisible("Catan session")

            clickUndo()
            assertNoteVisible("Catan session")
        }
    }
}
