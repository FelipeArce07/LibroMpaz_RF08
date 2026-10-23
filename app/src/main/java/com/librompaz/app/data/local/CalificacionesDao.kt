package com.librompaz.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CalificacionesDao {
    @Query("SELECT * FROM evaluaciones ORDER BY fecha DESC, nombreInstrumento")
    fun observarEvaluaciones(): Flow<List<EvaluacionEntity>>

    @Query("SELECT * FROM calificaciones")
    fun observarCalificaciones(): Flow<List<CalificacionEntity>>

    @Query("SELECT COUNT(*) FROM estudiantes")
    suspend fun cantidadEstudiantes(): Int

    @Insert suspend fun insertarEstudiantes(estudiantes: List<EstudianteEntity>)
    @Insert suspend fun insertarEvaluacion(evaluacion: EvaluacionEntity)
    @Insert suspend fun insertarCalificacion(calificacion: CalificacionEntity)
    @Update suspend fun actualizarCalificacion(calificacion: CalificacionEntity)

    @Query("SELECT * FROM calificaciones WHERE evaluacionId = :evaluacionId AND estudianteId = :estudianteId LIMIT 1")
    suspend fun buscarNota(evaluacionId: String, estudianteId: String): CalificacionEntity?
}
