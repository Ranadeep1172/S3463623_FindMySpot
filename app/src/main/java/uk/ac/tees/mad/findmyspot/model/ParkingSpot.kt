package uk.ac.tees.mad.findmyspot.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parking_spots")
data class ParkingSpot(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val availability: String,
    val pricePerHour: Double,
    val imageBase64: String? = null
)