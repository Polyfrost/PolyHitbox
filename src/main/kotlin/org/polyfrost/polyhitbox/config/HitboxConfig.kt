package org.polyfrost.polyhitbox.config

import org.polyfrost.compose.render.PolyColor

/**
 * Per-category hitbox styling. Instances are held by [HitboxCategory] and are bound to the
 * OneConfig settings tree in [ModConfig], so mutating fields here reflects live config edits.
 */
class HitboxConfig {
    /** Show condition: 0 = Always, 1 = Toggled, 2 = Hovered, 3 = Never. */
    var showCondition = 0

    /** Overrides the [HitboxCategory.DEFAULT] [showCondition] for this category when enabled. */
    var overwriteLogic = false

    /** Overrides the [HitboxCategory.DEFAULT] styling for this category when enabled. */
    var overwriteVisuals = false

    /** Line style: 0 = Normal, 1 = Proportioned, 2 = Dashed. */
    var lineStyle = 0
    var dashFactor = 10

    var hoverColor = false

    var iframeColor = false

    var showSide = false
    var sideColor: PolyColor = PolyColor.rgba(255, 255, 255, 63)
    var sideHoverColor: PolyColor = PolyColor.rgba(255, 255, 255, 63)
    var sideIframeColor: PolyColor = PolyColor.rgba(255, 85, 85, 63)

    var showOutline = true
    var outlineColor: PolyColor = PolyColor.rgba(255, 255, 255, 255)
    var outlineHoverColor: PolyColor = PolyColor.rgba(255, 255, 255, 255)
    var outlineIframeColor: PolyColor = PolyColor.rgba(255, 85, 85, 255)
    var outlineThickness = 2f

    var showEyeHeight = true
    var eyeHeightColor: PolyColor = PolyColor.rgba(255, 0, 0, 255)
    var eyeHeightHoverColor: PolyColor = PolyColor.rgba(255, 0, 0, 255)
    var eyeHeightIframeColor: PolyColor = PolyColor.rgba(255, 85, 85, 255)
    var eyeHeightThickness = 2f

    var showViewRay = true
    var viewRayColor: PolyColor = PolyColor.rgba(0, 0, 255, 255)
    var viewRayHoverColor: PolyColor = PolyColor.rgba(0, 0, 255, 255)
    var viewRayIframeColor: PolyColor = PolyColor.rgba(255, 85, 85, 255)
    var viewRayThickness = 2f

    @JvmField var sideArgb = 0
    @JvmField var sideHoverArgb = 0
    @JvmField var sideIframeArgb = 0
    @JvmField var outlineArgb = 0
    @JvmField var outlineHoverArgb = 0
    @JvmField var outlineIframeArgb = 0
    @JvmField var eyeHeightArgb = 0
    @JvmField var eyeHeightHoverArgb = 0
    @JvmField var eyeHeightIframeArgb = 0
    @JvmField var viewRayArgb = 0
    @JvmField var viewRayHoverArgb = 0
    @JvmField var viewRayIframeArgb = 0

    fun resolveColors() {
        if (showSide) {
            sideArgb = sideColor.argb
            if (hoverColor) sideHoverArgb = sideHoverColor.argb
            if (iframeColor) sideIframeArgb = sideIframeColor.argb
        }
        if (showOutline) {
            outlineArgb = outlineColor.argb
            if (hoverColor) outlineHoverArgb = outlineHoverColor.argb
            if (iframeColor) outlineIframeArgb = outlineIframeColor.argb
        }
        if (showEyeHeight) {
            eyeHeightArgb = eyeHeightColor.argb
            if (hoverColor) eyeHeightHoverArgb = eyeHeightHoverColor.argb
            if (iframeColor) eyeHeightIframeArgb = eyeHeightIframeColor.argb
        }
        if (showViewRay) {
            viewRayArgb = viewRayColor.argb
            if (hoverColor) viewRayHoverArgb = viewRayHoverColor.argb
            if (iframeColor) viewRayIframeArgb = viewRayIframeColor.argb
        }
    }
}
