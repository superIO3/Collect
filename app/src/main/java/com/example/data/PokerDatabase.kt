package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "hand_history")
data class HandHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val heroCardsStr: String, // e.g. "As Kh"
    val boardCardsStr: String, // e.g. "Ad Tc 4s"
    val street: String,
    val position: String,
    val potSize: Float,
    val currentBet: Float,
    val recommendedAction: String,
    val recommendedSizing: String,
    val heroEquity: Float,
    val strategicConcept: String,
    val explanation: String,
    val userActionTaken: String = "",
    val wasGtoCompliant: Boolean = true,
    val notes: String = ""
)

@Dao
interface HandDao {
    @Query("SELECT * FROM hand_history ORDER BY timestamp DESC")
    fun getAllHands(): Flow<List<HandHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHand(hand: HandHistoryEntity): Long

    @Query("DELETE FROM hand_history WHERE id = :id")
    suspend fun deleteHand(id: Long)

    @Query("DELETE FROM hand_history")
    suspend fun clearAll()
}

@Database(entities = [HandHistoryEntity::class], version = 1, exportSchema = false)
abstract class PokerDatabase : RoomDatabase() {
    abstract fun handDao(): HandDao
}
