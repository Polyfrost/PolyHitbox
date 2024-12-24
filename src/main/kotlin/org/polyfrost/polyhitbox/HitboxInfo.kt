package org.polyfrost.polyhitbox

import org.polyfrost.polyui.color.rgba

class HitboxInfo {
    var showMode = ShowMode.ALWAYS
    var showEyeline = true
    var showViewRay = true
    var showOutline = true
    var showSides = true
    var accurate = false
    var isDashed = false
    var dashFactor = 10

    var outlineWidth = 1f
    var eyelineWidth = 1f
    var viewRayWidth = 1f

    private var outlineColorNormal = rgba(255, 255, 255, 1f)
    private var eyelineColorNormal = rgba(255, 0, 0, 1f)
    private var viewRayColorNormal = rgba(0, 0, 255, 1f)
    private var sidesColorNormal = rgba(255, 255, 255, 0.2f)

    private var differentColorOnHover = false
    private var outlineColorHover = rgba(255, 255, 255, 0.9f)
    private var eyelineColorHover = rgba(255, 0, 0, 0.9f)
    private var viewRayColorHover = rgba(0, 0, 255, 0.9f)
    private var sidesColorHover = rgba(255, 255, 255, 0.3f)

    var isTargetted = false

    fun getOutlineColor(hover: Boolean) = if (hover && differentColorOnHover) outlineColorHover else outlineColorNormal
    fun getEyelineColor(hover: Boolean) = if (hover && differentColorOnHover) eyelineColorHover else eyelineColorNormal
    fun getViewRayColor(hover: Boolean) = if (hover && differentColorOnHover) viewRayColorHover else viewRayColorNormal
    fun getSidesColor(hover: Boolean) = if (hover && differentColorOnHover) sidesColorHover else sidesColorNormal

    enum class ShowMode {
        ALWAYS,
        HOVERED,
        NEVER
    }
}