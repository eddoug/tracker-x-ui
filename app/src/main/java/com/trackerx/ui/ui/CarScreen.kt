package com.trackerx.ui.ui

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackerx.ui.MainViewModel
import com.trackerx.ui.camera.CameraPosition
import com.trackerx.ui.camera.CameraRegistry
import com.trackerx.ui.scene3d.CarViewer
import com.trackerx.ui.scene3d.ViewPreset
import com.trackerx.ui.settings.SpeedUnit
import com.trackerx.ui.settings.TempUnit
import com.trackerx.ui.settings.celsiusToUnit
import com.trackerx.ui.settings.kmhToUnit
import com.trackerx.ui.vehicle.Reading
import com.trackerx.ui.vehicle.VehicleSnapshot

@Composable
fun CarScreen(vm: MainViewModel, landscape: Boolean) {
    if (landscape) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Car3DPane(vm, Modifier.weight(1.2f).fillMaxHeight())
            CarDataPane(vm, Modifier.weight(1f).fillMaxHeight())
        }
    } else {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Car3DPane(vm, Modifier.fillMaxWidth().weight(1f))
            CarDataPane(vm, Modifier.fillMaxWidth().weight(1.2f))
        }
    }
}

@Composable
fun Car3DPane(vm: MainViewModel, modifier: Modifier = Modifier) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val vehicle by vm.vehicle.collectAsStateWithLifecycle()
    val prefs = LocalUiPrefs.current
    var presetName by rememberSaveable { mutableStateOf(ViewPreset.THREE_QUARTER.name) }
    var spin by rememberSaveable { mutableStateOf(true) }
    val preset = ViewPreset.valueOf(presetName)
    // Parado (ou sem dado de velocidade): gira devagar em modo de exibição.
    val moving = (vehicle.speedKmh.value ?: 0.0) > 3.0

    GlassCard(modifier, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    settings.carModel, color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 1
                )
                Label("Modelo 3D ilustrativo · SUV genérico, não oficial")
            }
            Chip(
                text = when (vehicle.engineRunning) {
                    true -> "Motor ligado"
                    false -> "Motor desligado"
                    null -> "Motor: indisponível"
                },
                selected = vehicle.engineRunning == true
            )
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(20.dp))) {
            CarViewer(
                modifier = Modifier.fillMaxSize(),
                preset = preset,
                autoRotate = spin && prefs.animations && !moving,
                dark = prefs.dark
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ViewPreset.values().forEach { p ->
                Chip(p.label, selected = p == preset && !spin, onClick = { presetName = p.name; spin = false })
            }
            Chip("Girar", selected = spin, onClick = { spin = !spin })
        }
    }
}

@Composable
fun CarDataPane(vm: MainViewModel, modifier: Modifier = Modifier) {
    val v by vm.vehicle.collectAsStateWithLifecycle()
    val gps by vm.gps.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()

    fun speed(r: Reading) = r.copy(value = r.value?.let { kmhToUnit(it, settings.speedUnit) })
    fun temp(r: Reading) = r.copy(value = r.value?.let { celsiusToUnit(it, settings.tempUnit) })
    val speedUnit = if (settings.speedUnit == SpeedUnit.KMH) "km/h" else "mph"
    val tempUnit = if (settings.tempUnit == TempUnit.CELSIUS) "°C" else "°F"

    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Chip("Fonte: " + v.providerName.ifEmpty { "—" }, selected = v.connected)
            Text(v.status, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
        DataGrid(
            listOf<@Composable (Modifier) -> Unit>(
                { DataCard("Velocidade", speed(v.speedKmh), speedUnit, modifier = it) },
                { DataCard("RPM", v.rpm, "rpm", modifier = it) },
                { DataCard("Temperatura", temp(v.coolantC), tempUnit, modifier = it) },
                { DataCard("Bateria", v.batteryV, "V", 1, modifier = it) },
                { DataCard("Combustível", v.fuelPct, "%", modifier = it) },
                { DataCard("Consumo", v.consumptionKmL, "km/L", 1, modifier = it) },
                { DataCard("Carga do motor", v.engineLoadPct, "%", modifier = it) },
                { DataCard("Acelerador", v.throttlePct, "%", modifier = it) },
                { DataCard("Temp. admissão", temp(v.intakeC), tempUnit, modifier = it) },
                { DataCard("Pressão coletor", v.pressureKpa, "kPa", modifier = it) }
            )
        )
        GlassCard(Modifier.fillMaxWidth(), padding = 16.dp) {
            Label("GPS")
            Text(
                when {
                    !gps.permissionGranted -> "Sem permissão de localização"
                    !gps.hasFix -> "Indisponível (sem sinal)"
                    else -> "%.5f, %.5f".format(gps.latitude, gps.longitude)
                },
                color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp
            )
        }
        GlassCard(Modifier.fillMaxWidth(), padding = 16.dp) {
            Label("Códigos de falha (OBD)")
            Text(dtcText(v), color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
        }
        GlassCard(Modifier.fillMaxWidth(), padding = 16.dp) {
            Label("Câmeras")
            Spacer(Modifier.height(8.dp))
            val configured = CameraRegistry.configured()
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CameraPosition.values().forEach { p ->
                    Chip(p.label + if (p in configured) "" else " · N/D", selected = p in configured)
                }
            }
            Text(
                "Nenhuma fonte de vídeo configurada. A estrutura está pronta para receber câmeras no futuro.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun dtcText(v: VehicleSnapshot): String {
    val list = v.dtcs
    return when {
        list == null -> "Indisponível"
        list.isEmpty() -> "Nenhuma falha registrada" + if (v.speedKmh.source == com.trackerx.ui.vehicle.DataSource.SIMULATED) " (simulado)" else ""
        else -> list.joinToString(", ")
    }
}

/** Grade simples de 2 colunas (evita LazyGrid dentro de coluna rolável). */
@Composable
private fun DataGrid(cells: List<@Composable (Modifier) -> Unit>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        cells.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { cell -> cell(Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
