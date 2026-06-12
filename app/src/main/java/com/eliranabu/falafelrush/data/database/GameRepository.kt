package com.eliranabu.falafelrush.data.database

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val saveState: Flow<GameSaveState?> = gameDao.getSaveState()
    val reviews: Flow<List<CustomerReview>> = gameDao.getAllReviews()

    suspend fun getSaveStateOnce(): GameSaveState? {
        return gameDao.getSaveStateOnce()
    }

    suspend fun updateSaveState(state: GameSaveState) {
        gameDao.insertOrUpdateSaveState(state)
    }

    suspend fun insertReview(review: CustomerReview) {
        gameDao.insertReview(review)
    }

    suspend fun clearReviews() {
        gameDao.clearAllReviews()
    }

    suspend fun resetGame() {
        gameDao.resetGame()
    }
}
