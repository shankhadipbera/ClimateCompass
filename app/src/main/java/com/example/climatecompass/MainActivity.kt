package com.example.climatecompass

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.climatecompass.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
import android.Manifest
import android.content.Context
import android.database.Cursor
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
      ActivityMainBinding.inflate(layoutInflater)
    }

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var namesList: ArrayList<String>

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var defaultCity:String= "Kolkata"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        namesList = ArrayList()

        loadNames()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)

        getWeatherData(defaultCity)

        binding.addBtn.setOnClickListener {
            val name = binding.faviriteCity.text.toString().trim()
            if (name.isNotEmpty()) {
                if (db.addName(name)) {
                    Toast.makeText(this, "$name added to Favourite City list", Toast.LENGTH_SHORT).show()
                    loadNames()
                    binding.faviriteCity.text?.clear()
                } else {
                    Toast.makeText(this, "city already exists", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a city", Toast.LENGTH_SHORT).show()
            }
        }

        binding.searchCity.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (hasInternetPermission()) {
                    if (isInternetAvailable()) {
                        getWeatherData(query.toString())
                        hideKeyboard()
                        return true
                    } else {
                        Toast.makeText(this@MainActivity, "No internet connection", Toast.LENGTH_LONG).show()
                        return true
                    }
                } else {
                    requestInternetPermission()
                    return true
                }
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })

        binding.currentLocationBtn.setOnClickListener {

            if (hasInternetPermission()) {
                if (isInternetAvailable()) {
                    checkLocationPermission()
                } else {
                    Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
                }
            } else {
            }
        }

        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val name = namesList[position]
            getWeatherData(name)
            defaultCity=name
            //Toast.makeText(this, "Clicked: $name", Toast.LENGTH_SHORT).show()
        }

        binding.listView.setOnItemLongClickListener { _, _, position, _ ->
            val name = namesList[position]
            AlertDialog.Builder(this)
                .setTitle("Delete Name")
                .setMessage("Are you sure you want to delete $name?")
                .setPositiveButton("Yes") { _, _ ->
                    if (db.deleteName(name)) {
                        Toast.makeText(this, "$name deleted from favourite list", Toast.LENGTH_SHORT).show()
                        loadNames()
                    } else {
                        Toast.makeText(this, "Error in deleting city", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
            true
        }

    }

    private fun getWeatherData(city:String) {
        val retrofit =Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response=retrofit.getWeatherData(city,"place your api key here","metrics")
        response.enqueue(object : Callback<ClimateCompass>{
            override fun onResponse(p0: Call<ClimateCompass>, p1: Response<ClimateCompass>) {
                val responseBody = p1.body()
                if (p1.isSuccessful && responseBody != null) {
                    val temperature = responseBody?.main?.temp
                    val pressure = responseBody.main.pressure
                    val max_temperature = responseBody.main.temp_max
                    val min_temperature = responseBody.main.temp_min
                    val humudity = responseBody.main.humidity
                    val feelsLike = responseBody.main.feels_like
                    val sunRise = responseBody.sys.sunrise.toLong()
                    val sun_set = responseBody.sys.sunset.toLong()
                    val wind_Speed = responseBody.wind.speed
                    val condition = responseBody.weather.firstOrNull()?.main ?: "unknown"
                    val status = responseBody.weather.firstOrNull()?.description ?: "unknown"
                    val name = responseBody.name
                    val seaLevel = responseBody.main.sea_level
                    val gndLevel =responseBody.main.grnd_level



                    binding.location.text=name
                    binding.humudity.text="$humudity %"
                    binding.notes.text=status
                    binding.temperature.text="%.2f °C".format((temperature?.minus(273))!!.toFloat())
                    binding.windSpeed.text="$wind_Speed m/s"

                    if(binding.location.text.isNotEmpty()){
                        binding.weatherBtn.visibility=View.VISIBLE
                    }

                    if(binding.location.text.isNotEmpty()){
                        binding.weatherBtn.setOnClickListener {
                            val intent =Intent(this@MainActivity,DetailsActivity::class.java)
                            intent.putExtra("temperature",temperature)
                            intent.putExtra("pressure",pressure)
                            intent.putExtra("max_temperature",max_temperature)
                            intent.putExtra("min_temperature",min_temperature)
                            intent.putExtra("humudity",humudity)
                            intent.putExtra("feelsLike",feelsLike)
                            intent.putExtra("sunRise",sunRise)
                            intent.putExtra("sun_set",sun_set)
                            intent.putExtra("wind_Speed",wind_Speed)
                            intent.putExtra("condition",condition)
                            intent.putExtra("status",status)
                            intent.putExtra("name",name)
                            intent.putExtra("seaLevel",seaLevel)
                            intent.putExtra("gndLevel",gndLevel)
                            startActivity(intent)
                        }
                    }
                }
                else{
                    Toast.makeText(this@MainActivity, "API Error: ${p1.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(p0: Call<ClimateCompass>, p1: Throwable) {
                Toast.makeText(this@MainActivity, "Failed to fetch data: ${p1.message}", Toast.LENGTH_LONG).show()
            }

        })

    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = currentFocus
        currentFocusView?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
            it.clearFocus()

        }
    }
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // Permission already granted
            if (isLocationEnabled()) {
                getLastKnownLocation()
            } else {
                showGPSDisabledAlert()
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted, you can perform the location related task
                    if (isLocationEnabled()) {
                        getLastKnownLocation()
                    } else {
                        showGPSDisabledAlert()
                    }
                } else {
                    // Permission denied
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            INTERNET_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted, you can fetch data from API
                    if (isInternetAvailable()) {
                        getWeatherData("city_name")
                    } else {
                        Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Permission denied
                    Toast.makeText(this, "Internet permission denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                    if (location != null) {
                        val cityName = getCityName(location.latitude, location.longitude)
                        getWeatherData(cityName)
                        defaultCity=cityName
                      // Toast.makeText(this, "City: $cityName", Toast.LENGTH_LONG).show()
                    }
                }).addOnFailureListener(this, OnFailureListener {
                    Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show()
                })
        }
    }

    private fun getCityName(lat: Double, lon: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lon, 1)
        return if (addresses!!.isNotEmpty()) {
            addresses[0].locality ?: "Unknown City"
        } else {
            "Unknown City"
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showGPSDisabledAlert() {
        AlertDialog.Builder(this)
            .setTitle("Enable GPS")
            .setMessage("GPS is required for this app. Please enable GPS.")
            .setPositiveButton("OK") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun hasInternetPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestInternetPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), INTERNET_PERMISSION_REQUEST_CODE)
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    companion object {
        private const val INTERNET_PERMISSION_REQUEST_CODE = 2
    }
    

    private fun loadNames() {
        namesList.clear()
        val cursor: Cursor = db.getAllNames()
        if (cursor.moveToFirst()) {
            do {
                namesList.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            } while (cursor.moveToNext())
        }
        cursor.close()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, namesList)
        binding.listView.adapter = adapter
    }
}
