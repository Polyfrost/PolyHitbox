package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.gui.elements.config.ConfigDropdown
import cc.polyfrost.oneconfig.utils.InputHandler
import kotlin.reflect.jvm.javaField

class EntitySelector(
    description: String = "",
    category: String = "General",
    subcategory: String = "",
) : BasicOption(null, null, "Entity Selector", description, category, subcategory, 2) {
    val profileMap = ProfileMap()
    private val selectedIndex = 0
    private val categoryDropdown = ConfigDropdown(::selectedIndex.javaField, this, "Category", "", category, subcategory, 2, getCategoryNames())
    private var lastHeight = 0

    override fun getHeight() = lastHeight + 48

    override fun draw(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {
        categoryDropdown.draw(vg, x, y, inputHandler)
        lastHeight = getSelectedConfig()?.draw(vg, x, y + 48, inputHandler) ?: 0
    }

    override fun drawLast(vg: Long, x: Int, y: Int, inputHandler: InputHandler?) {
        categoryDropdown.drawLast(vg, x, y, inputHandler)
        super.drawLast(vg, x, y, inputHandler)
    }

    fun getSelectedConfig() = profileMap.profiles.getOrNull(selectedIndex)

    private fun getCategoryNames() = profileMap.profiles.map { config -> config.name }.toTypedArray()
}