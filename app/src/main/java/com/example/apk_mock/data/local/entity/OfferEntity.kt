package com.example.apk_mock.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "offers",
    indices = [
        Index(value = ["storeId"]),
        Index(value = ["categoryCode"])
    ]
)
data class OfferEntity(
    @PrimaryKey val id: Int,
    val storeId: Int,
    val categoryCode: String,
    val title: String,
    val description: String,
    val discount: Int,
    val validUntil: String
)
