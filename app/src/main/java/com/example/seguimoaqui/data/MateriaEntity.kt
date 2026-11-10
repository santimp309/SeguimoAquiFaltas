package com.example.seguimoaqui.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materias")
data class MateriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val creditos: Int,
    val fechaInicio: Long,
    val fechaFin: Long,
    val fechaP1: Long = 0L,
    val fechaP2: Long = 0L,
    val fechaP3: Long = 0L,
    val faltasP1: Int = 0,
    val faltasP2: Int = 0,
    val faltasP3: Int = 0
) {
    val faltasTotales: Int
        get() = faltasP1 + faltasP2 + faltasP3
}
