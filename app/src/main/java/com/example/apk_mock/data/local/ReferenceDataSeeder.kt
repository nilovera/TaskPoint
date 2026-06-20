package com.example.apk_mock.data.local

import android.content.Context
import androidx.room.withTransaction
import com.example.apk_mock.data.mapper.toCategoriaEntityList
import com.example.apk_mock.data.mapper.toOfferEntityList
import com.example.apk_mock.data.mapper.toStoreEntityList
import com.example.apk_mock.data.source.JsonDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReferenceDataSeeder(
    private val context: Context,
    private val database: TaskPointDatabase
) {
    suspend fun seedIfNeeded() {
        withContext(Dispatchers.IO) {
            val hasReferenceData = database.categoriaDao().countCategorias() > 0 &&
                database.offerDao().countStores() > 0 &&
                database.offerDao().countOffers() > 0

            if (hasReferenceData) return@withContext

            val dataSource = JsonDataSource(context)
            val categorias = dataSource.loadCategorias().toCategoriaEntityList()
            val stores = dataSource.loadStores().toStoreEntityList()
            val offers = dataSource.loadOffers().toOfferEntityList()

            database.withTransaction {
                database.categoriaDao().upsertCategorias(categorias)
                database.offerDao().upsertStores(stores)
                database.offerDao().upsertOffers(offers)
            }
        }
    }
}
