package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ManuscriptDao {
    @Query("SELECT * FROM manuscripts ORDER BY updatedAtMillis DESC")
    fun observeAll(): Flow<List<ManuscriptEntity>>

    @Query("SELECT COUNT(*) FROM manuscripts")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ManuscriptEntity): Long

    @Query("DELETE FROM manuscripts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
