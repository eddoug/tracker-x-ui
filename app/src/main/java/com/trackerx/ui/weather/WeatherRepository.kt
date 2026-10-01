package com.trackerx.ui.weather

import com.trackerx.ui.location.LocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Temperatura externa pelo serviço público Open-Meteo, na posição do GPS.
 * É previsão/observação meteorológica, não o sensor do carro. Sem internet ou GPS: null (N/D).
 */
class WeatherRepository(scope: CoroutineScope, private val location: LocationRepository) {
    private val _tempC = MutableStateFlow<Double?>(null)
    val tempC: StateFlow<Double?> = _tempC

    init {
        scope.launch(Dispatchers.IO) {
            while (true) {
                val gps = location.state.value
                val lat = gps.latitude
                val lon = gps.longitude
                var ok = false
                if (lat != null && lon != null) {
                    val t = fetch(lat, lon)
                    if (t != null) {
                        _tempC.value = t
                        ok = true
                    }
                }
                delay(if (ok) 15 * 60_000L else 20_000L)
            }
        }
    }

    private fun fetch(lat: Double, lon: Double): Double? = runCatching {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m"
        )
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        try {
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            parseTemperature(body)
        } finally {
            conn.disconnect()
        }
    }.getOrNull()

    companion object {
        fun parseTemperature(json: String): Double? = runCatching {
            JSONObject(json).getJSONObject("current").getDouble("temperature_2m")
        }.getOrNull()
    }
}
