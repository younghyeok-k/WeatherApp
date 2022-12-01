package com.example.test2.network


import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// 결과 xml 파일에 접근해서 정보 가져오기
interface WeatherInterface {
    // getUltraSrtFcst : 초단기 예보 조회 + 인증키
    @GET("getUltraSrtFcst?serviceKey=838R8j2OlUoP63LGXZlCROsfDobS%2FlgSz0LLyY4vl71MquB2n%2FXZZg9HtejYjInO6sGmfsILunQitjjdDMosOg%3D%3D")

    fun GetWeather(@Query("numOfRows") num_of_rows: Int,   // 한 페이지 경과 수
                   @Query("pageNo") page_no: Int,          // 페이지 번호
                   @Query("dataType") data_type: String,   // 응답 자료 형식
                   @Query("base_date") base_date: String,  // 발표 일자
                   @Query("base_time") base_time: String,  // 발표 시각
                   @Query("nx") nx: Int,                // 예보지점 X 좌표
                   @Query("ny") ny: Int
    )                // 예보지점 Y 좌표
            : Call<WEATHER>


}


// xml 파일 형식을 data class로 구현
data class WEATHER (val response : RESPONSE)
data class RESPONSE(val header : HEADER, val body : BODY)
data class HEADER(val resultCode : Int, val resultMsg : String)
data class BODY(val dataType : String, val items : ITEMS, val totalCount : Int)
data class ITEMS(val item : List<ITEM>)
// category : 자료 구분 코드, fcstDate : 예측 날짜, fcstTime : 예측 시간, fcstValue : 예보 값
data class ITEM(val category : String, val fcstDate : String, val fcstTime : String, val fcstValue : String)
// retrofit을 사용하기 위한 빌더 생성

val gson : Gson = GsonBuilder()
    .setLenient()
    .create()
private val retrofit = Retrofit.Builder()
    .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/")
    .addConverterFactory(GsonConverterFactory.create(gson))
    .build()

object ApiObject {
    val retrofitService: WeatherInterface by lazy {
        retrofit.create(WeatherInterface::class.java)
    }
}
