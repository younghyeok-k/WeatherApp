package com.example.test2.Dao


import androidx.room.*


@Dao
interface NationalWeatherInterface {
    @Query("SELECT * FROM NationalWeatherTable")
    fun getAll(): List<NationalWeatherTable>

    @Query("SELECT name1 FROM NationalWeatherTable")
    fun getName1() : List<String>

    @Query("SELECT name2 FROM NationalWeatherTable")
    fun getName2(): List<String>

    @Query("SELECT name3 FROM NationalWeatherTable")
    fun getName3(): List<String>

    @Insert
    fun insert(nationalWeatherTable: NationalWeatherTable)

    @Query("DELETE FROM NationalWeatherTable")
    fun deleteAll()



}
@Entity
data class NationalWeatherTable(
    @PrimaryKey val code: Long,
    val name1: String,
    val name2: String,
    val name3: String,
    val x: Int,
    val y: Int
)
@Database(entities = [NationalWeatherTable::class], version = 1)
abstract class NationalWeatherDatabase: RoomDatabase() {
    abstract fun nationalWeatherInterface(): NationalWeatherInterface
}