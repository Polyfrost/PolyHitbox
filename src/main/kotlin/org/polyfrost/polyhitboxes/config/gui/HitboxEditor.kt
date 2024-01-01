package org.polyfrost.polyhitboxes.config.gui

import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.gui.elements.config.ConfigDropdown
import cc.polyfrost.oneconfig.utils.InputHandler
import org.polyfrost.polyhitboxes.config.data.HitboxCategory
import kotlin.reflect.jvm.javaField

class HitboxEditor(
    description: String = "",
    category: String = "General",
    subcategory: String = "",
) : BasicOption(null, null, "Entity Selector", description, category, subcategory, 2) {
    private val categoryNames get() = HitboxCategory.entries.map { config -> config.display }.toTypedArray()
    private val selectedConfig get() = HitboxCategory.entries.getOrNull(selectedIndex)
    private val categoryDropdown = ConfigDropdown(::selectedIndex.javaField, this, "Category", "", category, subcategory, 1, categoryNames)
    private val selectedIndex = 0
    private var lastHeight = 0

    override fun getHeight() = lastHeight

    override fun draw(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {
        lastHeight = selectedConfig?.draw(vg, x, y - 16, inputHandler) ?: 0
        categoryDropdown.draw(vg, x, y, inputHandler)
    }

    override fun drawLast(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {
        super.drawLast(vg, x, y, inputHandler)
        selectedConfig?.drawLast(vg, x, inputHandler)
        categoryDropdown.drawLast(vg, x, y, inputHandler)
    }

    override fun keyTyped(key: Char, keyCode: Int) {
        selectedConfig?.keyTyped(key, keyCode)
        categoryDropdown.keyTyped(key, keyCode)
    }
}