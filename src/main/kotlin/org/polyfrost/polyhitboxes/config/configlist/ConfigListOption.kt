package org.polyfrost.polyhitboxes.config.configlist

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.gui.elements.BasicButton
import cc.polyfrost.oneconfig.renderer.asset.SVG
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.color.ColorPalette

private val PLUS_ICON = SVG("/assets/plus.svg")

@Suppress("UnstableAPIUsage")
class ConfigListOption<T>(
    val configList: ConfigList<T>,
    val config: Config,
    description: String,
    category: String,
    subcategory: String,
) : BasicOption(null, null, "", description, category, subcategory, 2) {
    private val addButton = BasicButton(32, 32, PLUS_ICON, BasicButton.ALIGNMENT_CENTER, ColorPalette.PRIMARY)
    private val configEntryList = configList.mapTo(ArrayList()) { hud ->
        ConfigEntry(this, hud)
    }
    private var planToRemove: ConfigEntry<T>? = null

    init {
        addButton.setClickAction {
            val hud = configList.newConfig()
            configEntryList.add(ConfigEntry(this, hud))
            configList.add(hud)
        }
    }

    override fun getHeight() = configEntryList.size * 48 + 32

    override fun draw(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {
        var nextY = y

        for (configEntry in configEntryList) {
            configEntry.drawInList(vg, x, nextY, inputHandler)
            nextY += 48
        }

        addButton.draw(vg, x.toFloat(), nextY.toFloat(), inputHandler)

        checkToRemove()
    }

    fun planToRemove(configEntry: ConfigEntry<T>) {
        planToRemove = configEntry
    }

    private fun checkToRemove() {
        val removing = (planToRemove ?: return)
        configEntryList.remove(removing)
        configList.remove(removing.config)
        planToRemove = null
    }
}