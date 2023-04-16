package com.test.weatherpoc.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukrithi.weatherpoc.LatLonModel
import com.test.weatherpoc.data.datasource.utils.Constants.Companion.APP_ID
import com.test.weatherpoc.data.model.WeatherModel
import com.test.weatherpoc.data.repository.WeatherRepository
import com.test.weatherpoc.ui.model.WeatherViewState
import kotlinx.coroutines.launch
import retrofit2.Response

class MainViewModel(private val weatherRepository: WeatherRepository) : ViewModel() {

    private val _viewState = MutableLiveData<WeatherViewState>()
    val viewState: LiveData<WeatherViewState> = _viewState

    //region get Latitude and Longitude of City/State/Country
    fun getLatLonData(city: String) {
        viewModelScope.launch {
            _viewState.value = WeatherViewState.Loading
            val latlonDataResult = weatherRepository.getLatLon(city, "1", APP_ID)
            if (latlonDataResult.isSuccessful) {
                if (latlonDataResult.body()?.isEmpty() == true) {
                    handleError("Please enter correct city name")
                } else {
                    handleResponse(latlonDataResult)
                }

            } else {
                handleError(latlonDataResult.message())

            }

        }
    }

    private fun handleResponse(latlonDataResult: Response<ArrayList<LatLonModel>>) {
        val latlonValues = latlonDataResult.body()?.get(0)
        val latitudeValue = latlonValues?.lat.toString()
        val longitudeValue = latlonValues?.lon.toString()

        getWeatherData(latitudeValue, longitudeValue)
    }
    //end region

    //region get Weather Data
    fun getWeatherData(latitudeValue: String, longitudeValue: String) {
        viewModelScope.launch {
            _viewState.value = WeatherViewState.Loading
            val getWeatherResponse =
                weatherRepository.getWeatherDetails(latitudeValue, longitudeValue, APP_ID)
            if (getWeatherResponse.isSuccessful) {
                getWeatherResponse.body()?.let { handleWeatherResponse(it) }
            } else {
                handleError(getWeatherResponse.message())
            }
        }
    }

    private fun handleWeatherResponse(weatherResponse: WeatherModel) {
        _viewState.value = WeatherViewState.Data(weatherResponse)
    }
    //end region

    private fun handleError(message: String) {
        _viewState.value = WeatherViewState.Error(message)
    }
}