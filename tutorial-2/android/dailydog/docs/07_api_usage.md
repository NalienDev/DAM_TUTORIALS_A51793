# 07 API Usage

## Provider
**Dog CEO API**: [https://dog.ceo/dog-api/](https://dog.ceo/dog-api/)

## Endpoints Used
- **Get Random Images**: `GET https://dog.ceo/api/breeds/image/random/20`
  - Returns an array of 20 random dog images.

## Example JSON Response
```json
{
    "message": [
        "[https://images.dog.ceo/breeds/beagle/n02088094_1003.jpg](https://images.dog.ceo/breeds/beagle/n02088094_1003.jpg)",
        "[https://images.dog.ceo/breeds/poodle-standard/n02113799_2280.jpg](https://images.dog.ceo/breeds/poodle-standard/n02113799_2280.jpg)"
    ],
    "status": "success"
}