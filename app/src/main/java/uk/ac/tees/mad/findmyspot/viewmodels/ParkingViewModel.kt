package uk.ac.tees.mad.findmyspot.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uk.ac.tees.mad.findmyspot.model.ParkingSpot
import java.io.ByteArrayOutputStream

class ParkingViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _parkingSpots = MutableStateFlow<List<ParkingSpot>>(emptyList())
    val parkingSpots: StateFlow<List<ParkingSpot>> = _parkingSpots.asStateFlow()

    init {
        fetchParkingSpots()
    }

    private fun fetchParkingSpots() {
        db.collection("parking_spots").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.e("Firestore", "Error fetching parking spots", e)
                return@addSnapshotListener
            }

            val spotsList = snapshots?.documents?.mapNotNull { doc ->
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
                        LatLng(latitude, longitude),
                        availability,
                        pricePerHour,
                         imageBase64
                    )
                } else null
            } ?: emptyList()

            _parkingSpots.value = spotsList
        }
    }

    fun getParkingSpotById(spotId: String): ParkingSpot? = _parkingSpots.value.find {
        it.id == spotId
    }

    fun addParkingSpot(newSpot: ParkingSpot, onSuccess: () -> Unit) {
        val spotData = hashMapOf(
            "name" to newSpot.name,
            "latitude" to newSpot.location.latitude,
            "longitude" to newSpot.location.longitude,
            "availability" to newSpot.availability,
            "price_per_hour" to newSpot.pricePerHour,
            "image_base64" to newSpot.imageBase64
        )

        db.collection("parking_spots").document(newSpot.id).set(spotData)
            .addOnSuccessListener {
                fetchParkingSpots()
                onSuccess()
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error adding parking spot", it)
            }
    }

    fun updateParkingSpot(updatedSpot: ParkingSpot, onSuccess: () -> Unit) {
        val spotData = hashMapOf(
            "name" to updatedSpot.name,
            "latitude" to updatedSpot.location.latitude,
            "longitude" to updatedSpot.location.longitude,
            "availability" to updatedSpot.availability,
            "price_per_hour" to updatedSpot.pricePerHour,
            "image_base64" to updatedSpot.imageBase64
        )

        db.collection("parking_spots").document(updatedSpot.id)
            .set(spotData)
            .addOnSuccessListener {
                fetchParkingSpots()
                onSuccess()
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error updating parking spot", it)
            }
    }


    fun deleteParkingSpot(spotId: String, onSuccess: () -> Unit) {
        db.collection("parking_spots").document(spotId)
            .delete()
            .addOnSuccessListener {
                fetchParkingSpots()
                onSuccess()
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
