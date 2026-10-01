package com.trackerx.ui.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ThemeMode { AUTO, DARK, LIGHT }
enum class SpeedUnit { KMH, MPH }
enum class TempUnit { CELSIUS, FAHRENHEIT }
enum class OrientationMode { AUTO, LANDSCAPE, PORTRAIT }
enum class NavApp(val label: String, val pkg: String) {
    GOOGLE_MAPS("Google Maps", "com.google.android.apps.maps"),
    WAZE("Waze", "com.waze")
}
enum class MediaApp(val label: String, val pkg: String) {
    SPOTIFY("Spotify", "com.spotify.music"),
    YT_MUSIC("YouTube Music", "com.google.android.apps.youtube.music")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val speedUnit: SpeedUnit = SpeedUnit.KMH,
    val tempUnit: TempUnit = TempUnit.CELSIUS,
    val orientation: OrientationMode = OrientationMode.AUTO,
    val navApp: NavApp = NavApp.GOOGLE_MAPS,
    val mediaApp: MediaApp = MediaApp.SPOTIFY,
    val autoStart: Boolean = true,
    val animations: Boolean = true,
    val cardScale: Float = 1f,
    val cardAlpha: Float = 0.72f,
    val useSimulatedData: Boolean = true,
    val obdAddress: String? = null,
    val obdName: String? = null,
    val carModel: String = "Chevrolet Tracker Premier 2023"
)

/** Regra pura (testável): o tema efetivo dado o modo e o tema do sistema. */
fun resolveDark(mode: ThemeMode, systemDark: Boolean): Boolean = when (mode) {
    ThemeMode.DARK -> true
    ThemeMode.LIGHT -> false
    ThemeMode.AUTO -> systemDark
}

fun kmhToUnit(kmh: Double, unit: SpeedUnit): Double =
    if (unit == SpeedUnit.MPH) kmh * 0.621371 else kmh

fun celsiusToUnit(c: Double, unit: TempUnit): Double =
    if (unit == TempUnit.FAHRENHEIT) c * 9.0 / 5.0 + 32.0 else c

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("trackerx_settings", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(load())
    val state: StateFlow<AppSettings> = _state

    fun update(transform: (AppSettings) -> AppSettings) {
        val next = transform(_state.value)
        _state.value = next
        save(next)
    }

    private inline fun <reified E : Enum<E>> enumPref(key: String, default: E): E =
        runCatching { enumValueOf<E>(prefs.getString(key, default.name) ?: default.name) }
            .getOrDefault(default)

    private fun load(): AppSettings {
        val d = AppSettings()
        return AppSettings(
            themeMode = enumPref("themeMode", d.themeMode),
            speedUnit = enumPref("speedUnit", d.speedUnit),
            tempUnit = enumPref("tempUnit", d.tempUnit),
            orientation = enumPref("orientation", d.orientation),
            navApp = enumPref("navApp", d.navApp),
            mediaApp = enumPref("mediaApp", d.mediaApp),
            autoStart = prefs.getBoolean("autoStart", d.autoStart),
            animations = prefs.getBoolean("animations", d.animations),
            cardScale = prefs.getFloat("cardScale", d.cardScale),
            cardAlpha = prefs.getFloat("cardAlpha", d.cardAlpha),
            useSimulatedData = prefs.getBoolean("useSimulatedData", d.useSimulatedData),
            obdAddress = prefs.getString("obdAddress", null),
            obdName = prefs.getString("obdName", null),
            carModel = prefs.getString("carModel", d.carModel) ?: d.carModel
        )
    }

    private fun save(s: AppSettings) {
        prefs.edit()
            .putString("themeMode", s.themeMode.name)
            .putString("speedUnit", s.speedUnit.name)
            .putString("tempUnit", s.tempUnit.name)
            .putString("orientation", s.orientation.name)
            .putString("navApp", s.navApp.name)
            .putString("mediaApp", s.mediaApp.name)
            .putBoolean("autoStart", s.autoStart)
            .putBoolean("animations", s.animations)
            .putFloat("cardScale", s.cardScale)
            .putFloat("cardAlpha", s.cardAlpha)
            .putBoolean("useSimulatedData", s.useSimulatedData)
            .putString("obdAddress", s.obdAddress)
            .putString("obdName", s.obdName)
            .putString("carModel", s.carModel)
            .apply()
    }
}
