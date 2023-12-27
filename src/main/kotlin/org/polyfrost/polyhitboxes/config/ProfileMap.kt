package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityHanging
import net.minecraft.entity.IProjectile
import net.minecraft.entity.player.EntityPlayer
import org.polyfrost.polyhitboxes.render.DummyWorld

class ProfileMap {
    val profiles = mutableListOf<CategorizedHitboxConfig>()
    private val sorted: List<CategorizedHitboxConfig>

    init {
        "Player"<EntityPlayer>(priorityLowToHigh = 999)
        "Self" { it is EntityPlayerSP && it.uniqueID == mc.thePlayer?.uniqueID }
        "Arrow"(example = DummyWorld.ARROW)
        "Projectile"<IProjectile>(priorityLowToHigh = 999, example = DummyWorld.SNOWBALL)
        "Decoration"<EntityHanging>(example = DummyWorld.ITEM_FRAME)
        "Armor Stand"(example = DummyWorld.ARMOR_STAND)
        "Item"(example = DummyWorld.ITEM)

        sorted = profiles.sortedBy { it.priority }
    }

    private inline operator fun <reified T> String.invoke(priorityLowToHigh: Int = 1000, example: T? = null) = register(this, { it is T }, priorityLowToHigh, example as? Entity)
    private operator fun String.invoke(priorityLowToHigh: Int = 1000, example: Entity? = null, condition: (Entity) -> Boolean) = register(this, condition, priorityLowToHigh, example)
    private fun register(name: String, condition: (Entity) -> Boolean, priorityLowToHigh: Int, example: Entity?) {
        profiles.add(CategorizedHitboxConfig(name, condition, priorityLowToHigh, example))
    }

    fun load(map: Map<String, HitboxProfile>) {
        for (config in profiles) {
            config.load(map[config.name])
        }
    }

    fun save(): Map<String, HitboxProfile> =
        profiles.associate { config ->
            config.name to config.profile
        }

    fun getHitboxProfile(entity: Entity): HitboxProfile =
        sorted
            .find { config ->
                config.condition(entity)
            }?.profile
            ?.takeIf { profile ->
                profile.overwriteDefault
            } ?: dummy

    private val dummy = HitboxProfile()
}