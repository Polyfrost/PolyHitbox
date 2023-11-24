package org.polyfrost.polyhitboxes.config.tree

import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.gui.animations.EaseInOutQuad
import cc.polyfrost.oneconfig.gui.pages.Page
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.dsl.drawRoundedRect

class HitboxTreeView : Page("Hitbox Tree View") {
    var selectedProfile: NamedHitbox = HitboxMainTree.all.hitbox
    var selectionAnimation = EaseInOutQuad(0, 0f, 0f, false)
    var options: List<BasicOption> = ConfigUtils.getClassOptions(HitboxMainTree.all.savedHitbox)

    override fun draw(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {
        if (inputHandler.isAreaClicked(x + 20f, y + 20f, 100f, 200f)) {
            val dy = inputHandler.mouseY() - (y + 20f)
            val index = dy.toInt() / 32

            HitboxMainTree.all.find(index)?.let { clicked ->
                selectionAnimation = EaseInOutQuad(100, selectionAnimation.get(), index.toFloat(), false)
                selectedProfile = clicked

                options = ConfigUtils.getClassOptions(selectedProfile.savedHitbox)
            }
        }

        vg.drawRoundedRect(x + 20, y + 20 + selectionAnimation.get() * 32, 100, 32, 10, 0xFF2A2C30.toInt())

        HitboxMainTree.all.drawBranch(vg, x + 20, y + 20, inputHandler)

        var optionY = y + 16
        for (option in options) {
            option.draw(vg, x + 150, optionY, inputHandler)
            optionY += option.height + 16
        }
    }

}