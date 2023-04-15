package com.test.weatherpoc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.test.weatherpoc.data.repository.WeatherRepository

@Suppress("UNCHECKED_CAST")
class MainViewModelProviderFactory(val weatherRepository: WeatherRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(weatherRepository) as T
    }
}