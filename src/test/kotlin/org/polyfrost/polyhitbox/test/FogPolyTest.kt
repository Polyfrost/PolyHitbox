package org.polyfrost.polyhitbox.test

//? if >1.8.9
import com.mojang.blaze3d.vertex.VertexConsumer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.polyhitbox.render.FogPoly
import org.polyfrost.polyhitbox.render.HitboxFog
//? if >1.8.9 {
import org.polyfrost.polyhitbox.config.HitboxConfig
import org.polyfrost.polyhitbox.render.HitboxRenderer
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt
//?}

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
    fun `straddlesAny agrees with checking each cut`() {
        val poly = ribbon()
        val cutSets = listOf(doubleArrayOf(), doubleArrayOf(4.0), doubleArrayOf(6.0), doubleArrayOf(1.0, 4.0, 6.0, 9.0), doubleArrayOf(1.0, 5.0), doubleArrayOf(4.0, Math.nextUp(4.0)))
        // Channel B runs from 2 to 3 blocks
        val slanted = FogPoly().apply {
            add(0.0, 0.0, 4.0, 4.0, 2.0, 0.0)
            add(0.0, 0.0, 6.0, 6.0, 3.0, 0.0)
            add(0.1, 0.0, 6.0, 6.0, 3.0, 0.0)
        }
        for (cuts in cutSets + listOf(doubleArrayOf(2.0), doubleArrayOf(3.0), doubleArrayOf(2.0, 3.0), doubleArrayOf(2.5))) {
            for (p in listOf(poly, slanted)) for (channel in 0..1) {
                assertEquals(cuts.any { p.straddles(channel, it) }, p.straddlesAny(channel, cuts))
            }
        }
        assertTrue(slanted.straddlesAny(1, doubleArrayOf(1.0, 2.5)))
        assertTrue(slanted.straddlesAny(1, doubleArrayOf(Math.nextUp(2.0))))
        assertFalse(slanted.straddlesAny(1, doubleArrayOf(2.0, 3.0)))
        assertFalse(slanted.straddlesAny(1, doubleArrayOf(5.0)))
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

    //? if >1.8.9 {
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

    //? if >=1.21.8 {
    private fun fog(alpha: Float, start: Float, end: Float, distanceStart: Float = Float.MAX_VALUE, distanceEnd: Float = Float.MAX_VALUE) =
        HitboxFog.capture(0f, 0f, 0f, alpha, start, end, distanceStart, distanceEnd)
    //?} else {
    /*private fun fog(alpha: Float, start: Float, end: Float) = HitboxFog.capture(0f, 0f, 0f, alpha, start, end, false)
    *///?}

    @Test
    fun `classification matches the per-quad path`() {
        try {
            fog(1f, 1f, 2f)
            assertEquals(HitboxFog.CLEAR, classify(0.25, 0.75))
            assertEquals(HitboxFog.UNCUT, classify(2.5, 3.0))
            assertEquals(HitboxFog.SPLIT, classify(0.5, 1.5))
            assertEquals(HitboxFog.SPLIT, classify(0.25, Double.NaN))

            //? if >=1.21.8 {
            fog(1f, 0f, 4f, 0f, 8f)
            assertEquals(HitboxFog.SPLIT, HitboxFog.classify(1.5, 2.9, 1.6, 3.3))
            //?}

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

    private class Drawn(val x: Float, val y: Float, val z: Float, val color: Int)

    private val black = 0x80000000.toInt()

    private fun ramp(distance: Double): Double = ((distance - 4.0) / 4.0).coerceIn(0.0, 1.0)

    //? if >=1.21.8 {
    private fun environmental(x: Double, y: Double, z: Double): Double = ramp(sqrt(x * x + y * y + z * z))

    private fun renderDistance(x: Double, y: Double, z: Double): Double = ramp(max(sqrt(x * x + z * z), abs(y)))

    private fun overlapping(x: Double, y: Double, z: Double): Double =
        max(environmental(x, y, z), (max(sqrt(x * x + z * z), abs(y)) / 16.0).coerceAtMost(1.0))

    private fun dominance(v: Drawn): Double {
        val x = v.x.toDouble()
        val y = v.y.toDouble()
        val z = v.z.toDouble()
        return HitboxFog.dominance(HitboxFog.distanceA(x, y, z), HitboxFog.distanceB(x, y, z))
    }
    //?} else {
    /*private fun environmental(x: Double, y: Double, z: Double): Double {
        val t = ramp(sqrt(x * x + y * y + z * z))
        return t * t * (3.0 - 2.0 * t)
    }
    *///?}

    private fun fogged(amount: Double): Int {
        fun channel(shift: Int) = ((argb ushr shift and 0xFF) * (1.0 - amount)).roundToInt() shl shift
        return (argb and 0xFF000000.toInt()) or channel(16) or channel(8) or channel(0)
    }

    private fun draw(fogAt: (Double, Double, Double) -> Double, render: (VertexConsumer) -> Unit): List<Drawn> {
        val positions = ArrayList<FloatArray>()
        val drawn = ArrayList<Drawn>()
        lateinit var vc: VertexConsumer
        vc = Proxy.newProxyInstance(
            VertexConsumer::class.java.classLoader,
            arrayOf(VertexConsumer::class.java),
            InvocationHandler { _, method, args ->
                when {
                    method.name == "addVertex" && args.size == 3 -> positions.add(FloatArray(3) { args[it] as Float })
                    method.name == "setColor" && args.size == 1 -> {
                        val p = positions.removeAt(positions.size - 1)
                        drawn.add(Drawn(p[0], p[1], p[2], args[0] as Int))
                    }
                    else -> error("Unexpected ${method.name}")
                }
                vc
            },
        ) as VertexConsumer
        render(vc)
        assertTrue(positions.isEmpty())
        assertEquals(0, drawn.size % 4)
        for (v in drawn) {
            val expected = fogged(fogAt(v.x.toDouble(), v.y.toDouble(), v.z.toDouble()))
            for (shift in 0..24 step 8) assertTrue(abs((v.color ushr shift and 0xFF) - (expected ushr shift and 0xFF)) <= 1)
        }
        for (quad in drawn.chunked(4)) {
            val a = quad.map { HitboxFog.distanceA(it.x.toDouble(), it.y.toDouble(), it.z.toDouble()) }
            val b = quad.map { HitboxFog.distanceB(it.x.toDouble(), it.y.toDouble(), it.z.toDouble()) }
            for (cut in HitboxFog.cutsA) assertFalse(a.min() < cut - 1.0e-4 && a.max() > cut + 1.0e-4)
            for (cut in HitboxFog.cutsB) assertFalse(b.min() < cut - 1.0e-4 && b.max() > cut + 1.0e-4)
        }
        return drawn
    }

    private fun drawBox(
        fogAt: (Double, Double, Double) -> Double,
        minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double,
    ): List<Drawn> = draw(fogAt) { vc ->
        HitboxRenderer.classifyFog(minX, minY, minZ, maxX, maxY, maxZ, 0f)
        HitboxRenderer.fillBox(vc, minX, minY, minZ, maxX, maxY, maxZ, argb)
    }

    private fun assertPartial(drawn: List<Drawn>) {
        assertEquals(24, drawn.size)
        for (v in drawn) assertTrue(v.color != argb && v.color != black)
    }

    // Split points carry interpolated distances, so boxes crossing fog are kept thin around the view axis where interpolation is nearly exact
    @Test
    fun `rendered boxes match per-vertex fog across fog boundaries`() {
        try {
            fog(1f, 4f, 8f)
            val clear = drawBox(::environmental, -0.5, -0.5, 1.0, 0.5, 0.5, 2.0)
            assertEquals(24, clear.size)
            assertTrue(clear.all { it.color == argb })
            val hidden = drawBox(::environmental, 0.0, 0.0, 10.0, 1.0, 1.0, 11.0)
            assertEquals(24, hidden.size)
            assertTrue(hidden.all { it.color == black })
            assertPartial(drawBox(::environmental, -0.05, -0.05, 5.05, 0.05, 0.05, 5.45))
            assertTrue(drawBox(::environmental, -1.0e-3, -1.0e-3, 3.0, 1.0e-3, 1.0e-3, 9.0).size > 24)

            //? if >=1.21.8 {
            fog(1f, Float.MAX_VALUE, Float.MAX_VALUE, 4f, 8f)
            val far = drawBox(::renderDistance, 0.0, 0.0, 10.0, 1.0, 1.0, 11.0)
            assertEquals(24, far.size)
            assertTrue(far.all { it.color == black })
            assertPartial(drawBox(::renderDistance, -0.05, -0.05, 5.05, 0.05, 0.05, 5.45))
            assertTrue(drawBox(::renderDistance, -1.0e-3, -1.0e-3, 3.0, 1.0e-3, 1.0e-3, 9.0).size > 24)

            // Render distance fog leads until 16/3 blocks, then environmental fog takes over without crossing either fog's cuts
            fog(1f, 4f, 8f, 0f, 16f)
            val swap = drawBox(::overlapping, -0.05, -0.05, 5.05, 0.05, 0.05, 5.6)
            assertTrue(swap.size > 24)
            assertTrue(swap.any { abs(dominance(it)) < 1.0e-3 })
            //?}
        } finally {
            fog(0f, Float.MAX_VALUE, Float.MAX_VALUE)
        }
    }

    private fun area(quad: List<Drawn>): Double {
        val (p0, p1, p2, p3) = quad
        val ux = (p2.x - p0.x).toDouble()
        val uy = (p2.y - p0.y).toDouble()
        val uz = (p2.z - p0.z).toDouble()
        val vx = (p3.x - p1.x).toDouble()
        val vy = (p3.y - p1.y).toDouble()
        val vz = (p3.z - p1.z).toDouble()
        val cx = uy * vz - uz * vy
        val cy = uz * vx - ux * vz
        val cz = ux * vy - uy * vx
        return sqrt(cx * cx + cy * cy + cz * cz) * 0.5
    }

    private fun drawRay(
        fogAt: (Double, Double, Double) -> Double,
        ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double,
        thickness: Float,
    ): List<Drawn> {
        val ray = draw(fogAt) { HitboxRenderer.viewRay(it, HitboxConfig(), ax, ay, az, bx, by, bz, argb, thickness) }
        for (quad in ray.chunked(4)) assertTrue(area(quad) > 0.0)
        return ray
    }

    @Test
    fun `view ray splits where it crosses fog`() {
        val renderer = HitboxRenderer::class.java
        val forward = renderer.getDeclaredField("fwdZ").apply { isAccessible = true }
        val scale = renderer.getDeclaredField("ribbonScale").apply { isAccessible = true }
        val savedScale = scale.getDouble(null)
        try {
            forward.setDouble(null, 1.0)
            scale.setDouble(null, 0.4)
            fog(1f, 4f, 8f)
            HitboxRenderer.classifyFog(-0.1, -0.1, 1.0, 0.1, 0.1, 1.2, 0f)
            val crossing = drawRay(::environmental, 0.05, 0.0, 3.8, 0.05, 0.0, 4.2, 5.0e-3f)
            assertTrue(crossing.size > 4)
            assertTrue(crossing.any { it.color == argb })
            assertTrue(crossing.any { it.color != argb })

            //? if >=1.21.8 {
            // The line sits clear of fog but the ribbon's upper edge reaches into it, where cylindrical distance is just the height
            fog(1f, Float.MAX_VALUE, Float.MAX_VALUE, 4f, 8f)
            val wide = drawRay(::renderDistance, -0.1, 3.5, 1.0, 0.1, 3.5, 1.0, 2.5f)
            assertTrue(wide.any { it.y > 4.4f && it.color != argb })
            //?}
        } finally {
            forward.setDouble(null, 0.0)
            scale.setDouble(null, savedScale)
            fog(0f, Float.MAX_VALUE, Float.MAX_VALUE)
        }
    }
    //?}

    //? if <1.21.8 && >1.8.9 {
    /*@Test
    fun `smooth fog follows the vanilla shape and curve`() {
        try {
            HitboxFog.capture(1f, 1f, 1f, 1f, 0f, 4f, false)
            assertEquals(13.0, HitboxFog.distanceA(3.0, 12.0, 4.0), 1.0e-12)
            assertEquals(0xFF282828.toInt(), HitboxFog.apply(1.0, 0.0, 0xFF000000.toInt()))
            assertEquals(0xFF808080.toInt(), HitboxFog.apply(2.0, 0.0, 0xFF000000.toInt()))
            assertEquals(HitboxFog.SPLIT, HitboxFog.classify(1.4, 0.0, 1.6, 0.0))
            HitboxFog.capture(1f, 1f, 1f, 1f, 0f, 4f, true)
            assertEquals(12.0, HitboxFog.distanceA(3.0, 12.0, 4.0), 1.0e-12)
            assertEquals(5.0, HitboxFog.distanceA(3.0, 0.0, 4.0), 1.0e-12)
        } finally {
            HitboxFog.capture(0f, 0f, 0f, 0f, Float.MAX_VALUE, Float.MAX_VALUE, false)
        }
    }
    *///?}
}
