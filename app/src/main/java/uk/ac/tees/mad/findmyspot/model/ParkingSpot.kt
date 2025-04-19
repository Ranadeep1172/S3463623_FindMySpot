package uk.ac.tees.mad.findmyspot.model

import com.google.android.gms.maps.model.LatLng

data class ParkingSpot(
    val id: String,
    val name: String,
    val location: LatLng,
    val availability: String,
    val pricePerHour: Double,
    val imageBase64: String? = null
)