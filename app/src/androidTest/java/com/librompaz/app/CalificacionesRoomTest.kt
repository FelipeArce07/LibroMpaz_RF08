package com.librompaz.app

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.librompaz.app.data.CalificacionesRepository
import com.librompaz.app.data.MockData
import com.librompaz.app.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CalificacionesRoomTest {
    @Test fun notasAuditoriaYReapertura() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val nombre = "rf08_prueba.db"
        context.deleteDatabase(nombre)
        var db = Room.databaseBuilder(context, AppDatabase::class.java, nombre).build()
        try {
            var repo = CalificacionesRepository(db)
            repo.cargarDatosIniciales()
            val ev = MockData.evaluaciones.first()
            val usuario = MockData.usuarios.first()
            val antes = repo.auditoria.first().size
            repo.guardarCalificaciones(ev, mapOf("E3A-3" to 5.8), usuario)
            repo.guardarCalificaciones(ev, mapOf("E3A-3" to 6.5), usuario)
            var notas = repo.calificaciones.first().filter { it.estudianteId == "E3A-3" && it.evaluacionId == ev.id }
            assertEquals(1, notas.size)
            assertEquals(6.5, notas.first().nota, 0.0)
            assertEquals(antes + 2, repo.auditoria.first().size)
            assertEquals("MODIFICAR", repo.auditoria.first().first().accion)
            // Guardar el mismo valor no genera una modificación ficticia.
            repo.guardarCalificaciones(ev, mapOf("E3A-3" to 6.5), usuario)
            assertEquals(antes + 2, repo.auditoria.first().size)
            // Un fallo de FK deshace incluso las notas anteriores del mismo lote.
            try {
                repo.guardarCalificaciones(ev, linkedMapOf("E3A-3" to 2.0, "NO_EXISTE" to 4.0), usuario)
                fail("Debía rechazar el estudiante inexistente")
            } catch (e: android.database.sqlite.SQLiteConstraintException) {
                assertEquals(antes + 2, repo.auditoria.first().size)
            }
            db.close()
            db = Room.databaseBuilder(context, AppDatabase::class.java, nombre).build()
            repo = CalificacionesRepository(db)
            repo.cargarDatosIniciales()
            notas = repo.calificaciones.first().filter { it.estudianteId == "E3A-3" && it.evaluacionId == ev.id }
            assertEquals(6.5, notas.single().nota, 0.0)
            assertEquals(antes + 2, repo.auditoria.first().size)
        } finally {
            db.close()
            context.deleteDatabase(nombre)
        }
    }
}
