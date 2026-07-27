package com.koinonia.igreja.presentation.features.reception

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.koinonia.igreja.presentation.features.reception.components.EditAttendanceDialog
import com.koinonia.igreja.presentation.features.reception.components.FamilyAndVisitorDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceptionScreen(
    onBack: () -> Unit,
    viewModel: ReceptionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val members by viewModel.membersList.collectAsState(initial = emptyList())
    val showPopup by viewModel.showFamilyPopup.collectAsState()
    val currentTitle by viewModel.currentEventTitle.collectAsState()
    val editingState by viewModel.editingMemberState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.manuallyFinalizeEvent() },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Finalizar Culto (Marcar Ausentes)")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            
            // Barra de Pesquisa Rápida
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Buscar membro pelo nome...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            // Lista Otimizada de Membros
            if (members.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Nenhum membro encontrado com '$searchQuery'." else "Nenhum membro cadastrado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(members, key = { it.member.id }) { memberState ->
                        val member = memberState.member
                        
                        val statusText = when {
                            memberState.isAbsent -> "Ausente"
                            memberState.isPresent && memberState.isLate -> {
                                val level = when {
                                    memberState.lateDurationMins <= 15 -> "Atraso leve"
                                    memberState.lateDurationMins <= 30 -> "Atraso moderado"
                                    else -> "Atraso grave"
                                }
                                "Atraso: ${memberState.lateDurationMins} min ($level)"
                            }
                            memberState.isPresent -> "Presente (Pontual)"
                            else -> "Não registrado"
                        }
                        
                        val statusColor = when {
                            memberState.isAbsent -> MaterialTheme.colorScheme.error
                            memberState.isPresent && memberState.isLate -> MaterialTheme.colorScheme.tertiary
                            memberState.isPresent -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        ListItem(
                            headlineContent = { Text(member.fullName) },
                            supportingContent = {
                                Column {
                                    Text(member.vehicleType ?: "Sem condução", style = MaterialTheme.typography.bodySmall)
                                    Text(statusText, color = statusColor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            },
                            trailingContent = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. Botão Presente Pontual (Primary / Verde do Tema)
                                    IconButton(
                                        onClick = {
                                            if (memberState.isPresent && !memberState.isLate) {
                                                viewModel.setAttendanceState(member, AttendanceStatus.NENHUM)
                                            } else {
                                                viewModel.setAttendanceState(member, AttendanceStatus.PONTUAL)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Presente Pontual",
                                            tint = if (memberState.isPresent && !memberState.isLate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                        )
                                    }

                                    // 2. Botão Presente com Atraso (Tertiary / Laranja do Tema)
                                    IconButton(
                                        onClick = {
                                            if (memberState.isPresent && memberState.isLate) {
                                                viewModel.setAttendanceState(member, AttendanceStatus.NENHUM)
                                            } else {
                                                viewModel.setAttendanceState(member, AttendanceStatus.ATRASADO)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = "Presente com Atraso",
                                            tint = if (memberState.isPresent && memberState.isLate) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                        )
                                    }

                                    // 3. Botão Ausente (Error / Vermelho do Tema)
                                    IconButton(
                                        onClick = {
                                            if (memberState.isAbsent) {
                                                viewModel.setAttendanceState(member, AttendanceStatus.NENHUM)
                                            } else {
                                                viewModel.setAttendanceState(member, AttendanceStatus.AUSENTE)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Ausente",
                                            tint = if (memberState.isAbsent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                        )
                                    }

                                    // 4. Botão Editar Chamada / Horário Customizado
                                    IconButton(
                                        onClick = {
                                            viewModel.startEditing(memberState)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar Horário",
                                            tint = if (memberState.isPresent || memberState.isAbsent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                        )
                                    }
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    // Modal de Edição de Horário / Presença
    editingState?.let { targetState ->
        EditAttendanceDialog(
            attendanceState = targetState,
            onDismiss = { viewModel.dismissEditing() },
            onSave = { status, hour, minute, lateMins ->
                viewModel.saveCustomAttendance(
                    member = targetState.member,
                    status = status,
                    customHour = hour,
                    customMinute = minute,
                    customLateMins = lateMins
                )
            }
        )
    }

    // Aciona o Modal Híbrido (Visitantes + Família)
    if (showPopup) {
        FamilyAndVisitorDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showFamilyPopup.value = false }
        )
    }
}
