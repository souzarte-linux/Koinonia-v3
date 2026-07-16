package com.koinonia.igreja.presentation.features.reception

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.koinonia.igreja.presentation.features.reception.components.FamilyAndVisitorDialog
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceptionScreen(
    onBack: () -> Unit,
    viewModel: ReceptionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    // Inicializa a recepção para fins de compilação/teste com um ID fixo ou obtido
    LaunchedEffect(Unit) {
        viewModel.initReception("evento_hoje", ZonedDateTime.now())
    }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val members by viewModel.membersList.collectAsState()
    val showPopup by viewModel.showFamilyPopup.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recepção: Culto Ordinário") },
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
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(members) { member ->
                    ListItem(
                        headlineContent = { Text(member.fullName) },
                        supportingContent = { Text(member.vehicleType ?: "Sem condução") },
                        trailingContent = {
                            Checkbox(
                                checked = false, // Aqui leria o estado real de presença
                                onCheckedChange = { viewModel.markPresence(member) }
                            )
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    // Aciona o Modal Híbrido (Visitantes + Família)
    if (showPopup) {
        FamilyAndVisitorDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showFamilyPopup.value = false }
        )
    }
}


