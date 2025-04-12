package uk.ac.tees.mad.findmyspot.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import uk.ac.tees.mad.findmyspot.model.ParkingSpot

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState()
    val permissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    var parkingSpots by remember { mutableStateOf<List<ParkingSpot>>(emptyList()) }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedSpot by remember { mutableStateOf<ParkingSpot?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    // Fetch parking spots from Firestore
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("parking_spots").get()
            .addOnSuccessListener { documents ->
                parkingSpots = documents.mapNotNull { doc ->
                    val name = doc.getString("name")
                    val latitude = doc.getDouble("latitude")
                    val longitude = doc.getDouble("longitude")
                    val availability = doc.getString("availability")
                    val pricePerHour = doc.getDouble("price_per_hour")

                    if (name != null && latitude != null && longitude != null && availability != null && pricePerHour != null) {
                        ParkingSpot(name, LatLng(latitude, longitude), availability, pricePerHour)
                    } else null
                }
            }
    }

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
                ParkingSpotDetails(spot) {
                    selectedSpot = null
                    scope.launch {
                        bottomSheetState.hide()
                    }
                }
            }
        },
        scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState),
        sheetPeekHeight = 0.dp
    ) {
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

@Composable
fun ParkingSpotDetails(spot: ParkingSpot, onDismiss: () -> Unit) {
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
            onClick = {
                val gmmIntentUri =
                    Uri.parse("geo:${spot.location.latitude},${spot.location.longitude}?q=${spot.name}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                onDismiss()
                context.startActivity(mapIntent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Navigate to Parking Spot")
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
