package com.librompaz.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// RN-02: únicamente inserción y lectura; no editar ni eliminar auditoría.
@Dao
interface AuditoriaDao {
    @Insert suspend fun insertar(registro: AuditoriaEntity)

    @Query("SELECT * FROM auditoria ORDER BY fechaHora DESC, rowid DESC")
    fun observar(): Flow<List<AuditoriaEntity>>
}
