package org.polyfrost.polyhitboxes.config.tree

import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.gui.animations.EaseInOutQuad
import cc.polyfrost.oneconfig.gui.pages.Page
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.dsl.drawRoundedRect
import org.polyfrost.polyhitboxes.PolyHitBoxes

private const val TREE_WIDTH = 200
private const val TREE_HEIGHT = 800

class HitboxTreePage : Page("Hitbox Tree View") {
    var selectedProfile: NamedHitbox = HitboxMainTree.all.hitbox
    var selectionAnimation = EaseInOutQuad(0, 0f, 0f, false)
    var options: List<BasicOption> = ConfigUtils.getClassOptions(HitboxMainTree.all.savedHitbox)

    override fun draw(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {
        if (inputHandler.isAreaClicked(x + 20f, y + 20f, TREE_WIDTH.toFloat(), TREE_HEIGHT.toFloat())) {
            val dy = inputHandler.mouseY() - (y + 20f)
            val index = dy.toInt() / BRANCH_HEIGHT

            HitboxMainTree.all.find(index)?.takeIf { it != selectedProfile }?.let { clicked ->
                selectionAnimation = EaseInOutQuad(100, selectionAnimation.get(), index.toFloat(), false)
                selectedProfile = clicked
                options = ConfigUtils.getClassOptions(selectedProfile.savedHitbox)
                options[0].addDependency("Inherited from parent") { true }
            }
        }

        vg.drawRoundedRect(x + 20, y + 20 + selectionAnimation.get() * BRANCH_HEIGHT, TREE_WIDTH, BRANCH_HEIGHT, 10, 0xFF2A2C30.toInt())

        HitboxMainTree.all.drawBranch(vg, x + 20, y + 20, inputHandler)

        var optionY = y + 16
        for (option in options) {
            option.draw(vg, x + 20 + TREE_WIDTH + 30, optionY, inputHandler)
            optionY += option.height + 16
        }

        PolyHitBoxes.previewHitbox = selectedProfile.savedHitbox
    }

}