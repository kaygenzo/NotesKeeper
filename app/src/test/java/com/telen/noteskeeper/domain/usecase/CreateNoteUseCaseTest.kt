package com.telen.noteskeeper.domain.usecase

import com.telen.noteskeeper.domain.repository.NoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateNoteUseCaseTest {

    private val repository: NoteRepository = mockk()
    private val useCase = CreateNoteUseCase(repository)

    @Test
    fun `invoke trims title and delegates to repository`() = runTest {
        coEvery { repository.createNote("My session", 123L) } returns 42L

        val result = useCase("  My session  ", 123L)

        assertEquals(42L, result)
        coVerify(exactly = 1) { repository.createNote("My session", 123L) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke throws when title is blank`() = runTest {
        useCase("   ", 123L)
    }
}
