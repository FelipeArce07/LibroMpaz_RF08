package com.librompaz.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.librompaz.app.data.CalificacionesRepository
import com.librompaz.app.data.local.AppDatabase
import com.librompaz.app.util.errorNota
import com.librompaz.app.util.errorFechaEvaluacion
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import com.librompaz.app.data.MockData
import com.librompaz.app.model.Anotacion
import com.librompaz.app.model.Asignatura
import com.librompaz.app.model.Calificacion
import com.librompaz.app.model.CargaDocente
import com.librompaz.app.model.Curso
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.Estudiante
import com.librompaz.app.model.Evaluacion
import com.librompaz.app.model.ObjetivoAprendizaje
import com.librompaz.app.model.Perfil
import com.librompaz.app.model.RegistroAuditoria
import com.librompaz.app.model.ResumenCurso
import com.librompaz.app.model.SesionClase
import com.librompaz.app.model.TipoAnotacion
import com.librompaz.app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** ViewModel compartido. RF-08 usa Room; los demás módulos conservan sus TODO. */
class LibroMpazViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = CalificacionesRepository(AppDatabase.obtener(application))
    private val _datosListos = MutableStateFlow(false)
    val datosListos = _datosListos.asStateFlow()
    private val _guardando = MutableStateFlow(false)
    val guardando = _guardando.asStateFlow()
    private val _mensajeCalificaciones = MutableStateFlow<String?>(null)
    val mensajeCalificaciones = _mensajeCalificaciones.asStateFlow()
    private val _errorNombre = MutableStateFlow<String?>(null)
    val errorNombre = _errorNombre.asStateFlow()
    private val _errorFecha = MutableStateFlow<String?>(null)
    val errorFecha = _errorFecha.asStateFlow()

    // =====================================================================
    // Módulo "Autenticación y alcance por rol"
    // =====================================================================

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
    val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo.asStateFlow()

    private val _errorLogin = MutableStateFlow<String?>(null)
    val errorLogin: StateFlow<String?> = _errorLogin.asStateFlow()

    // Dependencia mínima RF-01: permite probar RF-08 con las cuentas de la plantilla.
    fun login(usuario: String, clave: String): Boolean {
        if (usuario.isBlank() || clave.isBlank()) {
            _errorLogin.value = "Completa el usuario y la clave"
            return false
        }
        val encontrado = MockData.usuarios.find { it.usuario == usuario.trim() && it.clave == clave }
        if (encontrado == null) {
            _errorLogin.value = "Usuario o clave incorrectos"
            return false
        }
        _usuarioActivo.value = encontrado
        _errorLogin.value = null
        return true
    }

    fun logout() {
        _usuarioActivo.value = null
        _errorLogin.value = null
        _cursoSeleccionadoId.value = null
        _asignaturaSeleccionadaId.value = null
        limpiarMensajesCalificaciones()
    }

    // =====================================================================
    // Datos de referencia y selección (no es lógica de negocio)
    // =====================================================================

    val cursos: List<Curso> = MockData.cursos
    val asignaturas: List<Asignatura> = MockData.asignaturas
    private val estudiantes: List<Estudiante> = MockData.estudiantes
    private val objetivos: List<ObjetivoAprendizaje> = MockData.objetivos

    private val _cursoSeleccionadoId = MutableStateFlow<String?>(null)
    val cursoSeleccionadoId: StateFlow<String?> = _cursoSeleccionadoId.asStateFlow()

    private val _asignaturaSeleccionadaId = MutableStateFlow<String?>(null)
    val asignaturaSeleccionadaId: StateFlow<String?> = _asignaturaSeleccionadaId.asStateFlow()

    fun seleccionarCurso(cursoId: String?, asignaturaId: String? = null) {
        _cursoSeleccionadoId.value = cursoId
        _asignaturaSeleccionadaId.value = asignaturaId
    }

    fun cursoPorId(id: String?): Curso? = cursos.find { it.id == id }
    fun asignaturaPorId(id: String?): Asignatura? = asignaturas.find { it.id == id }
    fun estudiantePorId(id: String?): Estudiante? = estudiantes.find { it.id == id }
    fun estudiantesDelCurso(cursoId: String?): List<Estudiante> = estudiantes.filter { it.cursoId == cursoId }
    fun objetivosDe(asignaturaId: String?): List<ObjetivoAprendizaje> = objetivos.filter { it.asignaturaId == asignaturaId }

    /** RF-01: muestra únicamente las cargas del docente autenticado. */
    fun misCargas(usuario: Usuario?): List<CargaDocente> =
        MockData.cargas.filter { usuario?.perfil == Perfil.DOCENTE && it.docenteId == usuario.id }

    // =====================================================================
    // Módulo "Asistencia"
    // =====================================================================

    private val _asistencias = MutableStateFlow(MockData.asistencias)

    /**
     * TODO(equipo): devolver la asistencia ya guardada del curso en esa fecha
     * (idEstudiante -> estado), para mostrarla y poder corregirla. Hoy devuelve un mapa vacío.
     * Pendiente con el CITT: si se puede corregir la asistencia de días anteriores.
     */
    fun asistenciaGuardada(cursoId: String, fecha: String): Map<String, EstadoAsistencia> = emptyMap()

    /**
     * TODO(equipo): guardar (o sobrescribir) la asistencia del curso en esa fecha, con
     * registradoPor = usuario activo y la fecha y hora actuales, y dejar un RegistroAuditoria.
     * RF: "registrar la asistencia diaria con estados presente, ausente, atrasado, justificado".
     * Hoy no hace nada.
     */
    fun guardarAsistencia(cursoId: String, fecha: String, estados: Map<String, EstadoAsistencia>) {
        // Sin implementar todavía.
    }

    /**
     * TODO(equipo): porcentaje de asistencia del curso (0 a 100) para Inspectoría y Dirección.
     * Hoy devuelve null (sin calcular).
     */
    fun porcentajeAsistencia(cursoId: String): Int? = null

    /**
     * TODO(equipo): estudiantes con inasistencias reiteradas (por ejemplo, 3 o más ausencias),
     * con su número de ausencias. RF de Inspectoría: "identificar inasistencias reiteradas".
     * Hoy devuelve una lista vacía.
     */
    fun inasistenciasReiteradas(): List<Pair<Estudiante, Int>> = emptyList()

    // =====================================================================
    // Módulo "Cobertura curricular"
    // =====================================================================

    private val _sesiones = MutableStateFlow(MockData.sesiones)
    val sesiones: StateFlow<List<SesionClase>> = _sesiones.asStateFlow()

    fun sesionesDe(cursoId: String?, asignaturaId: String?): List<SesionClase> =
        _sesiones.value.filter { it.cursoId == cursoId && it.asignaturaId == asignaturaId }

    /**
     * TODO(equipo): guardar la sesión con los OA y el contenido trabajados, con auditoría.
     * RF: "asociar OA y contenidos didácticos a cada sesión/bloque lectivo". Hoy no hace nada.
     */
    fun guardarSesion(cursoId: String, asignaturaId: String, fecha: String, bloque: String, oaIds: List<String>, contenido: String) {
        // Sin implementar todavía.
    }

    /**
     * TODO(equipo): ids de los OA ya trabajados en el curso y asignatura (según las sesiones
     * registradas). RF de UTP: "vistas analíticas de cobertura curricular por curso".
     * Hoy devuelve un conjunto vacío.
     */
    fun oasTrabajados(cursoId: String?, asignaturaId: String?): Set<String> = emptySet()

    // =====================================================================
    // Módulo "Calificaciones"
    // =====================================================================

    val evaluaciones: StateFlow<List<Evaluacion>> = repositorio.evaluaciones
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val calificaciones: StateFlow<List<Calificacion>> = repositorio.calificaciones
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun evaluacionesDe(cursoId: String?, asignaturaId: String?): List<Evaluacion> =
        evaluaciones.value.filter { it.cursoId == cursoId && it.asignaturaId == asignaturaId }

    fun calificacionesDe(evaluacionId: String?): List<Calificacion> =
        calificaciones.value.filter { it.evaluacionId == evaluacionId }

    fun limpiarMensajesCalificaciones() {
        _mensajeCalificaciones.value = null
        _errorNombre.value = null
        _errorFecha.value = null
    }

    private fun puedeCalificar(cursoId: String, asignaturaId: String): Boolean {
        val usuario = _usuarioActivo.value ?: return false
        return usuario.perfil == Perfil.DOCENTE && MockData.cargas.any {
            it.docenteId == usuario.id && it.cursoId == cursoId && it.asignaturaId == asignaturaId
        }
    }

    fun crearEvaluacion(cursoId: String, asignaturaId: String, nombreInstrumento: String, fecha: String) {
        if (_guardando.value || !_datosListos.value) return
        limpiarMensajesCalificaciones()
        if (!puedeCalificar(cursoId, asignaturaId)) {
            _mensajeCalificaciones.value = "No tienes asignado este curso o asignatura."
            return
        }
        val nombre = nombreInstrumento.trim()
        val fechaLimpia = fecha.trim()
        _errorNombre.value = if (nombre.isBlank()) "Escribe el nombre del instrumento" else null
        _errorFecha.value = errorFechaEvaluacion(fechaLimpia)
        if (_errorNombre.value != null || _errorFecha.value != null) return
        if (evaluacionesDe(cursoId, asignaturaId).any {
                it.nombreInstrumento.equals(nombre, ignoreCase = true) && it.fecha == fechaLimpia }) {
            _errorNombre.value = "Ya existe ese instrumento en esta fecha para el curso y asignatura"
            return
        }
        val usuario = _usuarioActivo.value ?: return
        val nueva = Evaluacion(UUID.randomUUID().toString(), cursoId, asignaturaId, nombre, fechaLimpia)
        _guardando.value = true
        viewModelScope.launch {
            try {
                repositorio.crearEvaluacion(nueva, usuario)
                // Esperar a que la lista observada muestre lo guardado antes de habilitar otra operación.
                evaluaciones.first { lista -> lista.any { it.id == nueva.id } }
                _mensajeCalificaciones.value = "Evaluación creada correctamente"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeCalificaciones.value = "No se pudo crear la evaluación. Intenta nuevamente."
            } finally {
                _guardando.value = false
            }
        }
    }

    fun validarNota(texto: String): String? = errorNota(texto)

    fun guardarCalificaciones(evaluacionId: String, notas: Map<String, String>) {
        if (_guardando.value || !_datosListos.value) return
        _mensajeCalificaciones.value = null
        val evaluacion = evaluaciones.value.find { it.id == evaluacionId }
        if (evaluacion == null) {
            _mensajeCalificaciones.value = "La evaluación no existe"
            return
        }
        if (!puedeCalificar(evaluacion.cursoId, evaluacion.asignaturaId)) {
            _mensajeCalificaciones.value = "No tienes asignado este curso o asignatura."
            return
        }
        // Validar el lote completo ANTES de guardar: no dejar un guardado parcial.
        for ((estudianteId, texto) in notas) {
            if (estudiantePorId(estudianteId)?.cursoId != evaluacion.cursoId) {
                _mensajeCalificaciones.value = "El estudiante no pertenece a este curso"
                return
            }
            if (validarNota(texto) != null) {
                _mensajeCalificaciones.value = "Corrige las notas con error antes de guardar"
                return
            }
        }
        val nuevas = notas.filterValues { it.isNotBlank() }
            .mapValues { it.value.trim().replace(',', '.').toDouble() }
        if (nuevas.isEmpty()) {
            _mensajeCalificaciones.value = "No hay notas para guardar. Los vacíos no borran notas existentes."
            return
        }
        val usuario = _usuarioActivo.value ?: return
        _guardando.value = true
        viewModelScope.launch {
            try {
                repositorio.guardarCalificaciones(evaluacion, nuevas, usuario)
                calificaciones.first { lista -> nuevas.all { (id, nota) ->
                    lista.any { it.evaluacionId == evaluacionId && it.estudianteId == id && it.nota == nota }
                } }
                _mensajeCalificaciones.value = "Notas guardadas correctamente"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeCalificaciones.value = "No se guardaron las notas. Intenta nuevamente."
            } finally {
                _guardando.value = false
            }
        }
    }

    /**
     * TODO(equipo): detectar desfases u omisiones de registro de notas, por ejemplo
     * evaluaciones con estudiantes sin nota, o cursos/asignaturas sin evaluaciones.
     * RF de UTP: "identificar desfases u omisiones de registro". Hoy devuelve una lista vacía.
     */
    fun desfasesCalificaciones(): List<String> = emptyList()

    // =====================================================================
    // Módulo "Hoja de vida"
    // =====================================================================

    private val _anotaciones = MutableStateFlow(MockData.anotaciones)
    val anotaciones: StateFlow<List<Anotacion>> = _anotaciones.asStateFlow()

    fun anotacionesDe(estudianteId: String?): List<Anotacion> =
        _anotaciones.value.filter { it.estudianteId == estudianteId }

    /**
     * TODO(equipo): registrar la anotación con marca temporal y "firma digital" del docente
     * (usuario activo + fecha y hora), y dejar auditoría. Hoy no hace nada.
     */
    fun registrarAnotacion(estudianteId: String, tipo: TipoAnotacion, descripcion: String) {
        // Sin implementar todavía.
    }

    /**
     * TODO(equipo): anotaciones que requieren intervención de Inspectoría (por ejemplo,
     * negativas reiteradas de un mismo estudiante). Hoy devuelve una lista vacía.
     */
    fun anotacionesQueRequierenIntervencion(): List<Anotacion> = emptyList()

    // =====================================================================
    // Módulo "Panel administrativo" (Dirección) y auditoría
    // =====================================================================

    val auditoria: StateFlow<List<RegistroAuditoria>> = repositorio.auditoria
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            try {
                repositorio.cargarDatosIniciales()
                // La plantilla siempre tiene EV1. Esperar la carga antes de aceptar escrituras.
                evaluaciones.first { it.isNotEmpty() }
                calificaciones.first { it.isNotEmpty() }
                _datosListos.value = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeCalificaciones.value = "No se pudo abrir la base de datos. Cierra y vuelve a abrir la app."
            }
        }
    }

    /**
     * TODO(equipo): calcular las métricas consolidadas de cada curso (asistencia, sesiones,
     * notas registradas, anotaciones negativas). RF de Dirección: "estado global administrativo
     * del colegio". Hoy devuelve los cursos sin métricas (valores null).
     */
    fun resumenPorCurso(): List<ResumenCurso> = cursos.map { ResumenCurso(cursoId = it.id) }
}
