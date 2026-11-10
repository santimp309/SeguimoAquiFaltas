package com.example.seguimoaqui.repository

import com.example.seguimoaqui.data.MateriaDao
import com.example.seguimoaqui.data.MateriaEntity
import kotlinx.coroutines.flow.Flow

class MateriaRepository(private val materiaDao: MateriaDao) {

    val allMaterias: Flow<List<MateriaEntity>> = materiaDao.getAllMaterias()

    suspend fun insert(materia: MateriaEntity) {
        materiaDao.insertMateria(materia)
    }

    suspend fun update(materia: MateriaEntity) {
        materiaDao.updateMateria(materia)
    }

    suspend fun delete(materia: MateriaEntity) {
        materiaDao.deleteMateria(materia)
    }
}
