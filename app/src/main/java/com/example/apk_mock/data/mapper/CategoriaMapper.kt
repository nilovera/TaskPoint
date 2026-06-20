package com.example.apk_mock.data.mapper

import com.example.apk_mock.data.local.entity.CategoriaEntity
import com.example.apk_mock.domain.model.CategoriaTarea

fun CategoriaEntity.toDomain(): CategoriaTarea {
    return CategoriaTarea(
        id = id,
        name = name,
        code = code,
        description = description,
        activatesOffers = activatesOffers
    )
}

fun CategoriaTarea.toEntity(): CategoriaEntity {
    return CategoriaEntity(
        id = id,
        name = name,
        code = code,
        description = description,
        activatesOffers = activatesOffers
    )
}

fun List<CategoriaEntity>.toCategoriaDomainList(): List<CategoriaTarea> {
    return map { it.toDomain() }
}

fun List<CategoriaTarea>.toCategoriaEntityList(): List<CategoriaEntity> {
    return map { it.toEntity() }
}
