package com.example.test2


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.test2.Adapter.WeatherAdapter
import com.example.test2.Common.Common
import com.example.test2.Dao.NationalWeatherDatabase
import com.example.test2.Dao.WeatherLocationDatabase
import com.example.test2.Dao.WeatherLocationTable
import com.example.test2.Model.ModelWeather
import com.example.test2.network.ApiObject
import com.example.test2.network.ITEM
import com.example.test2.network.WEATHER
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class testActivity : AppCompatActivity() {
    lateinit var weatherRecyclerView: RecyclerView

    private var base_date = "20221101" // 발표 일자
    private var base_time = "1200" // 발표 시각
    private var nx = 50 // 예보지점 X 좌표
    private var ny = 75 // 예보지점 Y 좌표

    private lateinit var address: String
    private lateinit var lotemp: String
    private lateinit var lohumidity: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        val add = findViewById<TextView>(R.id.address)
        val tvDate =
            findViewById<TextView>(R.id.tvDate) // 오늘 날짜 텍스트뷰
        weatherRecyclerView = findViewById<RecyclerView>(R.id.weatherRecyclerView) // 날씨 리사이클러 뷰
// 리사이클러 뷰 매니저 설정
        weatherRecyclerView.layoutManager = LinearLayoutManager(this@testActivity)
        weatherRecyclerView.layoutManager =
            LinearLayoutManager(this).also { it.orientation = LinearLayoutManager.HORIZONTAL }
// 오늘 날짜 텍스트뷰 설정
        tvDate.text = SimpleDateFormat(
            "MM월 dd일",
            Locale.getDefault()
        ).format(Calendar.getInstance().time) + "날씨"


        val WeatherLocationDB =
            Room.databaseBuilder(applicationContext, WeatherLocationDatabase::class.java, "Weatherlocation")
                .allowMainThreadQueries()
                .build()


        if (intent.hasExtra("nx")) {

            nx = intent.getIntExtra("nx", nx)
            ny = intent.getIntExtra("ny", ny)
            address = intent.getStringExtra("address").toString()

        } else {
            Toast.makeText(applicationContext, "좌표값 없음", Toast.LENGTH_SHORT).show()
        }
        add.text = address
// nx, ny지점의 날씨 가져와서 설정하기


        setWeather(nx, ny)

        val btnc = findViewById<Button>(R.id.btncancel)
        btnc.setOnClickListener {
            onBackPressed()
        }
        val btnok = findViewById<Button>(R.id.btnok)
        btnok.setOnClickListener {
            //주소 온도 습도
            WeatherLocationDB.WeatherLocationInterface().insert(WeatherLocationTable( address,lotemp,lohumidity))
            var output = WeatherLocationDB.WeatherLocationInterface().getAll()

            Log.d("wb_db", "$output")
            var intent = Intent(this, LocationActivity::class.java)
            startActivity(intent)

        }

    }

    // 날씨 가져와서 설정하기
    private fun setWeather(nx: Int, ny: Int) {
        val cal = Calendar.getInstance()
        base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time) // 현재 날짜
        val timeH = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time)
        val timeM = SimpleDateFormat("MM", Locale.getDefault()).format(cal.time)

// API 가져오기 적당하게 변환
        base_time = Common().getBaseTime(timeH, timeM)
// API 가져오기 적당하게 변환
// base_time = getBaseTime(timeH, timeM)
// 현재 시각이 00시이고 45분 이하여서 baseTime이 2330이면 어제 정보 받아오기
        if (timeH == "00" && base_time == "2330") {
            cal.add(Calendar.DATE, -1).toString()
            base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        }

// 날씨 정보 가져오기
// (한 페이지 결과 수 = 60, 페이지 번호 = 1, 응답 자료 형식-"JSON", 발표 날싸, 발표 시각, 예보지점 좌표)
        val call =
            ApiObject.retrofitService.GetWeather(60, 1, "JSON", base_date, base_time, nx, ny)

// 비동기적으로 실행하기
        call.enqueue(object : retrofit2.Callback<WEATHER> {
            // 응답 성공 시
            override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
                if (response.isSuccessful) {
// 날씨 정보 가져오기
                    val it: List<ITEM> = response.body()!!.response.body.items.item

// 현재 시각부터 1시간 뒤의 날씨 6개를 담을 배열
                    val weatherArr = arrayOf(
                        ModelWeather(),
                        ModelWeather(),
                        ModelWeather(),
                        ModelWeather(),
                        ModelWeather(),
                        ModelWeather(),
                    )

// 배열 채우기
                    var index = 0
                    val totalCount = response.body()!!.response.body.totalCount - 1
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
                    weatherArr[0].fcstTime = "지금"

// 각 날짜 배열 시간 설정
                    for (i in 0..5) weatherArr[i].fcstTime = it[i].fcstTime

// 리사이클러 뷰에 데이터 연결
                    weatherRecyclerView.adapter = WeatherAdapter(weatherArr)
                    Log.d("시간:", base_time)
                    val tvTime = findViewById<TextView>(R.id.tvTime)
                    val imgWeather = findViewById<ImageView>(R.id.imgWeather)
                    val tvTemp = findViewById<TextView>(R.id.tvTemp)
                    val tvHumidity = findViewById<TextView>(R.id.tvHumidity)
                    tvTime.text = base_time
                    imgWeather.setImageResource(
                        getRainImage(
                            weatherArr[0].rainType,
                            weatherArr[0].sky,
                            weatherArr[0].fcstTime
                        )
                    )

                    lohumidity =weatherArr[0].humidity + "%"
                    lotemp=weatherArr[0].temp + "°"
                    tvTime.text = getTime(weatherArr[0].fcstTime)
                    tvHumidity.text = lohumidity
                    tvTemp.text = lotemp


// 토스트 띄우기
                    Toast.makeText(
                        applicationContext,
                        it[0].fcstDate + ", " + it[0].fcstTime + "의 날씨 정보입니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            //check
            // 응답 실패 시
            override fun onFailure(call: Call<WEATHER>, t: Throwable) {

            }
        })
    }

    fun getRainImage(rainType: String, sky: String, factTime: String): Int {
        return when (rainType) {
            "0" -> getWeatherImage(sky, factTime)
            "1" -> R.drawable.rainy
            "2" -> R.drawable.hail
            "3" -> R.drawable.snowy
            "4" -> R.drawable.brash
            else -> getWeatherImage(sky, factTime)
        }
    }

    fun getWeatherImage(sky: String, factTime: String): Int {
        // 하늘 상태
        return when (sky) {
            "1" -> getWeatherImage2(factTime)                      // 맑음
            "3" -> R.drawable.cloudy                     // 구름 많음
            "4" -> R.drawable.blur2                // 흐림
            else -> R.drawable.ic_launcher_foreground   // 오류
        }
    }

    fun getWeatherImage2(factTime: String): Int {

        return if (factTime.toInt() < 600) return R.drawable.after
        else if (factTime.toInt() < 1800) return R.drawable.sun2
        else return R.drawable.after


    }

    fun getTime(factTime: String): String {
        if (factTime != "지금") {
            var hourSystem: Int = factTime.toInt()
            var hourSystemString = ""


            if (hourSystem == 0) {
                return "오전 12시"
            } else if (hourSystem > 2100) {
                hourSystem -= 1200
                hourSystemString = hourSystem.toString()
                return "오후 ${hourSystemString[0]}${hourSystemString[1]}시"


            } else if (hourSystem == 1200) {
                return "오후 12시"
            } else if (hourSystem > 1200) {
                hourSystem -= 1200
                hourSystemString = hourSystem.toString()
                return "오후 ${hourSystemString[0]}시"

            } else if (hourSystem >= 1000) {
                hourSystemString = hourSystem.toString()

                return "오전 ${hourSystemString[0]}${hourSystemString[1]}시"
            } else {

                hourSystemString = hourSystem.toString()

                return "오전 ${hourSystemString[0]}시"

            }

        } else {
            return factTime
        }


    }


}