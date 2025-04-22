package uk.ac.tees.mad.findmyspot.ui.screens


import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import uk.ac.tees.mad.findmyspot.model.ParkingSpot
import uk.ac.tees.mad.findmyspot.viewmodels.ParkingViewModel
import uk.ac.tees.mad.findmyspot.viewmodels.decodeBase64ToImage
import uk.ac.tees.mad.findmyspot.viewmodels.encodeImageToBase64
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddSpotScreen(navController: NavController, viewModel: ParkingViewModel = viewModel()) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("Available") }
    var pricePerHour by remember { mutableStateOf("") }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var imageBase64 by remember { mutableStateOf<String?>(null) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                imageBase64 = encodeImageToBase64(it)
            }
        }

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
                title = { Text("Add Parking Spot") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                Text(text = "Latitude: ${it?.latitude}, Longitude: ${it?.longitude}")
            }

            imageBase64?.let {
                Image(
                    bitmap = decodeBase64ToImage(it),
                    contentDescription = "Parking Spot Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Button(onClick = { launcher.launch(null) }) {
                Text("Capture Image")
            }

            Button(
                onClick = {
                    userLocation?.let { location ->
                        val newSpot = ParkingSpot(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            availability = availability,
                            pricePerHour = pricePerHour.toDouble(),
                            imageBase64 = imageBase64
                        )
                        viewModel.addParkingSpot(newSpot) {
                            Toast.makeText(context, "Spot Added!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    } ?: Toast.makeText(
                        context,
                        "Location fetching failed. Turn GPS on",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Spot")
            }
        }
    }
}
