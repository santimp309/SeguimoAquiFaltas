package com.example.seguimoaqui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seguimoaqui.data.MateriaEntity
import com.example.seguimoaqui.repository.MateriaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class MateriaViewModel(private val repository: MateriaRepository) : ViewModel() {

    val materias: StateFlow<List<MateriaEntity>> = repository.allMaterias
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addMateria(nombre: String, creditos: Int, fechaInicio: Long, fechaFin: Long) {
        val (fechaP1, fechaP2, fechaP3) = calcularFechasParciales(fechaInicio, creditos)
        
        val newMateria = MateriaEntity(
            nombre = nombre,
            creditos = creditos,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            fechaP1 = fechaP1,
            fechaP2 = fechaP2,
            fechaP3 = fechaP3
        )
        viewModelScope.launch {
            repository.insert(newMateria)
        }
    }

    private fun calcularFechasParciales(fechaInicio: Long, creditos: Int): Triple<Long, Long, Long> {
        val clasesPorExamen = if (creditos == 3) listOf(8, 16, 24) else listOf(11, 22, 32)
        
        return Triple(
            sumarClasesHabiles(fechaInicio, clasesPorExamen[0]),
            sumarClasesHabiles(fechaInicio, clasesPorExamen[1]),
            sumarClasesHabiles(fechaInicio, clasesPorExamen[2])
        )
    }

    private fun sumarClasesHabiles(fechaInicio: Long, numClases: Int): Long {
        // Trabajamos estrictamente en UTC para no desfasar la hora 00:00 que viene de Compose
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { 
            timeInMillis = fechaInicio
            set(Calendar.HOUR_OF_DAY, 12) // Ponemos a las 12 PM para evitar bordes de medianoche
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        var clasesSumadas = 1 // El día de inicio cuenta como la clase 1

        while (clasesSumadas < numClases) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                clasesSumadas++
            }
        }
        
        // Volvemos a setear a medianoche UTC para ser consistentes con lo que devuelve el DatePicker
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        return calendar.timeInMillis
    }

    fun updateFechaParcial(materia: MateriaEntity, parcial: Int, nuevaFecha: Long) {
        val updatedMateria = when (parcial) {
            1 -> materia.copy(fechaP1 = nuevaFecha)
            2 -> materia.copy(fechaP2 = nuevaFecha)
            3 -> materia.copy(fechaP3 = nuevaFecha)
            else -> materia
        }
        viewModelScope.launch {
            repository.update(updatedMateria)
        }
    }

    fun updateFaltas(materia: MateriaEntity, parcial: Int, incremento: Int) {
        val updatedMateria = when (parcial) {
            1 -> materia.copy(faltasP1 = (materia.faltasP1 + incremento).coerceIn(0, 2))
            2 -> materia.copy(faltasP2 = (materia.faltasP2 + incremento).coerceIn(0, 2))
            3 -> materia.copy(faltasP3 = (materia.faltasP3 + incremento).coerceIn(0, 2))
            else -> materia
        }
        viewModelScope.launch {
            repository.update(updatedMateria)
        }
    }

    fun deleteMateria(materia: MateriaEntity) {
        viewModelScope.launch {
            repository.delete(materia)
        }
    }
}

class MateriaViewModelFactory(private val repository: MateriaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MateriaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MateriaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
