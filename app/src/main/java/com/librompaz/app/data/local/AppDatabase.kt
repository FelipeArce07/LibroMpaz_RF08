package com.librompaz.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EstudianteEntity::class, EvaluacionEntity::class,
    CalificacionEntity::class, AuditoriaEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calificacionesDao(): CalificacionesDao
    abstract fun auditoriaDao(): AuditoriaDao

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(context.applicationContext,
                    AppDatabase::class.java, "libro_mpaz.db").build().also { instancia = it }
            }
        }
    }
}
