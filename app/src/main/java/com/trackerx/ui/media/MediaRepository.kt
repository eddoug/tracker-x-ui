package com.trackerx.ui.media

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class NowPlaying(
    val title: String?,
    val artist: String?,
    val album: String?,
    val art: Bitmap?,
    val playing: Boolean,
    val packageName: String
)

data class MediaState(
    /** false = o usuário ainda não concedeu "acesso a notificações". */
    val accessGranted: Boolean = false,
    val now: NowPlaying? = null
)

/** Texto de status (função pura, coberta por testes). */
fun mediaStatusLabel(accessGranted: Boolean, hasSession: Boolean, playing: Boolean): String = when {
    !accessGranted -> "Acesso à mídia não concedido"
    !hasSession -> "Nenhum player ativo"
    playing -> "Reproduzindo"
    else -> "Pausado"
}

/** Controla players de outros apps (Spotify, YouTube Music…) via MediaSession. */
class MediaRepository(private val context: Context) {
    private val _state = MutableStateFlow(MediaState())
    val state: StateFlow<MediaState> = _state

    private val manager = context.getSystemService(MediaSessionManager::class.java)
    private val audio = context.getSystemService(AudioManager::class.java)
    private val component = ComponentName(context, MediaListenerService::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private var controller: MediaController? = null
    private var listening = false

    private val sessionsListener =
        MediaSessionManager.OnActiveSessionsChangedListener { list -> pick(list ?: emptyList()) }

    private val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = publish()
        override fun onPlaybackStateChanged(state: PlaybackState?) = publish()
        override fun onSessionDestroyed() = refresh()
    }

    /** Chamar ao abrir o app e ao voltar das configurações. */
    fun refresh() {
        try {
            val sessions = manager.getActiveSessions(component)
            if (!listening) {
                manager.addOnActiveSessionsChangedListener(sessionsListener, component, handler)
                listening = true
            }
            pick(sessions)
        } catch (e: SecurityException) {
            attach(null)
            _state.value = MediaState(accessGranted = false)
        }
    }

    private fun pick(sessions: List<MediaController>) {
        val best = sessions.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
            ?: sessions.firstOrNull()
        attach(best)
        publish()
    }

    private fun attach(next: MediaController?) {
        if (controller?.sessionToken == next?.sessionToken) return
        controller?.unregisterCallback(callback)
        controller = next
        next?.registerCallback(callback, handler)
    }

    private fun publish() {
        val c = controller
        if (c == null) {
            _state.value = MediaState(accessGranted = true, now = null)
            return
        }
        val m = c.metadata
        val art = m?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: m?.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: m?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
        _state.value = MediaState(
            accessGranted = true,
            now = NowPlaying(
                title = m?.getString(MediaMetadata.METADATA_KEY_TITLE)
                    ?: m?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE),
                artist = m?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                    ?: m?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST),
                album = m?.getString(MediaMetadata.METADATA_KEY_ALBUM),
                art = art,
                playing = c.playbackState?.state == PlaybackState.STATE_PLAYING,
                packageName = c.packageName
            )
        )
    }

    fun playPause() {
        val c = controller ?: return
        if (c.playbackState?.state == PlaybackState.STATE_PLAYING) c.transportControls.pause()
        else c.transportControls.play()
    }

    fun next() { controller?.transportControls?.skipToNext() }
    fun previous() { controller?.transportControls?.skipToPrevious() }

    fun volumeUp() = audio.adjustStreamVolume(
        AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI
    )

    fun volumeDown() = audio.adjustStreamVolume(
        AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI
    )
}
