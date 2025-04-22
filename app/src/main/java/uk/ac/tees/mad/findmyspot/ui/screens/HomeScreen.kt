package uk.ac.tees.mad.findmyspot.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import uk.ac.tees.mad.findmyspot.model.ParkingSpot
import uk.ac.tees.mad.findmyspot.viewmodels.ParkingViewModel

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ParkingViewModel = viewModel()
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState()
    val permissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    val parkingSpots by viewModel.parkingSpots.collectAsState()

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedSpot by remember { mutableStateOf<ParkingSpot?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    LaunchedEffect(permissionState) {
        if (permissionState.status.isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                }
            }
        } else {
            permissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(userLocation) {
        userLocation?.let {
            launch {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f)
                )
            }
        }

    }

    LaunchedEffect(selectedSpot) {
        selectedSpot?.let {
            bottomSheetState.show()
        }
    }
    val scope = rememberCoroutineScope()
    BottomSheetScaffold(
        sheetContent = {
            selectedSpot?.let { spot ->
                ParkingSpotDetails(spot, onDetails = {
                    navController.navigate("spot_detail/${spot.id}")
                }) {
                    selectedSpot = null
                    scope.launch {
                        bottomSheetState.hide()
                    }
                }
            }
        },
        scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState),
        sheetPeekHeight = 0.dp,
        topBar = {
            TopAppBar(
                title = { Text("FindMySpot") },
                actions = {
                    IconButton(onClick = { navController.navigate("profile_screen") }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    }
                }
            )
        },
    ) {

        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = { navController.navigate("add_spot") },
                modifier = Modifier
                    .padding(end = 16.dp, bottom = 100.dp)
                    .zIndex(1f)
                    .align(Alignment.BottomEnd),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Spot")
            }
            if (!permissionState.status.isGranted) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Location permission is required to display your location.")
                    Button(onClick = { permissionState.launchPermissionRequest() }) {
                        Text("Grant Permission")
                    }
                }
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    userLocation?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "You are here",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        )
                    }

                    // markers for parking spots
                    parkingSpots.forEach { spot ->
                        Marker(
                            state = MarkerState(position = spot.location),
                            title = spot.name,
                            snippet = "Availability: ${spot.availability} | Price: $${spot.pricePerHour}/hr",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                            onClick = {
                                selectedSpot = spot
                                true
                            }
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun ParkingSpotDetails(spot: ParkingSpot, onDetails: () -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = spot.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Availability: ${spot.availability}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = "Price: $${spot.pricePerHour}/hr",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onDetails,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("More details")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onDismiss() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("Close")
        }
    }
}
