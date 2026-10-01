package com.trackerx.ui.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.trackerx.ui.MainActivity
import com.trackerx.ui.TrackerApp

/**
 * Tenta abrir o app quando o tablet liga. O Android moderno bloqueia a abertura de
 * telas em segundo plano para a maioria dos apps; o jeito garantido de iniciar junto
 * com o tablet é definir o Tracker X UI como tela inicial (launcher).
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val app = context.applicationContext as? TrackerApp ?: return
        if (!app.container.settings.state.value.autoStart) return
        runCatching {
            context.startActivity(
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
