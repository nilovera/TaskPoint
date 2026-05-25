package com.example.apk_mock.domain.model

data class Store(
    val id: Int,
    val name: String,
    val categoryCode: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val logo: String
)

data class Offer(
    val id: Int,
    val storeId: Int,
    val categoryCode: String,
    val title: String,
    val description: String,
    val discount: Int,
    val validUntil: String
)

data class StoreOffer(
    val store: Store,
    val offer: Offer,
    val distanceMeters: Int
)
