package com.trackerx.ui.obd

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.trackerx.ui.vehicle.DataSource
import com.trackerx.ui.vehicle.Reading
import com.trackerx.ui.vehicle.VehicleDataProvider
import com.trackerx.ui.vehicle.VehicleSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.util.UUID

/**
 * Leitor OBD2 via adaptador ELM327 Bluetooth clássico (perfil serial SPP).
 * O adaptador precisa estar pareado nas configurações do Android.
 * Parâmetros que o carro não responde ficam como N/D — nada é inventado.
 */
class Elm327Provider(private val context: Context, private val address: String) : VehicleDataProvider {
    override val name = "OBD2 (ELM327)"

    private fun real(v: Double?) = if (v == null) Reading.NA else Reading(v, DataSource.REAL)

    @SuppressLint("MissingPermission")
    override fun stream(): Flow<VehicleSnapshot> = flow {
        while (currentCoroutineContext().isActive) {
            var socket: BluetoothSocket? = null
            try {
                emit(VehicleSnapshot(providerName = name, status = "Conectando ao OBD2…"))
                val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter
                    ?: throw IllegalStateException("Bluetooth indisponível")
                val device = adapter.getRemoteDevice(address)
                socket = device.createRfcommSocketToServiceRecord(SPP)
                socket.connect()
                val io = ElmIo(socket)
                for (cmd in listOf("ATZ", "ATE0", "ATL0", "ATS0", "ATH0", "ATSP0")) io.command(cmd)

                var dtcs: List<String>? = ObdParser.dtcs(io.command("03"))
                var loops = 0
                while (currentCoroutineContext().isActive) {
                    val speed = ObdParser.speedKmh(io.command("010D"))
                    val rpm = ObdParser.rpm(io.command("010C"))
                    val snapshot = VehicleSnapshot(
                        connected = true,
                        providerName = name,
                        status = "OBD2 conectado",
                        speedKmh = real(speed),
                        rpm = real(rpm),
                        coolantC = real(ObdParser.coolantC(io.command("0105"))),
                        batteryV = real(ObdParser.voltage(io.command("ATRV"))),
                        engineLoadPct = real(ObdParser.engineLoadPct(io.command("0104"))),
                        throttlePct = real(ObdParser.throttlePct(io.command("0111"))),
                        fuelPct = real(ObdParser.fuelPct(io.command("012F"))),
                        intakeC = real(ObdParser.intakeC(io.command("010F"))),
                        pressureKpa = real(ObdParser.manifoldKpa(io.command("010B"))),
                        dtcs = dtcs
                    )
                    emit(snapshot)
                    if (++loops % 150 == 0) dtcs = ObdParser.dtcs(io.command("03"))
                    delay(150)
                }
            } catch (e: SecurityException) {
                emit(VehicleSnapshot(providerName = name, status = "Sem permissão de Bluetooth"))
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                emit(VehicleSnapshot(providerName = name, status = "OBD2 desconectado"))
            } finally {
                runCatching { socket?.close() }
            }
            delay(5_000) // tenta reconectar
        }
    }.flowOn(Dispatchers.IO)

    private class ElmIo(socket: BluetoothSocket) {
        private val input = socket.inputStream
        private val output = socket.outputStream

        /** Envia um comando e lê até o prompt '>' do ELM327. */
        fun command(cmd: String): String {
            output.write((cmd + "\r").toByteArray())
            output.flush()
            val sb = StringBuilder()
            val deadline = System.currentTimeMillis() + 3_000
            while (System.currentTimeMillis() < deadline) {
                if (input.available() > 0) {
                    val c = input.read()
                    if (c < 0 || c.toChar() == '>') break
                    sb.append(c.toChar())
                } else {
                    Thread.sleep(5)
                }
            }
            return sb.toString()
        }
    }

    companion object {
        private val SPP: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}
