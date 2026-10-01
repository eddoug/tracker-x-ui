package com.trackerx.ui.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackerx.ui.BuildConfigInfo
import com.trackerx.ui.MainViewModel
import com.trackerx.ui.bluetooth.bluetoothStatusLabel
import com.trackerx.ui.settings.MediaApp
import com.trackerx.ui.settings.NavApp
import com.trackerx.ui.settings.OrientationMode
import com.trackerx.ui.settings.SpeedUnit
import com.trackerx.ui.settings.TempUnit
import com.trackerx.ui.settings.ThemeMode

@Composable
private fun <T> Choice(title: String, options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Column {
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (value, label) ->
                Chip(label, selected = value == selected, onClick = { onSelect(value) })
            }
        }
    }
}

@Composable
private fun Toggle(title: String, subtitle: String? = null, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            if (subtitle != null) Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun Group(title: String, content: @Composable () -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        Label(title)
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { content() }
    }
}

@Composable
fun SettingsScreen(vm: MainViewModel) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val bt by vm.bluetooth.collectAsStateWithLifecycle()
    val context = LocalContext.current
    fun open(action: String) {
        runCatching { context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle("Configurações")

        Group("Geral") {
            Choice(
                "Orientação da tela",
                listOf(OrientationMode.AUTO to "Automática", OrientationMode.LANDSCAPE to "Horizontal", OrientationMode.PORTRAIT to "Vertical"),
                s.orientation
            ) { v -> vm.updateSettings { it.copy(orientation = v) } }
            Text(
                "Em tablets com Android 16 o sistema pode ignorar a orientação fixa; nesse caso use a rotação do próprio Android.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp
            )
            Choice(
                "Tema",
                listOf(ThemeMode.AUTO to "Automático", ThemeMode.DARK to "Escuro", ThemeMode.LIGHT to "Claro"),
                s.themeMode
            ) { v -> vm.updateSettings { it.copy(themeMode = v) } }
            Choice(
                "Velocidade", listOf(SpeedUnit.KMH to "km/h", SpeedUnit.MPH to "mph"), s.speedUnit
            ) { v -> vm.updateSettings { it.copy(speedUnit = v) } }
            Choice(
                "Temperatura", listOf(TempUnit.CELSIUS to "°C", TempUnit.FAHRENHEIT to "°F"), s.tempUnit
            ) { v -> vm.updateSettings { it.copy(tempUnit = v) } }
            Toggle(
                "Iniciar automaticamente",
                "Ao ligar o tablet. O jeito garantido é definir o app como tela inicial.",
                s.autoStart
            ) { v -> vm.updateSettings { it.copy(autoStart = v) } }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryButton("Usar como tela inicial") { open(Settings.ACTION_HOME_SETTINGS) }
                PrimaryButton("Brilho e tela") { open(Settings.ACTION_DISPLAY_SETTINGS) }
                PrimaryButton("Idioma") { open(Settings.ACTION_LOCALE_SETTINGS) }
                PrimaryButton("Configurações do Android") { open(Settings.ACTION_SETTINGS) }
            }
        }

        Group("Bluetooth") {
            InfoLine("Status", bluetoothStatusLabel(bt))
            InfoLine("Dispositivo de áudio", bt.connectedDevices.joinToString().ifEmpty { "Nenhum" })
            PrimaryButton("Abrir Bluetooth do Android") { open(Settings.ACTION_BLUETOOTH_SETTINGS) }
        }

        Group("Carro") {
            InfoLine("Modelo", s.carModel)
            Toggle(
                "Usar dados simulados",
                "Demonstração do painel. Desligue para usar um adaptador OBD2.",
                s.useSimulatedData
            ) { v -> vm.updateSettings { it.copy(useSimulatedData = v) } }
            Text("Adaptador OBD2 (ELM327 Bluetooth, já pareado)", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            if (bt.paired.isEmpty()) {
                Text(
                    "Nenhum dispositivo pareado encontrado. Pareie o adaptador no Bluetooth do Android.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp
                )
            } else {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("Nenhum", selected = s.obdAddress == null, onClick = {
                        vm.updateSettings { it.copy(obdAddress = null, obdName = null) }
                    })
                    bt.paired.forEach { d ->
                        Chip(d.name, selected = d.address == s.obdAddress, onClick = {
                            vm.updateSettings { it.copy(obdAddress = d.address, obdName = d.name) }
                        })
                    }
                }
            }
        }

        Group("Interface") {
            Text("Tamanho dos textos dos cards", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            Slider(
                value = s.cardScale, valueRange = 0.8f..1.3f,
                onValueChange = { v -> vm.updateSettings { it.copy(cardScale = v) } }
            )
            Text("Transparência dos cards", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            Slider(
                value = s.cardAlpha, valueRange = 0.35f..1f,
                onValueChange = { v -> vm.updateSettings { it.copy(cardAlpha = v) } }
            )
            Toggle("Animações", checked = s.animations) { v -> vm.updateSettings { it.copy(animations = v) } }
            PrimaryButton("Papel de parede do Android") { open(Intent.ACTION_SET_WALLPAPER) }
        }

        Group("Mapa") {
            Choice(
                "Aplicativo padrão", NavApp.values().map { it to it.label }, s.navApp
            ) { v -> vm.updateSettings { it.copy(navApp = v) } }
        }

        Group("Mídia") {
            Choice(
                "Aplicativo padrão", MediaApp.values().map { it to it.label }, s.mediaApp
            ) { v -> vm.updateSettings { it.copy(mediaApp = v) } }
            PrimaryButton("Acesso ao controle de mídia") { open(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS) }
        }

        Text(
            "Tracker X UI ${BuildConfigInfo.VERSION} · projeto independente, sem vínculo com a Chevrolet/GM.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp
        )
    }
}
