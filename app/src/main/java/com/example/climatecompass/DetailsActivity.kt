package com.example.climatecompass

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.climatecompass.databinding.ActivityDetailsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DetailsActivity : AppCompatActivity() {
    private val binding: ActivityDetailsBinding by lazy {
        ActivityDetailsBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.cityName.text = intent.getStringExtra("name")
        binding.weather.text =intent.getStringExtra("condition")
        binding.humidity.text = intent.getIntExtra("humudity",0).toString()+ " %"
        binding.feelLike.text="%.2f °C".format((intent.getDoubleExtra("feelsLike",0.0)?.minus(273))!!.toFloat())
        binding.tempera.text = "%.2f °C".format((intent.getDoubleExtra("temperature",0.0)?.minus(273))!!.toFloat())
        binding.pressure.text=intent.getIntExtra("pressure",0).toString() + " hPa"
        binding.sunRise.text= "${timeS(intent.getLongExtra("sunRise",0))}"
        binding.sunSet.text="${timeS(intent.getLongExtra("sun_set",0))}"
        binding.windspeed.text =intent.getDoubleExtra("wind_Speed",0.0).toString()+" m/s"
        binding.tmpMax.text="%.2f °C".format((intent.getDoubleExtra("max_temperature",0.0)?.minus(273))!!.toFloat())
        binding.tmpMin.text ="%.2f °C".format((intent.getDoubleExtra("min_temperature",0.0)?.minus(273))!!.toFloat())
        binding.gndLevel.text= intent.getIntExtra("gndLevel",0).toString()+ " m"
        binding.seaLevel.text = intent.getIntExtra("seaLevel",0).toString()+ " m"

        binding.button.setOnClickListener {
            finish()
        }

    }
    fun dayName(timestamp: Long):String{
        val simpleDateFormat= SimpleDateFormat("EEEE", Locale.getDefault())
        return simpleDateFormat.format((Date()))
    }

    fun timeS(timestamp: Long):String{
        val simpleDateFormat= SimpleDateFormat("HH:mm", Locale.getDefault())
        return simpleDateFormat.format((Date(timestamp*1000)))
    }

    fun dateName():String{
        val simpleDateFormat= SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return simpleDateFormat.format((Date()))
    }
}