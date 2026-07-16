package com.koinonia.igreja.presentation.features.members

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberRegistrationScreen(
    onNavigateBack: () -> Unit,
    viewModel: MemberRegistrationViewModel = hiltViewModel()
) {
    val isSaved by viewModel.isSaved.collectAsState()

    // Reage ao estado de sucesso no salvamento para fechar a tela
    LaunchedEffect(isSaved) {
        if (isSaved) {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Novo Membro") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.saveMember()
                }
            ) {
                Text("Salvar Membro")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { PersonalDataSection(viewModel) }
            item { FamilySection(viewModel) }
            item { TransportSection(viewModel) }
            // Dependentes e Histórico de Ministério viriam em seções adicionais aqui
        }
    }
}

@Composable
fun FamilySection(viewModel: MemberRegistrationViewModel) {
    val isNewFamily by viewModel.isNewFamily.collectAsState()
    val familyNameInput by viewModel.familyNameInput.collectAsState()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Grupo Familiar", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(
                    checked = isNewFamily,
                    onCheckedChange = { viewModel.isNewFamily.value = it }
                )
                Text("Cadastrar nova família?")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isNewFamily) {
                OutlinedTextField(
                    value = familyNameInput,
                    onValueChange = { viewModel.familyNameInput.value = it },
                    label = { Text("Nome da Família") },
                    placeholder = { Text("Ex: Família Souza") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Vincular a uma família existente (Selecione no painel principal)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun TransportSection(viewModel: MemberRegistrationViewModel) {
    val hasVehicle by viewModel.hasVehicle.collectAsState()
    val vehicleType by viewModel.vehicleType.collectAsState()
    val vehicleModel by viewModel.vehicleModel.collectAsState()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Transporte", style = MaterialTheme.typography.titleMedium)
            
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(
                    checked = hasVehicle,
                    onCheckedChange = { viewModel.hasVehicle.value = it }
                )
                Text("Possui condução própria?")
            }

            if (hasVehicle) {
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    RadioButton(
                        selected = vehicleType == "CARRO",
                        onClick = { viewModel.vehicleType.value = "CARRO" }
                    )
                    Text("Carro", modifier = Modifier.padding(top = 12.dp, end = 16.dp))
                    
                    RadioButton(
                        selected = vehicleType == "MOTO",
                        onClick = { viewModel.vehicleType.value = "MOTO" }
                    )
                    Text("Moto", modifier = Modifier.padding(top = 12.dp))
                }
                
                OutlinedTextField(
                    value = vehicleModel,
                    onValueChange = { viewModel.vehicleModel.value = it },
                    label = { Text("Modelo do Veículo") },
                    placeholder = { Text("Ex: Yamaha Fazer 250cc") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PersonalDataSection(viewModel: MemberRegistrationViewModel) {
    val fullName by viewModel.fullName.collectAsState()
    val address by viewModel.address.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val isWhatsapp by viewModel.isWhatsapp.collectAsState()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Dados Pessoais", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = fullName,
                onValueChange = { viewModel.fullName.value = it },
                label = { Text("Nome Completo") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = address,
                onValueChange = { viewModel.address.value = it },
                label = { Text("Endereço") },
                placeholder = { Text("Ex: Rua Direita de Santo Antônio, Salvador - BA") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            
            Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { viewModel.phone.value = it },
                    label = { Text("Telefone") },
                    modifier = Modifier.weight(1f)
                )
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally, modifier = Modifier.padding(start = 8.dp)) {
                    Text("WhatsApp", style = MaterialTheme.typography.labelSmall)
                    Switch(
                        checked = isWhatsapp,
                        onCheckedChange = { viewModel.isWhatsapp.value = it }
                    )
                }
            }
        }
    }
}
