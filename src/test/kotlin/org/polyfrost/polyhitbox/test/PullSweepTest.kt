package org.polyfrost.polyhitbox.test

//? if >=26.2 {
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.state.level.CameraRenderState
//?}
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.polyhitbox.render.BlockBoundsCache
//? if >=26.2 {
import org.polyfrost.polyhitbox.render.HitboxRenderer
import java.lang.reflect.Proxy
//?}
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

    @Test
    fun `block cache keeps every entry through growth and forgets them on clear`() {
        val cache = BlockBoundsCache()
        val out = DoubleArray(6)
        val positions = (-20..20).flatMap { x -> (-3..3).flatMap { y -> (-20..20).map { z -> Triple(x, y * 600, z) } } }
        for ((x, y, z) in positions) cache.put(BlockBoundsCache.pack(x, y, z), doubleArrayOf(x.toDouble(), y.toDouble(), z.toDouble(), 0.0, 0.0, 0.0))
        for ((x, y, z) in positions) {
            assertTrue(cache.get(BlockBoundsCache.pack(x, y, z), out))
            assertArrayEquals(doubleArrayOf(x.toDouble(), y.toDouble(), z.toDouble(), 0.0, 0.0, 0.0), out)
        }
        assertFalse(cache.get(BlockBoundsCache.pack(21, 0, 0), out))
        cache.clear()
        for ((x, y, z) in positions) assertFalse(cache.get(BlockBoundsCache.pack(x, y, z), out))
        for ((x, y, z) in positions) cache.put(BlockBoundsCache.pack(x, y, z), doubleArrayOf(0.0, 0.0, 0.0, x.toDouble(), y.toDouble(), z.toDouble()))
        for ((x, y, z) in positions) {
            assertTrue(cache.get(BlockBoundsCache.pack(x, y, z), out))
            assertArrayEquals(doubleArrayOf(0.0, 0.0, 0.0, x.toDouble(), y.toDouble(), z.toDouble()), out)
        }
    }

    //? if >=26.2 {
    @Test
    fun `submitting a frame forgets cached blocks`() {
        val cache = HitboxRenderer::class.java.getDeclaredField("blockCache").apply { isAccessible = true }.get(null) as BlockBoundsCache
        val key = BlockBoundsCache.pack(1, 2, 3)
        val out = DoubleArray(6)
        cache.put(key, DoubleArray(6))
        assertTrue(cache.get(key, out))
        val collector = Proxy.newProxyInstance(SubmitNodeCollector::class.java.classLoader, arrayOf(SubmitNodeCollector::class.java)) { _, method, _ ->
            error("Unexpected ${method.name}")
        } as SubmitNodeCollector
        HitboxRenderer.submitHitboxes(CameraRenderState(), collector)
        assertFalse(cache.get(key, out))
    }
    //?}

    @Test
    fun `packed block keys stay distinct across the whole packable range`() {
        val horizontal = listOf(-(1 shl 25), -(1 shl 25) + 1, -(1 shl 24), -1025, -1024, -1, 0, 1, 1023, 1024, 1 shl 24, (1 shl 25) - 1)
        val vertical = listOf(-2048, -2047, -1, 0, 1, 2047)
        val positions = horizontal.flatMap { x -> vertical.flatMap { y -> horizontal.map { z -> Triple(x, y, z) } } }
        for ((x, y, z) in positions) assertTrue(BlockBoundsCache.packable(x, y, z))
        assertEquals(positions.size, positions.map { (x, y, z) -> BlockBoundsCache.pack(x, y, z) }.toSet().size)

        val cache = BlockBoundsCache()
        val out = DoubleArray(6)
        for ((x, y, z) in positions) cache.put(BlockBoundsCache.pack(x, y, z), doubleArrayOf(x.toDouble(), y.toDouble(), z.toDouble(), 1.0, 1.0, 1.0))
        for ((x, y, z) in positions) {
            assertTrue(cache.get(BlockBoundsCache.pack(x, y, z), out))
            assertArrayEquals(doubleArrayOf(x.toDouble(), y.toDouble(), z.toDouble(), 1.0, 1.0, 1.0), out)
        }

        assertFalse(BlockBoundsCache.packable(1 shl 25, 0, 0))
        assertFalse(BlockBoundsCache.packable(-(1 shl 25) - 1, 0, 0))
        assertFalse(BlockBoundsCache.packable(0, 0, 1 shl 25))
        assertFalse(BlockBoundsCache.packable(0, 0, -(1 shl 25) - 1))
        assertFalse(BlockBoundsCache.packable(0, 2048, 0))
        assertFalse(BlockBoundsCache.packable(0, -2049, 0))
    }
}
