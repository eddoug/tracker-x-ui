package com.trackerx.ui.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.GpsOff
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackerx.ui.MainViewModel
import com.trackerx.ui.bluetooth.bluetoothStatusLabel
import com.trackerx.ui.media.MediaState
import com.trackerx.ui.media.mediaStatusLabel
import com.trackerx.ui.navigation.Screen
import com.trackerx.ui.settings.SpeedUnit
import com.trackerx.ui.settings.TempUnit
import com.trackerx.ui.settings.celsiusToUnit
import com.trackerx.ui.settings.kmhToUnit
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun rememberNow(): Date {
    val now by produceState(initialValue = Date()) {
        while (true) {
            value = Date()
            delay(1000L - System.currentTimeMillis() % 1000L)
        }
    }
    return now
}

@Composable
fun HomeScreen(vm: MainViewModel, landscape: Boolean, go: (Screen) -> Unit, startVoice: () -> Unit) {
    if (landscape) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1.15f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ClockCard(Modifier.fillMaxWidth().weight(1f))
                StatusRow(vm, go)
                QuickRow(go, startVoice)
            }
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SpeedCard(vm, Modifier.fillMaxWidth().weight(1f))
                MediaCard(vm, Modifier.fillMaxWidth().weight(1f), onOpen = { go(Screen.MEDIA) })
            }
        }
    } else {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ClockCard(Modifier.fillMaxWidth().height(230.dp))
            StatusRow(vm, go)
            SpeedCard(vm, Modifier.fillMaxWidth().height(230.dp))
            MediaCard(vm, Modifier.fillMaxWidth().height(260.dp), onOpen = { go(Screen.MEDIA) })
            QuickRow(go, startVoice)
        }
    }
}

@Composable
private fun ClockCard(modifier: Modifier) {
    val now = rememberNow()
    val scale = LocalUiPrefs.current.scale
    val locale = Locale("pt", "BR")
    GlassCard(modifier) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(
                SimpleDateFormat("HH:mm", locale).format(now),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = (104 * scale).sp,
                fontWeight = FontWeight.ExtraLight,
                letterSpacing = (-2).sp,
                maxLines = 1
            )
            Text(
                SimpleDateFormat("EEEE, d 'de' MMMM", locale).format(now)
                    .replaceFirstChar { it.titlecase(locale) },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = (22 * scale).sp
            )
        }
    }
}

@Composable
private fun StatusRow(vm: MainViewModel, go: (Screen) -> Unit) {
    val gps by vm.gps.collectAsStateWithLifecycle()
    val bt by vm.bluetooth.collectAsStateWithLifecycle()
    val vehicle by vm.vehicle.collectAsStateWithLifecycle()
    val temp by vm.tempC.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val t = temp
        Chip(
            text = if (t == null) "Temp. externa N/D"
            else "%.0f°%s".format(celsiusToUnit(t, settings.tempUnit), if (settings.tempUnit == TempUnit.CELSIUS) "C" else "F"),
            icon = Icons.Rounded.Thermostat
        )
        Chip(
            text = when {
                !gps.permissionGranted -> "GPS sem permissão"
                !gps.providerEnabled -> "GPS desligado"
                gps.hasFix -> "GPS ativo"
                else -> "GPS buscando sinal"
            },
            icon = if (gps.hasFix) Icons.Rounded.GpsFixed else Icons.Rounded.GpsOff,
            selected = gps.hasFix
        )
        Chip(
            text = bluetoothStatusLabel(bt),
            icon = Icons.Rounded.Bluetooth,
            selected = bt.audioOverBluetooth,
            onClick = { go(Screen.BLUETOOTH) }
        )
        Chip(
            text = "Carro: " + vehicle.status.ifEmpty { "Indisponível" },
            icon = Icons.Rounded.DirectionsCar,
            selected = vehicle.connected,
            onClick = { go(Screen.CAR) }
        )
    }
}

@Composable
private fun SpeedCard(vm: MainViewModel, modifier: Modifier) {
    val gps by vm.gps.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val prefs = LocalUiPrefs.current
    val speed = gps.speedKmh?.let { kmhToUnit(it, settings.speedUnit) }
    val animated by animateFloatAsState(targetValue = (speed ?: 0.0).toFloat(), label = "speed")
    GlassCard(modifier) {
        Label("Velocidade · GPS")
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (speed == null) "N/D" else "%.0f".format(if (prefs.animations) animated else speed.toFloat()),
                color = if (speed == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                fontSize = (96 * prefs.scale).sp,
                fontWeight = FontWeight.Light,
                maxLines = 1
            )
            Text(
                if (settings.speedUnit == SpeedUnit.KMH) "km/h" else "mph",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AlbumArt(state: MediaState, size: Dp) {
    val art = state.now?.art
    val shape = RoundedCornerShape(22.dp)
    if (art != null) {
        Image(
            bitmap = art.asImageBitmap(),
            contentDescription = "Capa",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size).clip(shape)
        )
    } else {
        Box(
            Modifier.size(size).clip(shape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.MusicNote, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(size * 0.4f)
            )
        }
    }
}

@Composable
fun TransportControls(vm: MainViewModel, playing: Boolean, big: Boolean = false) {
    val side = if (big) 72.dp else 56.dp
    val center = if (big) 92.dp else 68.dp
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        RoundIconButton(Icons.Rounded.SkipPrevious, "Anterior", side) { vm.previous() }
        RoundIconButton(
            if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            if (playing) "Pausar" else "Tocar", center, filled = true
        ) { vm.playPause() }
        RoundIconButton(Icons.Rounded.SkipNext, "Próxima", side) { vm.next() }
    }
}

@Composable
private fun MediaCard(vm: MainViewModel, modifier: Modifier, onOpen: () -> Unit) {
    val media by vm.media.collectAsStateWithLifecycle()
    val now = media.now
    GlassCard(modifier, onClick = onOpen) {
        Label("Mídia · " + mediaStatusLabel(media.accessGranted, now != null, now?.playing == true))
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
            AlbumArt(media, 96.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    now?.title ?: if (media.accessGranted) "Nada tocando" else "Toque para liberar o controle de mídia",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Text(
                    now?.artist ?: "",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TransportControls(vm, playing = now?.playing == true)
        }
    }
}

@Composable
private fun QuickRow(go: (Screen) -> Unit, startVoice: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(Screen.NAV, Screen.MEDIA, Screen.CAR, Screen.APPS, Screen.SETTINGS).forEach { s ->
            Chip(text = s.label, icon = screenIcon(s), onClick = { go(s) })
        }
        Chip(text = "Voz", icon = Icons.Rounded.Mic, onClick = startVoice)
    }
}
