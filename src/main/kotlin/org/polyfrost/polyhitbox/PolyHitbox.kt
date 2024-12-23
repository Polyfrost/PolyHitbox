package org.polyfrost.polyhitbox

import net.minecraft.entity.Entity
import net.minecraftforge.fml.common.Mod

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
    private val hitboxInfo = HitboxInfo()
    private val hitboxMap = HashMap<Entity, HitboxInfo>()

    fun getHitboxInfo(entity: Entity): HitboxInfo {
        return hitboxInfo
    }
}
