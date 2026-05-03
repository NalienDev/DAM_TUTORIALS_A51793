# 04 Data Model

## DogImage (API Response)
- `message`: String (The URL of the image)
- `status`: String (Success/Error indicator)

## DogItem (UI/Domain Model)
- `id`: String (Unique hash or the URL itself)
- `imageUrl`: String
- `breedName`: String (Parsed from the URL)
- `isFavorite`: Boolean