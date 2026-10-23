package com.librompaz.app.util

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale

// Funciones puras, llamadas desde el ViewModel. Se prueban sin abrir Android.
fun errorNota(texto: String): String? {
    val normalizada = texto.trim().replace(',', '.')
    if (normalizada.isBlank()) return null // RF-08: vacío significa no ingresar una nota.
    if (!normalizada.matches(Regex("[0-9]+([.][0-9]+)?"))) {
        return "Escribe un número válido (ej. 5,8)"
    }
    val numero = normalizada.toDoubleOrNull()
        ?: return "Escribe un número válido (ej. 5,8)"
    if (numero < 1.0 || numero > 7.0) return "La nota debe estar entre 1,0 y 7,0"
    if (normalizada.contains('.') && normalizada.substringAfter('.').length > 1) {
        return "La nota admite un decimal como máximo"
    }
    return null
}

fun errorFechaEvaluacion(texto: String): String? {
    if (!texto.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}"))) {
        return "Usa el formato AAAA-MM-DD"
    }
    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
    formato.isLenient = false
    val posicion = ParsePosition(0)
    if (formato.parse(texto, posicion) == null || posicion.index != texto.length) {
        return "Ingresa una fecha válida"
    }
    return null
}
