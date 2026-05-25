package com.example.apk_mock.data.repository

import android.content.Context
import com.example.apk_mock.data.source.JsonDataSource
import com.example.apk_mock.domain.model.StoreOffer
import com.example.apk_mock.domain.repository.OfferRepository

class JsonOfferRepository(context: Context) : OfferRepository {

    private val dataSource = JsonDataSource(context)
    private val stores = dataSource.loadStores()
    private val offers = dataSource.loadOffers()

    override fun getOffersByCategory(categoryCode: String): List<StoreOffer> {
        val storesById = stores.associateBy { it.id }
        val distanceSequence = listOf(50, 120, 180, 240, 320, 450)

        return offers
            .filter { it.categoryCode == categoryCode }
            .mapNotNull { offer ->
                val store = storesById[offer.storeId] ?: return@mapNotNull null
                if (store.categoryCode != categoryCode) return@mapNotNull null
                offer to store
            }
            .distinctBy { (_, store) -> store.id }
            .take(4)
            .mapIndexed { index, (offer, store) ->
                StoreOffer(
                    store = store,
                    offer = offer,
                    distanceMeters = distanceSequence.getOrElse(index) { 500 + index * 100 }
                )
            }
    }
}
