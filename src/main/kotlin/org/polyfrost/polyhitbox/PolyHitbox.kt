package org.polyfrost.polyhitbox

//#if FABRIC
import net.fabricmc.api.ClientModInitializer
//#elseif FORGE
//#if MC >= 1.16.5
//$$ import net.minecraftforge.eventbus.api.IEventBus
//$$ import net.minecraftforge.fml.common.Mod
//$$ import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
//$$ import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
//#else
//$$ import net.minecraftforge.fml.common.Mod
//$$ import net.minecraftforge.fml.common.event.FMLInitializationEvent
//#endif
//#endif

import dev.deftu.omnicore.api.client.client
import org.apache.logging.log4j.LogManager
import org.polyfrost.oneconfig.api.config.v1.ConfigManager

//#if FORGE-LIKE
//#if MC >= 1.16.5
//$$ @Mod(PolyHitboxConstants.ID)
//#else
//$$ @Mod(modid = PolyHitboxConstants.ID, version = PolyHitboxConstants.VERSION)
//#endif
//#endif
object PolyHitbox
//#if FABRIC
    : ClientModInitializer
//#endif
{
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

    //#if FABRIC
    override
    //#elseif FORGE && MC <= 1.12.2
    //$$ @Mod.EventHandler
    //#endif
    fun onInitializeClient(
        //#if FORGE-LIKE
        //#if MC >= 1.16.5
        //$$ event: FMLClientSetupEvent
        //#else
        //$$ event: FMLInitializationEvent
        //#endif
        //#endif
    ) {
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

    //#if FORGE && MC >= 1.16.5
    //$$ init {
    //$$     setupForgeEvents(FMLJavaModLoadingContext.get().modEventBus)
    //$$ }
    //#endif

    //#if FORGE-LIKE && MC >= 1.16.5
    //$$ private fun setupForgeEvents(modEventBus: IEventBus) {
    //$$     modEventBus.addListener(this::onInitializeClient)
    //$$ }
    //#endif

    fun getHitboxInfo(): HitboxInfo {
        return hitboxInfo
    }
}
