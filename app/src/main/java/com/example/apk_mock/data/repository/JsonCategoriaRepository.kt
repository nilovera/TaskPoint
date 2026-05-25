package com.example.apk_mock.data.repository

import android.content.Context
import com.example.apk_mock.data.source.JsonDataSource
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.repository.CategoriaRepository

class JsonCategoriaRepository(context: Context) : CategoriaRepository {

    private val categorias: List<CategoriaTarea> = JsonDataSource(context).loadCategorias()

    override fun getCategorias(): List<CategoriaTarea> = categorias
}
