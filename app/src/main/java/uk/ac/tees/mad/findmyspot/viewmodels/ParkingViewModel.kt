package uk.ac.tees.mad.findmyspot.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.findmyspot.database.AppDatabase
import uk.ac.tees.mad.findmyspot.database.ParkingSpotDao
import uk.ac.tees.mad.findmyspot.model.ParkingSpot
import java.io.ByteArrayOutputStream

class ParkingViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()

    private val _parkingSpots = MutableStateFlow<List<ParkingSpot>>(emptyList())
    val parkingSpots: StateFlow<List<ParkingSpot>> = _parkingSpots.asStateFlow()

    private var parkingSpotDao: ParkingSpotDao =
        AppDatabase.getDbInstance(application).parkingSpotDao()

    init {
        viewModelScope.launch {
            // Fetch data from Room initially
            _parkingSpots.value = getParkingSpotsFromRoom()

            // Fetch data from Firestore and cache to Room
            fetchAndCacheParkingSpots()
        }
    }


    // Fetch parking spots from Room
    private suspend fun getParkingSpotsFromRoom(): List<ParkingSpot> {
        return parkingSpotDao.getAllParkingSpots()
    }

    // Save parking spots to Room
    private suspend fun saveParkingSpotsToRoom(parkingSpots: List<ParkingSpot>) {
        parkingSpotDao.insertParkingSpots(parkingSpots)
    }

    suspend fun deleteParkingSpot(parkingSpot: ParkingSpot) {
        parkingSpotDao.deleteParkingSpot(parkingSpot)
    }

    suspend fun fetchAndCacheParkingSpots() {
        val db = FirebaseFirestore.getInstance()

        db.collection("parking_spots").get().addOnSuccessListener { snapshots ->
            val spotsList = snapshots.documents.mapNotNull { doc ->
                val name = doc.getString("name")
                val latitude = doc.getDouble("latitude")
                val longitude = doc.getDouble("longitude")
                val availability = doc.getString("availability")
                val pricePerHour = doc.getDouble("price_per_hour")
                val imageBase64 = doc.getString("image_base64")

                if (name != null && latitude != null && longitude != null && availability != null && pricePerHour != null) {
                    ParkingSpot(
                        doc.id,
                        name,
                        latitude,
                        longitude,
                        availability,
                        pricePerHour,
                        imageBase64
                    )
                } else null
            }
            viewModelScope.launch {
                // Save data to Room
                saveParkingSpotsToRoom(spotsList)
            }
        }
    }

    fun getParkingSpotById(spotId: String): ParkingSpot? = _parkingSpots.value.find {
        it.id == spotId
    }

    fun addParkingSpot(newSpot: ParkingSpot, onSuccess: () -> Unit) {
        val spotData = hashMapOf(
            "name" to newSpot.name,
            "latitude" to newSpot.latitude,
            "longitude" to newSpot.longitude,
            "availability" to newSpot.availability,
            "price_per_hour" to newSpot.pricePerHour,
            "image_base64" to newSpot.imageBase64
        )

        db.collection("parking_spots").document(newSpot.id).set(spotData)
            .addOnSuccessListener {
                viewModelScope.launch {
                    fetchAndCacheParkingSpots()
                    onSuccess()
                }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error adding parking spot", it)
            }
    }

    fun updateParkingSpot(updatedSpot: ParkingSpot, onSuccess: () -> Unit) {
        val spotData = hashMapOf(
            "name" to updatedSpot.name,
            "latitude" to updatedSpot.latitude,
            "longitude" to updatedSpot.longitude,
            "availability" to updatedSpot.availability,
            "price_per_hour" to updatedSpot.pricePerHour,
            "image_base64" to updatedSpot.imageBase64
        )

        db.collection("parking_spots").document(updatedSpot.id)
            .set(spotData)
            .addOnSuccessListener {
                viewModelScope.launch {
                    fetchAndCacheParkingSpots()
                    onSuccess()
                }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error updating parking spot", it)
            }
    }


    fun deleteParkingSpot(spotId: String, onSuccess: () -> Unit) {
        db.collection("parking_spots").document(spotId)
            .delete()
            .addOnSuccessListener {
                viewModelScope.launch {
                    deleteParkingSpot(getParkingSpotById(spotId)!!)
                    fetchAndCacheParkingSpots()
                    onSuccess()
                }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error deleting parking spot", it)
            }
    }

}

fun encodeImageToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun decodeBase64ToImage(base64String: String): ImageBitmap {
    val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    return bitmap.asImageBitmap()
}
