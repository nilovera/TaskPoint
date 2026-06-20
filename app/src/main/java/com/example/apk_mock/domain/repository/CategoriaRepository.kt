package com.example.apk_mock.domain.repository

import com.example.apk_mock.domain.model.CategoriaTarea

interface CategoriaRepository {
    suspend fun getCategorias(): List<CategoriaTarea>
}
