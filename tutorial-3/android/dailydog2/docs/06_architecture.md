# 06 Architecture: MVVM

The app follows the Model-View-ViewModel pattern to ensure separation of concerns.

## Layers:
1. **View (XML + Activity)**: Responsible for displaying data and capturing user input.
2. **ViewModel**: Holds UI state and communicates with the Repository. Uses LiveData/Flow.
3. **Repository**: The "Single Source of Truth." Decides whether to fetch from the API or the Local Cache.
4. **Data Source (API/Room)**: 
   - **Retrofit**: For network calls.
   - **SharedPreferences/Room**: For caching and favorites.

## Flow:
`UI -> ViewModel -> Repository -> Retrofit/Cache`