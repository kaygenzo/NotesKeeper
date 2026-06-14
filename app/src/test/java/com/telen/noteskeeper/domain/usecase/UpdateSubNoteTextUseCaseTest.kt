package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.repository.SubNoteRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UpdateSubNoteTextUseCaseTest {

    private val repository: SubNoteRepository = mockk()
    private val useCase = UpdateSubNoteTextUseCase(repository)

    @Test
    fun `invoke delegates to repository`() = runTest {
        coJustRun { repository.updateSubNoteText(1L, "New text") }

        useCase(1L, "New text")

        coVerify(exactly = 1) { repository.updateSubNoteText(1L, "New text") }
    }
}
