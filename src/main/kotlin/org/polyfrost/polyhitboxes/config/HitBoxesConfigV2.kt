package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.Button
import cc.polyfrost.oneconfig.config.annotations.CustomOption
import cc.polyfrost.oneconfig.config.annotations.Exclude
import cc.polyfrost.oneconfig.config.annotations.Page
import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.config.data.PageLocation
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.config.elements.OptionPage
import cc.polyfrost.oneconfig.gui.elements.config.ConfigPageButton
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.monster.*
import net.minecraft.entity.passive.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.*
import org.polyfrost.polyhitboxes.PolyHitBoxes
import java.lang.reflect.Field

@Suppress("unused")
object HitBoxesConfigV2 : Config(Mod(PolyHitBoxes.NAME, ModType.UTIL_QOL), "${PolyHitBoxes.MODID}.json"), MutableMap<String, HitboxConfiguration> {

    @Exclude
    var innerMap = HashMap<String, HitboxEntityType>()

    init {
        initialize()
        registerAll()
        setupConditions()
    }

    private lateinit var defaultBox: HitboxEntityType

    private fun registerAll() {
        startCategory("Passive Entities")
        register<EntityArmorStand>("Armor Stand")
        register<EntityBat>("Bat")
        register<EntityChicken>("Chicken")
        register<EntityCow>("Cow")
        register<EntityHorse>("Horse")
        register<EntityMooshroom>("Mooshroom")
        register<EntityOcelot>("Ocelot")
        register<EntityPig>("Pig")
        defaultBox = register<EntityPlayer>("Player")
        register<EntityRabbit>("Rabbit")
        register<EntitySheep>("Sheep")
        register<EntitySquid>("Squid")
        register<EntityVillager>("Villager")
        register<EntityWolf>("Wolf")

        startCategory("Hostile Entities")
        register<EntityCaveSpider>("Cave Spider")
        register<EntitySquid>("Squid")
        TODO("finish registering")
    }

    private fun setupConditions() {
        for ((key, value) in optionNames) {
            if (key.contains("showHitbox")) continue
            if (key.contains("global")) continue
            val parent = value.parent as? HitboxConfiguration ?: continue
            val page = key.split(".").getOrNull(0) ?: continue
            value.addDependency("$page.showHitbox") { parent.showHitbox }
        }
    }

    fun getEntityType(entity: Entity) =
        innerMap.values.firstOrNull { hitboxEntityType ->
            hitboxEntityType.isInstance(entity)
        }?.getOrGlobal()

    override fun getCustomOption(field: Field, annotation: CustomOption, page: OptionPage, mod: Mod, migrate: Boolean): BasicOption? {
        for ((key, value) in innerMap) {
            val subcategory = ConfigUtils.getSubCategory(page, value.category, value.subcategory)
            val newPage = OptionPage(key, mod)
            generateOptionList(value, newPage, mod, migrate)
            val button = ConfigPageButton(null, null, key, "", value.category, value.subcategory, newPage)
            subcategory.topButtons.add(button)
        }
        return null
    }

    private var currentCategory: String = "General"
    private var currentSubCategory: String = ""

    private inline fun <reified T> register(name: String) = register(name, T::class.java)

    private fun register(name: String, entityClass: Class<*>): HitboxEntityType {
        val hitboxEntityType = HitboxEntityType(
            category = currentCategory,
            subcategory = currentSubCategory,
            entityClass = entityClass
        )
        innerMap[name] = hitboxEntityType
        return hitboxEntityType
    }

    private fun startCategory(category: String) {
        currentCategory = category
        innerMap[category] = HitboxEntityType(
            category = category,
            subcategory = currentSubCategory,
            hitboxConfig = Global(),
            entityClass = null,
        )
    }

    private fun startSubCategory(subcategory: String) {
        currentSubCategory = subcategory
    }

    override val keys get() = innerMap.keys
    override val size get() = innerMap.size
    override fun isEmpty() = innerMap.isEmpty()

    override fun remove(key: String): HitboxConfiguration? {
        val hitboxConfig = innerMap[key] ?: return null
        val inner = hitboxConfig.hitboxConfig
        inner.reset()
        return inner
    }

    override fun putAll(from: Map<out String, HitboxConfiguration>) {
        for ((key, value) in from) {
            put(key, value)
        }
    }

    override fun put(key: String, value: HitboxConfiguration): HitboxConfiguration? {
        val hitboxConfig = innerMap[key] ?: return null
        val old = hitboxConfig.hitboxConfig
        hitboxConfig.hitboxConfig = value
        return old
    }

    override fun get(key: String) = innerMap[key]?.hitboxConfig
    override fun containsKey(key: String) = innerMap.containsKey(key)

    override val entries
        get() = innerMap.mapValues { (_, value) ->
            value.hitboxConfig
        }.toMutableMap().entries

    override val values
        get() = innerMap.values.map {
            it.hitboxConfig
        }.toMutableList()

    override fun clear() {
        for ((_, value) in innerMap) {
            value.hitboxConfig.reset()
        }
    }

    override fun containsValue(value: HitboxConfiguration) =
        innerMap.values.any {
            it.hitboxConfig == value
        }

    data class HitboxEntityType(
        val category: String,
        val subcategory: String,
        var hitboxConfig: HitboxConfiguration = HitboxConfiguration(),
        val entityClass: Class<*>?,
    ) {
        fun isInstance(any: Any) = entityClass?.isInstance(any) ?: false
        fun getOrGlobal() = global?.takeIf { it.global } ?: hitboxConfig
        private val global = innerMap[category]?.hitboxConfig as? Global
    }
}
