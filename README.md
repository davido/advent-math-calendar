# 🎄 Advent Math Calendar --- Fullstack Application

Welcome to the **Advent Math Calendar**, a fullstack application that
delivers daily mathematical challenges throughout December --- complete
with locking logic, solution release rules, and a polished React
frontend.

This README is a comprehensive guide covering:

-   🎨 Features
-   🧭 Business Logic (doors, unlocking rules, examples)
-   🌐 API Documentation
-   🧱 Architecture Overview
-   🧪 Testing
-   ⚙️ Configuration
-   🛠 Development & Deployment
-   📁 File & Folder Structure

------------------------------------------------------------------------

# ✨ Features

-   **24 interactive Advent doors**
-   Backend in **Kotlin + Spring Boot 4**
-   Frontend in **React + Vite**, located in `advent-ui/`
-   JSON served via **Jackson 3**
-   Door images, task PDFs, solution PDFs
-   Date override for testing
-   Comprehensive test suite

------------------------------------------------------------------------

# 🧭 Business Logic --- How the Advent Calendar Works

The application follows specific, consistent rules for unlocking tasks
and solutions.

## 📅 1. Door Unlocking

🎁 A door can only be opened **once its calendar day has arrived**.

``` text
  Door Date    Can open on      Example
  ------------ ---------------- --------------
  01.12.2025   **01.12.2025**   OK 🎉
  05.12.2025   **05.12.2025**   OK
  10.12.2025   09.12.2025       ❌ too early
```

Implementation rule:

``` java
    unlocked = !today.isBefore(date)
```
------------------------------------------------------------------------

## 📘 2. Task Availability (PDF)

Once a door is unlocked:

✔️ The **task PDF remains available forever**\
(even if downloaded after Christmas).

------------------------------------------------------------------------

## 🧩 3. Solution Availability (PDF)

Solutions become available **one day later**.

**Important:**
👉 *No special weekend rules --- weekends are treated like any other
day.*

### Example timeline

``` text
  Door   Date               Task available   Solution available
  ------ ------------------ ---------------- --------------------
  1      01.12.2025 (Mon)   01.12.2025       02.12.2025
  2      02.12.2025 (Tue)   02.12.2025       03.12.2025
  6      06.12.2025 (Sat)   06.12.2025       07.12.2025 (Sun)
  7      07.12.2025 (Sun)   07.12.2025       08.12.2025
```

Because weekends are **not skipped**, solutions simply unlock at:

``` java
    solutionUnlocked = today.isAfter(date)
```

------------------------------------------------------------------------

## 📦 AdventDayDto Format

``` json
{
  "day": 7,
  "date": "2025-12-07",
  "unlocked": true,
  "solutionUnlocked": false,
  "title": "Türchen 7",
  "taskUrl": "/files/aufgabe-7.pdf",
  "solutionUrl": "/files/loesung-7.pdf",
  "imageUrl": "/images/doors/day-07.png"
}
```

------------------------------------------------------------------------

# 🌐 REST API

Base path:

    /api/advent

## 📗 GET /api/advent/days

Returns all 24 days.

## 📘 GET /api/advent/days/{day}

Returns a single item or:

-   `{ "error": "locked" }`
-   `{ "error": "not found" }`

------------------------------------------------------------------------

# 📁 Project Structure

``` text
advent-math-kalender/
├─ advent-ui/                 # React + Vite frontend
│  ├─ src/
│  ├─ public/
│  └─ dist/
│
├─ src/
│  ├─ main/kotlin/org/...     # Backend code
│  ├─ main/resources/static/  # Frontend build copied here
│  ├─ test/kotlin/            # Test suite
│
└─ pom.xml
```

------------------------------------------------------------------------

# 🧪 Testing

``` bash
mvn test
```

Tests cover:

-   date handling
-   locking rules
-   DTO validity
-   response formats
-   edge cases (invalid days, future days)

------------------------------------------------------------------------

# ⚙️ Configuration

## Override today's date

    advent.fixed-today=2025-12-07

Ways to pass it:

### CLI

``` bash
java -jar app.jar --advent.fixed-today=2025-12-07
```

### Environment variable

``` bash
export ADVENT_FIXED_TODAY=2025-12-07
```

------------------------------------------------------------------------

# 🛠 Development

## Backend

``` bash
mvn spring-boot:run
```

## Frontend

``` bash
cd advent-ui
npm install
npm run dev
```

------------------------------------------------------------------------

# 🚀 Production Build

``` bash
mvn clean package
```

This creates:

    target/advent-math-calendar-1.0.0.jar

Frontend is embedded automatically.

------------------------------------------------------------------------

# 🎅 Enjoy the Advent Season

This project is designed to be simple, beautiful, and flexible.

Ideas for future enhancements:

-   a Dockerfile 🐳
-   Nginx reverse proxy config
-   PDF generation pipeline

Merry math-adventing! 🎄🧮
