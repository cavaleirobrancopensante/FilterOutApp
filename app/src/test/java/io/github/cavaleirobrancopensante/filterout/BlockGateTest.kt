package io.github.cavaleirobrancopensante.filterout

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockGateTest {

    private var now = 1_000_000L
    private val gate = BlockGate(clock = { now })

    @Test fun blocksNormallyBeforeAnyDismissal() {
        assertTrue(gate.mayBlock())
    }

    @Test fun cannotBlockForFiveSecondsAfterDismissal() {
        gate.dismissed()
        assertFalse(gate.mayBlock())
        now += 4_999
        assertFalse(gate.mayBlock())
        now += 1
        assertTrue(gate.mayBlock())
    }

    @Test fun everyDismissalRestartsTheGrace() {
        gate.dismissed()
        now += 6_000
        assertTrue(gate.mayBlock())
        gate.dismissed()
        now += 1_000
        assertFalse(gate.mayBlock())
    }
}
