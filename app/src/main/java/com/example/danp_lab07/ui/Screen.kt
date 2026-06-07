package com.example.danp_lab07.ui

sealed class Screen(val route: String) {
    object Catalog : Screen("catalog")
    object Form : Screen("form?productId={productId}") {
        fun createRoute(productId: Int?) = "form?productId=$productId"
    }
    object Detail : Screen("detail/{productId}") {
        fun createRoute(productId: Int) = "detail/$productId"
    }
}
