package uk.ac.tees.mad.findmyspot.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.findmyspot.ui.screens.AddSpotScreen
import uk.ac.tees.mad.findmyspot.ui.screens.AuthScreen
import uk.ac.tees.mad.findmyspot.ui.screens.HomeScreen
import uk.ac.tees.mad.findmyspot.ui.screens.SplashScreen
import uk.ac.tees.mad.findmyspot.ui.screens.SpotDetailScreen
import uk.ac.tees.mad.findmyspot.viewmodels.ParkingViewModel

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    val viewModel: ParkingViewModel = viewModel()
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController)
        }

        composable("auth") {
            AuthScreen(navController)
        }
        composable("home") {
            HomeScreen(navController, viewModel)
        }

        composable("spot_detail/{spotId}") { backStackEntry ->
            val spotId = backStackEntry.arguments?.getString("spotId") ?: return@composable
            val spot = viewModel.getParkingSpotById(spotId)

            spot?.let {
                SpotDetailScreen(spot = it, onBack = { navController.popBackStack() })
            }
        }
        composable("add_spot") {
            AddSpotScreen(navController, viewModel)
        }
    }
}