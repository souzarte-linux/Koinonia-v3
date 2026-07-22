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
import com.koinonia.igreja.data.local.entity.MinistryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinistryManagementDialog(
    allMinistries: List<MinistryEntity>,
    onDismiss: () -> Unit,
    onSaveMinistry: (name: String, parentId: String?, minAge: Int?, maxAge: Int?, minMembershipMonths: Int?, notes: String?, existingId: String?) -> Unit,
    onDeleteMinistry: (id: String) -> Unit
) {
    var editingMinistry by remember { mutableStateOf<MinistryEntity?>(null) }
    var showRegistrationDialog by remember { mutableStateOf(false) }
    var deletingMinistryId by remember { mutableStateOf<String?>(null) }

    if (showRegistrationDialog) {
        MinistryRegistrationDialog(
            allMinistries = allMinistries.filter { it.id != editingMinistry?.id },
            onDismiss = {
                showRegistrationDialog = false
                editingMinistry = null
            },
            onConfirm = { name, parentId, minAge, maxAge, minMembershipMonths, notes ->
                onSaveMinistry(name, parentId, minAge, maxAge, minMembershipMonths, notes, editingMinistry?.id)
                showRegistrationDialog = false
                editingMinistry = null
            },
            ministryToEdit = editingMinistry
        )
    }

    if (deletingMinistryId != null) {
        AlertDialog(
            onDismissRequest = { deletingMinistryId = null },
            title = { Text("Excluir Ministério", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja excluir este ministério? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        deletingMinistryId?.let { onDeleteMinistry(it) }
                        deletingMinistryId = null
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingMinistryId = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ministérios", fontWeight = FontWeight.Bold) },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 250.dp, max = 450.dp)
            ) {
                if (allMinistries.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhum ministério cadastrado.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allMinistries, key = { it.id }) { ministry ->
                            val parentName = allMinistries.find { it.id == ministry.parentMinistryId }?.name
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
                                            text = ministry.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (!parentName.isNullOrBlank()) {
                                            Text(
                                                text = "Subministério de: $parentName",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Row {
                                        IconButton(
                                            onClick = {
                                                editingMinistry = ministry
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
                                                deletingMinistryId = ministry.id
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
                    text = { Text("Novo Ministério") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = {
                        editingMinistry = null
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
        }
    )
}
