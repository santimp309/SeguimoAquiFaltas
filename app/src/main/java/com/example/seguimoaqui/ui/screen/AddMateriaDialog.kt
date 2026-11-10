package com.example.seguimoaqui.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMateriaDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, Long, Long) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    
    val opcionesCreditos = listOf(3, 4)
    var creditos by remember { mutableStateOf(opcionesCreditos[0].toString()) }
    var expanded by remember { mutableStateOf(false) }
    
    val datePickerStateInicio = rememberDatePickerState()
    val datePickerStateFin = rememberDatePickerState()
    
    var showInicioPicker by remember { mutableStateOf(false) }
    var showFinPicker by remember { mutableStateOf(false) }

    // Usamos UTC para que el DatePicker no se retrase un día
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Materia") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de Materia") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = creditos,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Créditos") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        opcionesCreditos.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion.toString()) },
                                onClick = {
                                    creditos = opcion.toString()
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(onClick = { showInicioPicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Seleccionar Fecha de Inicio")
                }
                if (datePickerStateInicio.selectedDateMillis != null) {
                    Text("Inicio: ${dateFormatter.format(Date(datePickerStateInicio.selectedDateMillis!!))}", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Button(onClick = { showFinPicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Seleccionar Fecha de Fin")
                }
                if (datePickerStateFin.selectedDateMillis != null) {
                    Text("Fin: ${dateFormatter.format(Date(datePickerStateFin.selectedDateMillis!!))}", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val creditosInt = creditos.toIntOrNull()
                    val inicioLong = datePickerStateInicio.selectedDateMillis
                    val finLong = datePickerStateFin.selectedDateMillis
                    
                    if (nombre.isNotBlank() && creditosInt != null && inicioLong != null && finLong != null) {
                        onAdd(nombre, creditosInt, inicioLong, finLong)
                    }
                }
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    if (showInicioPicker) {
        DatePickerDialog(
            onDismissRequest = { showInicioPicker = false },
            confirmButton = {
                TextButton(onClick = { showInicioPicker = false }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerStateInicio)
        }
    }

    if (showFinPicker) {
        DatePickerDialog(
            onDismissRequest = { showFinPicker = false },
            confirmButton = {
                TextButton(onClick = { showFinPicker = false }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerStateFin)
        }
    }
}
