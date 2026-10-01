package com.trackerx.ui.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri

/** Telas do app (navegação interna por estado, sem biblioteca extra). */
enum class Screen(val label: String) {
    HOME("Início"),
    NAV("Mapa"),
    MEDIA("Mídia"),
    CAR("Carro"),
    SPLIT("Dividir"),
    APPS("Apps"),
    BLUETOOTH("Bluetooth"),
    SETTINGS("Ajustes")
}

object ExternalApps {
    fun isInstalled(context: Context, pkg: String): Boolean =
        context.packageManager.getLaunchIntentForPackage(pkg) != null

    /**
     * Abre outro app. Com adjacent = true pede ao Android para abri-lo ao lado
     * (tela dividida do sistema). Um app não pode embutir Maps/Waze dentro de si;
     * a tela dividida do Android é o caminho oficial.
     */
    fun launch(context: Context, pkg: String, adjacent: Boolean = false): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(pkg) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (adjacent) intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
        return runCatching { context.startActivity(intent) }.isSuccess
    }

    fun openStore(context: Context, pkg: String) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
