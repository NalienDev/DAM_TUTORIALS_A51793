# 09 Feature Extensions

This document details the additional features and improvements implemented after the initial planning phase

## 1. Advanced Architecture & UI
### MVVM Pattern Enforcement
- **Description**: Ensure a clean separation of concerns where the View only observes data.
- **Implementation**: 
    - `DogViewModel` handles state using `LiveData`.
    - `DogRepository` abstracts the data source (API vs. Local).

### Loading Indicators
- **Description**: Visual feedback for the user during asynchronous operations.
- **Tasks**: 
    - Add a `ProgressBar` to the center of the Main Screen.
    - Visibility is controlled by the `isLoading` state in the ViewModel.

## 2. Favorites System (FIFO Queue)
### Logic Requirements
- **Constraint**: Maximum of 5 favorite items.
- **Behavior**: First-In, First-Out (FIFO). When a 6th favorite is added, the oldest one is automatically removed.
- **Access**: Favorites are persisted using Room/SharedPreferences and are accessible directly from the UI.

## 3. Smart Caching & Navigation
### Sliding Window Cache
- **Goal**: Maintain a cache of up to 50 items.
- **Logic**: During scrolling, the system ensures at least 10 items are pre-loaded ahead of the current position and 10 items are kept in memory behind the current position.
- **Implementation**: Managed within the `DogRepository` and `DogAdapter`.

## 4. Offline Access
- **Description**: The app must remain functional without an internet connection.
- **Behavior**: 
    - If the API call fails, the app automatically switches to displaying the 50 cached items.
    - Favorites must always be available offline.

## 5. Resilience & Error Handling
### Graceful Degradation
- **Description**: The app should not crash on API errors.
- **Implementation**: 
    - Use `try-catch` blocks in the Repository.
    - Display "No Internet" or "Server Error" messages via Toasts or SnackBar.
    - Loading indicators must be hidden if an error occurs.