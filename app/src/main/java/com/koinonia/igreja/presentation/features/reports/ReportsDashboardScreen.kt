package com.koinonia.igreja.presentation.features.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReportsDashboardScreen(
    viewModel: ReportsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val attendanceRate by viewModel.attendanceRate.collectAsState()
    val totalMembers by viewModel.totalMembers.collectAsState()
    val absentCount by viewModel.absentCount.collectAsState()
    val visitorCount by viewModel.visitorCountThisMonth.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Dashboard Geral",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Cartões de estatísticas principais
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total de Membros",
                    value = "$totalMembers",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Taxa de Presença",
                    value = "${String.format("%.1f", attendanceRate)}%",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Ausentes Hoje",
                    value = "$absentCount",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Novos Visitantes",
                    value = "$visitorCount",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Gráfico Simples / Caixa de Insight
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Metas & Crescimento",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Presença média subiu 4% em relação ao mês anterior. Foco nas visitas domiciliares para os $absentCount ausentes recorrentes.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Button(
            onClick = onBack,
            colors = ButtonDefaults.outlinedButtonColors(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Voltar ao Menu Principal")
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
