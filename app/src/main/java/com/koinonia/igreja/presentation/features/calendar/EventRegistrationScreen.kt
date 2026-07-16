package com.koinonia.igreja.presentation.features.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventRegistrationScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados do Formulário (Numa implementação real, estariam no ViewModel)
    var title by remember { mutableStateOf("") }
    var locationType by remember { mutableStateOf("IGREJA_LOCAL") }
    var address by remember { mutableStateOf("") }
    var selectedMinistry by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Novo Evento/Culto Especial") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateBack) {
                Text("Salvar", modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título (Ex: Semana Evangelística)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Seleção de Local
            Text("Localização", style = MaterialTheme.typography.titleMedium)
            
            Row {
                RadioButton(selected = locationType == "IGREJA_LOCAL", onClick = { locationType = "IGREJA_LOCAL" })
                Text("Igreja Sede", modifier = Modifier.padding(top = 12.dp, end = 8.dp))
            }
            Row {
                RadioButton(selected = locationType == "URBANO", onClick = { locationType = "URBANO" })
                Text("Urbano (Bairro/Rua)", modifier = Modifier.padding(top = 12.dp, end = 8.dp))
            }
            Row {
                RadioButton(selected = locationType == "EXTERNO", onClick = { locationType = "EXTERNO" })
                Text("Externo (Parques/Ar Livre)", modifier = Modifier.padding(top = 12.dp))
            }

            // Exibe campos adicionais condicionalmente
            if (locationType == "URBANO" || locationType == "EXTERNO") {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { 
                        Text(if (locationType == "URBANO") "Endereço no bairro e Rua" else "Nome do Local/Parque") 
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Ministério Responsável
            Text("Ministério Responsável", style = MaterialTheme.typography.titleMedium)
            // Aqui entraria um DropdownMenu (ExposedDropdownMenuBox) com a lista de ministérios
            OutlinedTextField(
                value = selectedMinistry ?: "",
                onValueChange = {},
                label = { Text("Selecione o Ministério (Opcional)") },
                enabled = false, // Simulando um dropdown read-only que abre modal
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
