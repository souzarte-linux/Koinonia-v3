package com.koinonia.igreja.presentation.features.reports

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.koinonia.igreja.data.local.dao.AttendanceWithMemberInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbsenceFollowUpDialog(
    memberInfo: AttendanceWithMemberInfo,
    onDismiss: () -> Unit,
    onSave: (reason: String, details: String?, contactMethod: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("Saúde Própria") }
    val reasonsList = listOf("Saúde Própria", "Saúde Familiar", "Trabalho", "Estudo", "Atividade Pessoal", "Outros")
    
    var otherDetails by remember { mutableStateOf("") }
    
    var selectedContact by remember { mutableStateOf("WhatsApp") }
    val contactMethods = listOf("Pessoalmente", "WhatsApp", "Rede Social")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Contato Pastoral") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Membro: ${memberInfo.fullName}", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Motivo da Falta:")
                // Simulação de Dropdown para brevidade
                reasonsList.forEach { reason ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedReason == reason, onClick = { selectedReason = reason })
                        Text(reason)
                    }
                }

                if (selectedReason == "Outros") {
                    OutlinedTextField(
                        value = otherDetails,
                        onValueChange = { otherDetails = it },
                        label = { Text("Descreva o motivo") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text("Meio de Contato Realizado:")
                contactMethods.forEach { method ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedContact == method, onClick = { selectedContact = method })
                        Text(method)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(selectedReason, if (selectedReason == "Outros") otherDetails else null, selectedContact)
            }) {
                Text("Salvar Registro")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
