package org.polyfrost.polyhitboxes.config.configlist

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.config.elements.OptionPage

abstract class ConfigList<T> : ArrayList<T>() {
    abstract fun newConfig(): T
    abstract fun getConfigName(hud: T): String
    open fun postInitOptions(entry: ConfigEntry<T>, options: List<BasicOption>) {}

    fun addOptionTo(config: Config, page: OptionPage, description: String = "", category: String = "General", subcategory: String = ""): BasicOption {
        val option = ConfigListOption(this, config, description, category, subcategory)
        ConfigUtils.getSubCategory(page, category, subcategory).options.add(option)
        return option
    }
}