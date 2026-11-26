package org.polyfrost.polyhitbox.render

//? if >1.8.9
import net.minecraft.core.registries.BuiltInRegistries
//? if =1.8.9
//import net.minecraft.item.Item
import net.minecraft.world.entity.player.Player
import org.polyfrost.polyhitbox.config.ModConfig

object HeldItems {
    private var source: List<String>? = null
    private var ids: Set<String> = emptySet()
    private var lastHeldAt = 0L

    fun normalize(id: String): String {
        val s = id.trim().lowercase()
        return if (s.isEmpty() || ':' in s) s else "minecraft:$s"
    }

    fun active(player: Player): Boolean {
        val list = ModConfig.holdingItems
        if (list !== source) {
            source = list
            ids = list.map(::normalize).filter { it.isNotEmpty() }.toSet()
        }
        val now = System.currentTimeMillis()
        if (ids.isNotEmpty() && heldIds(player).any { it in ids }) {
            lastHeldAt = now
            return true
        }
        val keep = (ModConfig.keepShowingFor * 1000f).toLong()
        return keep > 0 && lastHeldAt > 0 && now - lastHeldAt <= keep
    }

    fun heldIds(player: Player): List<String> {
        //? if >1.8.9 {
        val hands = if (ModConfig.includeOffhand) listOf(player.mainHandItem, player.offhandItem) else listOf(player.mainHandItem)
        return hands
            .filter { !it.isEmpty }
            .map { BuiltInRegistries.ITEM.getKey(it.item).toString() }
        //?} else {
        /*val stack = player.itemInHand ?: return emptyList()
        val id = Item.REGISTRY.getKey(stack.item) ?: return emptyList()
        return listOf(id.toString())
        *///?}
    }
}
