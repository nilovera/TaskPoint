package com.example.apk_mock.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.apk_mock.data.local.SyncStatus

@Entity(
    tableName = "tareas",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["rutinaId"]),
        Index(value = ["categoriaCode"]),
        Index(value = ["syncStatus"])
    ]
)
data class TareaEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val titulo: String,
    val categoriaId: Int,
    val categoriaName: String,
    val categoriaCode: String,
    val categoriaDescription: String,
    val categoriaActivatesOffers: Boolean,
    val rutinaId: String?,
    val rutinaNombre: String?,
    val dia: String?,
    val horario: String?,
    val notas: String,
    val photoPath: String?,
    val completada: Boolean,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val updatedAt: Long = System.currentTimeMillis()
)
