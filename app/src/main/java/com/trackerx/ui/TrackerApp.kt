package com.trackerx.ui

import android.app.Application
import android.content.Context
import com.trackerx.ui.bluetooth.BluetoothMonitor
import com.trackerx.ui.launcher.AppsRepository
import com.trackerx.ui.location.LocationRepository
import com.trackerx.ui.media.MediaRepository
import com.trackerx.ui.settings.SettingsRepository
import com.trackerx.ui.vehicle.VehicleRepository
import com.trackerx.ui.weather.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class TrackerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Injeção de dependências manual: um objeto por módulo, criado uma vez. */
class AppContainer(context: Context) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val settings = SettingsRepository(context)
    val location = LocationRepository(context)
    val weather = WeatherRepository(scope, location)
    val bluetooth = BluetoothMonitor(context)
    val media = MediaRepository(context)
    val vehicle = VehicleRepository(context, scope, settings)
    val apps = AppsRepository(context)
}
