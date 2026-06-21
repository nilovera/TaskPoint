package com.example.apk_mock.data.remote

import com.example.apk_mock.data.remote.dto.OfferDto
import com.example.apk_mock.data.remote.dto.StoreDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OfferApi {
    @GET("stores")
    suspend fun getStores(@Query("categoryCode") categoryCode: String? = null): List<StoreDto>

    @GET("offers")
    suspend fun getOffers(@Query("categoryCode") categoryCode: String? = null): List<OfferDto>
}
