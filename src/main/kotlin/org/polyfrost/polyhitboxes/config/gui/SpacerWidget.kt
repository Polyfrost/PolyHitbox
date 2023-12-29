package org.polyfrost.polyhitboxes.config.gui

import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.utils.InputHandler

object SpacerWidget : BasicOption(null, null, "", "", "", "", 1) {
    override fun getHeight() = 48
    override fun draw(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {}
}