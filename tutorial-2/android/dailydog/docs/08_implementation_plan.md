# 08 Implementation Plan

## Phase 1: Project Setup & Core Data
- **Step 1**: Initialize Android project. Add dependencies (Retrofit, Glide/Coil for images, ViewModel, LiveData, Room for caching).
- **Step 2**: Create `DogItem` data class and the API Response wrapper.
- **Step 3**: Implement `DogApiService` interface and a `RetrofitInstance` provider.

## Phase 2: UI & Basic Architecture
- **Step 4**: Create `activity_main.xml` with `RecyclerView` and `SwipeRefreshLayout`.
- **Step 5**: Create `item_dog.xml` (CardView with ImageView and TextView for Breed).
- **Step 6**: Implement `DogAdapter` for the RecyclerView.
- **Step 7**: Create `DogViewModel` and `DogRepository` to handle basic API fetching and LiveData updates.

## Phase 3: Detail Screen & Favorites
- **Step 8**: Create `DetailsActivity` and `activity_details.xml` to show full-screen dog info.
- **Step 9**: Implement Favorite logic: Create a `LocalDatabase` (Room) to store up to 5 favorites (FIFO logic).
- **Step 10**: Add a "Favorite" toggle in the Detail screen and update the UI across the app.

## Phase 4: Advanced Caching & Offline
- **Step 11**: Implement Cache logic in `DogRepository`: Keep 50 items in local storage.
- **Step 12**: Implement "Offline Mode" check: If network fails, load from local cache.
- **Step 13**: Final UI polish: Add Loading Indicators (ProgressBar) and handle API error messages gracefully.

## Phase 5: Deployment
- **Step 14**: Deploy the app to Pixel 9 Pro.