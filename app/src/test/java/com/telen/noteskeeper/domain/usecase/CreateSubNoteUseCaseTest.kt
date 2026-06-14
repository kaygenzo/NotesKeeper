package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.repository.SubNoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateSubNoteUseCaseTest {

    private val repository: SubNoteRepository = mockk()
    private val useCase = CreateSubNoteUseCase(repository)

    @Test
    fun `invoke trims name and delegates to repository`() = runTest {
        coEvery { repository.createSubNote(1L, "Player 1") } returns 7L

        val result = useCase(1L, " Player 1 ")

        assertEquals(7L, result)
        coVerify(exactly = 1) { repository.createSubNote(1L, "Player 1") }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke throws when name is blank`() = runTest {
        useCase(1L, "")
    }
}
