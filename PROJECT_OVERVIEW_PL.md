# GymTrackApp — pełny opis projektu (handover)

> Ten dokument jest przeznaczony do przekazania osobie trzeciej (developerowi/recenzentowi/LLM). Zawiera **kompletny opis architektury, technologii, nawigacji, modelu danych, offline-first, synchronizacji, stanów oraz zachowań po utracie/powrocie internetu**.
>
> Stan na: **2026-01-14** (na podstawie kodu w repo).

---

## 1) Streszczenie aplikacji

**GymTrackApp** to aplikacja do śledzenia treningu siłowego:
- baza ćwiczeń (seed z assets) + możliwość tworzenia własnych ćwiczeń (custom)
- logowanie/rejestracja w Firebase Auth
- lokalna baza danych Room jako główne źródło prawdy (SSOT) dla UI
- treningi w kalendarzu (sesje → ćwiczenia → serie), statystyki/wykresy
- szablony treningów (templates)
- planer (planned workouts) z alarmami/powiadomieniami i obsługą restartu urządzenia
- moduł społecznościowy (Social): obserwowanie osób (following) + feed postów (cache w Room)

Kluczowa cecha projektu: **offline-first**. UI działa na danych w Room. Zmiany są zapisywane lokalnie i oznaczane jako „pending”, a następnie synchornizowane z Firestore w tle, gdy wróci internet.

---

## 2) Platforma, języki i wymagania

### Android / SDK
- `compileSdk = 35`
- `targetSdk = 35`
- `minSdk = 26`

### Języki
- **Kotlin** (aplikacja)
- **Gradle Kotlin DSL** (`*.gradle.kts`)
- **JSON** (assets `exercises.json`)
- XML (manifest i zasoby)

### Kompilacja
- Java/Kotlin target: **JVM 11**

---

## 3) Technologie i biblioteki

### UI
- Jetpack **Compose** + Material 3
- Navigation Compose (uwaga: w Gradle są dwie wersje `2.7.7` i `2.7.6` – dług techniczny)

### Architektura
- MVVM w praktyce: `Compose UI → ViewModel → Repository → DAO → Room`
- Brak Hilt/Dagger. Jest prosty kontener DI oparty o CompositionLocal: `GymTrackAppContainer` + `LocalAppContainer`.

### Dane / offline
- **Room** (tabele treningów, templatek, planera oraz cache Social)
- TypeConverters (`Converters`) do pól z listami itp.

### Chmura
- Firebase BOM `32.7.0`
- Firebase Auth
- Firebase Firestore

### Sync i tło
- WorkManager
  - `TrainingSyncWorker`, `TemplateSyncWorker`, `ExerciseSyncWorker`
  - `FirestoreCleanupWorker` (sprzątanie po soft delete)

### Inne
- Gson (seed ćwiczeń z assets)
- Coil (ładowanie obrazków)
- Vico (wykresy)

---

## 4) Uprawnienia i komponenty systemowe

`AndroidManifest.xml`:
- `RECEIVE_BOOT_COMPLETED` — odtwarzanie alarmów po restarcie / zmianie czasu
- `SCHEDULE_EXACT_ALARM` — dokładne alarmy
- `POST_NOTIFICATIONS` — powiadomienia

Komponenty:
- `MainActivity` (LAUNCHER)
- `BootReceiver` (BOOT_COMPLETED, TIME_CHANGED, TIMEZONE_CHANGED)
- `WorkoutNotificationReceiver`

---

## 5) Struktura domen (feature’y)

1. **Auth** (Firebase)
2. **Exercises**
   - seedowane ćwiczenia z `assets/exercises.json` do Room
   - dodatkowe custom exercises synchronizowane z Firestore
3. **Training**
   - `TrainingSession` + `SessionExercise` + `SessionSetDetails`
   - soft delete + pending sync
4. **Templates**
   - `WorkoutTemplate` + `TemplateExercise`
   - soft delete + pending sync
5. **Planner (planned workouts)**
   - `PlannedWorkoutEntity` + scheduler powiadomień
6. **Social**
   - following (źródło prawdy w Firestore, cache w Room)
   - posty (Firestore `posts`, cache w Room)

---

## 6) Nawigacja i flow UI

### Root flow
`MainActivity`:
- inicjalizuje `ExerciseDatabase`
- tworzy repozytoria dla ćwiczeń i treningów (część DI jest jeszcze „ręczna”)
- jeśli `authViewModel.currentUser == null` → `AuthScreen`
- jeśli zalogowany → `MainScreen`

### AuthScreen
- obsługa logowania i rejestracji
- nasłuch `authState` (Idle/Loading/Success/Error)

### MainScreen — nawigacja
- BottomBar oparty o lokalny stan `selectedIndex` (nie o route’y)
  - 0: Home
  - 1: Calendar
  - 2: Planner
- Dodatkowe route’y w `NavHost`:
  - `add_exercise/{sessionId}`
  - `set_details/{sessionExerciseId}/{exerciseId}`

Konsekwencja: „duże” zakładki są przełączane bez NavController (stan lokalny), a ekrany szczegółowe idą przez route’y.

---

## 7) Baza danych Room — pełna lista encji

Z `ExerciseDatabase.kt` (Room, `version = 15`, `fallbackToDestructiveMigration()`):

### Główne encje
- `Exercise`
- `TrainingSession`
- `SessionExercise`
- `SessionSetDetails`
- `WorkoutTemplate`
- `TemplateExercise`
- `PlannedWorkoutEntity`

### Encje cache Social
- `PostEntity` (`social_posts`)
- `UserCacheEntity` (`social_users`)
- `FollowingEntity` (`social_following`)

DAOs:
- `exerciseDao()`
- `trainingDao()`
- `templateDao()`
- `plannedWorkoutDao()`
- `socialDao()`

### Uwaga o migracjach
Użyto `fallbackToDestructiveMigration()`.
- Plus: prostota podczas rozwoju
- Minus: aktualizacja schematu może skasować dane lokalne
- Minimalizujemy ryzyko przez to, że źródłem backupu jest Firestore (po zalogowaniu robimy pull), ale **lokalne niezsynchronizowane pending** nadal jest ryzykiem — stąd strategia blokady wylogowania i praca w kolejce pending.

---

## 8) Offline-first: założenia i dlaczego to działa

### 8.1 SSOT: Room
- UI zawsze czyta z Room (Flow/LiveData)
- operacje użytkownika zapisują do Room natychmiast

Zalety:
- działanie bez internetu
- brak „spike’ów” UX od sieci
- deterministyczny stan UI (to co jest w Room, to jest prawdą)

### 8.2 Soft delete i syncStatus
Większość encji domenowych ma pola (w różnych klasach):
- `syncStatus`:
  - `0 = SYNCED`
  - `1 = PENDING_UPSERT`
  - `2 = PENDING_DELETE`
- `deletedAtMs` (soft delete)
- `updatedAtMs`
- `remoteId` (ID dokumentu w Firestore; lokalnie rekord ma swój `id`, a do Firestore idzie `remoteId`)

**Soft delete** (ustawienie `deletedAtMs` zamiast realnego kasowania) ma dwa cele:
1) UI filtruje rekordy usunięte (`deletedAtMs IS NULL`) i natychmiast „znika” z ekranu
2) worker w tle może wysłać informację o usunięciu do chmury, nawet jeśli offline

### 8.3 Synchronizacja asynchroniczna (WorkManager)
- Każda operacja modyfikująca w repozytorium (np. trening) kończy się `...SyncScheduler.enqueue(context)`.
- Scheduler używa constraintu `NetworkType.CONNECTED`.
- Work jest enqueuowany jako unique work (`ExistingWorkPolicy.KEEP`), aby nie spamować workerami.

Efekt: gdy internet wróci, system uruchomi workery i „wypchnie” kolejkę pending.

---

## 9) Reakcja na utratę/powrót internetu

### 9.1 Monitor sieci
`NetworkMonitor` emituje `Flow<NetworkState>`:
- `Offline`
- `ConnectedNoInternet` (jest sieć, ale brak VALIDATED; np. captive portal)
- `OnlineValidated`

Źródło prawdy dla online validated: `NetworkStatus.isOnline()` sprawdza `NET_CAPABILITY_INTERNET` i `NET_CAPABILITY_VALIDATED`.

### 9.2 Powrót internetu a sync
Mechanizm techniczny opiera się na WorkManager constraints (CONNECTED) + `Result.retry()`.
- Gdy jesteśmy offline i jest pending → worker się nie odpali lub zretry’uje
- Gdy wróci internet → WorkManager odpali pending sync

W projekcie jest też `NetworkMonitor` — zwykle używany do:
- blokowania akcji online-only (np. publikacja postów)
- pokazywania bannera “offline”
- ewentualnego triggerowania refresh/pull

---

## 10) Login / pull danych z chmury (dlaczego robimy to przy loginie)

W `AuthViewModel` (bardziej rozbudowana wersja) po `signIn`/`signUp` wykonywany jest pull:
1) custom exercises
2) training
3) templates
4) planner
5) cleanup orphaned pending

**Dlaczego przy loginie?**
- Na nowym telefonie lub po reinstalacji musimy odbudować lokalny Room.
- Minimalizujemy liczbę automatycznych pull’i (koszt Firestore) — codzienne operacje idą przez push worker.

W pull service’ach jest strategia MVP: **wipe + full pull**.
- Proste
- Unika konfliktów merge
- Wymaga sprzątania “osieroconych pending”

---

## 11) Kolejka pending, stany i edge-case’y

### 11.1 Treningi — pending
`TrainingRepository`:
- create/update/delete → ustawiają `syncStatus` i enqueue `TrainingSyncScheduler`.

`TrainingDao`:
- ma metody `getPendingSessions/getPendingSessionExercises/getPendingSessionSets`
- ma soft delete kaskadowy:
  - `softDeleteSession`
  - `softDeleteExercisesForSession`
  - `softDeleteSetsForSession`

`TrainingSyncWorker`:
1) wysyła rodziców (sessions)
2) wysyła dzieci (exercises)
3) wysyła wnuki (sets)

Ma „bezpieczne SKIP” jeśli brakuje rodzica/remoteId, żeby nie zablokować całego sync.

### 11.2 Template’y — pending
Analogicznie:
- `TemplateSyncWorker` pushuje templates i template exercises
- `TemplatePullService` robi wipe + pull

### 11.3 Custom exercises — pending
- `ExerciseSyncWorker` wypycha pending custom exercises do Firestore
- `ExercisePullService` robi wipe + pull

### 11.4 Sprzątanie „osieroconych pending”
`OrphanedPendingCleanup`:
- usuwa tylko rekordy `syncStatus != SYNCED`
- usuwa tylko faktycznie osierocone dzieci bez rodzica
- chroni przed sytuacją, że jeden wadliwy rekord blokuje całą synchronizację

### 11.5 Hard-delete po retencji
`FirestoreCleanupWorker`:
- retencja 30 dni
- Firestore: usuwa stare soft-deleted dokumenty
- Room: usuwa stare soft-deleted rekordy tylko gdy `syncStatus == SYNCED`

---

## 12) Blokada wylogowania (ochrona przed utratą danych offline)

Założenie systemowe: **nie pozwalamy wylogować się użytkownikowi, jeśli ma niewysłane zmiany (pending)**.

Powód:
- dane pending istnieją tylko lokalnie do momentu sync
- wylogowanie w tej aplikacji wiąże się z czyszczeniem danych lokalnych (privacy + multi-account) → pending mogłyby przepaść

Implementacyjnie oznacza to, że UI przy próbie wylogowania powinno:
1) sprawdzić, czy są pending rekordy (training/template/custom exercises/planner)
2) jeśli tak — zablokować wylogowanie i pokazać komunikat typu:
   - „Brak internetu / trwa synchronizacja. Wylogowanie jest zablokowane, aby nie utracić danych.”

W kodzie widać przycisk wylogowania w `MainScreen.kt` oraz rozbudowaną logikę `AuthViewModel.signOut()` (wipe lokalnych danych + `auth.signOut()`), ale **źródło prawdy “czy są pending?”** powinno pochodzić z DAO (np. count pending). To jest kluczowa reguła biznesowa aplikacji.

---

## 13) Social: offline cache, online-only write, paginacja

### 13.1 Cache w Room
Encje:
- `PostEntity` (denormalizacja: `exercisesJson` jako JSON)
- `UserCacheEntity`
- `FollowingEntity`

`SocialDao`:
- `observeExploreFeed(myId)` zwraca feed jako JOIN postów z tabelą following → posty pokazujemy tylko dla obserwowanych.
- `replaceFollowingForUser(myId, newEntities)` — po sync z Firestore wymieniamy cały following lokalnie (Firestore jest źródłem prawdy).

### 13.2 Repozytorium Firestore
`FirestoreSocialRepository`:
- `syncFollowing()` pobiera `users/{uid}/following` i zapisuje lokalnie jako cache.
- `refreshExploreFeed...()` pobiera posty z `posts`:
  - Firestore `whereIn("authorId", chunk)` ma limit 10 → repo batchuje po 10 i robi merge.
  - Sortowanie i kursor: `createdAtMs`.
  - Wyniki zapisuje do cache w Room.
- `observeExploreFeed()` zwraca Flow z Room → UI działa offline na już pobranych postach.

### 13.3 Tryb offline
W social, w sensie danych:
- **przeglądanie**: działa offline (Room cache)
- **odświeżanie feedu / paginacja**: wymaga internetu (Firestore)
- **publikacja/usuwanie postów**: wymaga internetu (Firestore). Repo robi 1) zapis do Firestore 2) update cache w Room.

To spełnia wymaganie: offline można przeglądać tylko to, co zostało wcześniej zcache’owane.

### 13.4 Publikowanie posta
`publishPost(sessionId)`:
1) pobiera dane treningu z Room (sesja + ćwiczenia + serie)
2) mapuje je do `PostDoc` (denormalizacja)
3) zapisuje do `firestore.collection("posts").document(postId).set(... merge)`
4) upsertuje post w Room cache
5) ustawia flagę UX: `trainingDao.setSessionPosted(isPosted = true)`

### 13.5 Usuwanie posta
`deletePost(post)`:
1) usuwa dokument w Firestore
2) usuwa z Room
3) resetuje flagę `isPosted` dla sesji, aby można było udostępnić ponownie

---

## 14) Planner i powiadomienia

W repo widać:
- `PlanningRepository` + `NotificationScheduler`
- receivery w manifeście (boot/time change)
- `PlannedWorkoutEntity` w Room

Założenie działania:
- plan treningów jest trzymany lokalnie (Room) i synchronizowany z Firestore
- alarmy/powiadomienia są odtwarzane po restarcie urządzenia przez `BootReceiver`

---

## 15) Jakość, testy i znane ograniczenia

### Testy
- są tylko przykładowe testy (`ExampleUnitTest`, `ExampleInstrumentedTest`).

### Znane ograniczenia
- w module `app/build.gradle.kts` są dwie wersje `navigation-compose`.
- `fallbackToDestructiveMigration()` — utrata danych lokalnych przy zmianie schematu.
- wymagane jest poprawnie ustawione `JAVA_HOME`, żeby uruchomić build na Windows.

---

## 16) Mapa plików (najważniejsze punkty wejścia)

- `app/src/main/java/com/example/gymtrackapp/MainActivity.kt` — wejście aplikacji, start UI
- `app/src/main/java/com/example/gymtrackapp/ui/screens/MainScreen.kt` — shell UI + nawigacja
- `app/src/main/java/com/example/gymtrackapp/ui/screens/AuthScreen.kt` — auth UI
- `app/src/main/java/com/example/gymtrackapp/data/ExerciseDatabase.kt` — definicja Room
- `app/src/main/java/com/example/gymtrackapp/data/dao/*` — DAO (training/template/planner/exercise)
- `app/src/main/java/com/example/gymtrackapp/data/sync/*` — pull services, sync workers, cleanup
- `app/src/main/java/com/example/gymtrackapp/utils/NetworkMonitor.kt` — wykrywanie online/offline
- `app/src/main/java/com/example/gymtrackapp/data/social/**` — social repo + cache
- `app/src/main/java/com/example/gymtrackapp/di/AppContainer.kt` — DI container

---

## 17) Kontrakty danych w Firestore (konwencje)

Z kodu wynika typowa struktura:
- `users/{uid}` — profil (displayName, avatarColor, ...)
- `users/{uid}/following/{otherUserId}` — relacja follow
- `posts/{postId}` — posty social
- Training (pod userem):
  - `users/{uid}/trainingSessions/{sessionRemoteId}`
  - `.../exercises/{exerciseRemoteId}`
  - `.../sets/{setRemoteId}`
- Templates (pod userem):
  - `users/{uid}/workoutTemplates/{templateRemoteId}`
  - `.../exercises/{exerciseRemoteId}`
- Custom exercises (pod userem):
  - `users/{uid}/customExercises/{exerciseId}`

Dodatkowo cleanup worker wymaga indeksów (opisane w komentarzach `FirestoreCleanupWorker`).

---

## 18) Co oznacza „pending” i jak to prezentować w UI

Rekomendowany model UI (zgodny z architekturą projektu):
- Rekord `syncStatus != SYNCED` oznacza, że czeka na wysyłkę.
- UI może pokazywać:
  - badge „Do zsynchronizowania”
  - stan globalny: „Offline / Brak internetu — zmiany zapisane lokalnie”
- Przy reconnect: WorkManager wysyła zmiany; po `mark*Synced()` status wraca do `SYNCED`.

---

## 19) Wymagania, które ten dokument pokrywa

- Język/SDK/technologie: TAK
- Compose UI i nawigacja: TAK
- Room (encje/DAO) i cache: TAK
- Firebase Auth/Firestore: TAK
- Offline-first: TAK (SSOT Room + pending + soft delete)
- Powrót internetu: TAK (NetworkMonitor + WorkManager constraints)
- Pending i stany: TAK (syncStatus + deletedAtMs + workery)
- Social offline browse + online write: TAK (Room cache + refresh/publish/delete przez Firestore)
- Blokada wylogowania aby nie stracić danych: opisane jako reguła biznesowa i powód


