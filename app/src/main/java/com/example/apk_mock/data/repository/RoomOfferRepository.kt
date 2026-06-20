package com.example.apk_mock.data.repository

import com.example.apk_mock.data.local.OfferCatalogImporter
import com.example.apk_mock.data.local.dao.OfferDao
import com.example.apk_mock.data.mapper.toDomain
import com.example.apk_mock.domain.model.StoreOffer
import com.example.apk_mock.domain.repository.OfferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomOfferRepository(
    private val offerDao: OfferDao,
    private val offerCatalogImporter: OfferCatalogImporter
) : OfferRepository {

    override suspend fun getOffersByCategory(categoryCode: String): List<StoreOffer> {
        return withContext(Dispatchers.IO) {
            offerCatalogImporter.importIfNeeded()

            val storesById = offerDao.getStoresByCategory(categoryCode)
                .associateBy { it.id }
            val distanceSequence = listOf(50, 120, 180, 240, 320, 450)

            offerDao.getOffersByCategory(categoryCode)
                .mapNotNull { offerEntity ->
                    val storeEntity = storesById[offerEntity.storeId] ?: return@mapNotNull null
                    offerEntity.toDomain() to storeEntity.toDomain()
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
}
