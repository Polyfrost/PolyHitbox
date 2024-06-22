package org.polyfrost.polyhitbox.config

import cc.polyfrost.oneconfig.config.annotations.*
import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.config.elements.BasicOption
import org.polyfrost.polyhitbox.render.HitboxPreview

class HitboxConfig {
    @Switch(name = "Enable", size = 2)
    var overwriteDefault = false

    @Dropdown(name = "Show Condition", options = ["Always", "Toggled", "Hovered", "Never"], size = 2)
    var showCondition = 1

    @Dropdown(name = "Line Style", options = ["Normal", "Proportioned", "Dashed"], size = 2)
    var lineStyle = 0

    @Slider(name = "Dash Factor", min = 1f, max = 20f, step = 1)
    var dashFactor = 10

    @Switch(name = "Accurate Hitbox")
    var accurate = true

    @Switch(name = "Different Color on Hover")
    var hoverColor = false

    @Checkbox(name = "Sides", size = 2)
    var showSide = false

    @DependOn(["showSide"])
    @Color(name = "Side Color")
    var sideColor = OneColor(255, 255, 255, 63)

    @DependOn(["showSide", "hoverColor"])
    @Color(name = "Hovered Side Color")
    var sideHoverColor = OneColor(255, 255, 255, 63)

    @Checkbox(name = "Outline", size = 2)
    var showOutline = true

    @DependOn(["showOutline"])
    @Color(name = "Outline Color")
    var outlineColor = OneColor(255, 255, 255, 255)

    @DependOn(["showOutline", "hoverColor"])
    @Color(name = "Hovered Outline Color")
    var outlineHoverColor = OneColor(255, 255, 255, 255)

    @DependOn(["showOutline"])
    @Slider(name = "Outline Thickness", min = 1f, max = 5f)
    var outlineThickness = 2f

    @Checkbox(name = "Eye Height", size = 2)
    var showEyeHeight = true

    @DependOn(["showEyeHeight"])
    @Color(name = "Eye Height Color")
    var eyeHeightColor = OneColor(255, 0, 0, 255)

    @DependOn(["showEyeHeight", "hoverColor"])
    @Color(name = "Hovered Eye Height Color")
    var eyeHeightHoverColor = OneColor(255, 0, 0, 255)

    @DependOn(["showEyeHeight"])
    @Slider(name = "Eye Height Thickness", min = 1f, max = 5f)
    var eyeHeightThickness = 2f

    @Checkbox(name = "View Ray", size = 2)
    var showViewRay = true

    @DependOn(["showViewRay"])
    @Color(name = "View Ray Color")
    var viewRayColor = OneColor(0, 0, 255, 255)

    @DependOn(["showViewRay", "hoverColor"])
    @Color(name = "Hovered View Ray Color")
    var viewRayHoverColor = OneColor(0, 0, 255, 255)

    @DependOn(["showViewRay"])
    @Slider(name = "View Ray Thickness", min = 1f, max = 5f)
    var viewRayThickness = 2f

    fun getOptions(category: HitboxCategory): ArrayList<BasicOption> {
        val options = ConfigUtils.getClassOptions(this)
        val fieldNameToOption = options.associateBy { option -> option.field.name }

        for ((name, option) in fieldNameToOption) {
            if (name != ::overwriteDefault.name)  {
                if (category != HitboxCategory.DEFAULT) {
                    option.addDependency(::overwriteDefault.name) { overwriteDefault }
                }
                if (name == ::dashFactor.name) {
                    option.addDependency(::lineStyle.name) { lineStyle == 2 }
                }
                if (name != ::showCondition.name) {
                    option.addDependency(::showCondition.name) { showCondition != 3 }
                }
            }

            val fieldName = option.field.getDeclaredAnnotation(DependOn::class.java)?.field ?: continue
            for (field in fieldName) {
                val dependedOption = fieldNameToOption[field] ?: continue
                option.addDependency(field) { dependedOption.get() == true }
            }
        }

        if (category == HitboxCategory.DEFAULT) {
            options.removeAt(0)
        }
        options.add(HitboxPreview(category))

        return options
    }

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FIELD)
    annotation class DependOn(val field: Array<String>)
}
