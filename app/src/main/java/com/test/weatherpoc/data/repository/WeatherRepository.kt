package com.test.weatherpoc.data.repository

import com.test.weatherpoc.data.datasource.service.RetrofitInstance


class WeatherRepository {
    suspend fun getLatLon(
        city: String,
        limit: String,
        appId: String
    ) = RetrofitInstance.api.getLatLon(city, limit, appId)

    suspend fun getWeatherDetails(
        latitude: String,
        longitude: String,
        appId: String
    ) = RetrofitInstance.api.getWeather(latitude, longitude, appId)
}