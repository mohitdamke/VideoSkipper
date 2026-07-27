package com.mohit.videoskipper.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mohit.videoskipper.presentation.screens.HomeScreen
import com.mohit.videoskipper.presentation.screens.ImageScreen
import com.mohit.videoskipper.presentation.screens.TextScreen

@Composable
fun NavigationScreen(
    featureOn: Boolean,
    onFeatureToggle: (Boolean) -> Unit
) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Home.route
    ) {

        composable(route = Routes.Home.route) {
            HomeScreen(
                featureOn = featureOn,
                onFeatureToggle = onFeatureToggle,
                navController = navController
            )
        }
        composable(route = Routes.Text.route) {
            TextScreen(
                navController = navController
            )
        }
        composable(route = Routes.Image.route) {
            ImageScreen(
                navController = navController
            )
        }

    }
}