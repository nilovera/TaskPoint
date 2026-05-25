package com.example.apk_mock.domain.repository

import com.example.apk_mock.domain.model.CategoriaTarea

interface CategoriaRepository {
    fun getCategorias(): List<CategoriaTarea>
}
