package com.librompaz.app

import com.librompaz.app.util.errorFechaEvaluacion
import com.librompaz.app.util.errorNota
import org.junit.Assert.*
import org.junit.Test

class ValidacionesCalificacionesTest {
    @Test fun aceptaComaPuntoLimitesYVacios() {
        listOf("1", "1,0", "7.0", "5,8", "5.8", " 6,2 ", "", "   ").forEach {
            assertNull("Debe aceptar: $it", errorNota(it))
        }
    }
    @Test fun rechazaRangoFormatoYPrecision() {
        listOf("0", "7,5", "8", "55", "abc", "NaN", "Infinity", "5.80", "5,88",
            "5,", ".5", "5e0", "5,8.0", "-1").forEach {
            assertNotNull("Debe rechazar: $it", errorNota(it))
        }
    }
    @Test fun fechaRealConFormatoEstricto() {
        assertNull(errorFechaEvaluacion("2026-10-22"))
        assertNull(errorFechaEvaluacion("2024-02-29"))
        listOf("", "2026-02-29", "2026-04-31", "2026-13-01", "22/10/2026",
            "2026-1-01", "2026-10-22extra").forEach {
            assertNotNull("Debe rechazar: $it", errorFechaEvaluacion(it))
        }
    }
}
