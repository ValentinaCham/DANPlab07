package com.example.danp_lab07

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.danp_lab07.repository.ProductRepository
import com.example.danp_lab07.ui.ProductDetailScreen
import com.example.danp_lab07.ui.ProductFormScreen
import com.example.danp_lab07.ui.ProductScreen
import com.example.danp_lab07.ui.Screen
import com.example.danp_lab07.ui.theme.DANPlab07Theme
import com.example.danp_lab07.viewmodel.ProductViewModel
import com.example.danp_lab07.viewmodel.ProductViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DANPlab07Theme {
                ProductApp()
            }
        }
    }
}

@Composable
fun ProductApp() {
    val navController = rememberNavController()
    val repository = remember { ProductRepository() }
    val viewModel: ProductViewModel = viewModel(
        factory = ProductViewModelFactory(repository)
    )

    NavHost(navController = navController, startDestination = Screen.Catalog.route) {
        composable(Screen.Catalog.route) {
            ProductScreen(
                viewModel = viewModel,
                onNavigateToForm = { productId ->
                    navController.navigate(Screen.Form.createRoute(productId))
                },
                onNavigateToDetail = { productId ->
                    navController.navigate(Screen.Detail.createRoute(productId))
                }
            )
        }
        composable(
            route = Screen.Form.route,
            arguments = listOf(navArgument("productId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null 
            })
        ) { backStackEntry ->
            val productIdStr = backStackEntry.arguments?.getString("productId")
            val productId = productIdStr?.toIntOrNull()
            ProductFormScreen(
                productId = productId,
                viewModel = viewModel,
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            ProductDetailScreen(
                productId = productId,
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.Form.createRoute(id))
                }
            )
        }
    }
}
