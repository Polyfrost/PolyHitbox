package org.polyfrost.polyhitbox.render

//? if <1.21.8 {
/*import com.mojang.blaze3d.shaders.FogShape
import com.mojang.blaze3d.systems.RenderSystem
*///?}
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

// Applies blindness, darkness, lava, powder snow, water and render distance fog to hitboxes exactly like vanilla
object HitboxFog {
    private val NO_CUTS = DoubleArray(0)

    private const val NO_FOG = 1.0e30

    private var red = 0f
    private var green = 0f
    private var blue = 0f
    private var alpha = 0f

    var cutsA: DoubleArray = NO_CUTS
        private set

    var cutsB: DoubleArray = NO_CUTS
        private set

    private fun stepCuts(start: Float): DoubleArray = doubleArrayOf(start.toDouble(), Math.nextUp(start).toDouble())

    private fun spherical(x: Double, y: Double, z: Double): Double = sqrt(x * x + y * y + z * z)

    private fun cylindrical(x: Double, y: Double, z: Double): Double = max(sqrt(x * x + z * z), abs(y))

    //? if >=1.21.8 {
    private var environmentalStart = Float.MAX_VALUE
    private var environmentalEnd = Float.MAX_VALUE
    private var renderDistanceStart = Float.MAX_VALUE
    private var renderDistanceEnd = Float.MAX_VALUE

    @JvmStatic
    fun capture(
        red: Float, green: Float, blue: Float, alpha: Float,
        environmentalStart: Float, environmentalEnd: Float, renderDistanceStart: Float, renderDistanceEnd: Float,
    ) {
        this.red = red
        this.green = green
        this.blue = blue
        this.alpha = alpha
        this.environmentalStart = environmentalStart
        this.environmentalEnd = environmentalEnd
        this.renderDistanceStart = renderDistanceStart
        this.renderDistanceEnd = renderDistanceEnd
        cutsA = linearCuts(environmentalStart, environmentalEnd)
        cutsB = linearCuts(renderDistanceStart, renderDistanceEnd)
    }

    private fun linearCuts(start: Float, end: Float): DoubleArray = when {
        start >= NO_FOG -> NO_CUTS
        start < end -> doubleArrayOf(start.toDouble(), end.toDouble())
        else -> stepCuts(start)
    }

    fun beginFrame() {}

    fun distanceA(x: Double, y: Double, z: Double): Double = spherical(x, y, z)

    fun distanceB(x: Double, y: Double, z: Double): Double = cylindrical(x, y, z)

    fun dominance(a: Double, b: Double): Double =
        (linear(a.toFloat(), environmentalStart, environmentalEnd) - linear(b.toFloat(), renderDistanceStart, renderDistanceEnd)).toDouble()

    private fun value(a: Double, b: Double): Float = max(
        linear(a.toFloat(), environmentalStart, environmentalEnd),
        linear(b.toFloat(), renderDistanceStart, renderDistanceEnd),
    )

    private fun linear(distance: Float, start: Float, end: Float): Float = when {
        distance <= start -> 0f
        distance >= end -> 1f
        else -> (distance - start) / (end - start)
    }
    //?} else {
    /*private const val SMOOTH_PIECES = 8

    private var start = Float.MAX_VALUE
    private var end = Float.MAX_VALUE
    private var cylinder = false

    fun beginFrame() {
        val lastStart = start
        val lastEnd = end
        //? if >=1.21.4 {
        val fog = RenderSystem.getShaderFog()
        red = fog.red
        green = fog.green
        blue = fog.blue
        alpha = fog.alpha
        start = fog.start
        end = fog.end
        cylinder = fog.shape == FogShape.CYLINDER
        //?} else {
        /*val color = RenderSystem.getShaderFogColor()
        red = color[0]
        green = color[1]
        blue = color[2]
        alpha = color[3]
        start = RenderSystem.getShaderFogStart()
        end = RenderSystem.getShaderFogEnd()
        cylinder = RenderSystem.getShaderFogShape() == FogShape.CYLINDER
        *///?}
        if (start != lastStart || end != lastEnd) cutsA = smoothCuts()
    }

    private fun smoothCuts(): DoubleArray = when {
        start >= NO_FOG -> NO_CUTS
        start < end -> DoubleArray(SMOOTH_PIECES + 1) { start + (end - start) * it.toDouble() / SMOOTH_PIECES }
        else -> stepCuts(start)
    }

    fun distanceA(x: Double, y: Double, z: Double): Double = if (cylinder) cylindrical(x, y, z) else spherical(x, y, z)

    fun distanceB(x: Double, y: Double, z: Double): Double = 0.0

    fun dominance(a: Double, b: Double): Double = 0.0

    private fun value(a: Double, b: Double): Float {
        val distance = a.toFloat()
        if (distance <= start) return 0f
        if (distance >= end) return 1f
        val t = (distance - start) / (end - start)
        return t * t * (3f - 2f * t)
    }
    *///?}

    fun apply(a: Double, b: Double, argb: Int): Int {
        val amount = value(a, b) * alpha
        if (amount <= 0f) return argb
        val opacity = argb ushr 24 and 0xFF
        val r = mix(argb ushr 16 and 0xFF, red, amount)
        val g = mix(argb ushr 8 and 0xFF, green, amount)
        val bl = mix(argb and 0xFF, blue, amount)
        return (opacity shl 24) or (r shl 16) or (g shl 8) or bl
    }

    private fun mix(channel: Int, fog: Float, amount: Float): Int =
        (channel + (fog * 255f - channel) * amount).roundToInt().coerceIn(0, 255)
}

internal class FogPoly {
    val x = DoubleArray(CAPACITY)
    val y = DoubleArray(CAPACITY)
    val z = DoubleArray(CAPACITY)
    val a = DoubleArray(CAPACITY)
    val b = DoubleArray(CAPACITY)
    val c = DoubleArray(CAPACITY)
    var n = 0

    fun add(x: Double, y: Double, z: Double, a: Double, b: Double) {
        add(x, y, z, a, b, HitboxFog.dominance(a, b))
    }

    fun add(x: Double, y: Double, z: Double, a: Double, b: Double, c: Double) {
        this.x[n] = x
        this.y[n] = y
        this.z[n] = z
        this.a[n] = a
        this.b[n] = b
        this.c[n] = c
        n++
    }

    fun add(from: FogPoly, i: Int) {
        add(from.x[i], from.y[i], from.z[i], from.a[i], from.b[i], from.c[i])
    }

    fun refreshDominance() {
        for (i in 0 until n) c[i] = HitboxFog.dominance(a[i], b[i])
    }

    fun distance(channel: Int): DoubleArray = when (channel) {
        0 -> a
        1 -> b
        else -> c
    }

    fun straddles(channel: Int, cut: Double): Boolean {
        val d = distance(channel)
        var lo = d[0]
        var hi = d[0]
        for (i in 1 until n) {
            lo = min(lo, d[i])
            hi = max(hi, d[i])
        }
        return lo < cut && hi > cut
    }

    fun split(channel: Int, cut: Double, below: FogPoly, above: FogPoly) {
        below.n = 0
        above.n = 0
        val d = distance(channel)
        for (i in 0 until n) {
            val j = if (i + 1 == n) 0 else i + 1
            val di = d[i]
            val dj = d[j]
            if (di <= cut) below.add(this, i)
            if (di >= cut) above.add(this, i)
            if ((di < cut && dj > cut) || (di > cut && dj < cut)) {
                val t = (cut - di) / (dj - di)
                val px = x[i] + (x[j] - x[i]) * t
                val py = y[i] + (y[j] - y[i]) * t
                val pz = z[i] + (z[j] - z[i]) * t
                val pa = a[i] + (a[j] - a[i]) * t
                val pb = b[i] + (b[j] - b[i]) * t
                val pc = c[i] + (c[j] - c[i]) * t
                below.add(px, py, pz, pa, pb, pc)
                above.add(px, py, pz, pa, pb, pc)
            }
        }
    }

    companion object {
        private const val CAPACITY = 32

        const val DOMINANCE = 2
    }
}
