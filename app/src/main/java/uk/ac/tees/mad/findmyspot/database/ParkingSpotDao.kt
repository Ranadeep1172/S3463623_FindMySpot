package uk.ac.tees.mad.findmyspot.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import uk.ac.tees.mad.findmyspot.model.ParkingSpot

@Dao
interface ParkingSpotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParkingSpots(parkingSpots: List<ParkingSpot>)

    @Query("SELECT * FROM parking_spots")
    suspend fun getAllParkingSpots(): List<ParkingSpot>

    @Query("SELECT * FROM parking_spots WHERE id = :id")
    suspend fun getParkingSpotById(id: String): ParkingSpot?

    @Delete
    suspend fun deleteParkingSpot(parkingSpot: ParkingSpot)
}
