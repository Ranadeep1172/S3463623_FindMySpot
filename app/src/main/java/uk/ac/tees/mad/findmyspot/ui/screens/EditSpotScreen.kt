package uk.ac.tees.mad.findmyspot.ui.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import uk.ac.tees.mad.findmyspot.model.ParkingSpot

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EditSpotScreen(
    spot: ParkingSpot,
    onBack: () -> Unit,
    onDone: (ParkingSpot) -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(spot.name) }
    var availability by remember { mutableStateOf(spot.availability) }
    var pricePerHour by remember { mutableStateOf(spot.pricePerHour.toString()) }

    var userLocation by remember { mutableStateOf(spot.location) }
    val permissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(permissionState) {
        if (permissionState.status.isGranted) {
            getLocation(fusedLocationClient, onGetLocation = {
                userLocation = it
            })
        } else {
            permissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Parking Spot") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Spot Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = availability,
                onValueChange = { availability = it },
                label = { Text("Availability") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pricePerHour,
                onValueChange = { pricePerHour = it },
                label = { Text("Price Per Hour") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            userLocation.let {
                Text(text = "Latitude: ${it.latitude}, Longitude: ${it.longitude}")
            }

            Button(
                onClick = {
                    userLocation.let { location ->
                        val updatedSpot = spot.copy(
                            name = name,
                            location = location,
                            availability = availability,
                            pricePerHour = pricePerHour.toDouble()
                        )
                        onDone(updatedSpot)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }

            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete Spot")
            }
        }
    }
}


fun getLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onGetLocation: (LatLng) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        location?.let {
            onGetLocation(LatLng(it.latitude, it.longitude))

        }
    }
}
