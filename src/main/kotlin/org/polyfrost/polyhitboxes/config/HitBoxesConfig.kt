package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.Button
import cc.polyfrost.oneconfig.config.annotations.Exclude
import cc.polyfrost.oneconfig.config.annotations.Page
import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.config.data.PageLocation
import net.minecraft.client.Minecraft
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
import kotlin.reflect.KClass

@Suppress("unused")
object HitBoxesConfig : Config(Mod(PolyHitBoxes.NAME, ModType.UTIL_QOL), "${PolyHitBoxes.MODID}.json") {

    @Exclude
    var configMap = HashMap<KClass<out Entity>, HitboxConfigWrapper>()

    @Button(name = "Reset All", text = "Reset")
    fun reset() {
        for (wrapper in configMap.values) {
            val hitboxConfig = wrapper.getOrNull ?: return
            hitboxConfig.reset()
        }
        passive.reset()
        hostile.reset()
        projectile.reset()
        other.reset()
    }

    @JvmField
    @Page(name = "Passive Entities", location = PageLocation.TOP)
    var passive = Global()

    @Hitbox(EntityArmorStand::class)
    @Page(name = "Armor Stand", location = PageLocation.TOP, category = "Passive Entities")
    var armorStand = HitboxConfiguration()

    @Hitbox(EntityBat::class)
    @Page(name = "Bat", location = PageLocation.TOP, category = "Passive Entities")
    var bat = HitboxConfiguration()

    @Hitbox(EntityChicken::class)
    @Page(name = "Chicken", location = PageLocation.TOP, category = "Passive Entities")
    var chicken = HitboxConfiguration()

    @Hitbox(EntityCow::class)
    @Page(name = "Cow", location = PageLocation.TOP, category = "Passive Entities")
    var cow = HitboxConfiguration()

    @Hitbox(EntityHorse::class)
    @Page(name = "Horse", location = PageLocation.TOP, category = "Passive Entities")
    var horse = HitboxConfiguration()

    @Hitbox(EntityMooshroom::class)
    @Page(name = "Mooshroom", location = PageLocation.TOP, category = "Passive Entities")
    var mooshroom = HitboxConfiguration()

    @Hitbox(EntityOcelot::class)
    @Page(name = "Ocelot", location = PageLocation.TOP, category = "Passive Entities")
    var ocelot = HitboxConfiguration()

    @Hitbox(EntityPig::class)
    @Page(name = "Pig", location = PageLocation.TOP, category = "Passive Entities")
    var pig = HitboxConfiguration()

    @JvmField
    @Hitbox(EntityPlayer::class)
    @Page(name = "Player", location = PageLocation.TOP, category = "Passive Entities")
    var player = HitboxConfiguration()

    @Hitbox(EntityRabbit::class)
    @Page(name = "Rabbit", location = PageLocation.TOP, category = "Passive Entities")
    var rabbit = HitboxConfiguration()

    @Hitbox(EntitySheep::class)
    @Page(name = "Sheep", location = PageLocation.TOP, category = "Passive Entities")
    var sheep = HitboxConfiguration()

    @Hitbox(EntitySquid::class)
    @Page(name = "Squid", location = PageLocation.TOP, category = "Passive Entities")
    var squid = HitboxConfiguration()

    @Hitbox(EntityVillager::class)
    @Page(name = "Villager", location = PageLocation.TOP, category = "Passive Entities")
    var villager = HitboxConfiguration()

    @Hitbox(EntityWolf::class)
    @Page(name = "Wolf", location = PageLocation.TOP, category = "Passive Entities")
    var wolf = HitboxConfiguration()

    // hostile
    @JvmField
    @Page(name = "Hostile Entities", location = PageLocation.TOP)
    var hostile = Global()

    @Hitbox(EntityCaveSpider::class)
    @Page(name = "Cave Spider", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    var caveSpider = HitboxConfiguration()

    @Hitbox(EntityCreeper::class)
    @Page(name = "Creeper", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    var creeper = HitboxConfiguration()

    @Hitbox(EntityGiantZombie::class)
    @Page(name = "Giant", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    var giant = HitboxConfiguration()

    @Hitbox(EntityGuardian::class)
    @Page(name = "Guardian", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    var guardian = HitboxConfiguration()

    @Hitbox(EntityIronGolem::class)
    @Page(name = "Iron Golem", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    var ironGolem = HitboxConfiguration()

    @Hitbox(EntitySilverfish::class)
    @Page(name = "Silverfish", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    var silverfish = HitboxConfiguration()

    @Page(name = "Skeleton", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    var skeleton = HitboxConfiguration()

    @Hitbox(EntitySlime::class)
    @Page(name = "Slime", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    var slime = HitboxConfiguration()

    @Hitbox(EntitySnowman::class)
    @Page(name = "Snow Golem", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    var snowGolem = HitboxConfiguration()

    @Hitbox(EntitySpider::class)
    @Page(name = "Spider", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    var spider = HitboxConfiguration()

    @Hitbox(EntityWitch::class)
    @Page(name = "Witch", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    var witch = HitboxConfiguration()

    @Hitbox(EntityZombie::class)
    @Page(name = "Zombie", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    var zombie = HitboxConfiguration()

    // nether
    @Hitbox(EntityBlaze::class)
    @Page(name = "Blaze", location = PageLocation.TOP, subcategory = "The Nether", category = "Hostile Entities")
    var blaze = HitboxConfiguration()

    @Hitbox(EntityGhast::class)
    @Page(name = "Ghast", location = PageLocation.TOP, subcategory = "The Nether", category = "Hostile Entities")
    var ghast = HitboxConfiguration()

    @Hitbox(EntityMagmaCube::class)
    @Page(name = "Magma Cube", location = PageLocation.TOP, subcategory = "The Nether", category = "Hostile Entities")
    var magmaCube = HitboxConfiguration()

    @Page(name = "Wither Skeleton", location = PageLocation.TOP, subcategory = "The Nether", category = "Hostile Entities")
    var witherSkeleton = HitboxConfiguration()

    @Hitbox(EntityPigZombie::class)
    @Page(name = "Zombie Pigman", location = PageLocation.TOP, subcategory = "The Nether", category = "Hostile Entities")
    var zombiePigman = HitboxConfiguration()

    // end
    @Hitbox(EntityEnderman::class)
    @Page(name = "Enderman", location = PageLocation.TOP, subcategory = "The End", category = "Hostile Entities")
    var enderman = HitboxConfiguration()

    @Hitbox(EntityEndermite::class)
    @Page(name = "Endermite", location = PageLocation.TOP, subcategory = "The End", category = "Hostile Entities")
    var endermite = HitboxConfiguration()

    @Hitbox(EntityDragon::class)
    @Page(name = "Ender Dragon", location = PageLocation.TOP, subcategory = "The End", category = "Hostile Entities")
    var enderDragon = HitboxConfiguration()

    @JvmField
    @Page(name = "Projectile Entities", location = PageLocation.TOP)
    var projectile = Global()

    @Hitbox(EntityArrow::class)
    @Page(name = "Arrow", location = PageLocation.TOP, category = "Projectiles")
    var arrow = HitboxConfiguration()

    @Hitbox(EntityEgg::class)
    @Page(name = "Egg", location = PageLocation.TOP, category = "Projectiles")
    var egg = HitboxConfiguration()

    @Hitbox(EntityFireball::class)
    @Page(name = "Fireball", location = PageLocation.TOP, category = "Projectiles")
    var fireball = HitboxConfiguration()

    @Hitbox(EntityFishHook::class)
    @Page(name = "Fish Hook", location = PageLocation.TOP, category = "Projectiles")
    var fishHook = HitboxConfiguration()

    @Hitbox(EntityPotion::class)
    @Page(name = "Potion", location = PageLocation.TOP, category = "Projectiles")
    var potion = HitboxConfiguration()

    @Hitbox(EntitySnowball::class)
    @Page(name = "Snowball", location = PageLocation.TOP, category = "Projectiles")
    var snowball = HitboxConfiguration()

    @JvmField
    @Page(name = "Others", location = PageLocation.TOP)
    var other = Global()

    @Page(name = "Self", location = PageLocation.TOP, category = "Others")
    var self = HitboxConfiguration()

    @Hitbox(EntityItem::class)
    @Page(name = "Item", location = PageLocation.TOP, category = "Others")
    var item = HitboxConfiguration()

    @Hitbox(EntityXPOrb::class)
    @Page(name = "XP Orb", location = PageLocation.TOP, category = "Others")
    var xpOrb = HitboxConfiguration()

    init {
        initialize()
        setupConditions()
    }

    /**
     * Sets the mod conditions, this will later be setup differently so that it is more efficient, this is it for now though.
     */
    private fun setupConditions() {
        for ((key, value) in optionNames) {
            if (key.contains("showHitbox")) continue
            if (key.contains("global")) continue
            val parent = value.parent as? HitboxConfiguration ?: continue
            val page = key.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            value.addDependency("$page.showHitbox") { parent.showHitbox }
        }
        for (declaredField in javaClass.declaredFields) {
            val hitboxAnnotation = ConfigUtils.findAnnotation(declaredField, Hitbox::class.java)
            val pageAnnotation = ConfigUtils.findAnnotation(declaredField, Page::class.java)
            if (hitboxAnnotation == null || pageAnnotation == null) continue
            configMap[hitboxAnnotation.value] = HitboxConfigWrapper(declaredField, pageAnnotation.category)
        }
    }

    fun getEntityType(entity: Entity): HitboxConfiguration {
        val hitboxConfigField = configMap[entity::class]
        if (hitboxConfigField != null) return hitboxConfigField.categoryOrDefault
        if (entity is EntitySkeleton) {
            return if (entity.skeletonType == 1) witherSkeleton else skeleton
        }
        if (entity == Minecraft.getMinecraft().thePlayer) return self
        for ((key, value) in configMap) {
            if (key.isInstance(entity)) return value.categoryOrDefault
        }
        return self
    }
}
