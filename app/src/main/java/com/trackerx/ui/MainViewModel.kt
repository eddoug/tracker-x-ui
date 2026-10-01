package com.trackerx.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trackerx.ui.launcher.AppEntry
import com.trackerx.ui.settings.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Ponte entre os módulos de dados e a interface (MVVM). */
class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val c = (app as TrackerApp).container

    val settings = c.settings.state
    val gps = c.location.state
    val tempC = c.weather.tempC
    val bluetooth = c.bluetooth.state
    val media = c.media.state
    val vehicle = c.vehicle.snapshot

    private val _apps = MutableStateFlow<List<AppEntry>>(emptyList())
    val apps: StateFlow<List<AppEntry>> = _apps

    fun updateSettings(transform: (AppSettings) -> AppSettings) = c.settings.update(transform)

    fun playPause() = c.media.playPause()
    fun next() = c.media.next()
    fun previous() = c.media.previous()
    fun volumeUp() = c.media.volumeUp()
    fun volumeDown() = c.media.volumeDown()

    fun refreshSystemState() {
        c.media.refresh()
        c.bluetooth.start()
        c.location.start()
    }

    fun loadApps() {
        viewModelScope.launch { _apps.value = c.apps.load() }
    }
}
