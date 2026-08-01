package org.polyfrost.polyhitbox.config

import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.decoration.HangingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.arrow.AbstractArrow
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull

private const val HIGH = 0
private const val MID = 1
private const val LOW = 2

enum class HitboxCategory(
    val displayName: String,
    val condition: (Entity) -> Boolean,
    val priority: Int = MID,
    var config: HitboxConfig = HitboxConfig(),
) {
    DEFAULT("General", { true }, LOW),
    PLAYER("Player", { it is Player }),
    SELF("Self", { it is LocalPlayer }, HIGH),
    MOB("Mob", { it is Mob }),
    MONSTER("Monster", { it is Enemy }, HIGH),
    PROJECTILE("Projectile", { it is Projectile }),
    ARROW("Arrow", { it is AbstractArrow }, HIGH),
    FIREBALL("Fireball", { it is Fireball }, HIGH),
    WITHER_SKULL("Wither Skull", { it is WitherSkull }, HIGH),
    FRAMES("Frames", { it is HangingEntity }),
    ARMOR_STAND("Armor Stand", { it is ArmorStand }),
    ITEM("Item", { it is ItemEntity }),
    XP("XP", { it is ExperienceOrb });

    companion object {
        private val sortedByPriority: List<HitboxCategory> =
            (entries - DEFAULT).sortedBy { it.priority }

        fun match(entity: Entity): HitboxConfig? =
            sortedByPriority.firstOrNull { it.condition(entity) }?.config

        fun logicOf(matched: HitboxConfig?): HitboxConfig =
            if (matched != null && matched.overwriteLogic) matched else DEFAULT.config

        fun visualsOf(matched: HitboxConfig?): HitboxConfig =
            if (matched != null && matched.overwriteVisuals) matched else DEFAULT.config
    }
}
