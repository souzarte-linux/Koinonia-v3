package com.koinonia.igreja.presentation.features.members.dialog

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.koinonia.igreja.data.local.entity.MinistryRoleEntity
import com.koinonia.igreja.domain.model.MinistryPositionTier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleManagementDialog(
    allRoles: List<MinistryRoleEntity>,
    onDismiss: () -> Unit,
    onSaveRole: (title: String, tier: MinistryPositionTier, existingId: String?) -> Unit,
    onDeleteRole: (id: String) -> Unit,
    onResetDefaults: (() -> Unit)? = null
) {
    var editingRole by remember { mutableStateOf<MinistryRoleEntity?>(null) }
    var showRegistrationDialog by remember { mutableStateOf(false) }
    var deletingRoleId by remember { mutableStateOf<String?>(null) }
    var showResetConfirmation by remember { mutableStateOf(false) }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Restaurar Padrões IASD", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja substituir todos os cargos atuais pela lista oficial de Cargos da Igreja Adventista do Sétimo Dia?") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDefaults?.invoke()
                        showResetConfirmation = false
                    }
                ) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showRegistrationDialog) {
        RoleRegistrationDialog(
            onDismiss = {
                showRegistrationDialog = false
                editingRole = null
            },
            onConfirm = { title, tier ->
                onSaveRole(title, tier, editingRole?.id)
                showRegistrationDialog = false
                editingRole = null
            },
            roleToEdit = editingRole
        )
    }

    if (deletingRoleId != null) {
        AlertDialog(
            onDismissRequest = { deletingRoleId = null },
            title = { Text("Excluir Cargo", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja excluir este cargo? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        deletingRoleId?.let { onDeleteRole(it) }
                        deletingRoleId = null
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingRoleId = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cargos", fontWeight = FontWeight.Bold) },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 250.dp, max = 450.dp)
            ) {
                if (allRoles.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhum cargo cadastrado.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allRoles, key = { it.id }) { role ->
                            val tierLabel = when (role.tier) {
                                MinistryPositionTier.DIRECTOR -> "Diretoria / Liderança"
                                MinistryPositionTier.TREASURY -> "Gestão Financeira"
                                MinistryPositionTier.SUPPORT -> "Apoio / Equipe"
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = role.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Nível: $tierLabel",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row {
                                        IconButton(
                                            onClick = {
                                                editingRole = role
                                                showRegistrationDialog = true
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                deletingRoleId = role.id
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Excluir",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ExtendedFloatingActionButton(
                    text = { Text("Novo Cargo") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = {
                        editingRole = null
                        showRegistrationDialog = true
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        },
        dismissButton = {
            if (onResetDefaults != null) {
                TextButton(onClick = { showResetConfirmation = true }) {
                    Text("Padrões IASD")
                }
            }
        }
    )
}
