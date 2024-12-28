package org.polyfrost.polyhitbox

import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityList
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import org.apache.logging.log4j.LogManager
import org.polyfrost.oneconfig.api.config.v1.ConfigManager

@Mod(
    modid = PolyHitbox.MODID,
    name = PolyHitbox.NAME,
    version = PolyHitbox.VERSION,
    modLanguageAdapter = "org.polyfrost.oneconfig.utils.v1.forge.KotlinLanguageAdapter"
)
object PolyHitbox {
    const val MODID = "@ID@"
    const val NAME = "@NAME@"
    const val VERSION = "@VER@"
    private val LOGGER = LogManager.getLogger("PolyHitbox")
    private val hitboxInfo = HitboxInfo("hitbox.yaml")
    private val hitboxMap = HashMap<Class<out Entity>, HitboxInfo>()

    private var hitboxesEnabled: Boolean
        get() = Minecraft.getMinecraft().renderManager.isDebugBoundingBox
        set(value) {
            Minecraft.getMinecraft().renderManager.isDebugBoundingBox = value
        }

    @Mod.EventHandler
    fun onFMLInit(event: FMLInitializationEvent) {
        hitboxInfo.tree.title = "PolyHitbox"
        hitboxInfo.tree = ConfigManager.active().register(hitboxInfo.tree)
        var enabled = false
        ConfigManager.active().gatherAll("hitbox").forEach {
            val name = it.id.substringBeforeLast('.').substringAfterLast('/')
            val cls = EntityList.stringToClassMapping[name]
            if (cls == null) {
                LOGGER.warn("Unknown entity class for name $name")
                return@forEach
            }
            val info = HitboxInfo(it.id)
            info.tree.overwrite(it)
            if (info.showMode != 2 /* NEVER */) enabled = true
            hitboxMap[cls] = info
        }
        if (hitboxInfo.showMode != 2) enabled = true
        hitboxesEnabled = enabled
    }

    fun getHitboxInfo(entity: Entity): HitboxInfo {
        val cls = entity::class.java
        return hitboxMap[cls] ?: hitboxMap[cls.superclass] ?: hitboxInfo
    }
}
