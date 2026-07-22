package com.koinonia.igreja.presentation.features.members.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.koinonia.igreja.domain.model.MinistryPositionTier

import com.koinonia.igreja.data.local.entity.MinistryRoleEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleRegistrationDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, tier: MinistryPositionTier) -> Unit,
    roleToEdit: MinistryRoleEntity? = null
) {
    var title by remember { mutableStateOf(roleToEdit?.title ?: "") }
    var selectedTier by remember { mutableStateOf(roleToEdit?.tier ?: MinistryPositionTier.SUPPORT) }
    var tierDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (roleToEdit != null) "Editar Cargo" else "Cadastrar Cargo", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorMessage = null },
                    label = { Text("Título do Cargo *") },
                    placeholder = { Text("Ex: Vice-Diretor(a)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Seleção de Tier (Nível do Cargo)
                ExposedDropdownMenuBox(
                    expanded = tierDropdownExpanded,
                    onExpandedChange = { tierDropdownExpanded = !tierDropdownExpanded }
                ) {
                    val labelTier = when (selectedTier) {
                        MinistryPositionTier.DIRECTOR -> "Diretoria / Liderança (DIRECTOR)"
                        MinistryPositionTier.TREASURY -> "Gestão Financeira / Tesouraria (TREASURY)"
                        MinistryPositionTier.SUPPORT -> "Apoio / Equipe (SUPPORT)"
                    }
                    OutlinedTextField(
                        value = labelTier,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nível de Permissão (Tier) *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tierDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = tierDropdownExpanded,
                        onDismissRequest = { tierDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Diretoria / Liderança (DIRECTOR)") },
                            onClick = {
                                selectedTier = MinistryPositionTier.DIRECTOR
                                tierDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Gestão Financeira / Tesouraria (TREASURY)") },
                            onClick = {
                                selectedTier = MinistryPositionTier.TREASURY
                                tierDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Apoio / Equipe (SUPPORT)") },
                            onClick = {
                                selectedTier = MinistryPositionTier.SUPPORT
                                tierDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "O título do cargo é obrigatório."
                        return@Button
                    }
                    onConfirm(title.trim(), selectedTier)
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
