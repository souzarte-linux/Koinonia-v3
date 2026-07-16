package com.koinonia.igreja.presentation.features.reception.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                
                // 1. SEÇÃO DE VISITANTES (NO TOPO)
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
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = visPhone,
                                onValueChange = { visPhone = it },
                                label = { Text("Celular (xx) x.xxxx-xxxx") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = visWhatsapp, onCheckedChange = { visWhatsapp = it })
                                Text("É WhatsApp?")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = visSocial,
                                onValueChange = { visSocial = it },
                                label = { Text("Rede Social (Instagram/Facebook)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Button(
                                onClick = { 
                                    if (visName.isNotBlank()) {
                                        viewModel.saveVisitor(visName, visPhone, visWhatsapp, visSocial)
                                        visName = ""
                                        visPhone = ""
                                        visSocial = ""
                                        visitorExpanded = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Text("Salvar Visitante")
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 2. SEÇÃO DE FAMÍLIA
                Text("Confirmar Família", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(familyMembers) { relative ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(relative.fullName, modifier = Modifier.weight(1f))
                            Button(onClick = { viewModel.markPresence(relative) }) {
                                Text("Presente")
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
