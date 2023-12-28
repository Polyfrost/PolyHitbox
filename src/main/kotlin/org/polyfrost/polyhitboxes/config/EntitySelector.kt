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
    private val categoryDropdown = ConfigDropdown(::selectedIndex.javaField, this, "Category", "", category, subcategory, 1, getCategoryNames())
    private val selectedIndex = 0
    private var lastHeight = 0

    override fun getHeight() = lastHeight

    override fun draw(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {
        lastHeight = getSelectedConfig()?.draw(vg, x, y - 16, inputHandler) ?: 0
        categoryDropdown.draw(vg, x, y, inputHandler)
    }

    override fun drawLast(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {
        super.drawLast(vg, x, y, inputHandler)
        getSelectedConfig()?.drawLast(vg, x, inputHandler)
        categoryDropdown.drawLast(vg, x, y, inputHandler)
    }

    private fun getSelectedConfig() = HitboxCategory.entries.getOrNull(selectedIndex)
    private fun getCategoryNames() = HitboxCategory.entries.map { config -> config.display }.toTypedArray()
}