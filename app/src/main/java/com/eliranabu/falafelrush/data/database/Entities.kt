package com.eliranabu.falafelrush.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_save_state")
data class GameSaveState(
    @PrimaryKey val id: Int = 1,
    val currentDay: Int = 1,
    val totalCoins: Int = 50, // Starts with 50 coins
    val speedUpgradeLevel: Int = 1,      // Faster ingredients tapping/loading
    val priceUpgradeLevel: Int = 1,      // Better basic pricing
    val patienceUpgradeLevel: Int = 1,   // Customers wait longer
    val marketingUpgradeLevel: Int = 1,  // Generates higher tips
    val autoSauceUpgradeLevel: Int = 0,   // Auto-adds Tahini if true/level > 0
    val soundEffectsEnabled: Boolean = true,
    val hasSeenTutorial: Boolean = false
)

@Entity(tableName = "customer_reviews")
data class CustomerReview(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val customerEmoji: String,
    val rating: Int,
    val comment: String,
    val dayNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)
