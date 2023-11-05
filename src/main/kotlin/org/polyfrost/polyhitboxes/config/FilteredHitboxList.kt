package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.elements.BasicOption
import org.polyfrost.polyhitboxes.config.configlist.ConfigEntry
import org.polyfrost.polyhitboxes.config.configlist.ConfigList

private val filterName = arrayOf("Player", "Passive", "Hostile", "Projectiles", "Self")

class FilteredHitboxList : ConfigList<FilteredHitboxConfig>() {
    override fun newConfig() = FilteredHitboxConfig()

    override fun getConfigName(hud: FilteredHitboxConfig) = filterName[hud.filterEntity]

    override fun postInitOptions(entry: ConfigEntry<FilteredHitboxConfig>, options: List<BasicOption>) {
        for (option in options) {
            if (option.name == "Show Hitbox") continue
            option.addDependency("Show Hitbox") { entry.config.showHitbox }
        }
    }
}