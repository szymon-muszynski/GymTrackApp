# Notatki robocze (Copilot) – GymTrackApp

Ten plik służy jako trwały „notatnik” podczas dopracowywania opisu pracy (architektura/Room/Firebase/offline/sync).

## TODO (aktywny wątek)
- [x] Zweryfikować opis rozdziału 6.3 vs realny kod: Room DB, encje, DAO, Converters, seed exercises.json, migracje.
- [x] Zweryfikować stwierdzenia o: listach w `Exercise`, polu `images`, budowaniu pełnego URL.
- [x] Zweryfikować: soft delete + propagacja na dzieci + znaczenie `onDelete = CASCADE`.
- [x] Zweryfikować: gdzie faktycznie wywoływane jest `loadExercisesFromAssets()` (trigger seeda).
- [x] Zweryfikować: czy w kodzie istnieje `@Transaction` w DAO.

## Ustalenia (zweryfikowane w kodzie)
### Room – konfiguracja bazy
- Plik: `data/ExerciseDatabase.kt`.
- Nazwa klasy: `ExerciseDatabase`.
- Singleton: `@Volatile INSTANCE` + `synchronized` w `getDatabase(context)`.
- `Room.databaseBuilder(...).fallbackToDestructiveMigration()`.
- `version = 15`, `exportSchema = false`.
- DAO: `exerciseDao`, `trainingDao`, `templateDao`, `plannedWorkoutDao`, `socialDao`.

### Encje włączone do Room
- `Exercise`, `TrainingSession`, `SessionExercise`, `SessionSetDetails`, `WorkoutTemplate`, `TemplateExercise`, `PlannedWorkoutEntity`.
- Social cache: `PostEntity`, `UserCacheEntity`, `FollowingEntity`.

### TypeConverters
- Plik: `data/converters/Converters.kt`.
- Jest tylko konwerter `List<String> <-> JSON String` (Gson).
- Pokrywa listy w `Exercise`: `primaryMuscles`, `secondaryMuscles`, `instructions`, `images`.

### Exercise seed (exercises.json) – CO i KIEDY
- Repo: `data/repository/ExerciseRepository.kt`.
- Metoda `loadExercisesFromAssets()`:
  - czyta `assets/exercises.json`, parsuje na `List<Exercise>`.
  - wykonuje `insertAll(exercises)` tylko jeśli `exerciseDao.getAllExercises().isEmpty()`.
- Trigger: `MainActivity.kt`.
  - `LaunchedEffect(Unit) { exerciseViewModel.loadAllExercises() }` jest uruchamiany **raz na start kompozycji** (po wejściu do `setContent { ... }`).
  - To dzieje się **niezależnie od logowania** (blok jest przed `if (currentUser == null) ... else ...`).
- W praktyce: seed jest wywoływany przy każdym uruchomieniu aplikacji, ale realny INSERT do DB nastąpi tylko, gdy tabela `exercises` jest pusta.

### Custom exercises – soft delete + sync
- Custom exercise ma `id = "custom_${UUID}"`, `isCustom = true`, `createdByUserId`.
- Usuwanie custom exercise to **soft delete** (`deletedAtMs`, `syncStatus = PENDING_DELETE`).
- Retencja/hard delete: `ExerciseDao.purgeDeletedCustomExercises(cutoffMs)` usuwa TYLKO `isCustom = 1` i tylko jeśli `syncStatus = SYNCED`.

### DAO – reaktywność i transakcje
- DAO zwracają `Flow` dla obserwacji; UI często mapuje to na `LiveData` przez `asLiveData()`.
- W kodzie **jest** `@Transaction` w `SocialDao.replaceFollowingForUser()`.
  - Funkcja czyści rekordy following dla danego usera i robi bulk upsert w ramach jednej transakcji.
- W pozostałych DAO (np. `TrainingDao`) nie ma `@Transaction`.
- Dodatkowo, w repo jest `RoomDatabase.withTransaction` (np. `TrainingRepository.copySessionToDate()`).

### Soft delete treningów – propagacja na dzieci
- `TrainingRepository.deleteSession()` robi kaskadowe soft delete przez 3 query w `TrainingDao`:
  - `softDeleteSetsForSession` -> `softDeleteExercisesForSession` -> `softDeleteSession`.

### onDelete = ForeignKey.CASCADE
- Jest ustawione m.in. w `SessionExercise` (po `TrainingSession` i `Exercise`) oraz `SessionSetDetails` (po `SessionExercise`).
- Działa tylko przy **twardym** usuwaniu rodzica (SQL DELETE), nie przy soft delete.

### images – tylko ścieżki w DB, pełny URL w UI
- `Exercise.images` jest trzymane w DB jako `List<String>`.
- W `ExerciseDetailsScreen.kt` pełny URL do obrazka budujesz dynamicznie:
  - `"https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/${exercise.images[page]}"`.
- To potwierdza, że w DB nie przechowujesz binariów ani pełnych URL-i.

## Wykryte nieścisłości do poprawy w tekście (Twoja wersja 6.3)
- Fragment o `@Transaction` był zbyt zero-jedynkowy: w projekcie masz `@Transaction` w `SocialDao`, ale nie masz go w `TrainingDao`. Najlepiej opisać oba mechanizmy: `@Transaction` (tam gdzie jest) + `withTransaction` w repo.
- Jeśli piszesz o transakcyjności soft delete w treningach, doprecyzuj, że to sekwencja operacji, a transakcje stosujesz tam, gdzie to krytyczne (np. kopiowanie).
