package com.example.test2

import android.content.res.AssetManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.room.Room
import com.example.test2.Dao.AppDatabase
import com.example.test2.Dao.NationalWeatherTable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream


class testActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)


        val NationalWeatherDB = Room.databaseBuilder(applicationContext, AppDatabase::class.java,"db").build()
        //val input = NationalWeatherTable(1114062500,"seoul", "jongrogu", "dasandong", 60, 126)

        val assetManager: AssetManager = resources.assets
        val inputStream: InputStream = assetManager.open("NationalWeatherDB.txt")

        inputStream.bufferedReader().readLines().forEach {
            var token = it.split("\t")
            var input = NationalWeatherTable(token[0].toLong(), token[1], token[2], token[3], token[4].toInt(), token[5].toInt())


                // 데이터에 읽고 쓸때는 쓰레드 사용
//            val thread2 = Thread({
//
//            }).start()
            CoroutineScope(Dispatchers.Main).launch {

            }
//            CoroutineScope(Dispatchers.IO).launch {
//                NationalWeatherDB.nationalWeatherInterface().insert(input)
//            }
            // Log.d("file_test", token.toString())
        }

        CoroutineScope(Dispatchers.IO).launch {
            //NationalWeatherDB.nationalWeatherInterface().deleteAll()
            //NationalWeatherDB.nationalWeatherInterface().insert(input)
            var output = NationalWeatherDB.nationalWeatherInterface().getAll()
            Log.d("db_test", "$output")
        }
    }
}