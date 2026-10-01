package com.trackerx.ui.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trackerx.ui.MainViewModel

enum class SplitModule(val label: String) {
    MAP("Mapa"), MEDIA("Música"), CAR_3D("Carro 3D"), CAR_DATA("Dados do carro")
}

/** Tela dividida interna: dois módulos lado a lado, trocáveis. */
@Composable
fun SplitScreen(vm: MainViewModel, landscape: Boolean) {
    var first by rememberSaveable { mutableStateOf(SplitModule.CAR_3D.name) }
    var second by rememberSaveable { mutableStateOf(SplitModule.MEDIA.name) }
    val a = SplitModule.valueOf(first)
    // Só um painel 3D por vez (um único motor gráfico ativo).
    val b = SplitModule.valueOf(second).let { if (it == SplitModule.CAR_3D && a == SplitModule.CAR_3D) SplitModule.CAR_DATA else it }

    val paneA: @Composable (Modifier) -> Unit = { m -> Pane(vm, a, m) { first = it.name } }
    val paneB: @Composable (Modifier) -> Unit = { m -> Pane(vm, b, m) { second = it.name } }
    if (landscape) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            paneA(Modifier.weight(1f).fillMaxHeight())
            paneB(Modifier.weight(1f).fillMaxHeight())
        }
    } else {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            paneA(Modifier.weight(1f).fillMaxWidth())
            paneB(Modifier.weight(1f).fillMaxWidth())
        }
    }
}

@Composable
private fun Pane(vm: MainViewModel, module: SplitModule, modifier: Modifier, onChange: (SplitModule) -> Unit) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SplitModule.values().forEach { m ->
                Chip(m.label, selected = m == module, onClick = { onChange(m) })
            }
        }
        Box(Modifier.fillMaxWidth().weight(1f)) {
            when (module) {
                SplitModule.MAP -> NavScreen(vm, compact = true)
                SplitModule.MEDIA -> MediaScreen(vm, landscape = false, compact = true)
                SplitModule.CAR_3D -> Car3DPane(vm, Modifier.fillMaxSize())
                SplitModule.CAR_DATA -> CarDataPane(vm, Modifier.fillMaxSize())
            }
        }
    }
}
