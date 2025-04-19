package uk.ac.tees.mad.findmyspot.ui.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.findmyspot.ui.screens.AddSpotScreen
import uk.ac.tees.mad.findmyspot.ui.screens.AuthScreen
import uk.ac.tees.mad.findmyspot.ui.screens.EditSpotScreen
import uk.ac.tees.mad.findmyspot.ui.screens.HomeScreen
import uk.ac.tees.mad.findmyspot.ui.screens.SplashScreen
import uk.ac.tees.mad.findmyspot.ui.screens.SpotDetailScreen
import uk.ac.tees.mad.findmyspot.viewmodels.ParkingViewModel

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    val viewModel: ParkingViewModel = viewModel()
    val context = LocalContext.current
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
                SpotDetailScreen(spot = it, onBack = { navController.popBackStack() }, onEdit = {
                    navController.navigate("edit_spot/${it.id}")
                })
            }
        }


        composable("add_spot") {
            AddSpotScreen(navController, viewModel)
        }

        composable("edit_spot/{spotId}") { backStackEntry ->
            val spotId = backStackEntry.arguments?.getString("spotId") ?: return@composable
            val spot = viewModel.getParkingSpotById(spotId)

            spot?.let {
                EditSpotScreen(spot = spot, onBack = { navController.navigateUp() }, onDelete = {
                    viewModel.deleteParkingSpot(spot.id) {
                        Toast.makeText(context, "Spot Deleted!", Toast.LENGTH_SHORT).show()
                        navController.navigate("home") {
                            popUpTo("spot_detail/{spotId}") {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }, onDone = { newSpot ->
                    viewModel.updateParkingSpot(newSpot) {
                        Log.d("ASDFZG", "Updated spot")
                        Toast.makeText(context, "Spot updated!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                })

            }

        }
    }
}