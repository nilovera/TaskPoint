package com.example.apk_mock.data.repository

import com.example.apk_mock.data.local.OfferCatalogImporter
import com.example.apk_mock.data.local.dao.CategoriaDao
import com.example.apk_mock.data.mapper.toCategoriaDomainList
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.repository.CategoriaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomCategoriaRepository(
    private val categoriaDao: CategoriaDao,
    private val offerCatalogImporter: OfferCatalogImporter
) : CategoriaRepository {

    override suspend fun getCategorias(): List<CategoriaTarea> = withContext(Dispatchers.IO) {
        offerCatalogImporter.importIfNeeded()
        categoriaDao.getCategorias().toCategoriaDomainList()
    }
}
