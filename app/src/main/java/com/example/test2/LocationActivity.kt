package com.example.test2

import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Point
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room

import com.example.test2.Dao.NationalWeatherDatabase
import com.example.test2.Dao.NationalWeatherTable
import com.example.test2.Dao.SpaceTokenizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream


class LocationActivity : AppCompatActivity() {
    private var curPoint: Point? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        var geocoder = Geocoder(applicationContext)
        var mResultlist: List<Address>
        val edadd = findViewById<MultiAutoCompleteTextView>(R.id.edadd)
        val btncheck = findViewById<Button>(R.id.btncheck)
        var tx = findViewById<TextView>(R.id.tx)
        var ty = findViewById<TextView>(R.id.ty)
        var ax: Double
        var ay: Double
        var adapter: ArrayAdapter<String>


        val back = findViewById<Button>(R.id.btnback)
        back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

//        val NationalWeatherDB = Room.databaseBuilder(applicationContext, AppDatabase::class.java,"db").build()
//        //val input = NationalWeatherTable(1114062500,"seoul", "jongrogu", "dasandong", 60, 126)
//


        val NationalWeatherDB =
            Room.databaseBuilder(applicationContext, NationalWeatherDatabase::class.java, "db")
                .allowMainThreadQueries()
                .build()
//        val assetManager: AssetManager = resources.assets
//        val inputStream: InputStream = assetManager.open("NationalWeatherDB.txt")
//
//        inputStream.bufferedReader().readLines().forEach {
//            var token = it.split("\t")
//            var input = NationalWeatherTable(token[0].toLong(), token[1], token[2], token[3], token[4].toInt(), token[5].toInt())
//            CoroutineScope(Dispatchers.Main).launch {
//                NationalWeatherDB.nationalWeatherInterface().insert(input)
//            }
//            // Log.d("file_test", token.toString())
//        }



        CoroutineScope(Dispatchers.IO).launch {
            //NationalWeatherDB.nationalWeatherInterface().deleteAll()
            //NationalWeatherDB.nationalWeatherInterface().insert(input)
            var output = NationalWeatherDB.nationalWeatherInterface().getAll()

            Log.d("db_test", "$output")
        }
        val word: MutableList<String> = ArrayList()
        val name1 = NationalWeatherDB.nationalWeatherInterface().getName1().distinct()
        val name2 = (NationalWeatherDB.nationalWeatherInterface().getName2().distinct())
        val name3 = (NationalWeatherDB.nationalWeatherInterface().getName3().distinct())
        word.addAll(name1)
        word.addAll(name2)
        word.addAll(name3)
        Log.d("db_test", "$word")


//        adapter =
//            ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, word)
//        edadd.setAdapter(adapter)
//        edadd.setTokenizer(SpaceTokenizer)

        ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            word
        ).also { adapter ->
            edadd.setAdapter(adapter) // ArrayAdpater 는 검색어 목록을 보여주는데 사용
            edadd.setTokenizer(SpaceTokenizer()) // Tokenizer 는 단어들을 띄어쓰기로 구분
        }


        btncheck.setOnClickListener {
            val input: String = edadd.text.toString().trim()
            var singleInputs: List<String> = input.split(",")
            var toastText = ""
            for ((index, item) in singleInputs.withIndex()) {

                if (item.isNotEmpty()) {
                    toastText += "Item $index : $item \n"
                }
            }


            mResultlist = geocoder.getFromLocationName(edadd.text.toString(), 3)
            ax = mResultlist[0].latitude
            ay = mResultlist[0].longitude
            curPoint = dfsXyConv(ax, ay)
            tx.text = curPoint!!.x.toString()
            ty.text = curPoint!!.y.toString()
            val intent = Intent(this, testActivity::class.java)
            intent.putExtra("nx", curPoint!!.x)
            intent.putExtra("ny", curPoint!!.y)
            intent.putExtra("address", mResultlist[0].locality)

            startActivity(intent)
        }

    }

    // 위경도를 기상청에서 사용하는 격자 좌표로 변환
    fun dfsXyConv(v1: Double, v2: Double): Point {
        val RE = 6371.00877     // 지구 반경(km)
        val GRID = 5.0          // 격자 간격(km)
        val SLAT1 = 30.0        // 투영 위도1(degree)
        val SLAT2 = 60.0        // 투영 위도2(degree)
        val OLON = 126.0        // 기준점 경도(degree)
        val OLAT = 38.0         // 기준점 위도(degree)
        val XO = 43             // 기준점 X좌표(GRID)
        val YO = 136       // 기준점 Y좌표(GRID)
        val DEGRAD = Math.PI / 180.0
        val re = RE / GRID
        val slat1 = SLAT1 * DEGRAD
        val slat2 = SLAT2 * DEGRAD
        val olon = OLON * DEGRAD
        val olat = OLAT * DEGRAD

        var sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn)
        var sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn
        var ro = Math.tan(Math.PI * 0.25 + olat * 0.5)
        ro = re * sf / Math.pow(ro, sn)

        var ra = Math.tan(Math.PI * 0.25 + (v1) * DEGRAD * 0.5)
        ra = re * sf / Math.pow(ra, sn)
        var theta = v2 * DEGRAD - olon
        if (theta > Math.PI) theta -= 2.0 * Math.PI
        if (theta < -Math.PI) theta += 2.0 * Math.PI
        theta *= sn

        val x = (ra * Math.sin(theta) + XO + 0.5).toInt()
        val y = (ro - ra * Math.cos(theta) + YO + 0.5).toInt()

        return Point(x, y)
    }
}