package com.example.test2.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiInterface {
    @GET("search/{type}")
    fun getSearchResult(
        @Header("X-Naver-Client-Id") id: String?,
        @Header("X-Naver-Client-Secret") pw: String?,
        @Path("type") type: String?,
        @Query("query") query: String?
    ): Call<String?>?
}



object ApiClient {

    private const val BASE_URL = "https://openapi.naver.com/v1/"
    private var retrofit: Retrofit? = null
    val instance: Retrofit?
        get() {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            }
            return retrofit
        }

}




