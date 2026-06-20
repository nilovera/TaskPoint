package com.example.apk_mock.data.repository

import com.example.apk_mock.data.local.ReferenceDataSeeder
import com.example.apk_mock.data.local.dao.CategoriaDao
import com.example.apk_mock.data.mapper.toCategoriaDomainList
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.repository.CategoriaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class RoomCategoriaRepository(
    private val categoriaDao: CategoriaDao,
    private val referenceDataSeeder: ReferenceDataSeeder
) : CategoriaRepository {

    override fun getCategorias(): List<CategoriaTarea> {
        return runBlocking(Dispatchers.IO) {
            referenceDataSeeder.seedIfNeeded()
            categoriaDao.getCategorias().toCategoriaDomainList()
        }
    }
}
