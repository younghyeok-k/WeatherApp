package com.example.test2.network


import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// 결과 xml 파일에 접근해서 정보 가져오기
interface WeatherVilageFcstInterface {
    // getVilageFcst : 단기 예보 조회 + 인증키
    @GET("getVilageFcst?serviceKey=838R8j2OlUoP63LGXZlCROsfDobS%2FlgSz0LLyY4vl71MquB2n%2FXZZg9HtejYjInO6sGmfsILunQitjjdDMosOg%3D%3D")

    fun GetWeather(@Query("numOfRows") num_of_rows: Int,   // 한 페이지 경과 수
                   @Query("pageNo") page_no: Int,          // 페이지 번호
                   @Query("dataType") data_type: String,   // 응답 자료 형식
                   @Query("base_date") base_date: String,  // 발표 일자
                   @Query("base_time") base_time: String,  // 발표 시각
                   @Query("nx") nx: Int, // 예보지점 X 좌표
                   @Query("ny") ny: Int
    )                // 예보지점 Y 좌표
            : Call<WEATHER>
}


private val retrofit = Retrofit.Builder()
    .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/")
    .addConverterFactory(GsonConverterFactory.create(gson))
    .build()

object ApiObjects {
    val retrofitService: WeatherVilageFcstInterface by lazy {
        retrofit.create(WeatherVilageFcstInterface::class.java)
    }
}