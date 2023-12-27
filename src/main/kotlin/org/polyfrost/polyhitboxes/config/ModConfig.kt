package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.CustomOption
import cc.polyfrost.oneconfig.config.annotations.Dropdown
import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.config.elements.OptionPage
import org.polyfrost.polyhitboxes.PolyHitBoxes
import java.lang.reflect.Field

object ModConfig : Config(Mod(PolyHitBoxes.NAME, ModType.UTIL_QOL), "${PolyHitBoxes.MODID}.json") {

    @Dropdown(name = "Show Hitbox", options = ["Always", "Toggled", "Never"])
    var showHitbox = 1

//    @Switch(name = "Accurate Hitboxes")
//    var accurate = false
//
//    @Switch(name = "Dashed Lines")
//    var dashedLines = false
//
//    @Checkbox(name = "Hitbox Outline")
//    var showOutline = true
//
//    @Color(name = "Hitbox Outline Color")
//    var outlineColor = OneColor(255, 255, 255, 255)
//
//    @Number(name = "Outline Thickness", min = 1f, max = 5f)
//    var outlineThickness = 2f
//
//    @Checkbox(name = "Eye Height")
//    var showEyeHeight = true
//
//    @Color(name = "Eye Height Color")
//    var eyeHeightColor = OneColor(255, 0, 0, 255)
//
//    @Number(name = "Eye Height Thickness", min = 1f, max = 5f)
//    var eyeHeightThickness = 2f
//
//    @Checkbox(name = "Look Vector")
//    var showLookVector = true
//
//    @Color(name = "Look Vector Color")
//    var lookVectorColor = OneColor(0, 0, 255, 255)
//
//    @Number(name = "Look Vector Thickness", min = 1f, max = 5f)
//    var lookVectorThickness = 2f
//
//    @CustomOption
//    @Transient
//    private val nametagPreview = NametagPreview(category = "General")

    private var profileMap = HashMap<String, HitboxProfile>()

    @CustomOption
    @Transient
    val entitySelector = EntitySelector(category = "Advanced")

    init {
        initialize()
    }

    override fun load() {
        super.load()
        entitySelector.profileMap.load(profileMap)
    }

    override fun save() {
        profileMap = HashMap(entitySelector.profileMap.save())
        super.save()
    }

    override fun getCustomOption(
        field: Field,
        annotation: CustomOption,
        page: OptionPage,
        mod: Mod,
        migrate: Boolean,
    ): BasicOption? {
//        return nametagPreview.also {
//            ConfigUtils.getSubCategory(page, it.category, it.subcategory).options.add(it)
//        }
        ConfigUtils.getSubCategory(page, "Advanced", "").options.add(entitySelector)
        return null
    }
}
