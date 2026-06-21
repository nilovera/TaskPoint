package com.example.apk_mock.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.apk_mock.data.local.SyncStatus
import com.example.apk_mock.data.local.entity.RutinaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RutinaDao {
    @Query("SELECT * FROM rutinas WHERE userId = :userId ORDER BY nombre COLLATE NOCASE ASC")
    fun observeRutinas(userId: String): Flow<List<RutinaEntity>>

    @Query("SELECT * FROM rutinas WHERE userId = :userId ORDER BY nombre COLLATE NOCASE ASC")
    suspend fun getRutinas(userId: String): List<RutinaEntity>

    @Query("SELECT * FROM rutinas WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getRutinaById(id: String, userId: String): RutinaEntity?

    @Query("SELECT * FROM rutinas WHERE userId = :userId AND syncStatus != :syncedStatus")
    suspend fun getPendingRutinas(
        userId: String,
        syncedStatus: SyncStatus = SyncStatus.SYNCED
    ): List<RutinaEntity>

    @Upsert
    suspend fun upsertRutina(rutina: RutinaEntity)

    @Upsert
    suspend fun upsertRutinas(rutinas: List<RutinaEntity>)

    @Query("UPDATE rutinas SET syncStatus = :syncStatus WHERE id = :id AND userId = :userId")
    suspend fun updateSyncStatus(
        id: String,
        userId: String,
        syncStatus: SyncStatus
    ): Int

    @Query("DELETE FROM rutinas WHERE id = :id AND userId = :userId")
    suspend fun deleteRutina(id: String, userId: String): Int

    @Query("DELETE FROM rutinas WHERE userId = :userId")
    suspend fun deleteRutinasByUser(userId: String): Int
}
