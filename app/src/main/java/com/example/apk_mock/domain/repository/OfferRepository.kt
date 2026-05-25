package com.example.apk_mock.domain.repository

import com.example.apk_mock.domain.model.StoreOffer

interface OfferRepository {
    fun getOffersByCategory(categoryCode: String): List<StoreOffer>
}
