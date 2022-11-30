package com.example.test2.Dao


import androidx.room.*


@Dao
interface WeatherLocationInterface {
    @Query("SELECT * FROM WeatherLocationTable")
    fun getAll(): MutableList<WeatherLocationTable>

    @Update
    fun update(WeatherLocationTable: WeatherLocationTable)

    @Insert
    fun insert(WeatherLocationTable: WeatherLocationTable)

    @Query("DELETE FROM WeatherLocationTable")
    fun deleteAll()

    @Query("DELETE FROM WeatherLocationTable WHERE id = :id")
    fun de(id:Int)

    @Query("UPDATE  WeatherLocationTable SET addcity = :addcity,wx=:wx,wy=:wy WHERE id = 0")
    fun up(addcity: String,wx: Int,wy: Int)
    @Delete
    fun delete(WeatherLocationTable:WeatherLocationTable)
}

@Entity
data class WeatherLocationTable(
    val addcity: String,
    val wx: Int,
    val wy: Int

) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Database(entities = [WeatherLocationTable::class], version = 1)
abstract class WeatherLocationDatabase : RoomDatabase() {
    abstract fun WeatherLocationInterface(): WeatherLocationInterface
}

