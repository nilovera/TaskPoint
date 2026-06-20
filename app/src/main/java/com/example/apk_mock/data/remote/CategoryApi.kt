package com.example.apk_mock.data.remote

import com.example.apk_mock.data.remote.dto.CategoryDto
import retrofit2.http.GET

interface CategoryApi {
    @GET("categories")
    suspend fun getCategories(): List<CategoryDto>
}
