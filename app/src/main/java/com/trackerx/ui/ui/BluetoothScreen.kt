package com.trackerx.ui.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackerx.ui.MainViewModel
import com.trackerx.ui.bluetooth.bluetoothStatusLabel

@Composable
fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun BluetoothScreen(vm: MainViewModel, requestPermissions: () -> Unit) {
    val bt by vm.bluetooth.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle("Bluetooth")
        GlassCard(Modifier.fillMaxWidth()) {
            Chip(bluetoothStatusLabel(bt), icon = Icons.Rounded.Bluetooth, selected = bt.audioOverBluetooth)
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoLine("Dispositivo conectado", bt.connectedDevices.joinToString().ifEmpty { "Nenhum" })
                InfoLine("Estado da conexão", if (bt.a2dpConnected) "Conectado" else "Desconectado")
                InfoLine("Perfil de áudio", if (bt.a2dpConnected) "A2DP (áudio de mídia)" else "Indisponível")
                InfoLine("Áudio multimídia", if (bt.audioOverBluetooth) "Saindo por Bluetooth" else "No alto-falante do tablet")
            }
        }
        if (!bt.permissionGranted) {
            GlassCard(Modifier.fillMaxWidth()) {
                Text(
                    "O app precisa da permissão \"Dispositivos por perto\" para ler o estado do Bluetooth.",
                    color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp
                )
                Spacer(Modifier.height(12.dp))
                PrimaryButton("Conceder permissão", onClick = requestPermissions)
            }
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Text(
                "O pareamento é feito nas configurações do Android. Depois de parear o tablet com o som do Tracker, " +
                    "o áudio de qualquer player sai pelo carro automaticamente.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp
            )
            Spacer(Modifier.height(12.dp))
            PrimaryButton("Abrir Bluetooth do Android", Icons.Rounded.Bluetooth) {
                runCatching {
                    context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
        }
    }
}
