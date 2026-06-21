package com.example.apk_mock.data.source

import android.content.Context
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.Offer
import com.example.apk_mock.domain.model.Store
import org.json.JSONArray

/**
 * Lee el catálogo local de ofertas. No contiene usuarios, tareas ni rutinas.
 */
class OfferCatalogDataSource(private val context: Context) {

    fun loadCategorias(): List<CategoriaTarea> {
        val json = readAsset("sandbox/categories.json")
        return JSONArray(json).toObjectList().map { row ->
            CategoriaTarea(
                id = row.getInt("id"),
                name = row.getString("name"),
                code = row.getString("code"),
                description = row.getString("description"),
                activatesOffers = row.optBoolean("activatesOffers", false)
            )
        }
    }

    fun loadStores(): List<Store> {
        val json = readAsset("sandbox/stores.json")
        return JSONArray(json).toObjectList().map { row ->
            Store(
                id = row.getInt("id"),
                name = row.getString("name"),
                categoryCode = row.getString("categoryCode"),
                address = row.getString("address"),
                latitude = row.getDouble("latitude"),
                longitude = row.getDouble("longitude"),
                logo = row.getString("logo")
            )
        }
    }

    fun loadOffers(): List<Offer> {
        val json = readAsset("sandbox/offers.json")
        return JSONArray(json).toObjectList().map { row ->
            Offer(
                id = row.getInt("id"),
                storeId = row.getInt("storeId"),
                categoryCode = row.getString("categoryCode"),
                title = row.getString("title"),
                description = row.getString("description"),
                discount = row.getInt("discount"),
                validUntil = row.getString("validUntil")
            )
        }
    }

    private fun readAsset(path: String): String {
        return context.assets.open(path).bufferedReader().use { it.readText() }
    }
}

private fun JSONArray.toObjectList() = List(length()) { index -> getJSONObject(index) }
