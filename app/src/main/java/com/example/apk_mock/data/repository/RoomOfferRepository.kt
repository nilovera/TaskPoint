package com.example.apk_mock.data.repository

import com.example.apk_mock.data.local.OfferCatalogImporter
import com.example.apk_mock.data.local.dao.OfferDao
import com.example.apk_mock.data.geocoding.distanceMeters
import com.example.apk_mock.data.mapper.toDomain
import com.example.apk_mock.domain.model.StoreOffer
import com.example.apk_mock.domain.repository.OfferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomOfferRepository(
    private val offerDao: OfferDao,
    private val offerCatalogImporter: OfferCatalogImporter
) : OfferRepository {

    override suspend fun getOffersByCategory(
        categoryCode: String,
        originLatitude: Double,
        originLongitude: Double
    ): List<StoreOffer> {
        return withContext(Dispatchers.IO) {
            offerCatalogImporter.importIfNeeded()

            val storesById = offerDao.getStoresByCategory(categoryCode)
                .associateBy { it.id }

            offerDao.getOffersByCategory(categoryCode)
                .mapNotNull { offerEntity ->
                    val storeEntity = storesById[offerEntity.storeId] ?: return@mapNotNull null
                    offerEntity.toDomain() to storeEntity.toDomain()
                }
                .distinctBy { (_, store) -> store.id }
                .map { (offer, store) ->
                    StoreOffer(
                        store = store,
                        offer = offer,
                        distanceMeters = distanceMeters(
                            originLatitude = originLatitude,
                            originLongitude = originLongitude,
                            destinationLatitude = store.latitude,
                            destinationLongitude = store.longitude
                        )
                    )
                }
                .sortedBy { offer -> offer.distanceMeters }
                .take(4)
        }
    }
}
