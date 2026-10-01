package com.trackerx.ui

import com.trackerx.ui.bluetooth.BluetoothState
import com.trackerx.ui.bluetooth.bluetoothStatusLabel
import com.trackerx.ui.media.mediaStatusLabel
import com.trackerx.ui.navigation.Screen
import com.trackerx.ui.settings.AppSettings
import com.trackerx.ui.settings.SpeedUnit
import com.trackerx.ui.settings.TempUnit
import com.trackerx.ui.settings.ThemeMode
import com.trackerx.ui.settings.celsiusToUnit
import com.trackerx.ui.settings.kmhToUnit
import com.trackerx.ui.settings.resolveDark
import com.trackerx.ui.voice.KeywordVoiceInterpreter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicTest {
    // Bluetooth
    @Test fun bluetoothStates() {
        assertEquals("Bluetooth indisponível", bluetoothStatusLabel(BluetoothState()))
        val base = BluetoothState(supported = true, permissionGranted = true)
        assertEquals("Bluetooth desligado", bluetoothStatusLabel(base))
        assertEquals("Nenhum dispositivo de áudio", bluetoothStatusLabel(base.copy(enabled = true)))
        assertEquals(
            "Áudio saindo por Bluetooth",
            bluetoothStatusLabel(base.copy(enabled = true, a2dpConnected = true, audioOverBluetooth = true))
        )
    }

    // Controle de mídia
    @Test fun mediaStates() {
        assertEquals("Acesso à mídia não concedido", mediaStatusLabel(false, false, false))
        assertEquals("Nenhum player ativo", mediaStatusLabel(true, false, false))
        assertEquals("Reproduzindo", mediaStatusLabel(true, true, true))
        assertEquals("Pausado", mediaStatusLabel(true, true, false))
    }

    // Navegação (por voz)
    @Test fun voiceNavigation() {
        assertEquals(Screen.NAV, KeywordVoiceInterpreter.interpret("Abrir mapa"))
        assertEquals(Screen.MEDIA, KeywordVoiceInterpreter.interpret("abrir música"))
        assertEquals(Screen.CAR, KeywordVoiceInterpreter.interpret("ABRIR CARRO"))
        assertEquals(Screen.SETTINGS, KeywordVoiceInterpreter.interpret("abrir configurações"))
        assertNull(KeywordVoiceInterpreter.interpret("qualquer coisa"))
    }

    // Configurações
    @Test fun settingsDefaultsAndUnits() {
        val s = AppSettings()
        assertTrue(s.useSimulatedData)
        assertNull(s.obdAddress)
        assertEquals(62.1371, kmhToUnit(100.0, SpeedUnit.MPH), 0.001)
        assertEquals(100.0, kmhToUnit(100.0, SpeedUnit.KMH), 0.001)
        assertEquals(212.0, celsiusToUnit(100.0, TempUnit.FAHRENHEIT), 0.001)
    }

    // Troca de tema
    @Test fun themeSwitch() {
        assertTrue(resolveDark(ThemeMode.DARK, systemDark = false))
        assertFalse(resolveDark(ThemeMode.LIGHT, systemDark = true))
        assertTrue(resolveDark(ThemeMode.AUTO, systemDark = true))
        assertFalse(resolveDark(ThemeMode.AUTO, systemDark = false))
    }
}
