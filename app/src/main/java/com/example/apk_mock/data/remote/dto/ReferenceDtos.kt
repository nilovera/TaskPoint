package com.example.apk_mock.data.remote.dto

data class CategoryDto(
    val id: Int,
    val name: String,
    val code: String,
    val description: String,
    val activatesOffers: Boolean
)

data class StoreDto(
    val id: Int,
    val name: String,
    val categoryCode: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val logo: String
)

data class OfferDto(
    val id: Int,
    val storeId: Int,
    val categoryCode: String,
    val title: String,
    val description: String,
    val discount: Int,
    val validUntil: String
)
