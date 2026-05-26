package com.example.productsalessupportclient.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.productsalessupportclient.data.network.AuthApi
import com.example.productsalessupportclient.data.repository.AuthRepository
import com.example.productsalessupportclient.presentation.auth.AuthViewModel
import com.example.productsalessupportclient.presentation.auth.LoginScreen
import com.example.productsalessupportclient.presentation.auth.RegisterScreen
import com.example.productsalessupportclient.presentation.role.ManagerHomeScreen
import com.example.productsalessupportclient.presentation.role.PurchaserHomeScreen
import com.example.productsalessupportclient.presentation.role.StorekeeperHomeScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ROLE = "role"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val repository = remember { AuthRepository(AuthApi()) }
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(repository)
    )

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onRegisterClick = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = { role ->
                    navController.navigate("${Routes.ROLE}/$role") {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("${Routes.ROLE}/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role")?.lowercase().orEmpty()
            val session = viewModel.currentSession

            if (session == null) {
                LoginScreen(
                    viewModel = viewModel,
                    onRegisterClick = { navController.navigate(Routes.REGISTER) },
                    onLoginSuccess = { }
                )
                return@composable
            }

            val logout: () -> Unit = {
                viewModel.logout()
                navController.navigate(Routes.LOGIN) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }

            when (role) {
                "purchaser" -> PurchaserHomeScreen(session = session, onLogout = logout)
                "manager" -> ManagerHomeScreen(session = session, onLogout = logout)
                "storekeeper" -> StorekeeperHomeScreen(session = session, onLogout = logout)
                else -> PurchaserHomeScreen(session = session, onLogout = logout)
            }
        }
    }
}