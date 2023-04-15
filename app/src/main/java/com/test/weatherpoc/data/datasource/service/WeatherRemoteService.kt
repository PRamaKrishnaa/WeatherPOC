package com.test.weatherpoc.data.datasource.service

import com.sukrithi.weatherpoc.LatLonModel
import com.test.weatherpoc.data.model.WeatherModel
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherRemoteService {
    @GET("/data/2.5/weather")
    suspend fun getWeather(
        @Query("lat") lat: String?,
        @Query("lon") lon: String?,
        @Query("appid") appid: String?
    ): Response<WeatherModel>

    @GET("/geo/1.0/direct")
    suspend fun getLatLon(
        @Query("q") q: String?,
        @Query("limit") limit: String?,
        @Query("appid") appid: String?
    ): Response<ArrayList<LatLonModel>>
}