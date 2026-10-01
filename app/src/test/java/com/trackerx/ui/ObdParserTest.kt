package com.trackerx.ui

import com.trackerx.ui.obd.ObdParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ObdParserTest {
    @Test fun rpm() = assertEquals(1726.0, ObdParser.rpm("41 0C 1A F8\r\r>")!!, 0.001)
    @Test fun speed() = assertEquals(60.0, ObdParser.speedKmh("410D3C")!!, 0.001)
    @Test fun coolant() = assertEquals(90.0, ObdParser.coolantC("41 05 82")!!, 0.001)
    @Test fun throttle() = assertEquals(100.0, ObdParser.throttlePct("41 11 FF")!!, 0.001)
    @Test fun voltage() = assertEquals(12.6, ObdParser.voltage("12.6V")!!, 0.001)
    @Test fun noDataIsNull() = assertNull(ObdParser.rpm("NO DATA"))
    @Test fun dtc() = assertEquals(listOf("P0133"), ObdParser.dtcs("43 01 33 00 00 00 00"))
    @Test fun noDtc() = assertEquals(emptyList<String>(), ObdParser.dtcs("43 00 00 00 00 00 00"))
}
