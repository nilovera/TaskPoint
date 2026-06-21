package com.example.apk_mock.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stores",
    indices = [Index(value = ["categoryCode"])]
)
data class StoreEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val categoryCode: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val logo: String
)
