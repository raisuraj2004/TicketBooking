# FunSeats

FunSeats is a Java Swing desktop app for browsing events (movies, concerts, standups, sports) and booking tickets with tiered pricing, seat tracking, and simple admin analytics.

## Features
- Browse & search events by name/venue/category
- Sign in/out (basic in-app session)
- Book tickets with **Gold / Silver / Bronze** tiers and quantity selection
- Seat availability tracking with concurrency-safe booking flow (queue + locks)
- Admin panel: add events and view booking stats (seats sold + total revenue)
- Receipt generation to `receipts/` and booking persistence to `bookings.txt`

## Tech/Concepts Used
- Java Swing UI (CardLayout-based screens)
- OOP + common patterns: Singleton (manager), Factory (events), Observer (seat availability)
- Multithreading primitives: `ExecutorService`, `BlockingQueue`, `ReentrantLock`, `AtomicInteger`
- File I/O for persistence and receipts

## Project Structure
- `src/main/Main.java` — application entry point
- `src/ui/` — Swing UI (Home, Booking, Admin, Login, etc.)
- `src/service/` — booking logic, queue, simulation helpers
- `src/model/` — domain models (Movie, Concert, SportEvent, Booking, etc.)
- `src/util/` — file handling + receipt generation
- `assets/` — category images used by the Home screen

## Run Locally

### Option 1: IDE (recommended)
Open the project in IntelliJ/Eclipse, mark `src/` as Sources Root, then run `main.Main`.

### Option 2: Command line

**Windows (PowerShell)**
```powershell
mkdir bin
javac -encoding UTF-8 -d bin (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object FullName)
java -cp bin main.Main
```

**macOS / Linux**
```bash
mkdir -p bin
javac -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp bin main.Main
```

## Notes
- Admin password (demo): `admin123` (see `src/service/BookingManager.java`)
- Bookings are loaded from `bookings.txt` on startup and saved on exit.

