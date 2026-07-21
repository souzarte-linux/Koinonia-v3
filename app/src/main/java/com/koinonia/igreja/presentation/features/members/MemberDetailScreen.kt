package com.koinonia.igreja.presentation.features.members

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    memberId: String,
    viewModel: MembersViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(memberId) {
        viewModel.loadMemberEntityDetails(memberId)
    }

    val member by viewModel.selectedMemberEntity.collectAsState()
    val children by viewModel.selectedChildren.collectAsState()
    val ministries by viewModel.selectedMinistries.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ficha do Membro") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (member == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val currentMember = member!!
            val context = LocalContext.current
            val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Avatar / Foto do Membro
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (currentMember.photoUrl != null) {
                            val bitmap = remember(currentMember.photoUrl) {
                                try {
                                    val uri = Uri.parse(currentMember.photoUrl)
                                    if (Build.VERSION.SDK_INT < 28) {
                                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                                    } else {
                                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                                        ImageDecoder.decodeBitmap(source)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    null
                                }
                            }

                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Foto do Membro",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Foto Carregada",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(70.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Sem Foto",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(70.dp)
                            )
                        }
                    }
                }

                // Card 1: Dados Pessoais
                item {
                    DetailCard(title = "Dados Pessoais") {
                        DetailRow(label = "Nome Completo", value = currentMember.fullName)
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                DetailRow(label = "RG", value = currentMember.rg ?: "Não informado")
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                val formattedCpf = remember(currentMember.cpf) {
                                    formatCpfString(currentMember.cpf)
                                }
                                DetailRow(label = "CPF", value = formattedCpf)
                            }
                        }

                        val age = remember(currentMember.birthDate) { calculateAge(currentMember.birthDate) }
                        val birthStr = remember(currentMember.birthDate) {
                            currentMember.birthDate?.let { dateFormat.format(it) } ?: "Não informada"
                        }
                        DetailRow(
                            label = "Data de Nascimento",
                            value = if (age != null) "$birthStr ($age anos)" else birthStr
                        )

                        DetailRow(label = "Estado Civil", value = currentMember.civilStatus ?: "Solteiro")
                        if (currentMember.civilStatus == "Casado(a)") {
                            DetailRow(label = "Cônjuge", value = currentMember.spouseName ?: "Não informado")
                        }
                    }
                }

                // Card 2: Contato e Localização
                item {
                    DetailCard(title = "Contato e Localização") {
                        val formattedPhone = remember(currentMember.phone) {
                            formatPhoneString(currentMember.phone)
                        }
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "Telefone",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formattedPhone,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (currentMember.isWhatsapp) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "WhatsApp",
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "WhatsApp",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF25D366)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Não é WhatsApp",
                                        tint = Color.Red,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Não é zap",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Red
                                    )
                                }
                            }
                        }

                        DetailRow(label = "E-mail", value = currentMember.socialMedia?.ifEmpty { "Não informado" } ?: "Não informado")
                        
                        DetailRow(
                            label = "Endereço Completo",
                            value = "${currentMember.street ?: ""}, Nº ${currentMember.number ?: ""}\n" +
                                    "Bairro: ${currentMember.neighborhood ?: ""}\n" +
                                    "CEP: ${currentMember.cep ?: ""} - ${currentMember.city ?: ""}/${currentMember.state ?: ""}\n" +
                                    "Complemento: ${currentMember.complement?.ifEmpty { "Não informado" } ?: "Não informado"}"
                        )
                    }
                }

                // Card 3: Eclesiástico e Transporte
                item {
                    DetailCard(title = "Eclesiástico e Transporte") {
                        val baptismStr = remember(currentMember.baptismDate) {
                            currentMember.baptismDate?.let { dateFormat.format(it) } ?: "Não informado"
                        }
                        val rebaptismStr = remember(currentMember.rebaptismDate) {
                            currentMember.rebaptismDate?.let { dateFormat.format(it) } ?: "Não informado"
                        }
                        DetailRow(label = "Data de Batismo", value = baptismStr)
                        DetailRow(label = "Data de Rebatismo", value = rebaptismStr)

                        DetailRow(
                            label = "Condução Própria",
                            value = if (currentMember.hasVehicle) {
                                "${currentMember.vehicleType ?: "Veículo"} - Modelo: ${currentMember.vehicleModel?.ifEmpty { "Não informado" } ?: "Não informado"}"
                            } else {
                                "Não possui"
                            }
                        )
                    }
                }

                // Card 4: Dependentes (Filhos)
                item {
                    DetailCard(title = "Dependentes (Filhos) - Total: ${children.size}") {
                        if (children.isEmpty()) {
                            Text(
                                text = "Nenhum dependente cadastrado.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            children.forEachIndexed { idx, child ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    val bday = child.birthDate?.let { dateFormat.format(it) } ?: "Não informada"
                                    val childAge = calculateAge(child.birthDate)
                                    val ageString = if (childAge != null) " ($childAge anos)" else ""
                                    Text(
                                        text = "${idx + 1}. ${child.fullName} - ${child.gender}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Nasc: $bday$ageString | Batizado: ${if (child.isBaptized) "Sim" else "Não"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (idx < children.size - 1) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Card 5: Histórico Ministerial
                item {
                    DetailCard(title = "Histórico Ministerial") {
                        if (ministries.isEmpty()) {
                            Text(
                                text = "Nenhuma atuação ministerial registrada.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            ministries.forEachIndexed { idx, min ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    val start = min.startDate?.let { dateFormat.format(it) } ?: "Início não informado"
                                    val end = min.endDate?.let { dateFormat.format(it) } ?: "Ativo"
                                    Text(
                                        text = "${min.role} em ${min.ministryName}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Período: $start até $end",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (idx < ministries.size - 1) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            content()
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

fun formatPhoneString(phone: String?): String {
    if (phone == null) return "Não informado"
    val digits = phone.filter { it.isDigit() }
    if (digits.length != 11) return phone
    return "(${digits.substring(0, 2)}) ${digits.substring(2, 3)}.${digits.substring(3, 7)}-${digits.substring(7, 11)}"
}

fun formatCpfString(cpf: String?): String {
    if (cpf == null) return "Não informado"
    val digits = cpf.filter { it.isDigit() }
    if (digits.length != 11) return cpf
    return "${digits.substring(0, 3)}.${digits.substring(3, 6)}.${digits.substring(6, 9)}-${digits.substring(9, 11)}"
}
