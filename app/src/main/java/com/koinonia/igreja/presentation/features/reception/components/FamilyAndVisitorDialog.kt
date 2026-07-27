package com.koinonia.igreja.presentation.features.reception.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.koinonia.igreja.presentation.features.reception.AttendanceStatus
import com.koinonia.igreja.presentation.features.reception.ReceptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyAndVisitorDialog(
    viewModel: ReceptionViewModel,
    onDismiss: () -> Unit
) {
    val familyMembers by viewModel.currentFamilyMembers.collectAsState()
    
    // Estados do Formulário de Visitante
    var visitorExpanded by remember { mutableStateOf(false) }
    var visName by remember { mutableStateOf("") }
    var visPhone by remember { mutableStateOf("") }
    var visWhatsapp by remember { mutableStateOf(false) }
    var visSocial by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ações Adicionais") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                
                // 1. SEÇÃO DE VISITANTES
                Card(
                    onClick = { visitorExpanded = !visitorExpanded },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Adicionar Visitante", fontWeight = FontWeight.Bold)
                        
                        if (visitorExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = visName,
                                onValueChange = { visName = it },
                                label = { Text("Nome Completo") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = visPhone,
                                onValueChange = { visPhone = it },
                                label = { Text("Telefone / Celular") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = visWhatsapp,
                                    onCheckedChange = { visWhatsapp = it }
                                )
                                Text("Possui WhatsApp?", style = MaterialTheme.typography.bodyMedium)
                            }
                            OutlinedTextField(
                                value = visSocial,
                                onValueChange = { visSocial = it },
                                label = { Text("Rede Social (Instagram/Facebook)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (visName.isNotBlank()) {
                                        viewModel.saveVisitor(visName, visPhone, visWhatsapp, visSocial)
                                        visName = ""
                                        visPhone = ""
                                        visWhatsapp = false
                                        visSocial = ""
                                        visitorExpanded = false
                                    }
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Salvar Visitante")
                            }
                        }
                    }
                }

                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // 2. SEÇÃO DE FAMILIARES
                Text(
                    text = "Membros da Família:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (familyMembers.isEmpty()) {
                    Text(
                        text = "Nenhum familiar pendente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(familyMembers) { relative ->
                            val member = relative.member
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(member.fullName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. Botão Presente Pontual (Primary)
                                    IconButton(
                                        onClick = {
                                            if (relative.isPresent && !relative.isLate) {
                                                viewModel.setAttendanceState(member, AttendanceStatus.NENHUM)
                                            } else {
                                                viewModel.setAttendanceState(member, AttendanceStatus.PONTUAL)
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Presente Pontual",
                                            tint = if (relative.isPresent && !relative.isLate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // 2. Botão Presente com Atraso (Tertiary)
                                    IconButton(
                                        onClick = {
                                            if (relative.isPresent && relative.isLate) {
                                                viewModel.setAttendanceState(member, AttendanceStatus.NENHUM)
                                            } else {
                                                viewModel.setAttendanceState(member, AttendanceStatus.ATRASADO)
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = "Presente com Atraso",
                                            tint = if (relative.isPresent && relative.isLate) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // 3. Botão Ausente (Error)
                                    IconButton(
                                        onClick = {
                                            if (relative.isAbsent) {
                                                viewModel.setAttendanceState(member, AttendanceStatus.NENHUM)
                                            } else {
                                                viewModel.setAttendanceState(member, AttendanceStatus.AUSENTE)
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Ausente",
                                            tint = if (relative.isAbsent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // 4. Botão Editar Chamada
                                    IconButton(
                                        onClick = {
                                            viewModel.startEditing(relative)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar Chamada",
                                            tint = if (relative.isPresent || relative.isAbsent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Concluir")
            }
        }
    )
}
