# 🏋️‍♂️ Aplikacja Treningowa: Offline-First Tracker

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blueviolet?style=flat&logo=kotlin)
![Android Jetpack](https://img.shields.io/badge/Jetpack_Compose-Material_3-green?style=flat&logo=android)
![Firebase](https://img.shields.io/badge/Firebase-Auth_%7C_Firestore-FFCA28?style=flat&logo=firebase)
![Room](https://img.shields.io/badge/Database-Room_ORM-blue?style=flat)
![Architecture](https://img.shields.io/badge/Architecture-MVVM_%7C_Repository-orange?style=flat)

Nowoczesna aplikacja mobilna do śledzenia postępów w treningu siłowym, zaprojektowana i zbudowana na platformę Android z naciskiem na niezawodność, ergonomię oraz architekturę **Offline-First**. 

Ten projekt to praca inżynierska, która rozwiązuje realny problem braku zasięgu na siłowniach, łącząc zaawansowaną logikę lokalną ze sprawną synchronizacją w chmurze i funkcjami społecznościowymi.

---

## 📱 Moduły funkcjonalne (Zrzuty ekranu)

### 🏋️‍♂️ Moduł treningowy (rejestrowanie i edycja treningów)
*Główny moduł aplikacji pozwalający na aktywne śledzenie sesji treningowej, dodawanie serii, obciążeń oraz powtórzeń w czasie rzeczywistym.*
<p align="center">
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/f29d44a1-3fbb-4b36-b7f1-222d62fefaa3" />
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/cda3f689-6b4c-451a-8e41-e7d10a80f4a2" />
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/d4065110-7b4c-4b48-a6c4-fb23f16cc5fe" />
</p>

### 📝 Moduł własnych szablonów i ćwiczeń
*Tworzenie spersonalizowanych planów treningowych oraz dodawanie własnych (niestandardowych) ćwiczeń do lokalnej bazy.*
<p align="center">
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/9f8d24c3-fed7-4302-9c35-b72e45f44a07" />
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/13bf3cbf-99e0-48dc-aba8-91af5f3daf54" />
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/a1f2494d-3da2-4dfc-ad60-cb8a9ca49714" />
</p>
<p align="center">
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/f74175b1-f5e9-485a-a54b-00ace7f9b83b" />
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/75b9b525-2997-45be-9827-604b1dba499d" />
</p>

### 📈 Moduł statystyk i analizy postępów
*Zaawansowana analityka, w tym wyliczanie 1RM (rekordów maksymalnych), mapa aktywności (heatmap) oraz wykresy objętości treningowej.*
<p align="center">
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/c24598dd-7888-4264-8e7f-b57fabf146be" />
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/f035fa64-8562-452c-904f-0ffa7853d8f5" />
</p>
<p align="center">
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/e5f1ec36-40f3-440d-ab9b-74439df513fb" />
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/7edefda1-9584-4fb7-be2f-bb5917eb6914" />
</p>

### 📅 Moduł planowania treningów
*Wbudowany kalendarz pozwalający na podgląd historii treningowej oraz planowanie przyszłych sesji z wykorzystaniem systemowych powiadomień.*
<p align="center">
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/b5c1e3ee-d257-4716-ae7c-cdc3b07b1d9a" />
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/ed60d3ee-bd16-498e-bd97-ed868735eb65" />
</p>

### 🌍 Moduł społecznościowy
*Tablica (Feed) pozwalająca na obserwowanie postępów innych użytkowników, udostępnianie własnych treningów i wzajemną motywację.*
<p align="center">
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/91b8e769-2acb-4771-b84d-00b0beff4870" />
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/da96476a-4e21-423e-b634-856dc780864c" />
</p>
<p align="center">
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/adf34666-ec9f-424f-9835-4c0933f211ca" />
  <img width="30%" height="600" alt="image" src="https://github.com/user-attachments/assets/df4ba636-fe3d-4d7c-bb24-90a00e626b64" />
</p>

---

## 🎯 Główne Funkcjonalności

* **Rejestrowanie i Planowanie Treningów:** Tworzenie sesji, dodawanie ćwiczeń, serii i parametrów (ciężar, powtórzenia) z zachowaniem ciągłości numeracji. Kopiowanie i modyfikacja całych planów.
* **Rozbudowana Baza Ćwiczeń:** Wbudowany, lokalny katalog ćwiczeń (JSON) dostępny w 100% offline, z możliwością tworzenia i synchronizowania własnych (Custom Exercises).
* **Zaawansowana Analityka:** Obliczenia wykonywane lokalnie (nie obciążają serwera). Aplikacja oferuje m.in. estymację 1RM (wzory Epley/Brzycki), mapę aktywności (heatmap), analizę objętości treningowej oraz wykresy progresji siłowej.
* **Moduł Społecznościowy:** Możliwość udostępniania zrealizowanych treningów, wyszukiwania użytkowników (wyszukiwanie prefiksowe) i obserwowania ich postępów na tablicy (Feed). Zoptymalizowany poprzez denormalizację danych w chmurze i lokalny cache.
* **System Powiadomień:** Wbudowany planer korzystający z systemowego `AlarmManager`, powiadamiający o nadchodzących treningach (nawet po restarcie urządzenia - `BootReceiver`).

---

## 🏗 Architektura & Podejście Offline-First

Fundamentem aplikacji jest architektura **MVVM (Model-View-ViewModel)** połączona ze wzorcem **Repository** oraz strategią **Single Source of Truth (SSOT)** opartą na lokalnej bazie SQLite (Room).

### Schemat Architektury Systemu

<p align="center">
  <img width="1126" height="591" alt="image" src="https://github.com/user-attachments/assets/f2755c93-6d14-48f2-8625-128b4edb5fc8" />
</p>

### Model Synchronizacji
Aplikacja została zbudowana tak, aby brak połączenia z siecią był traktowany jako standardowy stan, a nie błąd:
1. **Lokalny zapis (SSOT):** Wszystkie operacje zapisu i odczytu odbywają się natychmiast na bazie Room. UI nigdy nie czeka na odpowiedź serwera.
2. **Push (Wypychanie danych):** Zmiany oznaczane sunt flagami stanu (np. `PENDING_UPSERT`). Systemowy `WorkManager` w tle monitoruje te rekordy i asynchronicznie wysyła je do **Firebase Firestore**, gdy wykryje stabilne połączenie z siecią.
3. **Pull (Pobieranie):** Zastosowano strategię *Session-Based Consistency* (Wipe & Pull). Dane pobierane są przy logowaniu użytkownika oraz na żądanie (ręczne odświeżanie poprzez Pull-to-Refresh lub dedykowane przyciski), co umożliwia płynne korzystanie z wielu urządzeń.
4. **Zarządzanie Konfliktami & Bezpieczeństwo:** Wykorzystano znaczniki czasu (`updatedAtMs`), miękkie usuwanie (`Soft Delete` z propagacją) oraz blokadę wylogowania w trybie offline w celu ochrony niezsynchronizowanych danych.

---

## 🛠 Wykorzystane Technologie

### Język i Interfejs
* **Kotlin:** W pełni natywny, bezpieczny (Null Safety) i asynchroniczny (Coroutines/Flow).
* **Jetpack Compose:** Deklaratywny framework UI zgodny z wytycznymi Material Design 3, zoptymalizowany pod kątem obsługi jedną ręką.

### Baza Danych i Asynchroniczność
* **Room (SQLite):** Lokalna baza danych. W pełni zintegrowana z strumieniami `Flow`, zapewniająca natychmiastową, reaktywną aktualizację interfejsu (UDF - Unidirectional Data Flow).
* **Coroutines & WorkManager:** Obsługa procesów w tle, w tym gwarantowana dostawa danych (synchronizacja) nawet po zamknięciu aplikacji.

### Chmura (BaaS)
* **Firebase Authentication:** Bezpieczne zarządzanie użytkownikami (Email/Hasło, generowanie tokenów JWT).
* **Cloud Firestore (NoSQL):** Chmurowy magazyn danych pełniący rolę punktu synchronizacji, kopii zapasowej oraz backendu dla funkcji społecznościowych. 
* **Firestore Security Rules:** Rygorystyczna konfiguracja po stronie serwera zabezpieczająca prywatność danych treningowych (odczyt/zapis tylko dla właściciela).

---

## 💡 Dlaczego taka architektura? (Analiza Decyzji)

* **BaaS (Firebase) zamiast własnego REST API:** Zastosowanie modelu Serverless pozwoliło skupić zasoby na rozbudowie zaawansowanej logiki biznesowej i mechanizmu Offline-First, eliminując potrzebę czasochłonnego utrzymania infrastruktury i pisania własnych endpointów do synchronizacji.
* **Room zamiast Realm / NoSQL (Lokalnie):** Dane treningowe posiadają silny charakter relacyjny (Trening -> Ćwiczenie -> Serie). Rozwiązanie oparte na relacyjnej bazie (SQLite/Room) z użyciem kluczy obcych i operacji kaskadowych gwarantuje w tym przypadku znacznie wyższą stabilność i czystość modelu danych niż lokalne bazy obiektowe.
* **Natywny Kotlin zamiast Cross-Platform (Flutter/React Native):** Odrzucenie warstw pośrednich pozwoliło na maksymalną wydajność operacji na dużych lokalnych zbiorach danych, lepszą obsługę procesów w tle (`WorkManager`) oraz pełny dostęp do najnowszych narzędzi z pakietu Android Jetpack.

---

## 🚀 Jak uruchomić projekt

1. Sklonuj repozytorium: `git clone https://github.com/szymon-muszynski/GymTrackApp`
2. Otwórz projekt w **Android Studio**.
3. **Konfiguracja Firebase:**
   * Zarejestruj nową aplikację w Firebase Console.
   * Pobierz plik `google-services.json` i umieść go w katalogu `/app`.
   * W Firebase Console włącz *Authentication* (Email/Password) oraz *Firestore Database*.
   * (Opcjonalnie) Wdróż reguły bezpieczeństwa Firestore znajdujące się w pliku `firestore.rules`.
4. Zbuduj i uruchom aplikację na fizycznym urządzeniu (Android 10+) lub w emulatorze.

---

## 👨‍💻 O Autorze

**Szymon Muszyński**
* Projekt stanowi realizację pracy inżynierskiej.
