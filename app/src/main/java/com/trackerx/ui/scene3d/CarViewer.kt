package com.trackerx.ui.scene3d

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.Matrix
import android.view.Choreographer
import android.view.Gravity
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.filament.EntityManager
import com.google.android.filament.IndirectLight
import com.google.android.filament.LightManager
import com.google.android.filament.Skybox
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import java.nio.ByteBuffer

/** Ângulos prontos. A frente do modelo aponta para +X no arquivo GLB. */
enum class ViewPreset(val label: String, val yaw: Float, val pitch: Float) {
    THREE_QUARTER("3/4", -55f, 14f),
    FRONT("Frente", -90f, 6f),
    REAR("Traseira", 90f, 6f),
    SIDE("Lateral", 0f, 4f),
    TOP("Superior", 0f, 90f)
}

/**
 * Cena 3D com Filament. O modelo é um arquivo GLB em assets/models/car.glb;
 * para trocar por um modelo mais detalhado basta substituir o arquivo
 * (veja docs/MODELO_3D.md) — nenhuma tela precisa mudar.
 */
@SuppressLint("ClickableViewAccessibility")
class CarScene(context: Context, modelAsset: String = DEFAULT_MODEL) {
    val view = SurfaceView(context)
    private val choreographer = Choreographer.getInstance()
    private val viewer: ModelViewer
    private val base = FloatArray(16)
    private val tmp = FloatArray(16)
    private val out = FloatArray(16)
    private var hasBase = false
    private var yaw = ViewPreset.THREE_QUARTER.yaw
    private var pitch = ViewPreset.THREE_QUARTER.pitch
    private var targetYaw = yaw
    private var targetPitch = pitch
    private var lastPreset: ViewPreset? = null
    private var lastDark: Boolean? = null
    var autoRotate = true

    private val frame = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)
            if (autoRotate) targetYaw += 0.12f
            yaw += (targetYaw - yaw) * 0.12f
            pitch += (targetPitch - pitch) * 0.12f
            applyTransform()
            viewer.render(frameTimeNanos)
        }
    }

    init {
        // Registrado antes do ModelViewer: na remoção da tela, paramos de renderizar
        // antes de o ModelViewer destruir o motor gráfico.
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) = choreographer.postFrameCallback(frame)
            override fun onViewDetachedFromWindow(v: View) = choreographer.removeFrameCallback(frame)
        })
        viewer = ModelViewer(view)
        view.setOnTouchListener { _, event ->
            viewer.onTouchEvent(event)
            true
        }

        val bytes = context.assets.open(modelAsset).use { it.readBytes() }
        val buffer = ByteBuffer.allocateDirect(bytes.size)
        buffer.put(bytes)
        buffer.rewind()
        viewer.loadModelGlb(buffer)
        viewer.transformToUnitCube()

        val engine = viewer.engine
        val fill = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(0.75f, 0.88f, 1.0f)
            .intensity(45_000f)
            .direction(0.6f, -0.35f, 0.7f)
            .castShadows(false)
            .build(engine, fill)
        viewer.scene.addEntity(fill)
        viewer.scene.indirectLight = IndirectLight.Builder()
            .irradiance(1, floatArrayOf(0.55f, 0.6f, 0.68f))
            .intensity(32_000f)
            .build(engine)
        setDark(true)
    }

    fun setPreset(preset: ViewPreset) {
        if (preset == lastPreset) return
        lastPreset = preset
        // gira pelo caminho mais curto
        val current = ((targetYaw % 360f) + 360f) % 360f
        var delta = (preset.yaw - current) % 360f
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        targetYaw += delta
        targetPitch = preset.pitch
    }

    fun setDark(dark: Boolean) {
        if (dark == lastDark) return
        lastDark = dark
        val c = if (dark) floatArrayOf(0.02f, 0.03f, 0.045f) else floatArrayOf(0.86f, 0.89f, 0.92f)
        viewer.scene.skybox = Skybox.Builder().color(c[0], c[1], c[2], 1f).build(viewer.engine)
    }

    private fun applyTransform() {
        val asset = viewer.asset ?: return
        val tm = viewer.engine.transformManager
        val instance = tm.getInstance(asset.root)
        if (!hasBase) {
            tm.getTransform(instance, base)
            hasBase = true
        }
        // O ModelViewer posiciona o modelo em (0, 0, -4); giramos em torno desse ponto.
        Matrix.setIdentityM(tmp, 0)
        Matrix.translateM(tmp, 0, 0f, 0f, -4f)
        Matrix.rotateM(tmp, 0, pitch, 1f, 0f, 0f)
        Matrix.rotateM(tmp, 0, yaw, 0f, 1f, 0f)
        Matrix.translateM(tmp, 0, 0f, 0f, 4f)
        Matrix.multiplyMM(out, 0, tmp, 0, base, 0)
        tm.setTransform(instance, out)
    }

    companion object {
        const val DEFAULT_MODEL = "models/car.glb"

        init {
            Utils.init()
        }
    }
}

@Composable
fun CarViewer(
    modifier: Modifier = Modifier,
    preset: ViewPreset,
    autoRotate: Boolean,
    dark: Boolean
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            try {
                val scene = CarScene(context)
                scene.view.tag = scene
                scene.view
            } catch (t: Throwable) {
                TextView(context).apply {
                    text = "Visualização 3D indisponível neste dispositivo"
                    gravity = Gravity.CENTER
                    setTextColor(0xFF8A97A6.toInt())
                }
            }
        },
        update = { view ->
            (view.tag as? CarScene)?.let { scene ->
                scene.setPreset(preset)
                scene.autoRotate = autoRotate
                scene.setDark(dark)
            }
        }
    )
}
