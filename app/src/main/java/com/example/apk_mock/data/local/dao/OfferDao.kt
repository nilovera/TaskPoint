package com.example.apk_mock.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.apk_mock.data.local.entity.OfferEntity
import com.example.apk_mock.data.local.entity.StoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfferDao {
    @Query("SELECT * FROM stores ORDER BY name ASC")
    fun observeStores(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM offers ORDER BY id ASC")
    fun observeOffers(): Flow<List<OfferEntity>>

    @Query("SELECT * FROM stores WHERE categoryCode = :categoryCode ORDER BY name ASC")
    suspend fun getStoresByCategory(categoryCode: String): List<StoreEntity>

    @Query("SELECT * FROM offers WHERE categoryCode = :categoryCode ORDER BY id ASC")
    suspend fun getOffersByCategory(categoryCode: String): List<OfferEntity>

    @Query("SELECT * FROM offers WHERE storeId = :storeId ORDER BY id ASC")
    suspend fun getOffersByStore(storeId: Int): List<OfferEntity>

    @Query("SELECT COUNT(*) FROM stores")
    suspend fun countStores(): Int

    @Query("SELECT COUNT(*) FROM offers")
    suspend fun countOffers(): Int

    @Upsert
    suspend fun upsertStores(stores: List<StoreEntity>)

    @Upsert
    suspend fun upsertOffers(offers: List<OfferEntity>)
}
