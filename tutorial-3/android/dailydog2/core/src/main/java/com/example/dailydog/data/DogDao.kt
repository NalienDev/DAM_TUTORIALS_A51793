package com.example.dailydog.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface DogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDog(dog: DogItem)

    @Query("SELECT * FROM dogs WHERE isFavorite = 1 ORDER BY timestamp ASC")
    suspend fun getFavorites(): List<DogItem>

    @Query("SELECT COUNT(*) FROM dogs WHERE isFavorite = 1")
    suspend fun getFavoriteCount(): Int

    @Query("DELETE FROM dogs WHERE imageUrl IN (SELECT imageUrl FROM dogs WHERE isFavorite = 1 ORDER BY timestamp ASC LIMIT 1)")
    suspend fun deleteOldestFavorite()

    @Query("SELECT * FROM dogs WHERE imageUrl = :url LIMIT 1")
    suspend fun getDogByUrl(url: String): DogItem?
    
    @Query("UPDATE dogs SET isFavorite = 0 WHERE imageUrl = :url")
    suspend fun removeFavorite(url: String)

    @Transaction
    suspend fun addFavoriteFIFO(dog: DogItem) {
        val count = getFavoriteCount()
        if (count >= 5) {
            deleteOldestFavorite()
        }
        insertDog(dog.copy(isFavorite = true, timestamp = System.currentTimeMillis()))
    }
    
    @Query("SELECT * FROM dogs WHERE isFavorite = 0 ORDER BY timestamp DESC LIMIT 50")
    suspend fun getCacheDogs(): List<DogItem>

    @Query("SELECT COUNT(*) FROM dogs WHERE isFavorite = 0")
    suspend fun getCacheCount(): Int

    @Query("DELETE FROM dogs WHERE imageUrl IN (SELECT imageUrl FROM dogs WHERE isFavorite = 0 ORDER BY timestamp ASC LIMIT :amount)")
    suspend fun deleteOldestCache(amount: Int)

    @Transaction
    suspend fun insertCacheDogs(dogs: List<DogItem>) {
        dogs.forEach { 
            val existing = getDogByUrl(it.imageUrl)
            if (existing?.isFavorite != true) {
                insertDog(it) 
            }
        }
        val excess = getCacheCount() - 50
        if (excess > 0) {
            deleteOldestCache(excess)
        }
    }
}
