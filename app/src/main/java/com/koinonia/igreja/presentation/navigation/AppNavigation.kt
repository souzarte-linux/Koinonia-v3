package com.koinonia.igreja.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.koinonia.igreja.presentation.features.reports.ReportsDashboardScreen
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



    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "members_list" || currentRoute == "member_add" || currentRoute?.startsWith("member_details") == true,
                        onClick = {
                            navController.navigate("members_list") {
                                popUpTo("members_list") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Membros") },
                        label = { Text("Membros") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "reception",
                        onClick = {
                            navController.navigate("reception") {
                                popUpTo("members_list") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Chamada") },
                        label = { Text("Chamada") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "calendar",
                        onClick = {
                            navController.navigate("calendar") {
                                popUpTo("members_list") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Agenda") },
                        label = { Text("Agenda") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "reports",
                        onClick = {
                            navController.navigate("reports") {
                                popUpTo("members_list") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
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
            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(
                        viewModel = authViewModel,
                        onNavigateToHome = { role ->
                            val destination = if (role == AppRole.VIEWER) "reports" else "members_list"
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
                        onBack = {
                            navController.popBackStack()
                        },
                        onNavigateToCreateEvent = {
                            navController.navigate("event_create")
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

                composable("reception") {
                    // PROTEÇÃO DE ROTA (Guard Clause / RBAC)
                    if (currentRole == AppRole.ADMIN || currentRole == AppRole.DIACONO) {
                        val viewModel: ReceptionViewModel = hiltViewModel()
                        ReceptionScreen(
                            viewModel = viewModel,
                            onBack = {
                                navController.navigate("members_list") {
                                    popUpTo("members_list") { inclusive = true }
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
                    ReportsDashboardScreen(
                        viewModel = viewModel,
                        onBack = {
                            navController.navigate("members_list") {
                                popUpTo("members_list") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
