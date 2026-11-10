package com.example.seguimoaqui.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.seguimoaqui.data.MateriaEntity
import com.example.seguimoaqui.viewmodel.MateriaViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MateriaViewModel) {
    val materias by viewModel.materias.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seguimo Aqui") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir Materia")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(materias) { materia ->
                MateriaCard(materia = materia, viewModel = viewModel)
            }
        }

        if (showAddDialog) {
            AddMateriaDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { nombre, creditos, inicio, fin ->
                    viewModel.addMateria(nombre, creditos, inicio, fin)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun MateriaCard(materia: MateriaEntity, viewModel: MateriaViewModel) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val faltasRestantes = materia.creditos - materia.faltasTotales
    val cardColor = if (faltasRestantes < 0) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.surfaceVariant

    // Obtenemos "hoy" en base a la zona local, pero formateado a medianoche UTC
    // Esto nos permite comparar de igual a igual con las fechas de la DB
    val todayUtcMidnight = remember {
        val nowLaPaz = Calendar.getInstance(TimeZone.getTimeZone("America/La_Paz"))
        val calendarUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(nowLaPaz.get(Calendar.YEAR), nowLaPaz.get(Calendar.MONTH), nowLaPaz.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        calendarUtc.timeInMillis
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = materia.nombre, style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = { viewModel.deleteMateria(materia) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
            Text(
                text = "${dateFormatter.format(materia.fechaInicio)} - ${dateFormatter.format(materia.fechaFin)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Faltas Totales: ${materia.faltasTotales} / Permitidas: ${materia.creditos}",
                style = MaterialTheme.typography.bodyLarge,
                color = if (faltasRestantes < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(text = "Restantes: $faltasRestantes", style = MaterialTheme.typography.bodyMedium)

            if (faltasRestantes <= 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠️ Límite de faltas alcanzado o superado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ParcialControl(
                    parcial = 1, 
                    faltas = materia.faltasP1, 
                    fecha = materia.fechaP1,
                    dateFormatter = dateFormatter,
                    limiteGlobalAlcanzado = faltasRestantes <= 0,
                    isHabilitado = true, // P1 siempre está habilitado
                    onUpdateFaltas = { inc -> viewModel.updateFaltas(materia, 1, inc) },
                    onUpdateFecha = { nuevaFecha -> viewModel.updateFechaParcial(materia, 1, nuevaFecha) }
                )
                ParcialControl(
                    parcial = 2, 
                    faltas = materia.faltasP2,
                    fecha = materia.fechaP2,
                    dateFormatter = dateFormatter,
                    limiteGlobalAlcanzado = faltasRestantes <= 0,
                    isHabilitado = todayUtcMidnight >= materia.fechaP1, // P2 habilitado si ya pasó P1 (o es el mismo día)
                    onUpdateFaltas = { inc -> viewModel.updateFaltas(materia, 2, inc) },
                    onUpdateFecha = { nuevaFecha -> viewModel.updateFechaParcial(materia, 2, nuevaFecha) }
                )
                ParcialControl(
                    parcial = 3, 
                    faltas = materia.faltasP3,
                    fecha = materia.fechaP3,
                    dateFormatter = dateFormatter,
                    limiteGlobalAlcanzado = faltasRestantes <= 0,
                    isHabilitado = todayUtcMidnight >= materia.fechaP2, // P3 habilitado si ya pasó P2 (o es el mismo día)
                    onUpdateFaltas = { inc -> viewModel.updateFaltas(materia, 3, inc) },
                    onUpdateFecha = { nuevaFecha -> viewModel.updateFechaParcial(materia, 3, nuevaFecha) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcialControl(
    parcial: Int, 
    faltas: Int, 
    fecha: Long, 
    dateFormatter: SimpleDateFormat, 
    limiteGlobalAlcanzado: Boolean, 
    isHabilitado: Boolean,
    onUpdateFaltas: (Int) -> Unit,
    onUpdateFecha: (Long) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = fecha.takeIf { it > 0 })
    val alpha = if (isHabilitado) 1f else 0.4f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(alpha)
    ) {
        Text("P$parcial", style = MaterialTheme.typography.titleMedium)
        if (fecha > 0L) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(dateFormatter.format(fecha), style = MaterialTheme.typography.bodySmall)
                IconButton(onClick = { showDatePicker = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar Fecha", modifier = Modifier.size(16.dp))
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onUpdateFaltas(-1) },
                enabled = isHabilitado && faltas > 0 // Siempre puede restar si hay > 0 y está habilitado
            ) { 
                Text("-", style = MaterialTheme.typography.titleLarge) 
            }
            Text(text = faltas.toString(), style = MaterialTheme.typography.titleMedium)
            IconButton(
                onClick = { onUpdateFaltas(1) },
                enabled = isHabilitado && !limiteGlobalAlcanzado && faltas < 2 // Bloqueado si alcanzó límite global, o si el propio parcial llegó al límite de 2
            ) { 
                Text("+", style = MaterialTheme.typography.titleLarge) 
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onUpdateFecha(it) }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
