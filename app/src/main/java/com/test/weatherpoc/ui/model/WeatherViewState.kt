package com.test.weatherpoc.ui.model

import com.test.weatherpoc.data.model.WeatherModel

sealed class WeatherViewState {
    object Loading : WeatherViewState()

    data class Data(val data: WeatherModel) : WeatherViewState()

    data class Error(val errorMsg: String) : WeatherViewState()
}
