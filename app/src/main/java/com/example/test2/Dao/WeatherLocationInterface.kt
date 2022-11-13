package com.example.test2.Dao


import androidx.room.*


@Dao
interface WeatherLocationInterface {
    @Query("SELECT * FROM WeatherLocationTable")
    fun getAll(): List<WeatherLocationTable>


    @Insert
    fun insert(WeatherLocationTable: WeatherLocationTable)

    @Query("DELETE FROM WeatherLocationTable")
    fun deleteAll()

}
@Entity
data class WeatherLocationTable(
    val addcity: String,
    val temp: String,//기온
    val humidity: String,//습도

){
    @PrimaryKey (autoGenerate = true)var id:Int=0
}
@Database(entities = [WeatherLocationTable::class], version = 1)
abstract class WeatherLocationDatabase: RoomDatabase() {
    abstract fun WeatherLocationInterface(): WeatherLocationInterface
}

