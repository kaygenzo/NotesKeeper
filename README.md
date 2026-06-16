# NotesKeeper

An Android app for taking text and photo notes. Typical use case: saving the state of a board game session (D&D, Catan…) — one note per session, one sub-note per player, with free text and photos of the table — so you can pick up exactly where you left off. The `Note` → `SubNote` model is intentionally generic and can serve other contexts.

## Tech stack

- Kotlin 2.2.20, Java 21, AGP 8.13.0, Gradle 8.14.3
- minSdk 23, targetSdk/compileSdk 36
- Jetpack Compose (Material 3, no XML layouts)
- Clean Architecture + MVVM (`data` / `domain` / `presentation`)
- Koin (DI), Room (local database), Coroutines + Flow
- Navigation Compose with type-safe routes (kotlinx.serialization)
- Ktor (HTTP client wired in, ready for future network features)
- Firebase Crashlytics + Timber (Crashlytics tree in release, DebugTree in debug)
- Coil 3 (thumbnail loading), core-splashscreen
- Unit tests: JUnit 4 + MockK + kotlinx-coroutines-test + Robolectric (DAO tests)

## Architecture

```
com.telen.noteskeeper
├── core/                  DispatcherProvider (injectable, testable)
├── domain/
│   ├── model/             Note, SubNote, SubNoteDetail, Photo, PendingPhoto
│   ├── repository/        Interfaces (NoteRepository, SubNoteRepository, PhotoRepository)
│   └── usecase/           One class per use case, all tested
├── data/
│   ├── local/db/          Room: entities, DAOs (Flow), projections with COUNT
│   ├── local/file/        PhotoFileStorage (filesDir/photos + FileProvider)
│   ├── mapper/            Entity → domain mappers
│   ├── repository/        Implementations (Dispatchers.IO, injectable clock)
│   └── remote/            Ktor HttpClientFactory (ready for future use)
├── di/                    Koin modules
└── presentation/
    ├── navigation/        @Serializable routes + AppNavHost
    ├── theme/             Material 3
    ├── common/            EmptyState + ImageVector illustrations (empty states),
    │                      SwipeToRevealDeleteBox
    ├── notes/             Notes list + creation dialog (title + date picker)
    ├── subnotes/          Sub-notes list + creation dialog (name)
    ├── subnotedetail/     Read / edit: fixed-height scrollable text area,
    │                      camera button (edit mode), thumbnail grid,
    │                      delete via red cross, open externally on tap
    └── options/           Options screen (placeholder)
```

## Tests

```
./gradlew testDebugUnitTest
```

Coverage: all domain use cases, all repository implementations (DAOs and file storage mocked with MockK, test dispatcher, injectable fixed clock), all DAOs via in-memory Room database (Robolectric), and the background worker.

## CI/CD

| Workflow | Trigger condition | Jobs (in order) |
|---|---|---|
| `nightly` | Scheduled, every night at 02:00 CET, `develop` branch only | unit-tests → build-android-tests → build-debug |
| `pipeline-branch` | Push to any branch | unit-tests → build-android-tests → build-prod |
| `pipeline-tag` | Tag matching `v*` | build-prod → deploy-alpha |
