package com.trackerx.ui.vehicle

import kotlinx.coroutines.flow.Flow

/** Origem de cada valor mostrado. A interface nunca mistura as três sem rotular. */
enum class DataSource(val label: String) {
    REAL("REAL"),
    SIMULATED("SIMULADO"),
    ESTIMATED("ESTIMADO"),
    UNAVAILABLE("N/D")
}

data class Reading(val value: Double?, val source: DataSource) {
    val available: Boolean get() = value != null && source != DataSource.UNAVAILABLE

    companion object {
        val NA = Reading(null, DataSource.UNAVAILABLE)
    }
}

data class VehicleSnapshot(
    val connected: Boolean = false,
    val providerName: String = "",
    val status: String = "",
    val speedKmh: Reading = Reading.NA,
    val rpm: Reading = Reading.NA,
    val coolantC: Reading = Reading.NA,
    val batteryV: Reading = Reading.NA,
    val engineLoadPct: Reading = Reading.NA,
    val throttlePct: Reading = Reading.NA,
    val fuelPct: Reading = Reading.NA,
    val consumptionKmL: Reading = Reading.NA,
    val intakeC: Reading = Reading.NA,
    val pressureKpa: Reading = Reading.NA,
    /** null = não lido; lista vazia = lido, sem falhas. */
    val dtcs: List<String>? = null
) {
    /**
     * Só afirma "motor ligado" quando há RPM real ou simulado; sem dado, devolve null
     * (a interface mostra "Indisponível" em vez de adivinhar).
     */
    val engineRunning: Boolean? get() = rpm.value?.let { it > 300.0 }
}

/** Fonte de dados do veículo. Implementações: simulada e OBD2 (ELM327). */
interface VehicleDataProvider {
    val name: String
    fun stream(): Flow<VehicleSnapshot>
}
