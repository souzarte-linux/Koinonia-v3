package com.koinonia.igreja.presentation.features.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import com.koinonia.igreja.presentation.components.AppTopBar
import com.koinonia.igreja.data.local.entity.MemberEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onBack: () -> Unit,
    onMenuClick: (() -> Unit)? = null,
    currentMember: MemberEntity? = null,
    onProfileClick: (() -> Unit)? = null,
    viewModel: ReportsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    // Carrega estatísticas iniciais ao entrar na tela
    LaunchedEffect(Unit) {
        viewModel.loadEventAnalytics("evento_hoje")
    }

    val topAbsent by viewModel.topAbsentMembers.collectAsState(initial = emptyList())
    val arrivalPeaks by viewModel.arrivalPeaks.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Métricas & Relatórios",
                currentMember = currentMember,
                onMenuClick = onMenuClick,
                onBackClick = if (onMenuClick == null) onBack else null,
                onProfileClick = onProfileClick
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Picos de Chegada (Último Culto)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (arrivalPeaks.isEmpty()) {
                            Text(
                                text = "Nenhum pico registrado para o culto atual.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            // Representação textual do gráfico (Substituir por biblioteca Vico Chart)
                            arrivalPeaks.forEach { (time, count) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(time, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$count membros", fontWeight = FontWeight.Bold)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }

            item {
                Text("Atenção Pastoral: Maior Evasão", style = MaterialTheme.typography.titleMedium)
            }

            items(topAbsent) { member ->
                ListItem(
                    headlineContent = { Text(member.fullName) },
                    trailingContent = { 
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text("${member.absenceCount} faltas")
                        } 
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
