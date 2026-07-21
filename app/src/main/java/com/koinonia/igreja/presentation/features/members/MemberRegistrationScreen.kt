package com.koinonia.igreja.presentation.features.members

import android.graphics.Bitmap
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.koinonia.igreja.domain.model.AppRole
import java.io.File
import java.io.FileOutputStream
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
    val validationError by viewModel.validationError.collectAsState()

    // Reage ao estado de sucesso no salvamento para fechar a tela
    LaunchedEffect(isSaved) {
        if (isSaved) {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    if (validationError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.validationError.value = null },
            title = { Text("Dados Necessários") },
            text = { Text(validationError!!) },
            confirmButton = {
                Button(onClick = { viewModel.validationError.value = null }) {
                    Text("OK")
                }
            }
        )
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
            val editingMemberId by viewModel.editingMemberId.collectAsState()
            TopAppBar(
                title = { Text(if (editingMemberId != null) "Editar Membro" else "Novo Membro") },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigateBack()
                        viewModel.resetState()
                    }) {
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

@Composable
fun PersonalSection(viewModel: MemberRegistrationViewModel) {
    val fullName by viewModel.fullName.collectAsState()
    val photoUrl by viewModel.photoUrl.collectAsState()
    val birthDate by viewModel.birthDate.collectAsState()
    val civilStatus by viewModel.civilStatus.collectAsState()
    val rg by viewModel.rg.collectAsState()
    val cpf by viewModel.cpf.collectAsState()
    val spouseId by viewModel.spouseId.collectAsState()
    val spouseName by viewModel.spouseName.collectAsState()
    val isSpouseMember by viewModel.isSpouseMember.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()

    val context = LocalContext.current
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var showSpouseDialog by remember { mutableStateOf(false) }

    // Launcher para Câmera (Tirar Foto)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val file = File(context.cacheDir, "temp_camera_photo_${System.currentTimeMillis()}.jpg")
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()
                viewModel.photoUrl.value = Uri.fromFile(file).toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Launcher para Galeria (Selecionar Foto)
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
            onClick = { showPhotoSourceDialog = true }
        )

        // Diálogo para escolha de origem da Foto (Câmera vs Galeria)
        if (showPhotoSourceDialog) {
            AlertDialog(
                onDismissRequest = { showPhotoSourceDialog = false },
                title = { Text("Selecione a Origem") },
                text = { Text("Deseja tirar uma foto com a câmera ou escolher uma foto da galeria do aparelho?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            cameraLauncher.launch(null)
                        }
                    ) {
                        Text("Câmera")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            pickMediaLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Text("Galeria")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { viewModel.fullName.value = it },
            label = { Text("Nome Completo") },
            placeholder = { Text("Digite o nome completo") },
            modifier = Modifier.fillMaxWidth()
        )

        // Campos RG e CPF
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = rg,
                onValueChange = { viewModel.rg.value = it },
                label = { Text("RG") },
                placeholder = { Text("Ex: 12345678") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = cpf,
                onValueChange = { input -> 
                    viewModel.cpf.value = input.filter { it.isDigit() }.take(11)
                },
                label = { Text("CPF") },
                placeholder = { Text("Ex: 123.456.789-01") },
                visualTransformation = CpfVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

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
            onOptionSelected = { selected ->
                viewModel.civilStatus.value = selected
                if (selected == "Casado(a)") {
                    showSpouseDialog = true
                }
            }
        )

        // Dialog para Seleção/Digitação de Cônjuge caso Estado Civil seja Casado(a)
        if (showSpouseDialog && civilStatus == "Casado(a)") {
            AlertDialog(
                onDismissRequest = { showSpouseDialog = false },
                title = { Text("Vincular Cônjuge") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = !isSpouseMember,
                                onCheckedChange = { viewModel.isSpouseMember.value = !it }
                            )
                            Text("Cônjuge não é membro cadastrado")
                        }

                        if (isSpouseMember) {
                            val options = allMembers.map { it.fullName }
                            val selectedSpouse = allMembers.find { it.id == spouseId }
                            SimpleDropdownField(
                                label = "Selecione o Membro",
                                options = options,
                                selectedOption = selectedSpouse?.fullName ?: "Nenhum membro selecionado",
                                onOptionSelected = { name ->
                                    val found = allMembers.find { it.fullName == name }
                                    viewModel.spouseId.value = found?.id
                                }
                            )
                        } else {
                            OutlinedTextField(
                                value = spouseName,
                                onValueChange = { viewModel.spouseName.value = it },
                                label = { Text("Nome do Cônjuge") },
                                placeholder = { Text("Digite o nome completo do cônjuge") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showSpouseDialog = false }) {
                        Text("Confirmar")
                    }
                }
            )
        }
    }
}

@Composable
fun ContactLocationSection(viewModel: MemberRegistrationViewModel) {
    val phone by viewModel.phone.collectAsState()
    val isWhatsapp by viewModel.isWhatsapp.collectAsState()
    val socialMedia by viewModel.socialMedia.collectAsState()
    val email by viewModel.email.collectAsState()
    val createAccess by viewModel.createAccess.collectAsState()
    val generatedPassword by viewModel.generatedPassword.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState(initial = AppRole.NONE)
    val cep by viewModel.cep.collectAsState()
    val street by viewModel.street.collectAsState()
    val number by viewModel.number.collectAsState()
    val neighborhood by viewModel.neighborhood.collectAsState()
    val city by viewModel.city.collectAsState()
    val state by viewModel.state.collectAsState()
    val complement by viewModel.complement.collectAsState()

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
                label = { Text("Telefone (Usado para login)") },
                placeholder = { Text("Ex: (71) 9.9999-8888") },
                visualTransformation = PhoneVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text("WhatsApp", style = MaterialTheme.typography.labelSmall)
                // Switch com a cor verde do WhatsApp
                Switch(
                    checked = isWhatsapp,
                    onCheckedChange = { viewModel.isWhatsapp.value = it },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Color(0xFF25D366).copy(alpha = 0.5f),
                        checkedThumbColor = Color(0xFF25D366)
                    )
                )
            }
        }

        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.email.value = it },
            label = { Text("E-mail (Usado para login)") },
            placeholder = { Text("Ex: joao@gmail.com") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = socialMedia,
            onValueChange = { viewModel.socialMedia.value = it },
            label = { Text("Rede Social (@)") },
            placeholder = { Text("Ex: instagram") },
            modifier = Modifier.fillMaxWidth()
        )

        if (currentRole.hasFullAccess) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Criar acesso ao app", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Gera conta com e-mail/celular e senha temporária",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = createAccess,
                    onCheckedChange = { viewModel.createAccess.value = it }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (generatedPassword != null) {
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            AlertDialog(
                onDismissRequest = { viewModel.generatedPassword.value = null },
                title = { Text("Acesso Criado com Sucesso!") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("A conta de login foi gerada para o membro. Informe a senha temporária abaixo:")
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = generatedPassword!!,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                        Text(
                            "Aviso: Anote e informe esta senha ao membro com segurança — ela não será mostrada novamente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(generatedPassword!!))
                            viewModel.generatedPassword.value = null
                        }
                    ) {
                        Text("Copiar Senha e Fechar")
                    }
                }
            )
        }

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

        // Logradouro (Rua/Av) e Número na mesma linha
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = street,
                onValueChange = { viewModel.street.value = it },
                label = { Text("Logradouro (Rua/Av)") },
                placeholder = { Text("Ex: Rua das Flores") },
                modifier = Modifier.weight(3f)
            )
            OutlinedTextField(
                value = number,
                onValueChange = { viewModel.number.value = it },
                label = { Text("Nº") },
                placeholder = { Text("123") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = neighborhood,
            onValueChange = { viewModel.neighborhood.value = it },
            label = { Text("Bairro") },
            placeholder = { Text("Ex: Centro") },
            modifier = Modifier.fillMaxWidth()
        )

        // Cidade e Estado (UF) na mesma linha
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = city,
                onValueChange = { viewModel.city.value = it },
                label = { Text("Cidade") },
                placeholder = { Text("Ex: Salvador") },
                modifier = Modifier.weight(3f)
            )
            OutlinedTextField(
                value = state,
                onValueChange = { viewModel.state.value = it },
                label = { Text("UF") },
                placeholder = { Text("BA") },
                modifier = Modifier.weight(1f)
            )
        }

        // Complemento limitado a 400 caracteres com contador
        OutlinedTextField(
            value = complement,
            onValueChange = { if (it.length <= 400) viewModel.complement.value = it },
            label = { Text("Complemento") },
            placeholder = { Text("Ex: Apto 402, Bloco B") },
            supportingText = {
                Text(
                    text = "${complement.length}/400",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            },
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
    val allMembers by viewModel.allMembers.collectAsState()
    
    // Filtra membros batizados da igreja para busca
    val baptizedMembers = remember(allMembers) {
        allMembers.filter { it.baptismDate != null }
    }

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

                    // Autocompletar e busca assistida por membros batizados
                    var showSuggestions by remember { mutableStateOf(false) }
                    val matchingMembers = remember(child.fullName, baptizedMembers) {
                        if (child.fullName.length >= 3) {
                            baptizedMembers.filter { it.fullName.contains(child.fullName, ignoreCase = true) }
                        } else emptyList()
                    }
                    val isFound = remember(child.fullName, baptizedMembers) {
                        baptizedMembers.any { it.fullName.equals(child.fullName, ignoreCase = true) }
                    }

                    OutlinedTextField(
                        value = child.fullName,
                        onValueChange = { query -> 
                            viewModel.updateChild(index, child.copy(fullName = query))
                            showSuggestions = true
                        },
                        label = { Text("Nome Completo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Lista de sugestões abaixo do campo
                    if (showSuggestions && matchingMembers.isNotEmpty()) {
                        Surface(
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 120.dp)
                        ) {
                            LazyColumn {
                                items(matchingMembers) { member ->
                                    DropdownMenuItem(
                                        text = { Text("${member.fullName} (${calculateAge(member.birthDate)} anos)") },
                                        onClick = {
                                            viewModel.updateChild(
                                                index,
                                                child.copy(
                                                    fullName = member.fullName,
                                                    birthDate = member.birthDate,
                                                    isBaptized = true
                                                )
                                            )
                                            showSuggestions = false
                                        }
                                    )
                                }
                            }
                        }
                    } else if (child.fullName.length >= 3 && !isFound) {
                        // Aviso caso não seja membro batizado cadastrado
                        Text(
                            text = "⚠️ Filho(a) não cadastrado, mas o nome digitado será utilizado no campo.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

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
    val allMinistries by viewModel.allMinistries.collectAsState(initial = emptyList())
    val allRoles by viewModel.allRoles.collectAsState(initial = emptyList())

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
                        options = allMinistries.map { it.name },
                        selectedOption = role.ministryName.ifEmpty { "Selecione o Ministério" },
                        onOptionSelected = { name ->
                            val matchedId = allMinistries.find { it.name == name }?.id ?: name
                            viewModel.updateMinistryRole(index, role.copy(ministryName = name, ministryId = matchedId))
                        }
                    )

                    val roleListOptions = remember(allRoles) {
                        if (allRoles.isNotEmpty()) {
                            allRoles.map { it.title }
                        } else {
                            viewModel.roleOptions
                        }
                    }

                    SimpleDropdownField(
                        label = "Cargo",
                        options = roleListOptions,
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

class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 11) text.text.substring(0, 11) else text.text
        val out = StringBuilder()
        
        for (i in trimmed.indices) {
            if (i == 0) out.append("(")
            out.append(trimmed[i])
            if (i == 1) out.append(") ")
            if (i == 2) out.append(".")
            if (i == 6) out.append("-")
        }
        
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                var transformedOffset = offset
                if (offset > 0) transformedOffset += 1 // "("
                if (offset > 2) transformedOffset += 2 // ") "
                if (offset > 3) transformedOffset += 1 // "."
                if (offset > 7) transformedOffset += 1 // "-"
                return transformedOffset.coerceAtMost(out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                var originalOffset = offset
                if (offset > 0) originalOffset -= 1 // "("
                if (offset > 3) originalOffset -= 2 // ") "
                if (offset > 5) originalOffset -= 1 // "."
                if (offset > 10) originalOffset -= 1 // "-"
                return originalOffset.coerceAtMost(trimmed.length)
            }
        }
        
        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}

fun calculateAge(birthDate: Date?): Int? {
    if (birthDate == null) return null
    val birth = java.util.Calendar.getInstance().apply { time = birthDate }
    val today = java.util.Calendar.getInstance()
    var age = today.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
    if (today.get(java.util.Calendar.DAY_OF_YEAR) < birth.get(java.util.Calendar.DAY_OF_YEAR)) {
        age--
    }
    return age
}


class CpfVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 11) text.text.substring(0, 11) else text.text
        val out = StringBuilder()
        
        for (i in trimmed.indices) {
            out.append(trimmed[i])
            if (i == 2 || i == 5) out.append(".")
            if (i == 8) out.append("-")
        }
        
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                var transformedOffset = offset
                if (offset > 2) transformedOffset += 1 // first "."
                if (offset > 5) transformedOffset += 1 // second "."
                if (offset > 8) transformedOffset += 1 // "-"
                return transformedOffset.coerceAtMost(out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                var originalOffset = offset
                if (offset > 3) originalOffset -= 1
                if (offset > 7) originalOffset -= 1
                if (offset > 11) originalOffset -= 1
                return originalOffset.coerceAtMost(trimmed.length)
            }
        }
        
        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}
