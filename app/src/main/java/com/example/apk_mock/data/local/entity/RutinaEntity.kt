package com.example.apk_mock.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.apk_mock.data.local.SyncStatus

@Entity(
    tableName = "rutinas",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["syncStatus"])
    ]
)
data class RutinaEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val nombre: String,
    val icono: String,
    val direccion: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val diasSemana: String,
    val horarioInicio: String,
    val horarioFin: String,
    val descripcion: String,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val updatedAt: Long = System.currentTimeMillis()
)
