package com.koinonia.igreja.presentation.features.reception

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.koinonia.igreja.data.local.entity.MemberEntity
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

@Composable
fun FamilyAndVisitorDialog(
    viewModel: ReceptionViewModel,
    onDismiss: () -> Unit
) {
    val familyMembers by viewModel.currentFamilyMembers.collectAsState()
    var visitorName by remember { mutableStateOf("") }
    var visitorPhone by remember { mutableStateOf("") }
    var visitorSocialMedia by remember { mutableStateOf("") }
    var isWhatsapp by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Múltiplos Check-ins / Visitantes") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (familyMembers.isNotEmpty()) {
                    Text("Outros membros da Família:", style = MaterialTheme.typography.titleSpacerStyle)
                    familyMembers.forEach { member ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(member.fullName, modifier = Modifier.weight(1f))
                            Button(
                                onClick = { viewModel.markPresence(member) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Check-In", fontSize = 12.sp)
                            }
                        }
                    }
                    HorizontalDivider()
                }

                Text("Registrar Novo Visitante:", style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(
                    value = visitorName,
                    onValueChange = { visitorName = it },
                    label = { Text("Nome do Visitante") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = visitorPhone,
                    onValueChange = { visitorPhone = it },
                    label = { Text("Telefone") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isWhatsapp, onCheckedChange = { isWhatsapp = it })
                    Text("Possui WhatsApp?")
                }
                OutlinedTextField(
                    value = visitorSocialMedia,
                    onValueChange = { visitorSocialMedia = it },
                    label = { Text("Rede Social (Opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (visitorName.isNotBlank()) {
                        viewModel.saveVisitor(visitorName, visitorPhone, isWhatsapp, visitorSocialMedia)
                        visitorName = ""
                        visitorPhone = ""
                        visitorSocialMedia = ""
                    }
                    onDismiss()
                }
            ) {
                Text("Registrar Visitante e Concluir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

// Extensão utilitária de tipografia para compatibilidade M3
private val Typography.titleSpacerStyle: androidx.compose.ui.text.TextStyle
    @Composable
    get() = this.titleSmall.copy(fontWeight = FontWeight.Bold)
