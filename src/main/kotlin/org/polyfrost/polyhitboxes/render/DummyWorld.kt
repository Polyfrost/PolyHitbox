package org.polyfrost.polyhitboxes.render

import cc.polyfrost.oneconfig.events.EventManager
import cc.polyfrost.oneconfig.events.event.Stage
import cc.polyfrost.oneconfig.events.event.TickEvent
import cc.polyfrost.oneconfig.libs.eventbus.Subscribe
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.entity.projectile.EntitySnowball
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.world.World
import net.minecraft.world.WorldProviderSurface
import net.minecraft.world.gen.ChunkProviderDebug

object DummyWorld : World(null, null, WorldProviderSurface(), null, true) {
    val ARROW = EntityArrow(DummyWorld)
    val SNOWBALL = EntitySnowball(DummyWorld, 0.0, 0.0, 0.0)
    val ITEM_FRAME = EntityItemFrame(DummyWorld, BlockPos.ORIGIN, EnumFacing.SOUTH)
    val ITEM = EntityItem(DummyWorld, 0.0, 0.0, 0.0, ItemStack(Items.diamond)).apply {
        rotationYaw = 0f
    }
    val ARMOR_STAND = EntityArmorStand(DummyWorld).apply {
        val b = dataWatcher.getWatchableObjectByte(10).toInt() or 4
        dataWatcher.updateObject(10, b.toByte()) // show hands
    }

    init {
        chunkProvider = createChunkProvider()
        EventManager.INSTANCE.register(this)
    }

    @Subscribe
    fun onTick(event: TickEvent) {
        if (event.stage != Stage.END) return
        ITEM.hoverStart += 0.05f
    }

    override fun createChunkProvider() = ChunkProviderDebug(this)
    override fun getRenderDistanceChunks() = 0
}