package com.example.danp_lab07

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.danp_lab07.ui.ProductDetailScreen
import com.example.danp_lab07.ui.ProductFormScreen
import com.example.danp_lab07.ui.ProductScreen
import com.example.danp_lab07.ui.Screen
import com.example.danp_lab07.ui.theme.DANPlab07Theme
import com.example.danp_lab07.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DANPlab07Theme {
                ProductAppNavigation()
            }
        }
    }
}

@Composable
fun ProductAppNavigation() {
    val navController = rememberNavController()
    val viewModel: ProductViewModel = hiltViewModel()
    val context = LocalContext.current

    // Request Notification Permission for Android 13+
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            // Handle permission result
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.Form.createRoute(id))
                }
            )
        }
    }
}
