package com.example.apk_mock.domain.repository

import com.example.apk_mock.domain.model.StoreOffer

interface OfferRepository {
    suspend fun getOffersByCategory(categoryCode: String): List<StoreOffer>
}
