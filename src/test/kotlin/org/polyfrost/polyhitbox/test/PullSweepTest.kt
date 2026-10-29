package org.polyfrost.polyhitbox.test

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.polyhitbox.render.sweepHitsBlock

class PullSweepTest {
    private val boxMin = doubleArrayOf(-0.3, -1.6, 10.0)
    private val boxMax = doubleArrayOf(0.3, 0.2, 10.6)
    private val minScale = 8.5 / 10.6

    private fun hits(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double) =
        sweepHitsBlock(doubleArrayOf(minX, minY, minZ), doubleArrayOf(maxX, maxY, maxZ), boxMin, boxMax, minScale)

    @Test
    fun `wall the entity hugs is in the pulled path`() {
        assertTrue(hits(-0.5, -1.6, 9.0, 0.5, -0.6, 10.0))
    }

    @Test
    fun `thin pane just in front is in the pulled path`() {
        assertTrue(hits(-0.5, -1.0, 9.6, 0.5, 0.0, 9.7))
    }

    @Test
    fun `floor the entity stands on is not in the way`() {
        assertFalse(hits(-0.5, -2.6, 9.0, 0.5, -1.6, 11.0))
    }

    @Test
    fun `block behind the hitbox is not in the way`() {
        assertFalse(hits(-0.5, -1.6, 10.6, 0.5, -0.6, 11.6))
    }

    @Test
    fun `block in front but past the pull is not in the way`() {
        assertFalse(hits(-0.5, -1.6, 7.0, 0.5, -0.6, 8.0))
    }

    @Test
    fun `block off to the side is not in the way`() {
        assertFalse(hits(2.0, -1.6, 9.0, 3.0, -0.6, 10.0))
    }
}
