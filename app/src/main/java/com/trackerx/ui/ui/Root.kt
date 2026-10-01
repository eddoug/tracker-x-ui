package com.trackerx.ui.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VerticalSplit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackerx.ui.MainViewModel
import com.trackerx.ui.navigation.Screen
import com.trackerx.ui.settings.OrientationMode
import com.trackerx.ui.settings.resolveDark
import com.trackerx.ui.voice.KeywordVoiceInterpreter
import com.trackerx.ui.voice.voiceRecognitionIntent

fun screenIcon(screen: Screen): ImageVector = when (screen) {
    Screen.HOME -> Icons.Rounded.Home
    Screen.NAV -> Icons.Rounded.Navigation
    Screen.MEDIA -> Icons.Rounded.MusicNote
    Screen.CAR -> Icons.Rounded.DirectionsCar
    Screen.SPLIT -> Icons.Rounded.VerticalSplit
    Screen.APPS -> Icons.Rounded.Apps
    Screen.BLUETOOTH -> Icons.Rounded.Bluetooth
    Screen.SETTINGS -> Icons.Rounded.Settings
}

@Composable
fun TrackerRoot(vm: MainViewModel, requestPermissions: () -> Unit) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val dark = resolveDark(settings.themeMode, isSystemInDarkTheme())
    var screenName by rememberSaveable { mutableStateOf(Screen.HOME.name) }
    val screen = Screen.valueOf(screenName)
    val go: (Screen) -> Unit = { screenName = it.name }

    LaunchedEffect(settings.orientation) {
        (context as? Activity)?.requestedOrientation = when (settings.orientation) {
            OrientationMode.AUTO -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            OrientationMode.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            OrientationMode.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val heard = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (heard != null) {
            val target = KeywordVoiceInterpreter.interpret(heard)
            if (target != null) go(target)
            else Toast.makeText(context, "Não entendi: \"$heard\"", Toast.LENGTH_SHORT).show()
        }
    }
    val startVoice: () -> Unit = {
        runCatching { voiceLauncher.launch(voiceRecognitionIntent()) }.onFailure {
            Toast.makeText(context, "Reconhecimento de voz indisponível neste tablet", Toast.LENGTH_SHORT).show()
        }
    }

    // Como launcher, "voltar" leva ao Início em vez de fechar o app.
    BackHandler(enabled = screen != Screen.HOME) { go(Screen.HOME) }

    TrackerTheme(UiPrefs(dark, settings.cardAlpha, settings.cardScale, settings.animations)) {
        val bg = MaterialTheme.colorScheme.background
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(bg, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f), bg)
                    )
                )
                .systemBarsPadding()
        ) {
            val landscape = maxWidth > maxHeight
            val content: @Composable (Modifier) -> Unit = { modifier ->
                Box(modifier) {
                    if (settings.animations) {
                        Crossfade(targetState = screen, label = "screen") { s ->
                            ScreenContent(s, vm, landscape, go, startVoice, requestPermissions)
                        }
                    } else {
                        ScreenContent(screen, vm, landscape, go, startVoice, requestPermissions)
                    }
                }
            }
            if (landscape) {
                Row(Modifier.fillMaxSize()) {
                    Dock(screen, vertical = true, onSelect = go, onVoice = startVoice)
                    content(Modifier.weight(1f).fillMaxHeight())
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    content(Modifier.weight(1f).fillMaxWidth())
                    Dock(screen, vertical = false, onSelect = go, onVoice = startVoice)
                }
            }
        }
    }
}

@Composable
private fun ScreenContent(
    screen: Screen,
    vm: MainViewModel,
    landscape: Boolean,
    go: (Screen) -> Unit,
    startVoice: () -> Unit,
    requestPermissions: () -> Unit
) {
    Box(Modifier.fillMaxSize().padding(16.dp)) {
        when (screen) {
            Screen.HOME -> HomeScreen(vm, landscape, go, startVoice)
            Screen.NAV -> NavScreen(vm)
            Screen.MEDIA -> MediaScreen(vm, landscape)
            Screen.CAR -> CarScreen(vm, landscape)
            Screen.SPLIT -> SplitScreen(vm, landscape)
            Screen.APPS -> AppsScreen(vm)
            Screen.BLUETOOTH -> BluetoothScreen(vm, requestPermissions)
            Screen.SETTINGS -> SettingsScreen(vm)
        }
    }
}

@Composable
private fun Dock(current: Screen, vertical: Boolean, onSelect: (Screen) -> Unit, onVoice: () -> Unit) {
    val items = Screen.values().toList()
    val item: @Composable (Screen) -> Unit = { s ->
        DockItem(screenIcon(s), s.label, selected = s == current) { onSelect(s) }
    }
    val bg = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    if (vertical) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(92.dp)
                .background(bg)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items.forEach { item(it) }
            DockItem(Icons.Rounded.Mic, "Voz", selected = false, onClick = onVoice)
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().background(bg).padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item(it) }
            DockItem(Icons.Rounded.Mic, "Voz", selected = false, onClick = onVoice)
        }
    }
}

@Composable
private fun DockItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val fg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = fg, modifier = Modifier.size(28.dp))
        Text(label, color = fg, fontSize = 11.sp, maxLines = 1)
    }
}
