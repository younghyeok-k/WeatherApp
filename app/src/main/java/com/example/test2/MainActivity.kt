package com.example.test2


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Point
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test2.Adapter.WeatherAdapter
import com.example.test2.Common.Common
import com.example.test2.Model.ModelWeather
import com.example.test2.NewApi.ApiNews
import com.example.test2.network.ApiObject
import com.example.test2.network.ITEM
import com.example.test2.network.WEATHER
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

// 메인 액티비티
class MainActivity : AppCompatActivity() {
    lateinit var weatherRecyclerView: RecyclerView

    private var base_date = "20221101" // 발표 일자
    private var base_time = "1200" // 발표 시각
    private var nx = "81" // 예보지점 X 좌표
    private var ny = "75" // 예보지점 Y 좌표
    private var curPoint: Point? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvDate =
            findViewById<TextView>(R.id.tvDate) // 오늘 날짜 텍스트뷰
        weatherRecyclerView = findViewById<RecyclerView>(R.id.weatherRecyclerView) // 날씨 리사이클러 뷰
        val btnRefresh = findViewById<Button>(R.id.btnRefresh) // 새로고침 버튼


// 리사이클러 뷰 매니저 설정
        weatherRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        weatherRecyclerView.layoutManager =
            LinearLayoutManager(this).also { it.orientation = LinearLayoutManager.HORIZONTAL }
// 오늘 날짜 텍스트뷰 설정
        tvDate.text = SimpleDateFormat(
            "MM월 dd일",
            Locale.getDefault()
        ).format(Calendar.getInstance().time) + "날씨"

// nx, ny지점의 날씨 가져와서 설정하기
        requestLocation()
// <새로고침> 버튼 누를 때 날씨 정보 다시 가져오기
        btnRefresh.setOnClickListener {
            requestLocation()
        }



//        val thread = Thread({
//            var apiExamSearchBlog = ApiNews()
//            apiExamSearchBlog.main()
//        }).start()

        val btnmap=findViewById<Button>(R.id.btnmap)
        btnmap.setOnClickListener{
            val intent = Intent(this, LocationActivity::class.java)
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
                    tvTime.text = getTime(weatherArr[0].fcstTime)
                    tvHumidity.text = weatherArr[0].humidity + "%"
                    tvTemp.text = weatherArr[0].temp + "°"


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
//                AlertDialog.Builder(this@MainActivity)
//                    .setTitle("Title")
//                    .setMessage(t.message.toString())
//                    .setNegativeButton("cancel", object : DialogInterface.OnClickListener {
//                        override fun onClick(dialog: DialogInterface, which: Int) {
//                            Log.d("MyTag", "negative")
//                        }
//                    })
//
//                    .create()
//                    .show()
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
//
//        if (600<factTime.toInt() < 1800 ) {
//            return R.drawable.after                       // 맑음
//        } else{
//// 00  1800  1900
//            return R.drawable.sun2
//        }


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

    // 내 현재 위치의 위경도를 격자 좌표로 변환하여 해당 위치의 날씨정보 설정하기
    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        val locationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
        var currentLocation:String
        try {
            // 나의 현재 위치 요청
            val locationRequest = LocationRequest.create()
            locationRequest.run {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 60 * 1000    // 요청 간격(1초)
            }
            val locationCallback = object : LocationCallback() {
                // 요청 결과
                override fun onLocationResult(p0: LocationResult) {
                    p0.let {
                        for (location in it.locations) {

                            Log.d("위치", (location.latitude).toString())
                            Log.d("위치", (location.longitude).toString())
                            var geocoder = Geocoder(applicationContext)
                            var mResultlist: List<Address>
                            val address =
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)

                                mResultlist = geocoder.getFromLocation(
                                    location.latitude,
                                    location.longitude,
                                    1
                                )



                            currentLocation = mResultlist[0].locality

                            val txad = findViewById<TextView>(R.id.address);
                            txad.text = currentLocation
                            // 현재 위치의 위경도를 격자 좌표로 변환
                            curPoint = dfsXyConv(location.latitude, location.longitude)
                            Log.d("위치2", (curPoint!!.x).toString())
                            Log.d("위치2", (curPoint!!.y).toString())
                            // nx, ny지점의 날씨 가져와서 설정하기
                            setWeather(curPoint!!.x, curPoint!!.y)
                        }
                    }
                }
            }

            // 내 위치 실시간으로 감지
            Looper.myLooper()?.let {
                locationClient.requestLocationUpdates(
                    locationRequest, locationCallback,
                    it
                )
            }


        } catch (e: SecurityException) {
            e.printStackTrace()
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