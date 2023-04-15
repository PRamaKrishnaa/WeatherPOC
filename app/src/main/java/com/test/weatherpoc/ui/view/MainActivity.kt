package com.test.weatherpoc.ui.view

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.test.weatherpoc.R
import com.test.weatherpoc.data.model.WeatherModel
import com.test.weatherpoc.data.repository.WeatherRepository
import com.test.weatherpoc.databinding.ActivityMainBinding
import com.test.weatherpoc.ui.model.WeatherViewState
import com.test.weatherpoc.ui.viewmodel.MainViewModel
import com.test.weatherpoc.ui.viewmodel.MainViewModelProviderFactory
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    // region Variable Declaration
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = WeatherRepository()
        val provider = MainViewModelProviderFactory(repository)
        viewModel = ViewModelProvider(this, provider).get(MainViewModel::class.java)

        sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        initializeViews()

    }
    // endregion

    //region initizaltion
    private fun initializeViews() {
        with(binding) {
            val prevCityName = sharedPref.getString("City", null)
            if (prevCityName != null) {
                searchEdt.setText(prevCityName)
                setUpViewModelBindings(searchEdt.text.toString())
            }

            searchBtn.setOnClickListener {
                val cityName = searchEdt.text.toString()
                if (cityName.trim().isNotEmpty()) {
                    setUpViewModelBindings(cityName)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Please enter city name/state code/country code",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
        }

    }
    //end region

    //region viewmodel data
    private fun setUpViewModelBindings(cityName: String) {
        if (isNetworkConnected()) {
            viewModel.getLatLonData(cityName)
            observeViewModel(viewModel)
        } else {
            renderErrorState("No Internet!!")
        }

    }


    private fun observeViewModel(viewModel: MainViewModel) {
        with(viewModel) {
            viewState.observe(this@MainActivity) { renderViewState(it) }
        }
    }

    private fun renderViewState(viewState: WeatherViewState?) {

        when (viewState) {
            is WeatherViewState.Loading -> {
                showProgressBar()
            }
            is WeatherViewState.Data -> {
                renderDataState(viewState.data)
            }
            is WeatherViewState.Error -> {
                renderErrorState(viewState.errorMsg)
            }
            else -> {}
        }
    }

    private fun renderDataState(weatherDataValues: WeatherModel) {
        with(binding) {
            hideProgressBar()
            //saving the last searched city
            val edit = sharedPref.edit()
            edit.putString("City", weatherDataValues.name.toString())
            edit.apply()


            cityText.text = getString(R.string.city_name, weatherDataValues.name.toString())

            weatherState.text =
                getString(R.string.weather_state, weatherDataValues.weather.get(0).main.toString())

            description.text = getString(
                R.string.description,
                weatherDataValues.weather.get(0).description.toString()
            )

            temp.text = getString(
                R.string.temp, ((weatherDataValues.main?.temp.toString()).toDouble() - 273.15)
                    .roundToInt().toString() + " C"
            )

            feelsLike.text =
                getString(
                    R.string.feels_like,
                    ((weatherDataValues.main?.feelsLike.toString()).toDouble() - 273.15)
                        .roundToInt().toString() + " C"
                )

            tempMin.text =
                getString(
                    R.string.min_temp,
                    ((weatherDataValues.main?.tempMin.toString()).toDouble() - 273.15)
                        .roundToInt().toString() + " C"
                )

            tempMax.text =
                getString(
                    R.string.max_temp,
                    ((weatherDataValues.main?.tempMax.toString()).toDouble() - 273.15)
                        .roundToInt().toString() + " C"
                )

            val icon = weatherDataValues.weather.get(0).icon.toString()
            Glide.with(applicationContext)
                .load("https://openweathermap.org/img/wn/" + icon + ".png").into(iconImage)
        }
    }

    private fun renderErrorState(errorMsg: String) {
        hideProgressBar()
        Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
    }
    //end region

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }
}