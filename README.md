# Assignment 1 — Hello Kotlin. Hello Android World!

Course: Desenvolvimento de Aplicações Móveis (DAM)
Student: Lucas Filipe
Date: March 08, 2026
Repository URL: https://github.com/NalienDev/DAM_TP1/

---

## 1. Introduction

This assignment introduces the fundamentals of Kotlin and Android development. The goal is to establish a strong foundation in the Kotlin language — covering basic types, control flow, and object-oriented programming — while also setting up the Android development environment and building a first native Android application.

The work is divided into two main components:
- A **Kotlin console application** built with Maven in IntelliJ IDEA, covering basic exercises and an advanced OOP exercise (Virtual Library).
- An **Android application** called **Movie Buddy**, developed in Android Studio, which consumes a public REST API and persists data locally.

The assignment also introduces agentic programming, where AI tools are used to assist in the development of the Movie Buddy application.

---

## 2. System Overview

The repository contains two major components:

**Kotlin Console Application (`/kotlin`):** A Maven-configured Kotlin project with multiple exercises:
- `exer_1`: Array creation using different approaches (IntArray, range + map, Array constructor).
- `exer_2`: An interactive console calculator supporting arithmetic, boolean, and bitwise shift operations.
- `exer_3`: A functional sequence simulating a bouncing ball using `generateSequence` and `takeWhile`.
- `exer_vl`: A Virtual Library Management System showcasing OOP principles — abstract classes, inheritance, custom getters/setters, data classes, and companion objects.

**Android Application — Movie Buddy (`/android/code/MovieBuddy`):** A native Android app that allows users to search for movies and TV shows via the TVMaze public API, view details, and save personal ratings locally using SharedPreferences.

Additional introductory Android projects (`HelloWorldOptional`, `HelloWorldV2`) are included as optional exercises.

---

## 3. Architecture and Design

### Kotlin Component

The Kotlin project follows a simple package-based structure under the `dam` root package:
- `dam.exer_1`, `dam.exer_2`, `dam.exer_3` — individual exercise packages.
- `dam.exer_vl` — the Virtual Library domain model.

The Virtual Library applies core OOP principles:
- **Abstraction**: `Book` is an abstract class with an abstract method `getStorageInfo()`.
- **Inheritance & Polymorphism**: `PhysicalBook` and `DigitalBook` extend `Book` and provide their own implementations of `getStorageInfo()`.
- **Encapsulation**: Custom getters and setters enforce business rules (e.g., no negative copies, out-of-stock warnings).
- **Data Classes**: `LibraryMember` is a Kotlin data class, automatically providing `equals`, `hashCode`, and `toString`.
- **Companion Object**: `Library` uses a companion object to track the total number of books added across all library instances — a static-like pattern in Kotlin.

### Android Component — Movie Buddy

The application follows the **MVVM (Model-View-ViewModel)** architecture, as recommended by Google:

- **Model (Data Layer)**: Data classes (`MovieDto`, `TvMazeResponse`) and repositories (`MovieRepository`, `RatedMovieRepository`) are responsible for fetching and persisting data.
- **ViewModel (Business Logic Layer)**: `MovieListViewModel` exposes UI state via `StateFlow`, surviving configuration changes such as screen rotation.
- **View (Presentation Layer)**: Implemented with Fragments (`MovieListFragment`, `MovieDetailFragment`, `RatedMoviesFragment`) and View Binding for type-safe access to UI elements.

Navigation between screens is handled by the **Navigation Component** in a single-activity architecture.

---

## 4. Implementation

### Kotlin Exercises

**Exercise 1 — Arrays:**
Three approaches to create an integer array of the first 50 perfect squares:
```kotlin
val intArray = IntArray(50) { it * it }
val arrayRangeMap = (0..49).map { it * it }
val array = Array(50) { it * it }
```

**Exercise 2 — Calculator:**
A menu-driven console calculator using a `do-while` loop and `when` expression. Each operation mode (arithmetic, boolean, bitwise) runs in its own loop, parsing user input manually and handling invalid entries gracefully (e.g., division by zero, non-integer input).

**Exercise 3 — Bouncing Ball:**
```kotlin
fun dropBall(height: Float): List<Float> {
    return generateSequence(height) { it * 0.6f }
        .takeWhile { it > 1 }
        .toList()
}
```
Uses `generateSequence` to lazily produce each bounce height (60% of the previous), terminated by `takeWhile` once the height drops below 1 metre.

**Exercise — Virtual Library (`exer_vl`):**

- `Book` (abstract class): holds `title`, `author`, `year`, and `availableCopies`. The `publicationYear` getter computes an era label ("Classic", "Modern", "Contemporary") on the fly. The `availableCopies` setter rejects negative values and prints a warning when stock hits zero. The `init` block validates inputs using `require`.
- `PhysicalBook`: adds `weight` (grams) and `hasHardCover` (Boolean, default `true`).
- `DigitalBook`: adds `fileSize` (MB) and `format` (typed `Format` enum: PDF, EPUB, MOBI).
- `LibraryMember` (data class): stores `name`, `membershipId`, and a `MutableList<String>` of borrowed book titles.
- `Library`: manages a `MutableList<Book>`, implementing `addBook`, `borrowBook`, `returnBook`, `searchByAuthor`, and `showBooks`. A companion object tracks `totalBooksAdded` across all instances.

### Android — Movie Buddy

**API Integration (TVMaze):**
1. `MovieListFragment` captures the user's search input.
2. `MovieListViewModel` launches a coroutine and delegates to `MovieRepository`.
3. `MovieRepository` calls `RetrofitClient.apiService` on `Dispatchers.IO`.
4. The result is wrapped in a sealed state class (`MovieListUiState`) and emitted via `StateFlow`.
5. The Fragment collects the state using `repeatOnLifecycle`, updating the `RecyclerView` (via `MovieAdapter`) or showing loading/error states accordingly.

**Movie Detail:**
Clicking a movie navigates via `findNavController().navigate()` with a `Bundle` carrying the movie metadata. `MovieDetailFragment` displays the poster (loaded by Glide), summary, genre, and a rating interface.

**Ratings Persistence (SharedPreferences):**
`RatedMovieRepository` stores ratings as key-value pairs with keys formatted as `"rating_<MOVIE_ID>"`. The rated movies list is reconstructed by iterating all stored keys and filtering those starting with `"rating_"`.

**Key Libraries (`build.gradle.kts`):**
- Kotlin, target SDK 34
- Navigation Component (single-activity navigation)
- Coroutines & Kotlin Flow (async operations)
- Retrofit & OkHttp (REST API client + logging interceptor)
- Gson (JSON deserialisation into Kotlin data classes via `@SerializedName`)
- Glide (image loading and caching)

---

## 5. Testing and Validation

Testing was primarily done manually through the Android Emulator (Pixel 9 Pro AVD, API 34) and on a physical device via ADB.

**Kotlin exercises** were validated by running each `main()` function and comparing console output against the expected results specified in the tutorial (e.g., the Virtual Library expected output on pages 25–26 of the assignment PDF).

**Edge cases tested:**
- Exercise 2: Division by zero, non-integer input, invalid operators.
- `exer_vl`: Borrowing a book with zero available copies (should print a sorry message), setting `availableCopies` to a negative value (rejected silently), and creating a `Book` with a non-positive year (throws `IllegalArgumentException` via `require`).
- Movie Buddy: Searching for a title that returns no results, network errors, and re-opening the app to verify that saved ratings persist.

**Known limitations:**
- The calculator (Exercise 2) uses `findAnyOf` for operator detection, which may misparse expressions where a negative number follows an operator (e.g., `5+-3`).
- Movie Buddy does not handle offline mode; network failures are caught and displayed as error messages but the app does not cache search results.

---

## 6. Usage Instructions

### Android Application — Movie Buddy
1. Open **Android Studio**.
2. Select `File > Open` and navigate to `DAM_TP1/android/code/MovieBuddy`.
3. Wait for **Gradle Sync** to complete and download all dependencies.
4. Connect an Android device or launch an Emulator (minimum API 24).
5. Press **Run (Shift + F10)**.

### Kotlin Console Application
1. Open **IntelliJ IDEA**.
2. Select `Open Project` and choose `DAM_TP1/kotlin/pom.xml`.
3. IntelliJ will import the Maven configuration automatically.
4. Run the desired `main()` function from the corresponding exercise file.
   - Alternatively, build via terminal: `mvn package` then `java -jar ...`

---

# Autonomous Software Engineering Sections

## 7. Prompting Strategy

AI assistance (Antigravity) was used exclusively for the **Movie Buddy** application (Section 8 of the assignment — MIP task). The general prompting strategy followed the structure recommended in the tutorial: **Context + Goal + Constraints + Plan + Verification + Deliverables**.

The agent was first set to **Planning Mode** to generate an implementation plan and structured task list before any code was produced. Prompts were refined iteratively — starting with the UI and navigation structure, then adding the API integration layer, and finally the ratings persistence.

Example initial prompt structure used:
> *"You are an autonomous software engineering agent. Create a native Android application in Kotlin using XML Views and MVVM architecture named Movie Buddy. The app should allow users to search for movies via the TVMaze API, view details on a separate screen, and save personal ratings using SharedPreferences. Before writing any code, produce a detailed project plan defining the architecture, folder structure, key dependencies, and navigation strategy."*

Subsequent prompts were used to fix specific issues flagged during testing and to request explanations of generated code segments.

**ChatGPT** was used for smaller, targeted questions during the Kotlin exercises — specifically for clarifying language syntax and troubleshooting compilation errors (e.g., understanding how `backing fields` work in Kotlin setters, clarifying `generateSequence` behaviour).

**Claude** was used to assist in writing and structuring this report.

---

## 8. Autonomous Agent Workflow

For Movie Buddy, Antigravity handled the following development phases autonomously:

- **Planning**: Generated the MVVM folder structure, identified dependencies (Retrofit, Glide, Navigation Component, Coroutines), and proposed the navigation graph between fragments.
- **Coding**: Scaffolded the project files including data classes, repository interfaces, ViewModel with StateFlow, RecyclerView adapter, and XML layouts.
- **Debugging**: When issues arose during testing (e.g., Glide not loading images, navigation argument mismatches), the agent was prompted with the error output and proposed targeted fixes.
- **Documentation**: Helped generate inline comments and clarify the purpose of generated components upon request.

Human oversight was applied at each stage — the implementation plan was reviewed before execution was authorised, and each batch of generated files was inspected before proceeding.

---

## 9. Verification of AI-Generated Artifacts

All AI-generated code was reviewed manually before being accepted:

- **Architecture review**: The proposed MVVM structure was compared against the official Android architecture guidelines to confirm correctness.
- **Functional testing**: Each feature (search, detail screen, rating persistence) was tested on the emulator and on a physical device.
- **Code reading**: Every generated class was read through to understand its role. Where behaviour was unclear, Antigravity and ChatGPT were asked for explanations.
- **Static analysis**: Android Studio's built-in linter and compiler warnings were used to identify issues in the generated code (e.g., unused imports, missing null checks).

No generated code was submitted without being understood and verified.

---

## 10. Human vs AI Contribution

| Component | Primary Author |
|---|---|
| Exercise 1 (Arrays) | Human |
| Exercise 2 (Calculator) | Human |
| Exercise 3 (Bouncing Ball) | Human |
| Virtual Library (`exer_vl`) | Human |
| HelloWorldV2 (Android intro) | Human |
| Movie Buddy — initial scaffold | AI (Antigravity) |
| Movie Buddy — debugging & fixes | Human + AI (Antigravity) |
| Movie Buddy — final review & understanding | Human |
| Report writing | Human + AI (Claude) |

---

## 11. Ethical and Responsible Use

- **Over-reliance risk**: There was a conscious effort not to copy AI output blindly. Every generated code block was read, understood, and tested before use.
- **Accuracy limitations**: Antigravity occasionally generated code referencing slightly outdated API patterns. These were identified during testing and corrected manually.
- **Academic integrity**: AI was only used in sections explicitly marked `[AC YES, AI YES]` in the assignment. The Kotlin exercises (`exer_1` through `exer_vl`) were completed entirely without AI assistance, as required.
- **Responsibility**: All submitted content — including AI-assisted parts — was reviewed and is the full responsibility of the student.

---

# Development Process

## 12. Version Control and Commit History

The project was managed using a local Git repository initialised in IntelliJ IDEA, with commits made progressively throughout development. Each exercise and major feature was committed separately to reflect the progression of work. Commit messages follow a descriptive convention (e.g., `initialize project structure with Kotlin and Maven configuration`, `Finish 2nd and 3rd exercise`, `Android Studio Hello World App start`).

For the Movie Buddy project, the repository was initialised via Antigravity's Source Control panel, with commits made at each significant milestone (initial scaffold, comments, bug fixes).

---

## 13. Difficulties and Lessons Learned

- **Adapting from Java to Kotlin**: Coming from a Java background, some of Kotlin's syntax and conventions were initially confusing. Simple things like how to define and run a `main` function, and understanding when and why to use curly braces `{}` — for example in lambda expressions, constructors, and init blocks — took some getting used to. Over time these patterns became more natural, and the conciseness of Kotlin compared to Java became one of its most appreciated aspects.
- **Android layouts and constraints**: When working on the Hello World application in Android Studio, understanding what constraints were and how the ConstraintLayout system worked was not immediately obvious. Figuring out how to anchor views relative to each other and to the screen edges required some experimentation before it clicked.
- **String resources and `strings.xml`**: The concept of extracting hardcoded strings into `strings.xml` was unfamiliar at first. It was not immediately clear why this was necessary or how the `@string/` reference system connected the XML layout to the resource file. After working through it, the purpose for internationalisation and maintainability became clear.
- **API fetching in Movie Buddy**: Understanding the code responsible for consuming the TVMaze API was one of the bigger challenges. The combination of Retrofit interfaces, coroutines, and how the data flows from the repository up to the ViewModel and then to the UI required careful reading. Some Android Studio-specific patterns — such as `repeatOnLifecycle`, View Binding setup, and the Navigation Component — were also new and took time to understand properly.

---

## 14. Future Improvements

- **Exercise 2**: Improve the calculator's expression parser to handle negative operands and more complex expressions.
- **Movie Buddy**: Add offline caching of search results using Room or a local JSON cache.
- **Movie Buddy**: Implement a favourites list separate from the ratings screen.
- **Virtual Library**: Extend with a persistence layer (e.g., serialise to a JSON file) to simulate a real-world library database.

---

## 15. AI Usage Disclosure (Mandatory)

| Tool | How it was used |
|---|---|
| **Antigravity** | Used for the Movie Buddy application (MIP section). Generated the initial project scaffold, MVVM architecture, Retrofit/Glide integration, and assisted with debugging. Used in Planning Mode with iterative prompting. |
| **Claude (claude.ai)** | Used to assist in writing and structuring this README report. |
| **ChatGPT** | Used for small, targeted questions about Kotlin syntax (e.g., backing fields, `generateSequence`) and for troubleshooting specific compiler/runtime errors during the Kotlin exercises. |

I confirm that I remain fully responsible for all content submitted in this repository, including AI-assisted sections. All generated code was reviewed, understood, and tested before inclusion.
