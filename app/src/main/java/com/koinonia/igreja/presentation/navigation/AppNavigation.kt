package com.koinonia.igreja.presentation.navigation

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
import com.koinonia.igreja.domain.model.AppRole
import com.koinonia.igreja.presentation.features.auth.AuthState
import com.koinonia.igreja.presentation.features.auth.AuthViewModel
import com.koinonia.igreja.presentation.features.auth.ForgotPasswordScreen
import com.koinonia.igreja.presentation.features.auth.LoginScreen
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

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val currentRole by authViewModel.currentUserRole.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    // Determina a rota atual para exibição da BottomBar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != "login" && currentRoute != "forgot_password" && currentRoute != null

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        navController.navigate("calendar") {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
        }
    }

    LaunchedEffect(currentRole) {
        if (currentRole == AppRole.NONE) {
            navController.navigate("login") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (showBottomBar) {
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.height(16.dp))
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
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentRoute == "reception",
                            onClick = {
                                if (currentRoute != "reception") {
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
                                val destination = if (role == AppRole.VIEWER) "reports" else "calendar"
                                navController.navigate(destination) {
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

                    composable("members_list") {
                        val viewModel: MemberListViewModel = hiltViewModel()
                        MemberListScreen(
                            viewModel = viewModel,
                            onMenuClick = {
                                scope.launch { drawerState.open() }
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

                    composable("member_add") {
                        val viewModel: MemberRegistrationViewModel = hiltViewModel()
                        MemberRegistrationScreen(
                            viewModel = viewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(
                        route = "member_edit/{memberId}",
                        arguments = listOf(navArgument("memberId") { type = NavType.StringType })
                    ) { backStackEntry ->
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
                    CalendarScreen(
                        viewModel = viewModel,
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
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
                    EventRegistrationScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
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
                    // PROTEÇÃO DE ROTA (Guard Clause / RBAC)
                    if (currentRole == AppRole.ADMIN || currentRole == AppRole.DIACONO) {
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
                    } else {
                        // Redirecionamento forçado para painel de visualização se não tiver permissão
                        LaunchedEffect(Unit) {
                            navController.navigate("reports") {
                                popUpTo("reception") { inclusive = true }
                            }
                        }
                    }
                }

                composable("reports") {
                    val viewModel: ReportsViewModel = hiltViewModel()
                    DashboardScreen(
                        viewModel = viewModel,
                        onBack = {
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

