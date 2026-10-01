package com.trackerx.ui.vehicle

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

/** Dados de demonstração. Todos os valores saem rotulados como SIMULADO. */
class SimulatedVehicleDataProvider(private val periodMs: Long = 200L) : VehicleDataProvider {
    override val name = "Simulador"

    override fun stream(): Flow<VehicleSnapshot> = flow {
        val start = System.nanoTime()
        while (true) {
            emit(simulate((System.nanoTime() - start) / 1e9))
            delay(periodMs)
        }
    }

    companion object {
        private fun sim(v: Double) = Reading(v, DataSource.SIMULATED)

        /** Função pura: mesmo instante, mesmo resultado (usada nos testes). */
        fun simulate(t: Double): VehicleSnapshot {
            // Ciclo de 60 s: parado, acelera, cruzeiro, desacelera.
            val phase = (t % 60.0) / 60.0
            val speed = max(0.0, 62.0 * sin(phase * PI) + 8.0 * sin(t * 0.7) - 6.0)
            val throttle = (18.0 + 55.0 * max(0.0, sin(phase * 2 * PI))).coerceIn(0.0, 100.0)
            val rpm = 820.0 + speed * 32.0 + throttle * 9.0
            return VehicleSnapshot(
                connected = true,
                providerName = "Simulador",
                status = "Dados simulados",
                speedKmh = sim(speed),
                rpm = sim(rpm),
                coolantC = sim(88.0 + 3.0 * sin(t / 40.0)),
                batteryV = sim(13.9 + 0.2 * sin(t / 7.0)),
                engineLoadPct = sim((20.0 + throttle * 0.6).coerceIn(0.0, 100.0)),
                throttlePct = sim(throttle),
                fuelPct = sim(max(5.0, 68.0 - t / 600.0)),
                consumptionKmL = sim(if (speed < 3) 0.0 else 9.0 + 4.0 * sin(phase * PI)),
                intakeC = sim(34.0 + 2.0 * sin(t / 25.0)),
                pressureKpa = sim(35.0 + throttle * 0.6),
                dtcs = emptyList()
            )
        }
    }
}
