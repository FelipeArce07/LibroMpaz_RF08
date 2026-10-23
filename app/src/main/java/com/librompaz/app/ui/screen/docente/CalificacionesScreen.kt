package com.librompaz.app.ui.screen.docente

import android.app.DatePickerDialog
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.ui.components.SelectorDesplegable
import com.librompaz.app.util.hoy
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Módulo "Calificaciones" (docente): crear un instrumento de evaluación y registrar las notas
 * de cada estudiante (escala numérica chilena). Pide confirmación antes de guardar porque puede
 * sobrescribir notas (RNF del caso).
 *
 * RF-08: validaciones en ViewModel y persistencia local en Room.
 */
@Composable
fun CalificacionesScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val cursoId by viewModel.cursoSeleccionadoId.collectAsState()
    val asignaturaId by viewModel.asignaturaSeleccionadaId.collectAsState()
    val todas by viewModel.evaluaciones.collectAsState()
    val evaluaciones = remember(todas, cursoId, asignaturaId) { viewModel.evaluacionesDe(cursoId, asignaturaId) }
    val estudiantes = viewModel.estudiantesDelCurso(cursoId)

    val guardadas by viewModel.calificaciones.collectAsState()
    val guardando by viewModel.guardando.collectAsState()
    val listos by viewModel.datosListos.collectAsState()
    val mensaje by viewModel.mensajeCalificaciones.collectAsState()
    val errorNombre by viewModel.errorNombre.collectAsState()
    val errorFecha by viewModel.errorFecha.collectAsState()
    val context = LocalContext.current
    var evaluacionId by rememberSaveable(cursoId, asignaturaId) { mutableStateOf<String?>(null) }
    val evaluacion = evaluaciones.find { it.id == evaluacionId }
    var nuevoNombre by rememberSaveable(cursoId, asignaturaId) { mutableStateOf("") }
    var nuevaFecha by rememberSaveable(cursoId, asignaturaId) { mutableStateOf(hoy()) }
    // HashMap es serializable: mantiene el borrador al girar la pantalla.
    var notas by rememberSaveable(evaluacionId) { mutableStateOf(hashMapOf<String, String>()) }
    var cargadas by rememberSaveable(evaluacionId) { mutableStateOf(false) }
    var confirmando by remember { mutableStateOf(false) }
    // Reutiliza la validación del ViewModel y se actualiza al escribir una nota.
    val hayNotasInvalidas = notas.values.any { viewModel.validarNota(it) != null }

    LaunchedEffect(evaluacionId, listos) {
        if (listos && !cargadas) {
            notas = HashMap(guardadas.filter { it.evaluacionId == evaluacionId }
                .associate { it.estudianteId to it.nota.toString().replace('.', ',') })
            cargadas = true
        }
    }
    LaunchedEffect(cursoId, asignaturaId) { viewModel.limpiarMensajesCalificaciones() }

    Scaffold(topBar = { BarraSuperior("Calificaciones", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Nueva evaluación", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = nuevoNombre,
                onValueChange = { nuevoNombre = it; viewModel.limpiarMensajesCalificaciones() },
                enabled = listos && !guardando,
                isError = errorNombre != null,
                supportingText = { errorNombre?.let { Text(it) } },
                label = { Text("Nombre del instrumento") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nuevaFecha,
                    onValueChange = { nuevaFecha = it; viewModel.limpiarMensajesCalificaciones() },
                    enabled = listos && !guardando,
                    isError = errorFecha != null,
                    supportingText = { Text(errorFecha ?: "AAAA-MM-DD") },
                    label = { Text("Fecha") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(enabled = listos && !guardando, onClick = {
                    viewModel.crearEvaluacion(cursoId.orEmpty(), asignaturaId.orEmpty(), nuevoNombre.trim(), nuevaFecha)
                }) { Text("Crear") }
            }

            // Recurso nativo permitido: calendario Android, sin permisos ni sensores.
            OutlinedButton(enabled = listos && !guardando, onClick = {
                val hoy = Calendar.getInstance()
                DatePickerDialog(context, { _, anio, mes, dia ->
                    nuevaFecha = String.format(Locale.ROOT, "%04d-%02d-%02d", anio, mes + 1, dia)
                    viewModel.limpiarMensajesCalificaciones()
                }, hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH)).show()
            }) { Text("Elegir fecha en calendario") }

            if (!listos && mensaje == null) Text("Cargando calificaciones…")
            if (guardando) Text("Guardando…")

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SelectorDesplegable(
                etiqueta = "Evaluación",
                opciones = evaluaciones,
                seleccion = evaluacion,
                textoDe = { "${it.nombreInstrumento} (${it.fecha})" },
                onSeleccion = { if (!guardando) { evaluacionId = it.id; viewModel.limpiarMensajesCalificaciones() } },
                textoVacio = "Elige una evaluación"
            )

            if (evaluacion != null && listos && cargadas) {
                Text("Escala 1,0 a 7,0. Los campos vacíos no borran notas guardadas.")
                estudiantes.forEach { estudiante ->
                    val texto = notas[estudiante.id].orEmpty()
                    val error = if (texto.isBlank()) null else viewModel.validarNota(texto)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(estudiante.nombreFicticio, modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            value = texto,
                            onValueChange = {
                                notas = HashMap(notas + (estudiante.id to it))
                                viewModel.limpiarMensajesCalificaciones()
                            },
                            enabled = !guardando,
                            label = { Text("Nota") },
                            singleLine = true,
                            isError = error != null,
                            supportingText = if (error != null) {
                                { Text(error) }
                            } else {
                                null
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !guardando && !hayNotasInvalidas,
                    onClick = { confirmando = true }
                ) { Text("Guardar notas") }
                if (hayNotasInvalidas) {
                    Text("Corrige las notas con error para poder guardar.",
                        color = MaterialTheme.colorScheme.error)
                }
            }

            mensaje?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }
        }
    }

    if (confirmando) {
        AlertDialog(
            onDismissRequest = { confirmando = false },
            title = { Text("¿Guardar notas?") },
            text = { Text("Las notas que ya existían para esta evaluación se van a sobrescribir.") },
            confirmButton = {
                TextButton(enabled = !guardando && !hayNotasInvalidas, onClick = {
                    evaluacion?.let { viewModel.guardarCalificaciones(it.id, notas) }
                    confirmando = false
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmando = false }) { Text("Cancelar") }
            }
        )
    }
}
