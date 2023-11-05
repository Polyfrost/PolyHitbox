package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.annotations.Color
import cc.polyfrost.oneconfig.config.annotations.Dropdown
import cc.polyfrost.oneconfig.config.annotations.Slider
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.libs.universal.wrappers.UPlayer
import net.minecraft.entity.Entity
import net.minecraft.entity.INpc
import net.minecraft.entity.IProjectile
import net.minecraft.entity.monster.IMob
import net.minecraft.entity.passive.EntityAmbientCreature
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityWaterMob
import net.minecraft.entity.player.EntityPlayer

open class FilteredHitboxConfig : HitboxConfig {
    @Dropdown(
        name = "Filter Entity",
        options = [
            "Player",
            "Passive",
            "Hostile",
            "Projectiles",
            "Self"
        ]
    )
    var filterEntity = 0

    @Switch(name = "Show Hitbox")
    override var showHitbox = false

    @Switch(name = "Accurate Hitboxes")
    override var accurate = false

    @Switch(name = "Dashed")
    override var dashedHitbox = false

    @Switch(name = "Hitbox Outline")
    override var showOutline = true

    @Switch(name = "Eye Height")
    override var showEyeHeight = true

    @Switch(name = "Look Vector")
    override var showLookVector = true

    @Slider(name = "Outline Thickness", min = 1f, max = 5f)
    override var outlineThickness = 2f

    @Slider(name = "Eye Height Thickness", min = 1f, max = 5f)
    override var eyeHeightThickness = 2f

    @Slider(name = "Look Vector Thickness", min = 1f, max = 5f)
    override var lookVectorThickness = 2f

    @Color(name = "Hitbox Outline Color", size = 2)
    override var outlineColor = OneColor(-1)

    @Color(name = "Eye Height Color", size = 2)
    override var eyeHeightColor = OneColor(-0x10000)

    @Color(name = "Look Vector Color", size = 2)
    override var lookVectorColor = OneColor(-0xffff01)

    fun passFilter(entity: Entity) = when (filterEntity) {
        0 -> entity is EntityPlayer
        1 -> entity is EntityAnimal || entity is EntityAmbientCreature || entity is EntityWaterMob || entity is INpc
        2 -> entity is IMob
        3 -> entity is IProjectile
        4 -> entity == UPlayer.getPlayer()
        else -> false
    }
}
