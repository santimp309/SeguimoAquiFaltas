package com.example.seguimoaqui.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MateriaDao {
    @Query("SELECT * FROM materias ORDER BY id DESC")
    fun getAllMaterias(): Flow<List<MateriaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMateria(materia: MateriaEntity)

    @Update
    suspend fun updateMateria(materia: MateriaEntity)

    @Delete
    suspend fun deleteMateria(materia: MateriaEntity)
}
