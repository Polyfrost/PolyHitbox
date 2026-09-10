package org.polyfrost.polyhitbox.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.polyhitbox.render.FogPoly

class FogPolyTest {
    // A ribbon running from 4 to 6 blocks away with fog ending at 5 blocks
    private fun ribbon() = FogPoly().apply {
        add(0.0, 0.0, 4.0, 4.0, 0.0)
        add(0.0, 0.0, 6.0, 6.0, 0.0)
        add(0.1, 0.0, 6.0, 6.0, 0.0)
        add(0.1, 0.0, 4.0, 4.0, 0.0)
    }

    @Test
    fun `split leaves no piece crossing the cut`() {
        val poly = ribbon()
        assertTrue(poly.straddles(0, 5.0))
        assertFalse(poly.straddles(1, 5.0))

        val below = FogPoly()
        val above = FogPoly()
        poly.split(0, 5.0, below, above)

        assertEquals(4, below.n)
        assertEquals(4, above.n)
        assertFalse(below.straddles(0, 5.0))
        assertFalse(above.straddles(0, 5.0))
        for (i in 0 until below.n) assertTrue(below.a[i] <= 5.0)
        for (i in 0 until above.n) assertTrue(above.a[i] >= 5.0)
        for (poly in listOf(below, above)) {
            val onCut = (0 until poly.n).filter { poly.a[it] == 5.0 }
            assertEquals(2, onCut.size)
            for (i in onCut) assertEquals(5.0, poly.z[i], 1.0e-12)
        }
    }

    @Test
    fun `dominance splits a triangle where the two fogs swap`() {
        val tri = FogPoly().apply {
            add(0.0, 0.0, 0.0, 0.0, 0.0, -1.0)
            add(1.0, 0.0, 0.0, 0.0, 0.0, 1.0)
            add(0.0, 1.0, 0.0, 0.0, 0.0, 1.0)
        }
        assertTrue(tri.straddles(FogPoly.DOMINANCE, 0.0))

        val below = FogPoly()
        val above = FogPoly()
        tri.split(FogPoly.DOMINANCE, 0.0, below, above)

        assertEquals(3, below.n)
        assertEquals(4, above.n)
        for (i in 0 until below.n) assertTrue(below.c[i] <= 0.0)
        for (i in 0 until above.n) assertTrue(above.c[i] >= 0.0)
        val onCut = (0 until below.n).filter { below.c[it] == 0.0 }.map { below.x[it] + below.y[it] }
        assertEquals(listOf(0.5, 0.5), onCut)
    }
}
