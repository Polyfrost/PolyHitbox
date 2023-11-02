package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.libs.universal.wrappers.UPlayer
import com.google.common.collect.ForwardingMap
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.monster.*
import net.minecraft.entity.passive.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.*

object ConfigMap : ForwardingMap<String, HitboxConfiguration>() {
    var innerMap = mutableMapOf<String, HitboxEntityType>()

    override fun put(key: String, value: HitboxConfiguration): HitboxConfiguration? {
        val hitboxConfig = innerMap[key] ?: return null
        val old = hitboxConfig.hitboxConfig
        hitboxConfig.hitboxConfig = value
        return old
    }

    override fun putAll(from: Map<out String, HitboxConfiguration>) {
        for ((key, value) in from) {
            put(key, value)
        }
    }

    override fun delegate() =
        innerMap.mapValuesTo(
            mutableMapOf()
        ) { (_, value) ->
            value.hitboxConfig
        }

    private val defaultBox: HitboxEntityType
    private var currentCategory: String = "General"
    private var currentSubCategory: String = ""

    init {
        startCategory("Passive Entities") // todo: json unable to load as global
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
        startSubCategory("Overworld")
        register<EntityCaveSpider>("Cave Spider")
        register<EntityCreeper>("Creeper")
        register<EntityGiantZombie>("Giant")
        register<EntityGuardian>("Guardian")
        register<EntityIronGolem>("Iron Golem")
        register<EntitySilverfish>("Silverfish")
        registerSkeleton("Skeleton")
        register<EntitySlime>("Slime")
        register<EntitySnowman>("Snow Golem")
        register<EntitySpider>("Spider")
        register<EntityWitch>("Witch")
        register<EntityZombie>("Zombie")

        startSubCategory("The Nether")
        register<EntityBlaze>("Blaze")
        register<EntityGhast>("Ghast")
        register<EntityMagmaCube>("Magma Cube")
        registerSkeleton("Wither Skeleton", isWither = true)
        register<EntityPigZombie>("Zombie Pigman")

        startSubCategory("The End")
        register<EntityEnderman>("Enderman")
        register<EntityEndermite>("Endermite")
        register<EntityDragon>("Ender Dragon")

        startCategory("Projectiles")
        register<EntityArrow>("Arrow")
        register<EntityEgg>("Egg")
        register<EntityFireball>("Fireball")
        register<EntityFishHook>("Fish Hook")
        register<EntityPotion>("Potion")
        register<EntitySnowball>("Snowball")

        startCategory("Others")
        registerSelf("Self")
        register<EntityItem>("Item")
        register<EntityXPOrb>("XP Orb")
    }

    private fun startCategory(category: String) {
        currentCategory = category
        currentSubCategory = ""
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

    private fun register(name: String, entityClass: Class<*>): HitboxEntityType {
        val hitboxEntityType = HitboxEntityType(
            category = currentCategory,
            subcategory = currentSubCategory,
            entityClass = entityClass
        )
        innerMap[name] = hitboxEntityType
        return hitboxEntityType
    }

    private inline fun <reified T> register(name: String) = register(name, T::class.java)

    private fun registerSkeleton(name: String, isWither: Boolean = false): HitboxEntityType {
        val hitboxEntityType = object : HitboxEntityType(
            category = currentCategory,
            subcategory = currentSubCategory,
            entityClass = EntitySkeleton::class.java
        ) {
            override fun isInstance(entity: Any): Boolean {
                if (entity !is EntitySkeleton) return false
                return (entity.skeletonType == 1) == isWither
            }
        }
        innerMap[name] = hitboxEntityType
        return hitboxEntityType
    }

    private fun registerSelf(name: String): HitboxEntityType {
        val hitboxEntityType = object : HitboxEntityType(
            category = currentCategory,
            subcategory = currentSubCategory,
            entityClass = EntityPlayerSP::class.java
        ) {
            override fun isInstance(entity: Any) = entity == UPlayer.getPlayer()
        }
        innerMap[name] = hitboxEntityType
        return hitboxEntityType
    }

    fun getEntityType(entity: Entity) =
        innerMap.values.firstOrNull { hitboxEntityType ->
            hitboxEntityType.isInstance(entity)
        }?.getOrGlobal() ?: defaultBox.hitboxConfig

    open class HitboxEntityType(
        val category: String,
        val subcategory: String,
        var hitboxConfig: HitboxConfiguration = HitboxConfiguration(),
        val entityClass: Class<*>?,
    ) {
        open fun isInstance(entity: Any) = entityClass?.isInstance(entity) ?: false
        fun getOrGlobal() = global?.takeIf { it.global } ?: hitboxConfig
        private val global = innerMap[category]?.hitboxConfig as? Global
    }
}
