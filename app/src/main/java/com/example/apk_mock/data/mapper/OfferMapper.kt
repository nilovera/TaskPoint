package com.example.apk_mock.data.mapper

import com.example.apk_mock.data.local.entity.OfferEntity
import com.example.apk_mock.data.local.entity.StoreEntity
import com.example.apk_mock.domain.model.Offer
import com.example.apk_mock.domain.model.Store

fun StoreEntity.toDomain(): Store {
    return Store(
        id = id,
        name = name,
        categoryCode = categoryCode,
        address = address,
        latitude = latitude,
        longitude = longitude,
        logo = logo
    )
}

fun Store.toEntity(): StoreEntity {
    return StoreEntity(
        id = id,
        name = name,
        categoryCode = categoryCode,
        address = address,
        latitude = latitude,
        longitude = longitude,
        logo = logo
    )
}

fun OfferEntity.toDomain(): Offer {
    return Offer(
        id = id,
        storeId = storeId,
        categoryCode = categoryCode,
        title = title,
        description = description,
        discount = discount,
        validUntil = validUntil
    )
}

fun Offer.toEntity(): OfferEntity {
    return OfferEntity(
        id = id,
        storeId = storeId,
        categoryCode = categoryCode,
        title = title,
        description = description,
        discount = discount,
        validUntil = validUntil
    )
}

fun List<StoreEntity>.toStoreDomainList(): List<Store> {
    return map { it.toDomain() }
}

fun List<OfferEntity>.toOfferDomainList(): List<Offer> {
    return map { it.toDomain() }
}

fun List<Store>.toStoreEntityList(): List<StoreEntity> {
    return map { it.toEntity() }
}

fun List<Offer>.toOfferEntityList(): List<OfferEntity> {
    return map { it.toEntity() }
}
