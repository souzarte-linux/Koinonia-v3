package com.koinonia.igreja.presentation.features.members.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.koinonia.igreja.data.local.entity.MinistryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinistryRegistrationDialog(
    allMinistries: List<MinistryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, parentId: String?, minAge: Int?, maxAge: Int?, minMembershipMonths: Int?, notes: String?) -> Unit,
    ministryToEdit: MinistryEntity? = null
) {
    var name by remember { mutableStateOf(ministryToEdit?.name ?: "") }
    var selectedParentId by remember { mutableStateOf<String?>(ministryToEdit?.parentMinistryId) }
    var parentDropdownExpanded by remember { mutableStateOf(false) }
    var minAgeText by remember { mutableStateOf(ministryToEdit?.minAge?.toString() ?: "") }
    var maxAgeText by remember { mutableStateOf(ministryToEdit?.maxAge?.toString() ?: "") }
    var minMembershipMonthsText by remember { mutableStateOf(ministryToEdit?.minMembershipMonths?.toString() ?: "") }
    var notes by remember { mutableStateOf(ministryToEdit?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ministryToEdit != null) "Editar Ministério" else "Cadastrar Ministério", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
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
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Nome do Ministério *") },
                    placeholder = { Text("Ex: Ministério da Saúde") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Ministério Pai (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = parentDropdownExpanded,
                    onExpandedChange = { parentDropdownExpanded = !parentDropdownExpanded }
                ) {
                    val selectedParentName = allMinistries.find { it.id == selectedParentId }?.name ?: "Nenhum (Ministério Principal)"
                    OutlinedTextField(
                        value = selectedParentName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ministério Pai (Opcional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = parentDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = parentDropdownExpanded,
                        onDismissRequest = { parentDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nenhum (Ministério Principal)") },
                            onClick = {
                                selectedParentId = null
                                parentDropdownExpanded = false
                            }
                        )
                        allMinistries.forEach { ministry ->
                            DropdownMenuItem(
                                text = { Text(ministry.name) },
                                onClick = {
                                    selectedParentId = ministry.id
                                    parentDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minAgeText,
                        onValueChange = { minAgeText = it.filter { c -> c.isDigit() } },
                        label = { Text("Idade Mín.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxAgeText,
                        onValueChange = { maxAgeText = it.filter { c -> c.isDigit() } },
                        label = { Text("Idade Máx.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = minMembershipMonthsText,
                    onValueChange = { minMembershipMonthsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Meses Mínimos de Batismo") },
                    placeholder = { Text("Ex: 6") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações/Notas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "O nome do ministério é obrigatório."
                        return@Button
                    }
                    onConfirm(
                        name.trim(),
                        selectedParentId,
                        minAgeText.toIntOrNull(),
                        maxAgeText.toIntOrNull(),
                        minMembershipMonthsText.toIntOrNull(),
                        notes.ifBlank { null }
                    )
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
