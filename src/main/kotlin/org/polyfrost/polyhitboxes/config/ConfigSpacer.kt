package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.utils.InputHandler

object ConfigSpacer : BasicOption(null, null, "", "", "", "", 1) {
    override fun getHeight() = 48
    override fun draw(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {}
}