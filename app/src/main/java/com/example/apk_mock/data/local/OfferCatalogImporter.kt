package com.example.apk_mock.data.local

import android.content.Context
import androidx.room.withTransaction
import com.example.apk_mock.data.mapper.toCategoriaEntityList
import com.example.apk_mock.data.mapper.toOfferEntityList
import com.example.apk_mock.data.mapper.toStoreEntityList
import com.example.apk_mock.data.source.OfferCatalogDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Importa a Room el único contenido local incluido con la app: el catálogo de ofertas.
 */
class OfferCatalogImporter(
    private val context: Context,
    private val database: TaskPointDatabase
) {
    suspend fun importIfNeeded() {
        withContext(Dispatchers.IO) {
            val hasOfferCatalog = database.categoriaDao().countCategorias() > 0 &&
                database.offerDao().countStores() > 0 &&
                database.offerDao().countOffers() > 0

            if (hasOfferCatalog) return@withContext

            val dataSource = OfferCatalogDataSource(context)
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
