package com.jyoti.assistant.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jyoti.feature.home.HomeRoute
import com.jyoti.feature.home.homeScreen
import com.jyoti.feature.settings.SettingsRoute
import com.jyoti.feature.settings.settingsScreen

/**
 * Single place that stitches feature modules together. Each feature module exposes
 * its own `xyzScreen(navController)` extension on NavGraphBuilder (see feature:home,
 * feature:settings), so wiring in a brand-new feature later is a one-line addition here —
 * the feature module itself is self-contained and doesn't need to know about others.
 */
@Composable
fun JyotiNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = HomeRoute
) {
    NavHost(navController = navController, startDestination = startDestination) {
        homeScreen(navController)
        settingsScreen(navController)
    }
}
