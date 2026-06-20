package com.example.apk_mock.data.repository

import com.example.apk_mock.data.local.ReferenceDataSeeder
import com.example.apk_mock.data.local.dao.OfferDao
import com.example.apk_mock.data.mapper.toDomain
import com.example.apk_mock.domain.model.StoreOffer
import com.example.apk_mock.domain.repository.OfferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class RoomOfferRepository(
    private val offerDao: OfferDao,
    private val referenceDataSeeder: ReferenceDataSeeder
) : OfferRepository {

    override fun getOffersByCategory(categoryCode: String): List<StoreOffer> {
        return runBlocking(Dispatchers.IO) {
            referenceDataSeeder.seedIfNeeded()

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
