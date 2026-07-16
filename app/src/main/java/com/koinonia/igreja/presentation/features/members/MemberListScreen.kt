package com.koinonia.igreja.presentation.features.members

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberListScreen(
    onNavigateToRegistration: () -> Unit,
    viewModel: MemberListViewModel
) {
    val members by viewModel.membersList.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Membros") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToRegistration) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Membro")
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(members) { member ->
                ListItem(
                    headlineContent = { Text(member.fullName) },
                    supportingContent = { Text(member.phone ?: "Sem telefone") },
                    trailingContent = { 
                        if (member.syncPending) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Sincronização pendente",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } 
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
