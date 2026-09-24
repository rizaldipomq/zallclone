package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CloneDao {
    @Query("SELECT * FROM clone_instances ORDER BY isFavorite DESC, lastLaunchedAt DESC, createdAt DESC")
    fun getAllClones(): Flow<List<CloneEntity>>

    @Query("SELECT * FROM clone_instances WHERE id = :id LIMIT 1")
    fun getCloneById(id: Long): Flow<CloneEntity?>

    @Query("SELECT * FROM clone_instances WHERE id = :id LIMIT 1")
    suspend fun getCloneByIdDirect(id: Long): CloneEntity?

    @Query("SELECT * FROM clone_instances WHERE packageName = :packageName ORDER BY cloneNumber ASC")
    fun getClonesByPackage(packageName: String): Flow<List<CloneEntity>>

    @Query("SELECT * FROM clone_instances WHERE packageName = :packageName ORDER BY cloneNumber ASC")
    suspend fun getClonesByPackageDirect(packageName: String): List<CloneEntity>

    @Query("SELECT COUNT(*) FROM clone_instances")
    fun getCloneCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM clone_instances WHERE packageName = :packageName")
    suspend fun getCloneCountForPackage(packageName: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClone(clone: CloneEntity): Long

    @Update
    suspend fun updateClone(clone: CloneEntity)

    @Delete
    suspend fun deleteClone(clone: CloneEntity)

    @Query("DELETE FROM clone_instances WHERE id = :id")
    suspend fun deleteCloneById(id: Long)

    @Query("UPDATE clone_instances SET lastLaunchedAt = :timestamp, launchCount = launchCount + 1 WHERE id = :id")
    suspend fun markLaunched(id: Long, timestamp: Long)

    @Query("UPDATE clone_instances SET storageBytes = :bytes WHERE id = :id")
    suspend fun updateStorageBytes(id: Long, bytes: Long)
}
