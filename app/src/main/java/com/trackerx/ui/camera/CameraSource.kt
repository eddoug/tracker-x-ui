package com.trackerx.ui.camera

import kotlinx.coroutines.flow.Flow

/**
 * Abstrações para futuras fontes de vídeo. O tablet não tem acesso às câmeras
 * originais do Tracker; uma fonte real (ex.: placa de captura USB/UVC ou câmera IP)
 * deve implementar CameraSource e ser registrada em CameraRegistry.
 */
enum class CameraPosition(val label: String) {
    REAR("Ré"), FRONT("Frontal"), LEFT("Lateral esquerda"), RIGHT("Lateral direita"), SURROUND("360°")
}

sealed interface CameraState {
    data object NotConfigured : CameraState
    data object Connecting : CameraState
    data object Streaming : CameraState
    data class Error(val message: String) : CameraState
}

interface CameraSource {
    val position: CameraPosition
    val state: Flow<CameraState>
    fun start()
    fun stop()
}

object CameraRegistry {
    private val sources = mutableMapOf<CameraPosition, CameraSource>()
    fun register(source: CameraSource) { sources[source.position] = source }
    fun get(position: CameraPosition): CameraSource? = sources[position]
    fun configured(): List<CameraPosition> = sources.keys.toList()
}
