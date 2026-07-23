package com.koinonia.igreja.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Badge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.koinonia.igreja.data.repository.AuthResolutionState
import com.koinonia.igreja.domain.model.AppRole
import com.koinonia.igreja.presentation.features.auth.AuthState
import com.koinonia.igreja.presentation.features.auth.AuthViewModel
import com.koinonia.igreja.presentation.features.auth.ForgotPasswordScreen
import com.koinonia.igreja.presentation.features.auth.LoginScreen
import com.koinonia.igreja.presentation.features.auth.CreatePermanentPasswordScreen
import com.koinonia.igreja.presentation.features.calendar.CalendarScreen
import com.koinonia.igreja.presentation.features.calendar.CalendarViewModel
import com.koinonia.igreja.presentation.features.calendar.EventRegistrationScreen
import com.koinonia.igreja.presentation.features.members.MemberRegistrationScreen
import com.koinonia.igreja.presentation.features.members.MemberRegistrationViewModel
import com.koinonia.igreja.presentation.features.members.MemberListViewModel
import com.koinonia.igreja.presentation.features.members.MemberDetailScreen
import com.koinonia.igreja.presentation.features.members.MemberListScreen
import com.koinonia.igreja.presentation.features.members.MembersViewModel
import com.koinonia.igreja.presentation.features.reception.ReceptionScreen
import com.koinonia.igreja.presentation.features.reception.ReceptionViewModel
import com.koinonia.igreja.presentation.features.reports.DashboardScreen
import com.koinonia.igreja.presentation.features.reports.ReportsViewModel
import com.koinonia.igreja.presentation.features.members.dialog.MinistryManagementDialog
import com.koinonia.igreja.presentation.features.members.dialog.RoleManagementDialog
import com.koinonia.igreja.presentation.features.treasury.TreasuryScreen
import com.koinonia.igreja.presentation.features.unauthorized.UnauthorizedScreen

import com.koinonia.igreja.presentation.components.UserProfileEditDialog
import com.koinonia.igreja.core.biometric.BiometricPromptManager
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val currentRole by authViewModel.currentUserRole.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val authResolutionState by authViewModel.authResolutionState.collectAsState()
    val directedMinistries by authViewModel.directedMinistries.collectAsState()
    val currentMember by authViewModel.currentMember.collectAsState()
    val isBiometricEnabled by authViewModel.isBiometricEnabled.collectAsState()
    val memberRegistrationViewModel: MemberRegistrationViewModel = hiltViewModel()

    val context = LocalContext.current
    val biometricPromptManager = remember(context) { BiometricPromptManager(context.applicationContext) }

    var showMinistryManagementDialog by remember { mutableStateOf(false) }
    var showRoleManagementDialog by remember { mutableStateOf(false) }
    var showUserProfileEditDialog by remember { mutableStateOf(false) }

    // Determina a rota atual para exibição da BottomBar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != "login" && currentRoute != "forgot_password" && currentRoute != "change_password" && currentRoute != null

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(authResolutionState) {
        when (val state = authResolutionState) {
            is AuthResolutionState.LOADING -> {
                // Do nothing
            }
            is AuthResolutionState.UNAUTHENTICATED -> {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
            is AuthResolutionState.AUTHENTICATED -> {
                val mustChange = authViewModel.checkIfMustChangePassword()
                if (mustChange) {
                    navController.navigate("change_password") {
                        popUpTo(0) { inclusive = true }
                    }
                } else {
                    navController.navigate("calendar") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    LaunchedEffect(currentRoute, authResolutionState) {
        if (authResolutionState is AuthResolutionState.AUTHENTICATED) {
            val mustChange = authViewModel.checkIfMustChangePassword()
            if (mustChange && currentRoute != "change_password") {
                navController.navigate("change_password") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    if (authResolutionState is AuthResolutionState.LOADING) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val hasDrawer = currentRoute != "login" && currentRoute != "forgot_password" && currentRoute != "change_password" && currentRoute != null && (currentRole.hasFullAccess || currentRole.hasTreasuryAccess)

    if (showMinistryManagementDialog) {
        val allMinistries by memberRegistrationViewModel.allMinistries.collectAsState(initial = emptyList())
        MinistryManagementDialog(
            allMinistries = allMinistries,
            onDismiss = { showMinistryManagementDialog = false },
            onSaveMinistry = { name, parentId, minAge, maxAge, minMembershipMonths, notes, existingId ->
                memberRegistrationViewModel.addMinistry(name, parentId, minAge, maxAge, minMembershipMonths, notes, existingId)
            },
            onDeleteMinistry = { id ->
                memberRegistrationViewModel.deleteMinistry(id)
            },
            onResetDefaults = {
                memberRegistrationViewModel.resetToDefaultMinistriesAndRoles()
            }
        )
    }

    if (showRoleManagementDialog) {
        val allRoles by memberRegistrationViewModel.allRoles.collectAsState(initial = emptyList())
        RoleManagementDialog(
            allRoles = allRoles,
            onDismiss = { showRoleManagementDialog = false },
            onSaveRole = { title, tier, existingId ->
                memberRegistrationViewModel.addRole(title, tier, existingId)
            },
            onDeleteRole = { id ->
                memberRegistrationViewModel.deleteRole(id)
            },
            onResetDefaults = {
                memberRegistrationViewModel.resetToDefaultMinistriesAndRoles()
            }
        )
    }

    if (showUserProfileEditDialog) {
        UserProfileEditDialog(
            member = currentMember,
            isBiometricAvailable = biometricPromptManager.isBiometricAvailable(),
            isBiometricEnabled = isBiometricEnabled,
            onDismiss = { showUserProfileEditDialog = false },
            onSaveProfile = { updatedMember, enableBiometric ->
                authViewModel.setBiometricEnabled(enableBiometric)
                authViewModel.updateCurrentMemberProfile(updatedMember) { success ->
                    showUserProfileEditDialog = false
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (hasDrawer) {
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (currentRole.hasFullAccess) {
                        Text(
                            text = "Secretaria",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                        NavigationDrawerItem(
                            label = { Text("Membros") },
                            selected = currentRoute == "members_list" || currentRoute == "member_add" || currentRoute?.startsWith("member_details") == true,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("members_list") {
                                    popUpTo("members_list") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        NavigationDrawerItem(
                            label = { Text("Ministérios") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showMinistryManagementDialog = true
                            },
                            icon = { Icon(Icons.Default.CorporateFare, contentDescription = null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        NavigationDrawerItem(
                            label = { Text("Cargos") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showRoleManagementDialog = true
                            },
                            icon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }

                    if (currentRole.hasTreasuryAccess) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tesouraria",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                        NavigationDrawerItem(
                            label = { Text("Tesouraria") },
                            selected = currentRoute == "treasury",
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("treasury") {
                                    popUpTo("calendar") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentRoute == "reception" || currentRoute?.startsWith("reception") == true,
                            onClick = {
                                if (currentRoute?.startsWith("reception") != true) {
                                    navController.navigate("reception") {
                                        popUpTo("calendar") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Chamada") },
                            label = { Text("Chamada") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "calendar",
                            onClick = {
                                if (currentRoute != "calendar") {
                                    navController.navigate("calendar") {
                                        popUpTo("calendar") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = { Icon(Icons.Default.DateRange, contentDescription = "Agenda") },
                            label = { Text("Agenda") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "reports",
                            onClick = {
                                if (currentRoute != "reports") {
                                    navController.navigate("reports") {
                                        popUpTo("calendar") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(Icons.Default.Info, contentDescription = "Métricas") },
                            label = { Text("Métricas") }
                        )
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                NavHost(navController = navController, startDestination = "calendar") {
                    composable("login") {
                        LoginScreen(
                            viewModel = authViewModel,
                            onNavigateToHome = { role ->
                                navController.navigate("calendar") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onForgotPasswordClick = {
                                navController.navigate("forgot_password")
                            }
                        )
                    }

                    composable("forgot_password") {
                        ForgotPasswordScreen(
                            viewModel = authViewModel,
                            onResetSent = {
                                navController.navigate("login") {
                                    popUpTo("forgot_password") { inclusive = true }
                                }
                            },
                            onBackToLogin = {
                                navController.navigate("login") {
                                    popUpTo("forgot_password") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("change_password") {
                        CreatePermanentPasswordScreen(
                            viewModel = authViewModel,
                            onPasswordChanged = {
                                navController.navigate("calendar") {
                                    popUpTo("change_password") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("members_list") {
                        if (!currentRole.hasFullAccess) {
                            LaunchedEffect(Unit) {
                                navController.navigate("calendar") {
                                    popUpTo("calendar") { inclusive = true }
                                }
                            }
                        } else {
                            val viewModel: MemberListViewModel = hiltViewModel()
                            MemberListScreen(
                                viewModel = viewModel,
                                onMenuClick = {
                                    scope.launch { drawerState.open() }
                                },
                                currentMember = currentMember,
                                onProfileClick = {
                                    showUserProfileEditDialog = true
                                },
                                onEditMember = { memberId ->
                                    navController.navigate("member_edit/$memberId")
                                },
                                onNavigateToDetails = { memberId ->
                                    navController.navigate("member_details/$memberId")
                                },
                                onNavigateToRegistration = {
                                    navController.navigate("member_add")
                                }
                            )
                        }
                    }

                    composable("member_add") {
                        if (!currentRole.hasFullAccess) {
                            LaunchedEffect(Unit) {
                                navController.navigate("calendar") {
                                    popUpTo("calendar") { inclusive = true }
                                }
                            }
                        } else {
                            val viewModel: MemberRegistrationViewModel = hiltViewModel()
                            MemberRegistrationScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    composable(
                        route = "member_edit/{memberId}",
                        arguments = listOf(navArgument("memberId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        if (!currentRole.hasFullAccess) {
                            LaunchedEffect(Unit) {
                                navController.navigate("calendar") {
                                    popUpTo("calendar") { inclusive = true }
                                }
                            }
                        } else {
                            val memberId = backStackEntry.arguments?.getString("memberId") ?: ""
                            val viewModel: MemberRegistrationViewModel = hiltViewModel()
                            LaunchedEffect(memberId) {
                                viewModel.loadMemberToEdit(memberId)
                            }
                            MemberRegistrationScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                composable(
                    route = "member_details/{memberId}",
                    arguments = listOf(navArgument("memberId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val memberId = backStackEntry.arguments?.getString("memberId") ?: ""
                    val viewModel: MembersViewModel = hiltViewModel()
                    MemberDetailScreen(
                        memberId = memberId,
                        viewModel = viewModel,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable("calendar") {
                    val viewModel: CalendarViewModel = hiltViewModel()
                    val hasDrawerItems = currentRole.hasFullAccess || currentRole.hasTreasuryAccess
                    CalendarScreen(
                        viewModel = viewModel,
                        currentMember = currentMember,
                        onProfileClick = {
                            showUserProfileEditDialog = true
                        },
                        onMenuClick = if (hasDrawerItems) {
                            { scope.launch { drawerState.open() } }
                        } else null,
                        onBack = {
                            navController.popBackStack()
                        },
                        onNavigateToCreateEvent = {
                            navController.navigate("event_create")
                        },
                        onNavigateToReception = { eventId ->
                            navController.navigate("reception?eventId=$eventId")
                        }
                    )
                }

                composable("event_create") {
                    val canCreate = currentRole.hasFullAccess || directedMinistries.isNotEmpty()
                    if (!canCreate) {
                        LaunchedEffect(Unit) {
                            navController.navigate("calendar") {
                                popUpTo("calendar") { inclusive = true }
                            }
                        }
                    } else {
                        EventRegistrationScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }

                composable(
                    route = "reception?eventId={eventId}",
                    arguments = listOf(
                        navArgument("eventId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId")
                    val viewModel: ReceptionViewModel = hiltViewModel()
                    LaunchedEffect(eventId) {
                        viewModel.initReception(eventId, null)
                    }
                    ReceptionScreen(
                        viewModel = viewModel,
                        onBack = {
                            navController.navigate("calendar") {
                                popUpTo("calendar") { inclusive = true }
                            }
                        }
                    )
                }

                composable("reports") {
                    if (!currentRole.hasFullAccess) {
                        LaunchedEffect(Unit) {
                            navController.navigate("calendar") {
                                popUpTo("calendar") { inclusive = true }
                            }
                        }
                    } else {
                        val viewModel: ReportsViewModel = hiltViewModel()
                        DashboardScreen(
                            viewModel = viewModel,
                            currentMember = currentMember,
                            onProfileClick = {
                                showUserProfileEditDialog = true
                            },
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            },
                            onBack = {
                                navController.navigate("calendar") {
                                    popUpTo("calendar") { inclusive = true }
                                }
                            }
                        )
                    }
                }

                composable("treasury") {
                    if (!currentRole.hasTreasuryAccess) {
                        LaunchedEffect(Unit) {
                            navController.navigate("unauthorized") {
                                popUpTo("calendar") { inclusive = false }
                            }
                        }
                    } else {
                        TreasuryScreen(
                            currentMember = currentMember,
                            onProfileClick = {
                                showUserProfileEditDialog = true
                            },
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            }
                        )
                    }
                }

                composable("unauthorized") {
                    UnauthorizedScreen(
                        onNavigateToCalendar = {
                            navController.navigate("calendar") {
                                popUpTo("calendar") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
}

