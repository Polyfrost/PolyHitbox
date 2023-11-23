package org.polyfrost.polyhitboxes.config.tree

import cc.polyfrost.oneconfig.gui.pages.Page
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.dsl.drawRoundedRect

class HitboxTreeView : Page("Hitbox Tree View") {
    var selectedProfile: NamedHitbox = HitboxMainTree.all.hitbox
    var selectedIndex: Int = 0

    override fun draw(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {
        if (inputHandler.isAreaClicked(x + 100f, y + 100f, 100f, 200f)) {
            val dy = inputHandler.mouseY() - (y + 100f)
            val index = dy.toInt() / 32
            HitboxMainTree.all.click(index)?.let { clicked -> // todo: separate out expanding check
                selectedIndex = index
                selectedProfile = clicked
            }
        }

        vg.drawRoundedRect(x + 100, y + 100 + selectedIndex * 32, 100, 32, 10, 0x55FFFFFF)

        HitboxMainTree.all.drawBranch(vg, x + 100, y + 100, inputHandler)

    }

}