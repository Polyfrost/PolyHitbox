package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.config.elements.OptionSubcategory
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityHanging
import net.minecraft.entity.IProjectile
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityArrow
import org.polyfrost.polyhitboxes.render.DummyWorld
import org.polyfrost.polyhitboxes.render.HitboxPreview

private const val HIGH = 0
private const val MID = 1
private const val LOW = 2

enum class HitboxCategory(
    val display: String,
    val condition: (Entity) -> Boolean,
    val priority: Int = MID,
    val example: Entity? = null,
) {
    DEFAULT(
        display = "Default",
        condition = { true },
        priority = LOW
    ) {
        override var config: HitboxConfig
            get() = super.config
            set(value) {
                value.overwriteDefault = true
                super.config = value
            }

        override fun initOptions(options: MutableList<BasicOption>) {
            options[0] = ConfigSpacer // replace "overwrite default" with empty spacer
            super.initOptions(options)
        }
    },
    PLAYER(
        display = "Player",
        condition = { it is EntityPlayer }
    ),
    SELF(
        display = "Self",
        condition = { it is EntityPlayerSP && it.uniqueID == mc.thePlayer?.uniqueID },
        priority = HIGH
    ),
    ARROW(
        display = "Arrow",
        condition = { it is EntityArrow },
        example = DummyWorld.ARROW,
        priority = HIGH
    ),
    PROJECTILE(
        display = "Projectile",
        condition = { it is IProjectile },
        example = DummyWorld.SNOWBALL
    ),
    DECORATION(
        display = "Decoration",
        condition = { it is EntityHanging },
        example = DummyWorld.ITEM_FRAME,
    ),
    ARMOR_STAND(
        display = "Armor Stand",
        condition = { it is EntityArmorStand },
        example = DummyWorld.ARMOR_STAND,
    ),
    ITEM(
        display = "Item",
        condition = { it is EntityItem },
        example = DummyWorld.ITEM,
    ),
    ;

    companion object {
        private val sorted: List<HitboxCategory> = (entries - DEFAULT).sortedBy { it.priority }
        fun getHitboxConfig(entity: Entity): HitboxConfig =
            (sorted
                .find { category ->
                    category.condition(entity)
                }?.takeIf { category ->
                    category.config.overwriteDefault
                } ?: DEFAULT).config
    }

    private val hitboxPreview = HitboxPreview(this)
    private val subcategory by lazy {
        OptionSubcategory("", "").apply {
            options = ConfigUtils.getClassOptions(config)
            initOptions(options)
        }
    }
    open var config = HitboxConfig()

    open fun initOptions(options: MutableList<BasicOption>) {
        for (option in options) {
            if (option.name == "Overwrite Default") continue
            option.addDependency("Overwrite Default") { config.overwriteDefault }
            if (option.name == "Show Condition") continue
            option.addDependency("Show Condition") { config.showCondition != 3 }
        }
        options.add(0, ConfigSpacer)
        options.add(hitboxPreview)
    }

    fun draw(vg: Long, x: Int, y: Int, inputHandler: InputHandler) = subcategory.draw(vg, x, y, inputHandler)
    fun drawLast(vg: Long, x: Int, inputHandler: InputHandler) = subcategory.drawLast(vg, x, inputHandler)
}
