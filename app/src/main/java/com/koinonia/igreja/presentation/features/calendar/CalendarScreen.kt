package com.koinonia.igreja.presentation.features.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.koinonia.igreja.data.local.converter.EventType
import com.koinonia.igreja.data.local.converter.LocationType
import com.koinonia.igreja.data.local.entity.EventEntity
import com.koinonia.igreja.domain.model.AppRole
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    onNavigateToCreateEvent: () -> Unit = {},
    onNavigateToReception: (String) -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val events by viewModel.events.collectAsState(initial = emptyList())
    val currentRole by viewModel.currentUserRole.collectAsState(initial = AppRole.NONE)
    val currentUserEmail = remember { viewModel.getCurrentUserEmail() }
    val directedMinistries by viewModel.directedMinistries.collectAsState(initial = emptyList())
    val ministriesList by viewModel.ministriesList.collectAsState(initial = emptyList())

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<EventEntity?>(null) }
    var newTitle by remember { mutableStateOf("") }
    var newTime by remember { mutableStateOf("19:30") }
    var selectedEventType by remember { mutableStateOf(EventType.EXTRAORDINARIO) }
    var selectedLocationType by remember { mutableStateOf(LocationType.IGREJA_LOCAL) }
    var selectedAddress by remember { mutableStateOf("") }
    var selectedMinistryId by remember { mutableStateOf<String?>(null) }
    var dialogError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val canCreateEvents = currentRole.hasFullAccess || directedMinistries.isNotEmpty()

    val selectedDateEvents = remember(events, selectedDate) {
        events.filter { event ->
            val eventDate = event.startTime.toInstant().atZone(ZoneId.of("America/Bahia")).toLocalDate()
            eventDate == selectedDate
        }
    }

    if (showCreateDialog) {
        var menuExpanded by remember { mutableStateOf(false) }
        val allowedMinistries = remember(currentRole, directedMinistries, ministriesList) {
            if (currentRole.hasFullAccess) {
                ministriesList
            } else {
                ministriesList.filter { m -> directedMinistries.any { it.ministryId == m.id } }
            }
        }

        // Auto-seleciona ministério se for único
        LaunchedEffect(allowedMinistries) {
            if (editingEvent == null) {
                if (!currentRole.hasFullAccess && allowedMinistries.size == 1) {
                    selectedMinistryId = allowedMinistries.first().id
                }
            }
        }

        val selectedMinistryName = remember(selectedMinistryId, ministriesList) {
            if (selectedMinistryId == null) "Nenhum / Liderança Geral"
            else ministriesList.find { it.id == selectedMinistryId }?.name ?: selectedMinistryId!!
        }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(if (editingEvent != null) "Editar Evento" else "Novo Evento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (dialogError != null) {
                        Text(
                            text = dialogError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Título do Evento") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTime,
                        onValueChange = { newTime = it },
                        label = { Text("Horário (HH:mm)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Seleção de Tipo de Evento
                    Column {
                        Text("Tipo de Evento", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EventType.values().forEach { type ->
                                val selected = selectedEventType == type
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedEventType = type },
                                    label = {
                                        Text(
                                            text = when(type) {
                                                EventType.ORDINARIO -> "Culto Ord."
                                                EventType.EXTRAORDINARIO -> "Culto Ext."
                                                EventType.EXTERNO -> "Externo"
                                                EventType.REUNIAO -> "Reunião"
                                            },
                                            fontSize = 10.sp
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Seleção de Localização
                    Column {
                        Text("Localização", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LocationType.values().forEach { loc ->
                                val selected = selectedLocationType == loc
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedLocationType = loc },
                                    label = {
                                        Text(
                                            text = when(loc) {
                                                LocationType.IGREJA_LOCAL -> "Templo"
                                                LocationType.URBANO -> "Urbano"
                                                LocationType.EXTERNO -> "Externo"
                                            },
                                            fontSize = 10.sp
                                        )
                                    }
                                )
                            }
                        }
                    }

                    if (selectedLocationType != LocationType.IGREJA_LOCAL) {
                        OutlinedTextField(
                            value = selectedAddress,
                            onValueChange = { selectedAddress = it },
                            label = { Text("Endereço / Local") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Seleção de Ministério
                    Column {
                        Text("Ministério Responsável", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selectedMinistryName)
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                if (currentRole.hasFullAccess) {
                                    DropdownMenuItem(
                                        text = { Text("Nenhum / Liderança Geral") },
                                        onClick = {
                                            selectedMinistryId = null
                                            menuExpanded = false
                                        }
                                    )
                                }
                                allowedMinistries.forEach { min ->
                                    DropdownMenuItem(
                                        text = { Text(min.name) },
                                        onClick = {
                                            selectedMinistryId = min.id
                                            menuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            val event = editingEvent
                            scope.launch {
                                // Verifica conflito apenas se o evento que está sendo criado/editado não for Ordinário
                                val hasConflict = if (selectedEventType != EventType.ORDINARIO) {
                                    viewModel.hasOrdinaryConflict(
                                        date = selectedDate,
                                        time = newTime,
                                        eventIdToIgnore = event?.id
                                    )
                                } else {
                                    false
                                }

                                val allowed = com.koinonia.igreja.core.util.EventPermissions.canManageEvent(
                                    event = event,
                                    targetMinistryId = selectedMinistryId,
                                    currentRole = currentRole,
                                    directedMinistries = directedMinistries,
                                    hasOrdinaryConflict = hasConflict
                                )

                                if (allowed) {
                                    if (event != null) {
                                        viewModel.editEvent(
                                            id = event.id,
                                            title = newTitle,
                                            date = selectedDate,
                                            time = newTime,
                                            type = selectedEventType,
                                            locationType = selectedLocationType,
                                            address = if (selectedLocationType == LocationType.IGREJA_LOCAL) null else selectedAddress,
                                            ministryId = selectedMinistryId,
                                            creatorEmail = event.creatorEmail
                                        )
                                    } else {
                                        viewModel.addEvent(
                                            title = newTitle,
                                            date = selectedDate,
                                            time = newTime,
                                            type = selectedEventType,
                                            locationType = selectedLocationType,
                                            address = if (selectedLocationType == LocationType.IGREJA_LOCAL) null else selectedAddress,
                                            ministryId = selectedMinistryId
                                        )
                                    }
                                    dialogError = null
                                    showCreateDialog = false
                                } else {
                                    if (hasConflict) {
                                        dialogError = "Este horário coincide com um Culto Ordinário. Apenas ADM, Pastor ou Ancião podem agendar eventos nesse horário."
                                    } else {
                                        dialogError = "Você não tem permissão para gerenciar eventos para este ministério."
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Agenda") },
                navigationIcon = {
                    if (onMenuClick != null) {
                        IconButton(onClick = onMenuClick) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    } else {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (canCreateEvents) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingEvent = null
                        newTitle = ""
                        newTime = "19:30"
                        selectedEventType = EventType.EXTRAORDINARIO
                        selectedLocationType = LocationType.IGREJA_LOCAL
                        selectedAddress = ""
                        selectedMinistryId = null
                        dialogError = null
                        showCreateDialog = true
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Novo Evento") },
                    text = { Text("Novo Evento") }
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            
            // Seletor de Mês e Ano
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.selectDate(selectedDate.minusMonths(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mês Anterior")
                }
                Text(
                    text = selectedDate.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR")).replaceFirstChar { it.uppercase() } + " " + selectedDate.year,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { viewModel.selectDate(selectedDate.plusMonths(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Próximo Mês")
                }
            }

            // Calendário Mensal
            CalendarGrid(
                selectedDate = selectedDate,
                onDateSelected = { viewModel.selectDate(it) },
                events = events,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            HorizontalDivider()

            // Lista de compromissos do dia selecionado
            Text(
                text = "Compromissos de ${selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", 
                modifier = Modifier.padding(16.dp), 
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            
            if (selectedDateEvents.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum compromisso agendado para hoje.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedDateEvents) { event ->
                        val formattedDateTime = remember(event.startTime) {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("America/Bahia")
                            }.format(event.startTime)
                        }

                        val containerColor = when (event.type) {
                            EventType.ORDINARIO -> MaterialTheme.colorScheme.primaryContainer
                            EventType.EXTRAORDINARIO -> MaterialTheme.colorScheme.secondaryContainer
                            EventType.EXTERNO -> MaterialTheme.colorScheme.tertiaryContainer
                            EventType.REUNIAO -> Color(0xFFEADDFF)
                        }
                        val typeLabel = when (event.type) {
                            EventType.ORDINARIO -> "Culto Ordinário"
                            EventType.EXTRAORDINARIO -> "Culto Extraordinário"
                            EventType.EXTERNO -> "Evento Externo"
                            EventType.REUNIAO -> "Reunião"
                        }

                        val canModify = remember(event, currentRole, directedMinistries) {
                            com.koinonia.igreja.core.util.EventPermissions.canManageEvent(
                                event = event,
                                targetMinistryId = event.ministryId,
                                currentRole = currentRole,
                                directedMinistries = directedMinistries,
                                hasOrdinaryConflict = false
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = event.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$typeLabel • $formattedDateTime h",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (!event.address.isNullOrBlank()) {
                                        Text(
                                            text = "Local: ${event.address}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (canCreateEvents) {
                                    IconButton(onClick = {
                                        onNavigateToReception(event.id)
                                    }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.List,
                                            contentDescription = "Chamada do Evento",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                if (canModify) {
                                    IconButton(onClick = {
                                        editingEvent = event
                                        newTitle = event.title
                                        val localTime = event.startTime.toInstant().atZone(ZoneId.of("America/Bahia")).toLocalTime()
                                        newTime = String.format(Locale.US, "%02d:%02d", localTime.hour, localTime.minute)
                                        selectedEventType = event.type
                                        selectedLocationType = event.locationType
                                        selectedAddress = event.address ?: ""
                                        selectedMinistryId = event.ministryId
                                        dialogError = null
                                        showCreateDialog = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar Evento",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    IconButton(onClick = {
                                        viewModel.deleteEvent(event.id)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Excluir Evento",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    events: List<EventEntity>,
    modifier: Modifier = Modifier
) {
    val yearMonth = remember(selectedDate) { YearMonth.from(selectedDate) }
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value
    
    val daysOfWeek = listOf("D", "S", "T", "Q", "Q", "S", "S")
    
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        val offset = firstDayOfWeek % 7
        val totalCells = daysInMonth + offset
        val rows = (totalCells + 6) / 7
        
        for (r in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                for (c in 0 until 7) {
                    val index = r * 7 + c
                    val dayNum = index - offset + 1
                    
                    if (dayNum in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayNum)
                        val isSelected = date == selectedDate
                        
                        val hasEvents = remember(events, date) {
                            events.any { event ->
                                val eventDate = event.startTime.toInstant().atZone(ZoneId.of("America/Bahia")).toLocalDate()
                                eventDate == date
                            }
                        }
                        
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .clickable { onDateSelected(date) }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = dayNum.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurface
                                )
                                if (hasEvents) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.primary
                                            )
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

fun canModifyEvent(event: EventEntity, role: AppRole, email: String?): Boolean {
    if (role == AppRole.ADMIN) return true
    if (role == AppRole.DIACONO) {
        return event.creatorEmail != null && event.creatorEmail == email
    }
    return false
}
