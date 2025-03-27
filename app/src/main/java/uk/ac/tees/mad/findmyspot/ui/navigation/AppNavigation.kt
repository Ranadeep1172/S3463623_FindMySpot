package uk.ac.tees.mad.findmyspot.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { }
    }
}