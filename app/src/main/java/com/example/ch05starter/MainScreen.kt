package com.example.ch05starter

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

private const val ANIM_MS = 300

// Semua layar slide dari kanan saat masuk, keluar ke kiri; saat back arahnya dibalik
private val enterAnim: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(tween(ANIM_MS)) { it } + fadeIn(tween(ANIM_MS))
}
private val exitAnim: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(tween(ANIM_MS)) { -it } + fadeOut(tween(ANIM_MS))
}
private val popEnterAnim: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(tween(ANIM_MS)) { -it } + fadeIn(tween(ANIM_MS))
}
private val popExitAnim: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(tween(ANIM_MS)) { it } + fadeOut(tween(ANIM_MS))
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick  = {
                            navController.navigate(item.route) {
                                popUpTo(Routes.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        },
                        icon  = {
                            Icon(
                                imageVector =
                                    if (currentRoute == item.route)
                                        item.iconSelected
                                    else
                                        item.iconUnselected,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Routes.Home.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(
                route              = Routes.Home.route,
                enterTransition    = enterAnim,
                exitTransition     = exitAnim,
                popEnterTransition = popEnterAnim,
                popExitTransition  = popExitAnim
            ) { HomeScreen(navController) }

            composable(
                route              = Routes.Explore.route,
                enterTransition    = enterAnim,
                exitTransition     = exitAnim,
                popEnterTransition = popEnterAnim,
                popExitTransition  = popExitAnim
            ) { ExploreScreen() }

            composable(
                route              = Routes.Profile.route,
                enterTransition    = enterAnim,
                exitTransition     = exitAnim,
                popEnterTransition = popEnterAnim,
                popExitTransition  = popExitAnim
            ) { ProfileScreen() }

            composable(
                route              = Routes.Detail.route,
                arguments          = listOf(
                    navArgument("itemId") { type = NavType.IntType }
                ),
                deepLinks          = listOf(
                    navDeepLink { uriPattern = "myapp://article/{itemId}" }
                ),
                enterTransition    = enterAnim,
                exitTransition     = exitAnim,
                popEnterTransition = popEnterAnim,
                popExitTransition  = popExitAnim
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
                DetailScreen(
                    itemId = itemId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
