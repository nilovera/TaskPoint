package com.example.apk_mock.domain.model

data class Tarea(
    val id: String,
    val titulo: String,
    val categoria: CategoriaTarea,
    val rutinaId: String?,
    val rutinaNombre: String?,
    val dia: DiaSemana?,
    val horario: String?,
    val notas: String,
    val photoPath: String? = null,
    val completada: Boolean = false
)

data class CategoriaTarea(
    val id: Int,
    val name: String,
    val code: String,
    val description: String,
    val activatesOffers: Boolean
) {
    val label: String
        get() = name.uppercase()
}
