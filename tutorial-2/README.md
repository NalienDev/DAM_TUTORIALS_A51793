# Assignment 2 — Kotlin Exercises, WeatherApp, and Assisted Code Generation

Course: Desenvolvimento de Aplicações Móveis (DAM)
Student: Lucas Filipe
Date: April 12, 2026
Repository URL: https://github.com/NalienDev/DAM_TUTORIALS_A51793

---

## 1. Introduction

This assignment builds on the foundations of Kotlin and Android development established in Assignment 1. The goal is to deepen the understanding of advanced Kotlin features — such as generics, sealed classes, higher-order functions, and operator overloading — and to translate these skills into two robust, distinct native Android applications.

The work is divided into three main components:
- A **Kotlin console application** built with Maven in IntelliJ IDEA, focusing on advanced concepts (Generics, Sealed Classes, Functional Pipelines, Operator Overloading).
- An **Android application** named **CoolWeatherApp**, a weather-tracking app implementing direct API fetching and system location services.
- An **Android application** named **Daily Doggo** (Assisted Code Generation), which uses AI agents to rapidly produce an app employing modern libraries like Retrofit, Room, and Coroutines.

The assignment also continues the exploration of agentic programming, comparing how AI tools were used during the creation of the Daily Doggo application versus traditional development in CoolWeatherApp.

---

## 2. System Overview

The repository contains two major components separated into sub-directories:

**Kotlin Console Application (`/kotlin`):** A Maven-configured Kotlin project with four dedicated exercises:
- `Cache.kt`: Implementation of a generic caching mechanism supporting insertion, eviction, fallback generation, and transformation.
- `Event.kt`: A system modeling discrete events (Login, Purchase, Logout) using sealed classes and functional extensions to filter and process user activity.
- `Pipeline.kt`: A functional pipeline simulation letting users dynamically string together data transformation operations.
- `Vec2.kt`: A representation of 2D mathematical vectors, utilizing operator overloading and data classes for intuitive geometric math.

**Android Applications (`/android`):**
- **CoolWeatherApp (`coolweatherapp`):** Fetches climate data from the Open-Meteo REST API, resolving GPS location dynamically, and visually adjusting the theme between day and night logic.
- **Daily Doggo (`dailydog`):** A generated app displaying a grid of random dog pictures fetching from an external API using Retrofit, supporting "Favorite" features with persistent offline caching via a Room database.

---

## 3. Architecture and Design

### Kotlin Component

The Kotlin project utilizes modern language paradigms:

- **Generics**: `Cache` implements generic bounds (`<K: Any, V: Any>`) ensuring type safety and flexibility across any data type pairs.
- **Sealed Classes**: `Event` uses sealed classes to strictly bound inheritance, explicitly outlining state types (`Login`, `Purchase`, `Logout`), enforcing exhaustive `when` expressions.
- **Functional & Higher-Order Functions**: `Pipeline` thrives on passing functions as variables (`(List<String>) -> List<String>`), and includes custom infix operator composition (`andThen`).
- **Operator Overloading**: `Vec2` directly intercepts basic math operations (`+`, `-`, `*`) by overloading `plus`, `minus`, and `times`, abstracting away complex object invocations.

### Android Component — Apps

#### CoolWeatherApp
- Follows the **MVVM (Model-View-ViewModel)** architecture pattern dynamically decoupled from complex libraries.
- The **ViewModel Layer (`WeatherViewModel`)** handles raw network streams (`URL.openStream()`) dynamically parsing JSON strings to local Data Classes via Gson on `Dispatchers.IO`.
- Direct consumption of Android System APIs, most notably the `LocationManager` to extract the host device's active GPS coordinate if permissions are granted.

#### Daily Doggo (Assisted Code Generation)
- Embraces Android's strongly recommended modern ecosystem within MVVM.
- **Networking**: `RetrofitInstance` paired with `DogApiService` completely abstracts HTTP requests compared to the native networking of `CoolWeatherApp`.
- **Persistence Layer**: Incorporates a localized SQL wrapper (`DogDatabase` and `DogDao`) utilizing **Room** to warehouse user metadata ("favorites") permanently for offline retrieval.
- Exposes UI states back to the activity reacting natively through LiveData to SwipeRefreshLayouts.

---

## 4. Implementation

### Kotlin Exercises

**`Cache.kt` — Generics and Lambdas**
Implements a reusable cache dictionary (`mutableMapOf<K, V>`). Highlight functions include:
- `getOrPut(key, default)`: Checks for existence; if missing, invokes a trailing lambda (`default: () -> V`) to lazily generate the missing value.
- `transform(key, action)`: Modifies existing values by passing the old value into an operative block, seamlessly resolving mapping alterations.

**`Event.kt` — Sealed Class Pattern Matching**
Models a rigid hierarchy under `sealed class Event`:
- Contains `Login`, `Purchase`, and `Logout` representations housing user and timestamp.
- Custom extension function `filterIsInstance<Event.Purchase>()` cleanly strips irrelevant events making calculations like `totalSpent()` inherently safe.

**`Pipeline.kt` — Dynamic Execution Chains**
Allows composing lists of behaviors:
- A `MutableList` of `Pair<String, (List<String>) -> List<String>>` keeps track of pipeline stages.
- `compose` method lets two stages merge using an extension function `andThen` combining lambda bodies sequentially.
- `fork` allows copying input data traversing two separate branches simultaneously.

**`Vec2.kt` — Data Mathematics**
Automates vector physics:
- Utilizes operator overloading (`operator fun times()`) to provide commutative multiplier behaviors (e.g., both `vector * 2.0` and `2.0 * vector`).
- Implements `Comparable` resolving conditional queries (`<`, `>`) structurally dependent on geometric magnitudes (via Pythagorean theorem logic).

### Android — CoolWeatherApp

- `MainActivity.kt` checks permissions via `ActivityResultContracts.RequestPermission()`. It manipulates the interface, reading coordinates from EditText blocks and passing them to `WeatherViewModel`. Contains dynamic theming determining whether to show Day-oriented or Night-oriented graphics based on calculations provided by the ViewModel against sunrise/set hours.
- `WeatherViewModel.kt` operates without Retrofit. It constructs a raw `https://api.open-meteo.com/v1/forecast?...` URI and extracts JSON directly into models via `Gson().fromJson`.

### Android — Daily Doggo

- Represents standard modern-day Android scaffolding. A `DogRepository` handles data traffic direction—choosing to pull directly from `DogDao` (Room database queries) for favorites or pinging `RetrofitInstance` for generalized feeds.
- `DogViewModel.kt` controls loading indicators, broadcasting the resulting LiveData array directly to `MainActivity`, efficiently rebuilding visual grids. Note that interactions are deeply abstracted against `try/catch` and lifecycle configurations seamlessly resolving Coroutines scope handling.

---

## 5. Testing and Validation

Testing was primarily done manually through the Android Emulator and physical device testing.

**Kotlin exercises** were validated by asserting outputs matching functional expectations.
- Simulated pipeline filtering, checking for sequence breakage.
- Vector operator checks against mathematically provable baseline values.

**CoolWeatherApp Tested Scenarios:**
- Location permission denial fallbacks routing to default Lisbon coordinates.
- Dynamic Theme transition: validated manually through internal time-index spoofing forcing the application into daytime or nighttime UI scopes.

**Daily Doggo Tested Scenarios:**
- Connectivity validation: Testing empty UI states under offline "Airplane Mode" constraints against Room Cache.
- Refresh gestures utilizing SwipeRefreshLayout triggers accurately rebuilding grids.

---

## 6. Usage Instructions

### Android Applications
1. Open **Android Studio**.
2. Select `File > Open` and navigate to either `tutorial-2/android/coolweatherapp` or `tutorial-2/android/dailydog`.
3. Wait for **Gradle Sync** to finish indexing components.
4. Select emulator/device and execute using **Run (Shift + F10)**.

### Kotlin Console Application
1. Open **IntelliJ IDEA**.
2. Select `Open Project` and choose `tutorial-2/kotlin/pom.xml`.
3. Locate into `/dam/src/main/kotlin/` and run any of the specific exercise files via the green play button next to the `main()` function block.

---

# Autonomous Software Engineering Sections

## 7. Prompting Strategy

AI assistance (Antigravity and ChatGPT) was utilized strategically during the generation of the **Daily Doggo** application.

For Daily Doggo, the overall generation workflow involved defining: **Objective + Architecture Preference (Room/Retrofit) + View Structure**.
Example constraint prompt:
> *"Create a dog gallery Android app named Daily Doggo using MVVM. Include a RecyclerView grid, fetch remote APIs utilizing Retrofit, but ensure users can offline cache 'Favorite' distinct dogs locally involving Room. Incorporate pull-to-refresh mechanisms."*

Subsequent follow-ups primarily served as alignment iterations — identifying missing UI markers or smoothing data flow constraints during grid loading delays.

For the **Kotlin Exercises** and **CoolWeatherApp**, logic sequences, UI, and mathematics logic were human-authored to solidify foundational programmatic understanding.

---

## 8. Autonomous Agent Workflow

For Daily Doggo, the AI managed scaffolding natively:
- **Environment Bootstrapping**: Handled Room entity schemas and Retrofit interface scaffolding dynamically.
- **Integration**: Bound UI variables structurally, passing intent parameters successfully into a `DetailsActivity`.
- **Optimization**: Human reviews caught UI lifecycle inefficiencies, prompting the agent to redefine the ViewModel's Coroutines management for better context cancellation.

---

## 9. Verification of AI-Generated Artifacts

Verification was strictly manual code-review loops combined with linter analysis:
- **Room Dao validation**: Ensured database schema versions were structurally sound. Hand-verified the local `DogItem` data mappings against Retrofit `@SerializedName` constraints preventing crashing.
- **Coroutines Scrutiny**: Verified `Dispatchers.IO` isolation stopping main-thread blockages, especially around database querying behaviors.

---

## 10. Human vs AI Contribution

| Component | Primary Author |
|---|---|
| Kotlin Exercise: `Cache.kt` | Human |
| Kotlin Exercise: `Event.kt` | Human |
| Kotlin Exercise: `Pipeline.kt` | Human |
| Kotlin Exercise: `Vec2.kt` | Human |
| Android: CoolWeatherApp | Human |
| Android: Daily Doggo — Initial build | AI (Antigravity) |
| Android: Daily Doggo — Debugging & refinement| Human + AI |
| Documentation | Human (+ AI review context) |

---

## 11. Ethical and Responsible Use

- **Understanding First**: AI was used to offload boilerplate instantiation (Room, Retrofit) on Daily Doggo to test Code Gen efficiency, but all generated underlying SQL directives and networking scopes were fully vetted to prevent hallucinated architecture structures.
- **Independence**: The Kotlin array exercises and complex native interactions (LocationManager) in `CoolWeatherApp` were kept entirely isolated from generation mechanisms securing raw foundational learning scopes. All projects assert explicit ownership and responsibility.

---

# Development Process

## 12. Version Control and Commit History

The project was managed using a local Git repository initialised in IntelliJ IDEA, with commits made progressively throughout development. Each exercise and major feature was committed separately to reflect the progression of work. Commit messages follow a descriptive convention.

For the Daily Doggo project, the repository was initialised via Antigravity's Source Control panel, with commits made at each significant milestone (initial scaffold, Room database integration, Retrofit debugging).

## 13. Difficulties and Lessons Learned

- **Functional Data Pipelines**: Implementing the data pipeline (`Pipeline.kt`) utilizing advanced higher-order functions and functional paradigms proved syntactically challenging. Transitioning from standard iterative loops to chaining dynamic operations required a conceptual shift in visualizing data transformations.
- **Dynamic Theming and State in WeatherApp**: In the `CoolWeatherApp`, one of the most prominent hurdles was establishing the logic that dynamically changes the application's background based on the current time of day. Compounding this difficulty was ensuring that as the user queries weather across distinct global locations via latitude and longitude coordinates, the background correctly updates to reflect the accurate local time and ambient conditions.
- **Build Configurations and Tooling**: Launching and compiling the `Daily Doggo` application directly through the IDE initially surfaced multiple frustrations due to complicated Gradle synchronization issues and dependency conflicts, which required thorough debugging to achieve environment stability.

## 14. Future Improvements

- **Enhanced Interface Readability**: Overhauling the layout and typography of the `CoolWeatherApp` to provide greatly improved readability, ensuring atmospheric data is quickly and intuitively digestible at a glance.
- **Interactive Location Maps**: Replacing the raw latitude and longitude text input approach with an integrated, interactive map interface in the `CoolWeatherApp`, allowing users to simply pinpoint and select global locations visually.

## 15. AI Usage Disclosure

| Tool | How it was used |
|---|---|
| **Antigravity** | Bootstrapped and structured the Daily Doggo project via prompted architecture logic involving Retrofit and Room constraints. |
| **Claude / ChatGPT** | Utilized for addressing small, targeted questions regarding nuanced Kotlin syntax, and provided debugging assistance when resolving complex theme and XML styling issues within Android Studio. |

I confirm responsibility for the artifact contents produced utilizing generative tools where listed, fully understanding all codebase logics utilized structurally.
