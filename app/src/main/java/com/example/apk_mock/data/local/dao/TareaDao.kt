package com.example.apk_mock.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.apk_mock.data.local.SyncStatus
import com.example.apk_mock.data.local.entity.TareaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {
    @Query("SELECT * FROM tareas WHERE userId = :userId ORDER BY dia ASC, horario ASC, titulo COLLATE NOCASE ASC")
    fun observeTareas(userId: String): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE userId = :userId ORDER BY dia ASC, horario ASC, titulo COLLATE NOCASE ASC")
    suspend fun getTareas(userId: String): List<TareaEntity>

    @Query("SELECT * FROM tareas WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getTareaById(id: String, userId: String): TareaEntity?

    @Query("SELECT * FROM tareas WHERE rutinaId = :rutinaId AND userId = :userId ORDER BY dia ASC, horario ASC, titulo COLLATE NOCASE ASC")
    suspend fun getTareasByRutina(rutinaId: String, userId: String): List<TareaEntity>

    @Query("SELECT COUNT(*) FROM tareas WHERE rutinaId = :rutinaId AND userId = :userId")
    suspend fun countTareasByRutina(rutinaId: String, userId: String): Int

    @Query("SELECT * FROM tareas WHERE userId = :userId AND syncStatus != :syncedStatus")
    suspend fun getPendingTareas(
        userId: String,
        syncedStatus: SyncStatus = SyncStatus.SYNCED
    ): List<TareaEntity>

    @Upsert
    suspend fun upsertTarea(tarea: TareaEntity)

    @Upsert
    suspend fun upsertTareas(tareas: List<TareaEntity>)

    @Query("UPDATE tareas SET rutinaNombre = :nuevoNombre, syncStatus = :syncStatus, updatedAt = :updatedAt WHERE rutinaId = :rutinaId AND userId = :userId")
    suspend fun updateRutinaNombre(
        rutinaId: String,
        userId: String,
        nuevoNombre: String,
        syncStatus: SyncStatus = SyncStatus.PENDING_UPDATE,
        updatedAt: Long = System.currentTimeMillis()
    ): Int

    @Query("DELETE FROM tareas WHERE id = :id AND userId = :userId")
    suspend fun deleteTarea(id: String, userId: String): Int

    @Query("DELETE FROM tareas WHERE rutinaId = :rutinaId AND userId = :userId")
    suspend fun deleteTareasByRutina(rutinaId: String, userId: String): Int

    @Query("DELETE FROM tareas WHERE userId = :userId")
    suspend fun deleteTareasByUser(userId: String): Int
}
