package com.telen.noteskeeper.data.work

import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.telen.noteskeeper.domain.usecase.CleanupDatabaseUseCase
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class DatabaseCleanupWorkerTest {

    private val cleanupDatabase: CleanupDatabaseUseCase = mockk()

    @Before
    fun setUp() {
        startKoin {
            modules(module { single { cleanupDatabase } })
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `doWork returns success when cleanup completes without error`() = runTest {
        coJustRun { cleanupDatabase() }
        val worker = buildWorker()

        val result = worker.doWork()

        assertEquals(Result.success(), result)
    }

    @Test
    fun `doWork returns retry when cleanup throws an exception`() = runTest {
        coEvery { cleanupDatabase() } throws RuntimeException("DB failure")
        val worker = buildWorker()

        val result = worker.doWork()

        assertEquals(Result.retry(), result)
    }

    // doWork() only uses the Koin-injected use case — no real Context or WorkerParameters needed.
    private fun buildWorker(): DatabaseCleanupWorker {
        val context = mockk<Context>(relaxed = true)
        val params = mockk<WorkerParameters>(relaxed = true)
        return DatabaseCleanupWorker(context, params)
    }
}
