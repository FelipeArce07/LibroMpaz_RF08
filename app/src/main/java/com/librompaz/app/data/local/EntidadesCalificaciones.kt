package com.librompaz.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "estudiantes")
data class EstudianteEntity(
    @PrimaryKey val id: String,
    val nombreFicticio: String,
    val cursoId: String
)

@Entity(tableName = "evaluaciones",
    indices = [Index(value = ["cursoId", "asignaturaId", "nombreInstrumento", "fecha"], unique = true)])
data class EvaluacionEntity(
    @PrimaryKey val id: String,
    val cursoId: String,
    val asignaturaId: String,
    val nombreInstrumento: String,
    val fecha: String
)

@Entity(tableName = "calificaciones",
    foreignKeys = [
        ForeignKey(entity = EvaluacionEntity::class, parentColumns = ["id"], childColumns = ["evaluacionId"]),
        ForeignKey(entity = EstudianteEntity::class, parentColumns = ["id"], childColumns = ["estudianteId"])
    ],
    indices = [Index(value = ["evaluacionId", "estudianteId"], unique = true), Index("estudianteId")])
data class CalificacionEntity(
    @PrimaryKey val id: String,
    val evaluacionId: String,
    val estudianteId: String,
    val nota: Double,
    val registradoPor: String
)

@Entity(tableName = "auditoria")
data class AuditoriaEntity(
    @PrimaryKey val id: String,
    val usuario: String,
    val perfil: String,
    val accion: String,
    val detalle: String,
    val fechaHora: String
)
