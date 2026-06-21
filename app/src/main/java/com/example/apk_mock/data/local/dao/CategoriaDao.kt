package com.example.apk_mock.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.apk_mock.data.local.entity.CategoriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {
    @Query("SELECT * FROM categorias ORDER BY id ASC")
    fun observeCategorias(): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categorias ORDER BY id ASC")
    suspend fun getCategorias(): List<CategoriaEntity>

    @Query("SELECT * FROM categorias WHERE code = :code LIMIT 1")
    suspend fun getCategoriaByCode(code: String): CategoriaEntity?

    @Query("SELECT COUNT(*) FROM categorias")
    suspend fun countCategorias(): Int

    @Upsert
    suspend fun upsertCategorias(categorias: List<CategoriaEntity>)
}
