package com.telen.noteskeeper.screen

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.telen.noteskeeper.domain.usecase.ClearAllDataUseCase
import com.telen.noteskeeper.robot.MainRule
import com.telen.noteskeeper.robot.notesRobot
import com.telen.noteskeeper.robot.subNoteDetailRobot
import com.telen.noteskeeper.robot.subNotesRobot
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SubNoteDetailScreenTest : KoinComponent {

    @get:Rule
    val rule: MainRule = createAndroidComposeRule()

    private val clearAllData: ClearAllDataUseCase by inject()

    @Before
    fun setUp() = runBlocking { clearAllData() }

    /** Helper: navigates all the way to a sub-note detail screen. */
    private fun openDetailFor(noteTitle: String, subNoteName: String) {
        rule.notesRobot {
            openCreateNoteDialog()
            typeNoteTitle(noteTitle)
            confirmNoteCreation()
            clickNote(noteTitle)
        }
        rule.subNotesRobot {
            openCreateSubNoteDialog()
            typeSubNoteName(subNoteName)
            confirmSubNoteCreation()
            clickSubNote(subNoteName)
        }
    }

    @Test
    fun subNoteName_isShown_inToolbar() {
        openDetailFor("Campaign", "Gandalf")
        rule.subNoteDetailRobot { assertScreenTitleVisible("Gandalf") }
    }

    @Test
    fun emptyTextPlaceholder_isShown_whenNoText() {
        openDetailFor("Campaign", "Legolas")
        rule.subNoteDetailRobot { assertEmptyTextPlaceholderVisible() }
    }

    @Test
    fun editButton_isVisible_inViewMode() {
        openDetailFor("Campaign", "Aragorn")
        rule.subNoteDetailRobot { assertEditButtonVisible() }
    }

    @Test
    fun tappingEdit_switchesToEditMode_andShowsCameraButton() {
        openDetailFor("Campaign", "Gimli")
        rule.subNoteDetailRobot {
            clickEdit()
            assertSaveButtonVisible()
            assertCameraButtonVisible()
        }
    }

    @Test
    fun typingTextAndSaving_showsTextInViewMode() {
        openDetailFor("Campaign", "Frodo")
        rule.subNoteDetailRobot {
            clickEdit()
            typeText("Carries the One Ring")
            clickSave()
            assertTextVisible("Carries the One Ring")
        }
    }

    @Test
    fun backButton_navigatesBackToSubNotesList() {
        openDetailFor("Campaign", "Sam")
        rule.subNoteDetailRobot { clickBack() }
        rule.subNotesRobot { assertSubNoteVisible("Sam") }
    }

    @Test
    fun editingText_andGoingBack_persistsText() {
        openDetailFor("Campaign", "Merry")
        rule.subNoteDetailRobot {
            clickEdit()
            typeText("Auto-saved note")
            clickBack()
        }
        // Navigate back in and verify text was saved automatically
        rule.subNotesRobot { clickSubNote("Merry") }
        rule.subNoteDetailRobot { assertTextVisible("Auto-saved note") }
    }
}
