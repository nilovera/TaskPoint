package com.example.apk_mock.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categorias",
    indices = [Index(value = ["code"], unique = true)]
)
data class CategoriaEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val code: String,
    val description: String,
    val activatesOffers: Boolean
)
