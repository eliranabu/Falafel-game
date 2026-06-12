package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM game_save_state WHERE id = 1 LIMIT 1")
    fun getSaveState(): Flow<GameSaveState?>

    @Query("SELECT * FROM game_save_state WHERE id = 1 LIMIT 1")
    suspend fun getSaveStateOnce(): GameSaveState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSaveState(state: GameSaveState)

    @Query("SELECT * FROM customer_reviews ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<CustomerReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: CustomerReview)

    @Query("DELETE FROM customer_reviews")
    suspend fun clearAllReviews()

    @Transaction
    suspend fun resetGame() {
        insertOrUpdateSaveState(GameSaveState(id = 1))
        clearAllReviews()
    }
}
