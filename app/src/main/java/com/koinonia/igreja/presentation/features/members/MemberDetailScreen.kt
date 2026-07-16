package com.koinonia.igreja.presentation.features.members

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.koinonia.igreja.core.util.SalvadorDateTimeFormatter

@Composable
fun MemberDetailScreen(
    memberId: String,
    viewModel: MembersViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(memberId) {
        viewModel.loadMemberDetails(memberId)
    }

    val member by viewModel.selectedMember.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Ficha do Membro",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (member == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val currentMember = member!!
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = currentMember.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Cargo/Ministério: ${currentMember.role}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                DetailItem(label = "E-mail", value = currentMember.email)
                DetailItem(label = "Telefone", value = currentMember.phone)
                DetailItem(
                    label = "Data de Admissão",
                    value = SalvadorDateTimeFormatter.format(currentMember.joinedAt)
                )
                DetailItem(
                    label = "Status de Cadastro",
                    value = if (currentMember.isActive) "Ativo no Sistema" else "Inativo/Desligado"
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        viewModel.deleteMember(currentMember.id) {
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Excluir Cadastro", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Button(
            onClick = onBack,
            colors = ButtonDefaults.outlinedButtonColors(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Voltar", fontSize = 16.sp)
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
