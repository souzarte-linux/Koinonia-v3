package com.koinonia.igreja.presentation.features.members

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    // Estado local para controle do Accordion (quais seções estão expandidas)
    val expandedSections = remember {
        mutableStateMapOf(
            "Perfil" to true,
            "Contato" to false,
            "Eclesiástico" to false,
            "Família" to false,
            "Transporte" to false,
            "Dependentes" to false,
            "Ministério" to false
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo Membro") },
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
            // Seção 1: Perfil
            item {
                ExpandableSection(
                    title = "1. Perfil",
                    isExpanded = expandedSections["Perfil"] ?: false,
                    onToggle = { expandedSections["Perfil"] = !(expandedSections["Perfil"] ?: false) }
                ) {
                    PersonalSection(viewModel)
                }
            }

            // Seção 2: Contato e Localização
            item {
                ExpandableSection(
                    title = "2. Contato e Localização",
                    isExpanded = expandedSections["Contato"] ?: false,
                    onToggle = { expandedSections["Contato"] = !(expandedSections["Contato"] ?: false) }
                ) {
                    ContactLocationSection(viewModel)
                }
            }

            // Seção 3: Histórico Eclesiástico
            item {
                ExpandableSection(
                    title = "3. Histórico Eclesiástico",
                    isExpanded = expandedSections["Eclesiástico"] ?: false,
                    onToggle = { expandedSections["Eclesiástico"] = !(expandedSections["Eclesiástico"] ?: false) }
                ) {
                    EcclesiasticalSection(viewModel)
                }
            }

            // Seção 4: Estrutura Familiar
            item {
                ExpandableSection(
                    title = "4. Estrutura Familiar",
                    isExpanded = expandedSections["Família"] ?: false,
                    onToggle = { expandedSections["Família"] = !(expandedSections["Família"] ?: false) }
                ) {
                    FamilyStructureSection(viewModel)
                }
            }

            // Seção 5: Transporte
            item {
                ExpandableSection(
                    title = "5. Transporte",
                    isExpanded = expandedSections["Transporte"] ?: false,
                    onToggle = { expandedSections["Transporte"] = !(expandedSections["Transporte"] ?: false) }
                ) {
                    TransportSection(viewModel)
                }
            }

            // Seção 6: Dependentes (Filhos)
            item {
                ExpandableSection(
                    title = "6. Dependentes (Filhos)",
                    isExpanded = expandedSections["Dependentes"] ?: false,
                    onToggle = { expandedSections["Dependentes"] = !(expandedSections["Dependentes"] ?: false) }
                ) {
                    ChildrenSection(viewModel)
                }
            }

            // Seção 7: Atuação Ministerial
            item {
                ExpandableSection(
                    title = "7. Atuação Ministerial",
                    isExpanded = expandedSections["Ministério"] ?: false,
                    onToggle = { expandedSections["Ministério"] = !(expandedSections["Ministério"] ?: false) }
                ) {
                    MinistrySection(viewModel)
                }
            }

            // Espaço adicional no final para rolar além do FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ExpandableSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Recolher" else "Expandir"
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}

// Helper para cálculo da idade baseado na data de nascimento
fun calculateAge(birthDate: Date?): Int? {
    if (birthDate == null) return null
    val birthCal = java.util.Calendar.getInstance().apply { time = birthDate }
    val todayCal = java.util.Calendar.getInstance()
    var age = todayCal.get(java.util.Calendar.YEAR) - birthCal.get(java.util.Calendar.YEAR)
    if (todayCal.get(java.util.Calendar.DAY_OF_YEAR) < birthCal.get(java.util.Calendar.DAY_OF_YEAR)) {
        age--
    }
    return age
}

@Composable
fun PersonalSection(viewModel: MemberRegistrationViewModel) {
    val fullName by viewModel.fullName.collectAsState()
    val photoUrl by viewModel.photoUrl.collectAsState()
    val birthDate by viewModel.birthDate.collectAsState()
    val civilStatus by viewModel.civilStatus.collectAsState()

    // Lançador nativo para selecionar imagens da galeria (Photo Picker)
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.photoUrl.value = uri.toString()
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Foto de Perfil com suporte real à exibição da imagem selecionada
        PhotoUploadPlaceholder(
            photoUrl = photoUrl,
            onClick = {
                pickMediaLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { viewModel.fullName.value = it },
            label = { Text("Nome Completo") },
            placeholder = { Text("Digite o nome completo") },
            modifier = Modifier.fillMaxWidth()
        )

        // Exibe a idade ao lado da data de nascimento se selecionada
        val age = remember(birthDate) { calculateAge(birthDate) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                DateField(
                    label = "Data de Nascimento",
                    selectedDate = birthDate,
                    onDateSelected = { viewModel.birthDate.value = it }
                )
            }
            if (age != null) {
                Surface(
                    shape = FilterChipDefaults.shape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "$age anos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        SimpleDropdownField(
            label = "Estado Civil",
            options = listOf("Solteiro(a)", "Casado(a)", "Divorciado(a)", "Viúvo(a)", "União Estável"),
            selectedOption = civilStatus,
            onOptionSelected = { viewModel.civilStatus.value = it }
        )
    }
}

@Composable
fun ContactLocationSection(viewModel: MemberRegistrationViewModel) {
    val phone by viewModel.phone.collectAsState()
    val isWhatsapp by viewModel.isWhatsapp.collectAsState()
    val socialMedia by viewModel.socialMedia.collectAsState()
    val cep by viewModel.cep.collectAsState()
    val street by viewModel.street.collectAsState()
    val neighborhood by viewModel.neighborhood.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = phone,
                onValueChange = { input -> 
                    viewModel.phone.value = input.filter { it.isDigit() }.take(11)
                },
                label = { Text("Telefone") },
                placeholder = { Text("Ex: 71999998888") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text("WhatsApp", style = MaterialTheme.typography.labelSmall)
                Switch(
                    checked = isWhatsapp,
                    onCheckedChange = { viewModel.isWhatsapp.value = it }
                )
            }
        }

        OutlinedTextField(
            value = socialMedia,
            onValueChange = { viewModel.socialMedia.value = it },
            label = { Text("Rede Social (@)") },
            placeholder = { Text("Ex: instagram") },
            modifier = Modifier.fillMaxWidth()
        )

        // Adição do Campo CEP com teclado numérico acima do Logradouro
        OutlinedTextField(
            value = cep,
            onValueChange = { input ->
                viewModel.cep.value = input.filter { it.isDigit() }.take(8)
            },
            label = { Text("CEP") },
            placeholder = { Text("Ex: 40000000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = street,
            onValueChange = { viewModel.street.value = it },
            label = { Text("Logradouro (Rua/Av)") },
            placeholder = { Text("Ex: Rua das Flores, 123") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = neighborhood,
            onValueChange = { viewModel.neighborhood.value = it },
            label = { Text("Bairro") },
            placeholder = { Text("Ex: Centro") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EcclesiasticalSection(viewModel: MemberRegistrationViewModel) {
    val baptismDate by viewModel.baptismDate.collectAsState()
    val rebaptismDate by viewModel.rebaptismDate.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DateField(
            label = "Data de Batismo",
            selectedDate = baptismDate,
            onDateSelected = { viewModel.baptismDate.value = it }
        )

        DateField(
            label = "Data de Rebatismo (Opcional)",
            selectedDate = rebaptismDate,
            onDateSelected = { viewModel.rebaptismDate.value = it }
        )
    }
}

@Composable
fun FamilyStructureSection(viewModel: MemberRegistrationViewModel) {
    val isNewFamily by viewModel.isNewFamily.collectAsState()
    val familyNameInput by viewModel.familyNameInput.collectAsState()
    val families by viewModel.families.collectAsState()
    val selectedFamilyId by viewModel.selectedFamilyId.collectAsState()

    val selectedFamily = families.find { it.id == selectedFamilyId }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isNewFamily,
                onCheckedChange = { viewModel.isNewFamily.value = it }
            )
            Text("Cadastrar nova família?")
        }

        if (isNewFamily) {
            OutlinedTextField(
                value = familyNameInput,
                onValueChange = { viewModel.familyNameInput.value = it },
                label = { Text("Nome da Família") },
                placeholder = { Text("Ex: Família Souza") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            SimpleDropdownField(
                label = "Vincular a Família Existente",
                options = families.map { it.name },
                selectedOption = selectedFamily?.name ?: "Selecione uma família",
                onOptionSelected = { name ->
                    val found = families.find { it.name == name }
                    viewModel.selectedFamilyId.value = found?.id
                }
            )
        }
    }
}

@Composable
fun TransportSection(viewModel: MemberRegistrationViewModel) {
    val hasVehicle by viewModel.hasVehicle.collectAsState()
    val vehicleType by viewModel.vehicleType.collectAsState()
    val vehicleModel by viewModel.vehicleModel.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = hasVehicle,
                onCheckedChange = { viewModel.hasVehicle.value = it }
            )
            Text("Possui condução própria?")
        }

        if (hasVehicle) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = vehicleType == "CARRO",
                        onClick = { viewModel.vehicleType.value = "CARRO" }
                    )
                    Text("Carro")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = vehicleType == "MOTO",
                        onClick = { viewModel.vehicleType.value = "MOTO" }
                    )
                    Text("Moto")
                }
            }

            OutlinedTextField(
                value = vehicleModel,
                onValueChange = { viewModel.vehicleModel.value = it },
                label = { Text("Modelo do Veículo") },
                placeholder = { Text("Ex: Honda Civic / CG 160") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ChildrenSection(viewModel: MemberRegistrationViewModel) {
    val childrenList by viewModel.children.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { viewModel.addChild() },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar Filho")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Adicionar Filho")
        }

        childrenList.forEachIndexed { index, child ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Filho #${index + 1}", style = MaterialTheme.typography.titleSmall)
                        IconButton(onClick = { viewModel.removeChild(index) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remover Filho",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    OutlinedTextField(
                        value = child.fullName,
                        onValueChange = { viewModel.updateChild(index, child.copy(fullName = it)) },
                        label = { Text("Nome Completo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text("Sexo: ", style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = child.gender == "Masculino",
                                onClick = { viewModel.updateChild(index, child.copy(gender = "Masculino")) }
                            )
                            Text("Masc")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = child.gender == "Feminino",
                                onClick = { viewModel.updateChild(index, child.copy(gender = "Feminino")) }
                            )
                            Text("Fem")
                        }
                    }

                    DateField(
                        label = "Data de Nascimento",
                        selectedDate = child.birthDate,
                        onDateSelected = { date ->
                            viewModel.updateChild(index, child.copy(birthDate = date))
                        }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("É batizado?")
                        Switch(
                            checked = child.isBaptized,
                            onCheckedChange = { viewModel.updateChild(index, child.copy(isBaptized = it)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MinistrySection(viewModel: MemberRegistrationViewModel) {
    val ministryRolesList by viewModel.ministryRoles.collectAsState()

    val ministryOptions = listOf(
        "Louvor e Adoração",
        "Ensino e Discipulado",
        "Infantil (Koinoninho)",
        "Jovens e Adolescentes",
        "Ação Social",
        "Comunicação e Mídia",
        "Diaconato",
        "Intercessão e Oração"
    )

    val roleOptions = listOf(
        "Líder de Ministério",
        "Vice-Líder",
        "Integrante",
        "Apoio Técnico",
        "Professor",
        "Diácono / Diaconisa"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { viewModel.addMinistryRole() },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar Cargo")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Adicionar Cargo")
        }

        ministryRolesList.forEachIndexed { index, role ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cargo #${index + 1}", style = MaterialTheme.typography.titleSmall)
                        IconButton(onClick = { viewModel.removeMinistryRole(index) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remover Cargo",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    SimpleDropdownField(
                        label = "Ministério",
                        options = ministryOptions,
                        selectedOption = role.ministryName.ifEmpty { "Selecione o Ministério" },
                        onOptionSelected = { name ->
                            viewModel.updateMinistryRole(index, role.copy(ministryName = name, ministryId = name))
                        }
                    )

                    SimpleDropdownField(
                        label = "Cargo",
                        options = roleOptions,
                        selectedOption = role.role.ifEmpty { "Selecione o Cargo" },
                        onOptionSelected = { roleName ->
                            viewModel.updateMinistryRole(index, role.copy(role = roleName))
                        }
                    )

                    DateField(
                        label = "Data Início",
                        selectedDate = role.startDate,
                        onDateSelected = { date ->
                            viewModel.updateMinistryRole(index, role.copy(startDate = date))
                        }
                    )

                    DateField(
                        label = "Data Fim (Opcional)",
                        selectedDate = role.endDate,
                        onDateSelected = { date ->
                            viewModel.updateMinistryRole(index, role.copy(endDate = date))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PhotoUploadPlaceholder(
    photoUrl: String?,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
    ) {
        if (photoUrl != null) {
            val bitmap = remember(photoUrl) {
                try {
                    val uri = Uri.parse(photoUrl)
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
                    contentDescription = "Foto Selecionada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Foto Carregada",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(60.dp)
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Adicionar Foto",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Adicionar Foto",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogComponent(
    initialDate: Long? = null,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    onDismiss()
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun DateField(
    label: String,
    selectedDate: Date?,
    onDateSelected: (Date?) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedDate != null) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    "$label: ${sdf.format(selectedDate)}"
                } else {
                    "Selecionar $label"
                },
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Selecionar data"
            )
        }
    }

    if (showDialog) {
        DatePickerDialogComponent(
            initialDate = selectedDate?.time,
            onDateSelected = { timeMillis ->
                onDateSelected(timeMillis?.let { Date(it) })
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun SimpleDropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expandir"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
