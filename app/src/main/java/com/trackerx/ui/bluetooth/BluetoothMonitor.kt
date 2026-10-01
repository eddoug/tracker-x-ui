package com.trackerx.ui.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PairedDevice(val name: String, val address: String)

data class BluetoothState(
    val supported: Boolean = false,
    val enabled: Boolean = false,
    val permissionGranted: Boolean = false,
    val a2dpConnected: Boolean = false,
    val connectedDevices: List<String> = emptyList(),
    /** O áudio de mídia do tablet está saindo por Bluetooth (ex.: som do Tracker). */
    val audioOverBluetooth: Boolean = false,
    val paired: List<PairedDevice> = emptyList()
)

/** Texto de status (função pura, coberta por testes). */
fun bluetoothStatusLabel(s: BluetoothState): String = when {
    !s.supported -> "Bluetooth indisponível"
    !s.permissionGranted -> "Permissão de Bluetooth pendente"
    !s.enabled -> "Bluetooth desligado"
    s.audioOverBluetooth -> "Áudio saindo por Bluetooth"
    s.a2dpConnected -> "Conectado (áudio não roteado)"
    else -> "Nenhum dispositivo de áudio"
}

/**
 * Observa o estado do Bluetooth usando só APIs oficiais. O app não pareia nem
 * transmite áudio por conta própria: o Android cuida do A2DP; aqui só lemos o estado.
 */
class BluetoothMonitor(private val context: Context) {
    private val _state = MutableStateFlow(BluetoothState())
    val state: StateFlow<BluetoothState> = _state

    private val adapter: BluetoothAdapter? =
        context.getSystemService(BluetoothManager::class.java)?.adapter
    private val audio = context.getSystemService(AudioManager::class.java)
    private var a2dp: BluetoothProfile? = null
    private var started = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, i: Intent?) = refresh()
    }

    private val audioCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(added: Array<out AudioDeviceInfo>?) = refresh()
        override fun onAudioDevicesRemoved(removed: Array<out AudioDeviceInfo>?) = refresh()
    }

    private fun hasPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
            PackageManager.PERMISSION_GRANTED

    fun start() {
        if (!started) {
            started = true
            val filter = IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED)
            }
            ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
            audio?.registerAudioDeviceCallback(audioCallback, Handler(Looper.getMainLooper()))
        }
        val bt = adapter
        if (a2dp == null && bt != null && hasPermission()) {
            runCatching {
                bt.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
                    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                        a2dp = proxy
                        refresh()
                    }

                    override fun onServiceDisconnected(profile: Int) {
                        a2dp = null
                        refresh()
                    }
                }, BluetoothProfile.A2DP)
            }
        }
        refresh()
    }

    @SuppressLint("MissingPermission")
    fun refresh() {
        val granted = hasPermission()
        val a = adapter
        if (a == null) {
            _state.value = BluetoothState(supported = false)
            return
        }
        var enabled = false
        var connected = emptyList<String>()
        var paired = emptyList<PairedDevice>()
        if (granted) {
            runCatching {
                enabled = a.isEnabled
                connected = a2dp?.connectedDevices?.map { it.name ?: it.address } ?: emptyList()
                paired = a.bondedDevices?.map { PairedDevice(it.name ?: it.address, it.address) }
                    ?: emptyList()
            }
        }
        val overBt = runCatching {
            audio?.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                ?.any { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP } == true
        }.getOrDefault(false)
        _state.value = BluetoothState(
            supported = true,
            enabled = enabled,
            permissionGranted = granted,
            a2dpConnected = connected.isNotEmpty() || overBt,
            connectedDevices = connected,
            audioOverBluetooth = overBt,
            paired = paired
        )
    }
}
