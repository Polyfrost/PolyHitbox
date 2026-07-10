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
// YALMM backports modern (26.2) mappings to 1.21.1-1.21.10, so these packages are uniform.
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
    ARROW("Arrow", { it is AbstractArrow }, HIGH),
    FIREBALL("Fireball", { it is Fireball }),
    PROJECTILE("Projectile", { it is Projectile }),
    WITHER_SKULL("Wither Skull", { it is WitherSkull }, HIGH),
    FRAMES("Frames", { it is HangingEntity }),
    ARMOR_STAND("Armor Stand", { it is ArmorStand }),
    ITEM("Item", { it is ItemEntity }),
    XP("XP", { it is ExperienceOrb });

    companion object {
        private val sortedByPriority: List<HitboxCategory> =
            (entries - DEFAULT).sortedBy { it.priority }

        /**
         * Resolves the styling to use for [entity]: the highest-priority matching category that
         * overrides the default, otherwise the [DEFAULT] styling.
         */
        fun resolve(entity: Entity): HitboxConfig {
            val matched = sortedByPriority.firstOrNull { it.condition(entity) }?.config
            return if (matched != null && matched.overwriteDefault) matched else DEFAULT.config
        }
    }
}
