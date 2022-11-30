package com.example.test2

import android.content.Intent
import android.graphics.Point
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.test2.Adapter.LocationAdpater
import com.example.test2.Common.Common
import com.example.test2.Dao.*

import com.example.test2.Model.ModelLocation
import com.example.test2.Touch.SwipeController
import com.example.test2.network.ApiObject
import com.example.test2.network.ITEM
import com.example.test2.network.WEATHER
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class LocationActivity : AppCompatActivity() {
    private var curPoint: Point? = null
    private var base_time = "1200"
    lateinit var locationRecyclerView: RecyclerView
    private var base_date = "20221101"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        var geocoder = Geocoder(applicationContext)
        var mResultlist: List<Address>
        val edadd = findViewById<MultiAutoCompleteTextView>(R.id.edadd)
        val btncheck = findViewById<Button>(R.id.btncheck)
        var ax: Double
        var ay: Double
        locationRecyclerView = findViewById<RecyclerView>(R.id.locationrecyclerview)

        locationRecyclerView.layoutManager = LinearLayoutManager(this@LocationActivity)
        val back = findViewById<Button>(R.id.btnback)
        back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val NationalWeatherDB =
            Room.databaseBuilder(applicationContext, NationalWeatherDatabase::class.java, "db")
                .allowMainThreadQueries()
                .build()

        val WeatherLocationDB = Room.databaseBuilder(
            applicationContext,
            WeatherLocationDatabase::class.java,
            "Weatherlocation"
        )
            .allowMainThreadQueries()
            .build()

        CoroutineScope(Dispatchers.IO).launch {
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

        ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, word).also { adapter ->
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

            val intent = Intent(this, testActivity::class.java)
            intent.putExtra("nx", curPoint!!.x)
            intent.putExtra("ny", curPoint!!.y)
            intent.putExtra("address", mResultlist[0].locality)

            startActivity(intent)
        }
        var DBlist: MutableList<WeatherLocationTable>

        DBlist = WeatherLocationDB.WeatherLocationInterface().getAll()
//            setWeather(WeatherLocationDB.WeatherLocationInterface().getAll())
        Log.d("개수", DBlist.size.toString())
       var mainx = intent.getIntExtra("mainx", 81)
        var mainY= intent.getIntExtra("mainy", 75)
        var mainadd= intent.getStringExtra("mainaddress").toString()

       var loAdapArr: MutableList< ModelLocation> = mutableListOf()

        Locationadd(mainadd,mainx,mainY,loAdapArr)//0
        if(DBlist.size>=1) {
            for (i in 0..DBlist.size - 1) {
                Locationadd(DBlist[i].addcity, DBlist[i].wx, DBlist[i].wy, loAdapArr)
                Log.d("setWether", DBlist[i].id.toString())
            }
        }
        //진주시가 제일위에가도록ㅎ ㅐ야함

        val adpter = LocationAdpater(this,loAdapArr)
        val itemTouchHelper = ItemTouchHelper(SwipeController(adpter))
        adpter.setOnItemClickListener(object : LocationAdpater.OnItemClickListener {
            override fun onItemClick(v: View, pos: Int) {
                if(DBlist.size>=1) {
//                WeatherLocationDB.WeatherLocationInterface().delete(DBlist[pos-1])
                }
                locationRecyclerView.adapter?.notifyDataSetChanged()
                adpter.notifyDataSetChanged()
            }


        })

        itemTouchHelper.attachToRecyclerView(locationRecyclerView)
        locationRecyclerView.adapter = adpter
        locationRecyclerView.adapter?.notifyDataSetChanged()
        adpter.notifyDataSetChanged()
        loAdapArr.clear()
    }


    // 날씨 가져와서 설정하기
    private fun Locationadd(

        loaddress: String,
        nx: Int,
        ny: Int,
        loAdapArr: MutableList<ModelLocation>
    ) {
        val cal = Calendar.getInstance()
        base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time) // 현재 날짜
        val timeH = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time)
        val timeM = SimpleDateFormat("MM", Locale.getDefault()).format(cal.time)
        base_time = Common().getBaseTime(timeH, timeM)
        if (timeH == "00" && base_time == "2330") {
            cal.add(Calendar.DATE, -1).toString()
            base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        }
        val call =
            ApiObject.retrofitService.GetWeather(60, 1, "JSON", base_date, base_time, nx, ny)

        call.enqueue(object : retrofit2.Callback<WEATHER> {
            override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
                if (response.isSuccessful) {
                    val it: List<ITEM> = response.body()!!.response.body.items.item
                    val weatherArr = mutableListOf(
                        ModelLocation(),
                        ModelLocation(),
                        ModelLocation(),
                        ModelLocation(),
                        ModelLocation(),
                        ModelLocation(),
                        )
                    var index = 0
                    val totalCount = response.body()!!.response.body.totalCount - 1
                    weatherArr[0].address = loaddress
                    for (i in 0..totalCount) {
                        index %= 6
                        when (it[i].category) {
                            "PTY" -> weatherArr[index].rainType = it[i].fcstValue // 강수 형태
                            "REH" -> weatherArr[index].humidity = it[i].fcstValue // 습도
                            "SKY" -> weatherArr[index].sky = it[i].fcstValue // 하늘 상태
                            "T1H" -> weatherArr[index].temp = it[i].fcstValue // 기온

                            else -> continue
                        }
                        index++
                    }
                    for (i in 0..5) weatherArr[i].fcstTime = it[i].fcstTime
                    loAdapArr.add( weatherArr[0])
                    locationRecyclerView.adapter?.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<WEATHER>, t: Throwable) {

            }
        }


        )
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