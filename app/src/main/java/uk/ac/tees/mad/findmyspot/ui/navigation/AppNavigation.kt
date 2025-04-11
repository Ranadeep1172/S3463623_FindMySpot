package uk.ac.tees.mad.findmyspot.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.findmyspot.ui.screens.AuthScreen
import uk.ac.tees.mad.findmyspot.ui.screens.HomeScreen
import uk.ac.tees.mad.findmyspot.ui.screens.SplashScreen

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController)
        }

        composable("auth") {
            AuthScreen(navController)
        }
        composable("home") {
            HomeScreen()
        }
    }
}