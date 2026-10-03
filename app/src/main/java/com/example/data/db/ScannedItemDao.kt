package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedItemDao {
    @Query("SELECT * FROM recycling_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ScannedItemEntity>>

    @Query("SELECT * FROM recycling_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ScannedItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ScannedItemEntity): Long

    @Query("DELETE FROM recycling_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recycling_history")
    suspend fun clearAll()
}
