package com.example.apk_mock.domain.model

data class Tarea(
    val id: String,
    val titulo: String,
    val categoria: CategoriaTarea,
    val rutinaId: String?,
    val rutinaNombre: String?,
    val dias: List<DiaSemana>,
    val horario: String?,
    val notas: String,
    val photoPath: String? = null,
    val completada: Boolean = false,
    val requiereRevisionHorario: Boolean = false
)

val Tarea.diasOrdenados: List<DiaSemana>
    get() = dias.distinct().sortedBy { it.ordinal }

fun Tarea.ocurreEn(dia: DiaSemana): Boolean = dia in dias

fun Tarea.perteneceARutina(rutina: Rutina): Boolean {
    val assignedRutinaId = rutinaId?.takeIf { it.isNotBlank() }
    return if (assignedRutinaId != null) {
        assignedRutinaId == rutina.id
    } else {
        rutinaNombre?.takeIf { it.isNotBlank() } == rutina.nombre
    }
}

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
