package com.telen.noteskeeper.di

import androidx.room.Room
import com.telen.noteskeeper.core.DefaultDispatcherProvider
import com.telen.noteskeeper.core.DispatcherProvider
import com.telen.noteskeeper.data.local.db.AppDatabase
import com.telen.noteskeeper.data.local.file.PhotoFileStorage
import com.telen.noteskeeper.data.remote.HttpClientFactory
import com.telen.noteskeeper.data.repository.BackupRepositoryImpl
import com.telen.noteskeeper.data.repository.NoteRepositoryImpl
import com.telen.noteskeeper.data.repository.PhotoRepositoryImpl
import com.telen.noteskeeper.data.repository.SubNoteRepositoryImpl
import com.telen.noteskeeper.domain.repository.BackupRepository
import com.telen.noteskeeper.domain.repository.NoteRepository
import com.telen.noteskeeper.domain.repository.PhotoRepository
import com.telen.noteskeeper.domain.repository.SubNoteRepository
import com.telen.noteskeeper.domain.usecase.CancelPhotoCaptureUseCase
import com.telen.noteskeeper.domain.usecase.CleanupDatabaseUseCase
import com.telen.noteskeeper.domain.usecase.ClearAllDataUseCase
import com.telen.noteskeeper.domain.usecase.ConfirmPhotoCaptureUseCase
import com.telen.noteskeeper.domain.usecase.CreateNoteUseCase
import com.telen.noteskeeper.domain.usecase.CreateSubNoteUseCase
import com.telen.noteskeeper.domain.usecase.DeletePhotoUseCase
import com.telen.noteskeeper.domain.usecase.ExportDataUseCase
import com.telen.noteskeeper.domain.usecase.ImportDataUseCase
import com.telen.noteskeeper.domain.usecase.ObserveNoteUseCase
import com.telen.noteskeeper.domain.usecase.ObserveNotesUseCase
import com.telen.noteskeeper.domain.usecase.ObserveSubNoteDetailUseCase
import com.telen.noteskeeper.domain.usecase.ObserveSubNotesUseCase
import com.telen.noteskeeper.domain.usecase.PreparePhotoCaptureUseCase
import com.telen.noteskeeper.domain.usecase.UpdateNoteStatusUseCase
import com.telen.noteskeeper.domain.usecase.UpdateNotesOrderUseCase
import com.telen.noteskeeper.domain.usecase.UpdateSubNoteStatusUseCase
import com.telen.noteskeeper.domain.usecase.UpdateSubNoteTextUseCase
import com.telen.noteskeeper.domain.usecase.UpdateSubNotesOrderUseCase
import com.telen.noteskeeper.presentation.notes.NotesViewModel
import com.telen.noteskeeper.presentation.options.OptionsViewModel
import com.telen.noteskeeper.presentation.subnotedetail.SubNoteDetailViewModel
import com.telen.noteskeeper.presentation.subnotes.SubNotesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val coreModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, AppDatabase.NAME)
            .build()
    }
    single { get<AppDatabase>().noteDao() }
    single { get<AppDatabase>().subNoteDao() }
    single { get<AppDatabase>().photoDao() }
    single { PhotoFileStorage(androidContext()) }
}

val networkModule = module {
    single { HttpClientFactory.create() }
}

val repositoryModule = module {
    single<NoteRepository> { NoteRepositoryImpl(get(), get()) }
    single<SubNoteRepository> { SubNoteRepositoryImpl(get(), get(), get()) }
    single<PhotoRepository> { PhotoRepositoryImpl(get(), get(), get()) }
    single<BackupRepository> { BackupRepositoryImpl(get(), get(), get(), get(), get()) }
}

val useCaseModule = module {
    factory { ObserveNotesUseCase(get()) }
    factory { ObserveNoteUseCase(get()) }
    factory { CreateNoteUseCase(get()) }
    factory { ObserveSubNotesUseCase(get()) }
    factory { CreateSubNoteUseCase(get()) }
    factory { ObserveSubNoteDetailUseCase(get()) }
    factory { UpdateSubNoteTextUseCase(get()) }
    factory { PreparePhotoCaptureUseCase(get()) }
    factory { ConfirmPhotoCaptureUseCase(get()) }
    factory { CancelPhotoCaptureUseCase(get()) }
    factory { DeletePhotoUseCase(get()) }
    factory { UpdateNoteStatusUseCase(get()) }
    factory { UpdateNotesOrderUseCase(get()) }
    factory { UpdateSubNoteStatusUseCase(get()) }
    factory { UpdateSubNotesOrderUseCase(get()) }
    factory { CleanupDatabaseUseCase(get(), get()) }
    factory { ExportDataUseCase(get(), get()) }
    factory { ImportDataUseCase(get(), androidContext()) }
    factory { ClearAllDataUseCase(get()) }
}

val viewModelModule = module {
    viewModel { NotesViewModel(get(), get(), get(), get()) }
    viewModel { params -> SubNotesViewModel(params.get(), get(), get(), get(), get(), get()) }
    viewModel { params ->
        SubNoteDetailViewModel(
            subNoteId = params.get(),
            observeSubNoteDetail = get(),
            updateSubNoteText = get(),
            preparePhotoCapture = get(),
            confirmPhotoCapture = get(),
            cancelPhotoCapture = get(),
            deletePhoto = get(),
        )
    }
    viewModel { OptionsViewModel(get(), get(), get()) }
}

val appModules = listOf(
    coreModule,
    databaseModule,
    networkModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
)
