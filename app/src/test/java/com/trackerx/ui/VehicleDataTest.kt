package com.trackerx.ui

import com.trackerx.ui.vehicle.DataSource
import com.trackerx.ui.vehicle.Reading
import com.trackerx.ui.vehicle.SimulatedVehicleDataProvider
import com.trackerx.ui.vehicle.VehicleSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleDataTest {
    @Test fun simulatedValuesAreLabelledAndInRange() {
        for (t in 0..600) {
            val s = SimulatedVehicleDataProvider.simulate(t.toDouble())
            assertEquals(DataSource.SIMULATED, s.speedKmh.source)
            assertEquals(DataSource.SIMULATED, s.rpm.source)
            assertTrue(s.speedKmh.value!! in 0.0..200.0)
            assertTrue(s.rpm.value!! in 500.0..7000.0)
            assertTrue(s.fuelPct.value!! in 0.0..100.0)
        }
    }

    @Test fun simulationIsDeterministic() =
        assertEquals(SimulatedVehicleDataProvider.simulate(12.5), SimulatedVehicleDataProvider.simulate(12.5))

    @Test fun providerEmits() = runTest {
        assertTrue(SimulatedVehicleDataProvider().stream().first().connected)
    }

    @Test fun missingDataIsNotInvented() {
        val empty = VehicleSnapshot()
        assertFalse(empty.speedKmh.available)
        assertNull(empty.engineRunning)
        assertEquals(Reading.NA, empty.fuelPct)
        assertNull(empty.dtcs)
    }
}
