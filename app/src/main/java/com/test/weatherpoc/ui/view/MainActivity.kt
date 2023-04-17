package com.test.weatherpoc.ui.view

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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


class MainActivity : AppCompatActivity(), LocationListener {

    // region Variable Declaration
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var sharedPref: SharedPreferences
    private lateinit var locationManager: LocationManager
    private var latitude: Double? = null
    private var longitude: Double? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = WeatherRepository()
        val provider = MainViewModelProviderFactory(repository)
        viewModel = ViewModelProvider(this, provider).get(MainViewModel::class.java)

        sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        //To get current location
        checkLocationPermission()

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        initializeViews()

    }

    // To fetch latitude and longitude from current location
    override fun onLocationChanged(location: Location) {
        latitude = location.latitude
        longitude = location.longitude

        if (isNetworkConnected()) {
            Toast.makeText(applicationContext, "Showing current location data", Toast.LENGTH_LONG)
                .show()
            viewModel.getWeatherData(latitude.toString(), longitude.toString())
            observeViewModel(viewModel)
            binding.searchEdt.setText("")
        } else {
            renderErrorState("No Internet!!")
        }

    }
    //end region
    // endregion

    //region initialization
    private fun initializeViews() {
        with(binding) {
            val prevCityName = sharedPref.getString("City", null)
            if (prevCityName != null) {
                searchEdt.setText(prevCityName)
                setUpViewModelBindings(searchEdt.text.toString().trim())
            }
            searchBtn.setOnClickListener {
                val cityName = searchEdt.text.toString().trim()
                if (cityName.isNotEmpty()) {
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

    //region Location permission request
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), 1
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), 1
                )
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkGpsPermissionAccess()
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                // Toast.makeText(this, "Please enable permission in settings", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //to check whether GPS is enabled or not after location permission access granted
    private fun checkGpsPermissionAccess() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                100,
                50f,
                this
            )
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps()
            }
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton(
                "Yes"
            ) { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton(
                "No"
            ) { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }
    // end region

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                100,
                50f,
                this
            )
        }
    }

    override fun onStart() {
        super.onStart()

        checkGpsPermissionAccess()

    }

    override fun onPause() {
        super.onPause()
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            locationManager.removeUpdates(this)
        }
    }

    //Checking active internet connection
    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }
}
