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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackerx.ui.MainViewModel
import com.trackerx.ui.bluetooth.bluetoothStatusLabel
import com.trackerx.ui.media.mediaStatusLabel
import com.trackerx.ui.navigation.ExternalApps
import com.trackerx.ui.settings.MediaApp

@Composable
fun MediaScreen(vm: MainViewModel, landscape: Boolean, compact: Boolean = false) {
    val media by vm.media.collectAsStateWithLifecycle()
    val bt by vm.bluetooth.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val now = media.now

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!compact) SectionTitle("Mídia", Modifier.fillMaxWidth())
        GlassCard(Modifier.fillMaxWidth()) {
            Label(mediaStatusLabel(media.accessGranted, now != null, now?.playing == true))
            Spacer(Modifier.height(14.dp))
            val info: @Composable () -> Unit = {
                Text(
                    now?.title ?: "Nada tocando",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = if (compact) 22.sp else 30.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    textAlign = if (landscape && !compact) TextAlign.Start else TextAlign.Center
                )
                Text(
                    listOfNotNull(now?.artist, now?.album).joinToString(" · "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(18.dp))
                TransportControls(vm, playing = now?.playing == true, big = !compact)
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Chip("Volume −", icon = Icons.Rounded.VolumeDown, onClick = { vm.volumeDown() })
                    Chip("Volume +", icon = Icons.Rounded.VolumeUp, onClick = { vm.volumeUp() })
                }
            }
            if (landscape && !compact) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    AlbumArt(media, 240.dp)
                    Spacer(Modifier.width(28.dp))
                    Column(Modifier.weight(1f)) { info() }
                }
            } else {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    AlbumArt(media, if (compact) 150.dp else 240.dp)
                    Spacer(Modifier.height(18.dp))
                    info()
                }
            }
        }

        if (!media.accessGranted) {
            GlassCard(Modifier.fillMaxWidth()) {
                Text(
                    "Para mostrar e controlar a música de outros apps (Spotify, YouTube Music…), o Android exige " +
                        "que você libere o \"acesso a notificações\" para o Tracker X UI. O app usa isso só para a mídia.",
                    color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp
                )
                Spacer(Modifier.height(12.dp))
                PrimaryButton("Liberar controle de mídia") {
                    runCatching {
                        context.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }
            }
        }

        GlassCard(Modifier.fillMaxWidth()) {
            Label("Saída de áudio")
            Spacer(Modifier.height(8.dp))
            Chip(
                bluetoothStatusLabel(bt) + if (bt.connectedDevices.isNotEmpty()) " · " + bt.connectedDevices.joinToString() else "",
                icon = Icons.Rounded.Bluetooth, selected = bt.audioOverBluetooth
            )
            Spacer(Modifier.height(14.dp))
            Label("Abrir player")
            Spacer(Modifier.height(8.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MediaApp.values().forEach { app ->
                    Chip(app.label, icon = Icons.Rounded.MusicNote, onClick = {
                        if (!ExternalApps.launch(context, app.pkg)) ExternalApps.openStore(context, app.pkg)
                    })
                }
            }
        }
    }
}
