package com.librompaz.app.data

import androidx.room.withTransaction
import com.librompaz.app.data.local.*
import com.librompaz.app.model.*
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CalificacionesRepository(private val db: AppDatabase) {
    private val dao = db.calificacionesDao()
    private val auditoriaDao = db.auditoriaDao()

    val evaluaciones = dao.observarEvaluaciones().map { lista ->
        lista.map { Evaluacion(it.id, it.cursoId, it.asignaturaId, it.nombreInstrumento, it.fecha) }
    }
    val calificaciones = dao.observarCalificaciones().map { lista ->
        lista.map { Calificacion(it.id, it.evaluacionId, it.estudianteId, it.nota, it.registradoPor) }
    }
    val auditoria = auditoriaDao.observar().map { lista ->
        lista.map { RegistroAuditoria(it.id, it.usuario, Perfil.valueOf(it.perfil), it.accion, it.detalle, it.fechaHora) }
    }

    // Solo la primera apertura. Nunca reponer las notas de ejemplo sobre notas editadas.
    suspend fun cargarDatosIniciales() {
        db.withTransaction {
            if (dao.cantidadEstudiantes() == 0) {
                dao.insertarEstudiantes(MockData.estudiantes.map { EstudianteEntity(it.id, it.nombreFicticio, it.cursoId) })
                MockData.evaluaciones.forEach {
                    dao.insertarEvaluacion(EvaluacionEntity(it.id, it.cursoId, it.asignaturaId, it.nombreInstrumento, it.fecha))
                }
                MockData.calificaciones.forEach {
                    dao.insertarCalificacion(CalificacionEntity(it.id, it.evaluacionId, it.estudianteId, it.nota, it.registradoPor))
                }
                MockData.auditoria.forEach {
                    auditoriaDao.insertar(AuditoriaEntity(it.id, it.usuario, it.perfil.name, it.accion, it.detalle, it.fechaHora))
                }
            }
        }
    }

    private suspend fun registrarAuditoria(usuario: Usuario, accion: String, detalle: String) {
        auditoriaDao.insertar(AuditoriaEntity(UUID.randomUUID().toString(), usuario.nombre,
            usuario.perfil.name, accion, detalle,
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())))
    }

    suspend fun crearEvaluacion(evaluacion: Evaluacion, usuario: Usuario) {
        db.withTransaction {
            dao.insertarEvaluacion(EvaluacionEntity(evaluacion.id, evaluacion.cursoId,
                evaluacion.asignaturaId, evaluacion.nombreInstrumento, evaluacion.fecha))
            registrarAuditoria(usuario, "CREAR", "Evaluación ${evaluacion.nombreInstrumento}; curso ${evaluacion.cursoId}; asignatura ${evaluacion.asignaturaId}; fecha ${evaluacion.fecha}")
        }
    }

    // Todas las notas y sus auditorías se confirman juntas o se deshacen juntas.
    suspend fun guardarCalificaciones(evaluacion: Evaluacion, notas: Map<String, Double>, usuario: Usuario) {
        db.withTransaction {
            notas.forEach { (estudianteId, nota) ->
                val anterior = dao.buscarNota(evaluacion.id, estudianteId)
                if (anterior == null || anterior.nota != nota) {
                    val nueva = CalificacionEntity(anterior?.id ?: UUID.randomUUID().toString(),
                        evaluacion.id, estudianteId, nota, usuario.nombre)
                    if (anterior == null) dao.insertarCalificacion(nueva)
                    else dao.actualizarCalificacion(nueva)
                    registrarAuditoria(usuario, if (anterior == null) "CREAR" else "MODIFICAR",
                        "Nota $estudianteId: ${anterior?.nota ?: "sin nota"} -> $nota; evaluación ${evaluacion.nombreInstrumento} (${evaluacion.id}); curso ${evaluacion.cursoId}; asignatura ${evaluacion.asignaturaId}; fecha ${evaluacion.fecha}")
                }
            }
        }
    }
}
