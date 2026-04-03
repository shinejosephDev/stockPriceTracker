package com.sj.stocktracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sj.stocktracker.ui.details.DetailScreen
import com.sj.stocktracker.ui.feed.FeedScreen

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Route.Feed.path
    ) {
        composable(route = Route.Feed.path) {
            FeedScreen(
                onStockClick = { symbol ->
                    navController.navigate(Route.Details.createRoute(symbol))
                }
            )
        }

        composable(
            route = Route.Details.path,
            arguments = listOf(
                navArgument("symbol") { type = NavType.StringType }
            ),
        ) {
            DetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class Route(val path: String) {
    data object Feed : Route("feed")
    data object Details : Route("details/{symbol}") {
        fun createRoute(symbol: String): String = "details/$symbol"
    }
}