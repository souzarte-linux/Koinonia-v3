package com.koinonia.igreja.presentation.features.reception.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.koinonia.igreja.presentation.features.reception.MemberAttendanceState
import java.util.Calendar

@Composable
fun EditAttendanceDialog(
    attendanceState: MemberAttendanceState,
    onDismiss: () -> Unit,
    onSave: (status: String, hour: Int, minute: Int, lateMins: Int) -> Unit
) {
    val member = attendanceState.member

    // Determina o status inicial
    var selectedStatus by remember {
        mutableStateOf(
            when {
                attendanceState.isAbsent -> "ABSENT"
                attendanceState.isPresent && attendanceState.isLate -> "LATE"
                attendanceState.isPresent -> "PRESENT"
                else -> "PRESENT"
            }
        )
    }

    // Inicializa campos de hora e minuto
    val calendar = remember {
        Calendar.getInstance().apply {
            attendanceState.arrivalTime?.let { time = it }
        }
    }

    var hourText by remember {
        mutableStateOf(String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)))
    }
    var minuteText by remember {
        mutableStateOf(String.format("%02d", calendar.get(Calendar.MINUTE)))
    }
    var lateMinsText by remember {
        mutableStateOf(attendanceState.lateDurationMins.let { if (it > 0) it.toString() else "15" })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Editar Chamada",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = member.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Situação da Presença:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                // Opções de Status
                Column(modifier = Modifier.selectableGroup()) {
                    // 1. Presente Pontual (Verde)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = (selectedStatus == "PRESENT"),
                            onClick = { selectedStatus = "PRESENT" }
                        )
                        Text(
                            text = "Presente (Pontual)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // 2. Presente com Atraso (Laranja)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = (selectedStatus == "LATE"),
                            onClick = { selectedStatus = "LATE" }
                        )
                        Text(
                            text = "Presente (Com Atraso)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFEF6C00),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // 3. Ausente (Vermelho)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = (selectedStatus == "ABSENT"),
                            onClick = { selectedStatus = "ABSENT" }
                        )
                        Text(
                            text = "Ausente",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // 4. Não registrado / Limpar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = (selectedStatus == "NONE"),
                            onClick = { selectedStatus = "NONE" }
                        )
                        Text(
                            text = "Não registrado (Limpar)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                HorizontalDivider()

                // Campos de Horário de Chegada (Se Presente ou Atrasado)
                if (selectedStatus == "PRESENT" || selectedStatus == "LATE") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Horário de Chegada:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = hourText,
                                onValueChange = { if (it.length <= 2) hourText = it },
                                label = { Text("Hora (HH)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            Text(
                                text = ":",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = minuteText,
                                onValueChange = { if (it.length <= 2) minuteText = it },
                                label = { Text("Minuto (mm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }

                // Campo de Minutos de Atraso (Apenas se LATE)
                if (selectedStatus == "LATE") {
                    OutlinedTextField(
                        value = lateMinsText,
                        onValueChange = { lateMinsText = it },
                        label = { Text("Minutos de Atraso") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hour = hourText.toIntOrNull()?.coerceIn(0, 23) ?: calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = minuteText.toIntOrNull()?.coerceIn(0, 59) ?: calendar.get(Calendar.MINUTE)
                    val lateMins = lateMinsText.toIntOrNull()?.coerceAtLeast(0) ?: 15

                    onSave(selectedStatus, hour, minute, lateMins)
                }
            ) {
                Text("Salvar Alterações")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
