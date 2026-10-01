package com.trackerx.ui.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.VerticalSplit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trackerx.ui.MainViewModel
import com.trackerx.ui.navigation.ExternalApps
import com.trackerx.ui.settings.NavApp

@Composable
fun NavScreen(vm: MainViewModel, compact: Boolean = false) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!compact) SectionTitle("Navegação")
        NavApp.values().sortedByDescending { it == settings.navApp }.forEach { app ->
            val installed = ExternalApps.isInstalled(context, app.pkg)
            GlassCard(Modifier.fillMaxWidth()) {
                Row {
                    Text(
                        app.label, color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)
                    )
                    if (app == settings.navApp) Chip("Padrão", selected = true)
                }
                Spacer(Modifier.height(4.dp))
                Label(if (installed) "Instalado" else "Não instalado")
                Spacer(Modifier.height(14.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (installed) {
                        PrimaryButton("Tela cheia", Icons.Rounded.Navigation) { ExternalApps.launch(context, app.pkg) }
                        PrimaryButton("Abrir ao lado", Icons.Rounded.VerticalSplit) {
                            ExternalApps.launch(context, app.pkg, adjacent = true)
                        }
                    } else {
                        PrimaryButton("Instalar") { ExternalApps.openStore(context, app.pkg) }
                    }
                }
            }
        }
        if (!compact) {
            Text(
                "O Android não permite embutir o Google Maps ou o Waze dentro de outro app. " +
                    "\"Abrir ao lado\" usa a tela dividida do próprio Android: o mapa fica numa metade e o Tracker X UI na outra.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp
            )
        }
    }
}
