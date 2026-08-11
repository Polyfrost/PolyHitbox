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
//? if >=1.21.11 {
import net.minecraft.world.entity.projectile.arrow.AbstractArrow
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull
//?} else {
/*import net.minecraft.world.entity.projectile.AbstractArrow
import net.minecraft.world.entity.projectile.Fireball
import net.minecraft.world.entity.projectile.WitherSkull
*///?}
import java.util.concurrent.ConcurrentHashMap

private const val HIGH = 0
private const val MID = 1
private const val LOW = 2

private const val NEVER = 3

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
        private val all: Array<HitboxCategory> = entries.toTypedArray()

        private val sortedByPriority: Array<HitboxCategory> =
            (entries - DEFAULT).sortedBy { it.priority }.toTypedArray()

        private val byClass = ConcurrentHashMap<Class<*>, HitboxCategory>()

        fun match(entity: Entity): HitboxConfig {
            val type = entity.javaClass
            var category = byClass[type]
            if (category == null) {
                category = resolve(entity)
                byClass[type] = category
            }
            return category.config
        }

        private fun resolve(entity: Entity): HitboxCategory {
            for (category in sortedByPriority) {
                if (category.condition(entity)) return category
            }
            return DEFAULT
        }

        fun logicOf(matched: HitboxConfig): HitboxConfig =
            if (matched.overwriteLogic) matched else DEFAULT.config

        fun visualsOf(matched: HitboxConfig): HitboxConfig =
            if (matched.overwriteVisuals) matched else DEFAULT.config

        fun anythingVisible(): Boolean {
            for (category in all) {
                val matched = category.config
                if (logicOf(matched).showCondition == NEVER) continue
                val visuals = visualsOf(matched)
                if (!visuals.showSide && !visuals.showOutline && !visuals.showEyeHeight && !visuals.showViewRay) continue
                return true
            }
            return false
        }

        fun resolveColors() {
            for (category in all) category.config.resolveColors()
        }
    }
}
