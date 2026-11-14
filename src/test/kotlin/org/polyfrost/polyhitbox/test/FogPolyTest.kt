package org.polyfrost.polyhitbox.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.polyhitbox.render.FogPoly
import org.polyfrost.polyhitbox.render.HitboxFog

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

    //? if >=1.21.8 {
    private val argb = 0x80336699.toInt()

    private fun classify(lo: Double, hi: Double): Int {
        val mode = HitboxFog.classify(lo, lo, hi, hi)
        if (mode == HitboxFog.SPLIT) return mode
        val poly = FogPoly()
        for (i in 0..16) poly.add(0.0, 0.0, 0.0, lo + (hi - lo) * i / 16, lo + (hi - lo) * i / 16)
        for (cut in HitboxFog.cutsA) assertFalse(poly.straddles(0, cut))
        for (cut in HitboxFog.cutsB) assertFalse(poly.straddles(1, cut))
        assertFalse(poly.straddles(FogPoly.DOMINANCE, 0.0))
        if (mode == HitboxFog.CLEAR) {
            for (i in 0 until poly.n) assertEquals(argb, HitboxFog.apply(poly.a[i], poly.b[i], argb))
            assertEquals(argb, HitboxFog.apply(0.0, 0.0, argb))
        }
        return mode
    }

    private fun fog(alpha: Float, start: Float, end: Float, distanceStart: Float = Float.MAX_VALUE, distanceEnd: Float = Float.MAX_VALUE) =
        HitboxFog.capture(0f, 0f, 0f, alpha, start, end, distanceStart, distanceEnd)

    @Test
    fun `classification matches the per-quad path`() {
        try {
            fog(1f, 1f, 2f)
            assertEquals(HitboxFog.CLEAR, classify(0.25, 0.75))
            assertEquals(HitboxFog.UNCUT, classify(2.5, 3.0))
            assertEquals(HitboxFog.SPLIT, classify(0.5, 1.5))
            assertEquals(HitboxFog.SPLIT, classify(0.25, Double.NaN))

            fog(1f, 0f, 4f, 0f, 8f)
            assertEquals(HitboxFog.SPLIT, HitboxFog.classify(1.5, 2.9, 1.6, 3.3))

            fog(Float.POSITIVE_INFINITY, 1f, 2f)
            assertEquals(HitboxFog.SPLIT, classify(0.25, 0.75))
            fog(0f, Float.NEGATIVE_INFINITY, 1f)
            assertEquals(HitboxFog.SPLIT, classify(2.0, 3.0))
            fog(0f, -Float.MAX_VALUE, Float.MAX_VALUE)
            assertEquals(HitboxFog.SPLIT, classify(Math.nextDown(Float.MAX_VALUE).toDouble(), Float.MAX_VALUE * (1.0 - 1.0e-12)))
        } finally {
            fog(0f, Float.MAX_VALUE, Float.MAX_VALUE)
        }
    }
    //?}
}
