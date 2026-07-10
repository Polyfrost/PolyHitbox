package org.polyfrost.polyhitbox.config

import org.polyfrost.compose.render.PolyColor

/**
 * Per-category hitbox styling. Instances are held by [HitboxCategory] and are bound to the
 * OneConfig settings tree in [ModConfig], so mutating fields here reflects live config edits.
 */
class HitboxConfig {
    /** Show condition: 0 = Always, 1 = Toggled, 2 = Hovered, 3 = Never. */
    var showCondition = 1

    /** Overrides the [HitboxCategory.DEFAULT] styling for this category when enabled. */
    var overwriteDefault = false

    /** Line style: 0 = Normal, 1 = Proportioned, 2 = Dashed. */
    var lineStyle = 0
    var dashFactor = 10

    var accurate = true
    var hoverColor = false

    var showSide = false
    var sideColor: PolyColor = PolyColor.rgba(255, 255, 255, 63)
    var sideHoverColor: PolyColor = PolyColor.rgba(255, 255, 255, 63)

    var showOutline = true
    var outlineColor: PolyColor = PolyColor.rgba(255, 255, 255, 255)
    var outlineHoverColor: PolyColor = PolyColor.rgba(255, 255, 255, 255)
    var outlineThickness = 2f

    var showEyeHeight = true
    var eyeHeightColor: PolyColor = PolyColor.rgba(255, 0, 0, 255)
    var eyeHeightHoverColor: PolyColor = PolyColor.rgba(255, 0, 0, 255)
    var eyeHeightThickness = 2f

    var showViewRay = true
    var viewRayColor: PolyColor = PolyColor.rgba(0, 0, 255, 255)
    var viewRayHoverColor: PolyColor = PolyColor.rgba(0, 0, 255, 255)
    var viewRayThickness = 2f
}
