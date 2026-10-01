package com.trackerx.ui.vehicle

import android.content.Context
import com.trackerx.ui.obd.Elm327Provider
import com.trackerx.ui.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Escolhe a fonte (simulador, OBD2 ou nenhuma) conforme as configurações. */
class VehicleRepository(
    private val context: Context,
    scope: CoroutineScope,
    settings: SettingsRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val snapshot: StateFlow<VehicleSnapshot> = settings.state
        .map { Triple(it.useSimulatedData, it.obdAddress, it.obdName) }
        .distinctUntilChanged()
        .flatMapLatest { (simulated, address, _) ->
            when {
                simulated -> SimulatedVehicleDataProvider().stream()
                address != null -> Elm327Provider(context, address).stream()
                else -> flowOf(
                    VehicleSnapshot(
                        connected = false,
                        providerName = "Nenhum",
                        status = "OBD2 não configurado"
                    )
                )
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), VehicleSnapshot(status = "Iniciando"))
}
