package org.polyfrost.polyhitbox

import dev.deftu.omnicore.api.client.client
import org.apache.logging.log4j.LogManager
import org.polyfrost.oneconfig.api.config.v1.ConfigManager

//#if FORGE
//$$ @net.minecraftforge.fml.common.Mod(
//#if MC >=1.20.1 || MC <=1.12.2
//$$     modid = PolyHitbox.MODID,
//$$     name = PolyHitbox.NAME,
//$$     version = PolyHitbox.VERSION,
//$$     modLanguageAdapter = "org.polyfrost.oneconfig.utils.v1.forge.KotlinLanguageAdapter"
//#else
//$$     value = PolyHitbox.MODID
//#endif
//$$ )
//#endif
object PolyHitbox
//#if FABRIC
    : net.fabricmc.api.ClientModInitializer
//#endif
{
    const val MODID = "@MOD_ID@"
    const val NAME = "@MOD_NAME@"
    const val VERSION = "@MOD_VERSION@"
    private val LOGGER = LogManager.getLogger("PolyHitbox")
    private val hitboxInfo = HitboxInfo("hitbox.yaml")

    private var hitboxesEnabled: Boolean
        get() {
            //#if MC < 1.14
            //$$ return client.renderManager.isDebugBoundingBox
            //#else
            return client.entityRenderDispatcher.shouldRenderHitboxes()
            //#endif
        }
        set(value) {
            //#if MC < 1.14
            //$$ client.renderManager.isDebugBoundingBox = value
            //#else
            client.entityRenderDispatcher.setRenderHitboxes(value)
            //#endif
        }

    fun initialize() {
        hitboxInfo.tree.title = "PolyHitbox"
        hitboxInfo.tree = ConfigManager.active().register(hitboxInfo.tree).tree
        var enabled = false
        if (hitboxInfo.showMode != 2) {
            enabled = true
        }

        //#if FABRIC && MC > 1.14
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STARTED.register { tick ->
            hitboxesEnabled = enabled
        }
        //#else
        //hitboxesEnabled = enabled
        //#endif
    }

    // TODO: Fix 1.16.5 Forge (idk what changed)
    //#if FORGE
    //$$ @net.minecraftforge.fml.common.Mod.EventHandler
    //$$ fun onFMLInit(event: net.minecraftforge.fml.common.event.FMLInitializationEvent) {
    //$$     initialize()
    //$$ }
    //#else
    override fun onInitializeClient() {
        initialize()
    }
    //#endif

    fun getHitboxInfo(): HitboxInfo {
        return hitboxInfo
    }
}
