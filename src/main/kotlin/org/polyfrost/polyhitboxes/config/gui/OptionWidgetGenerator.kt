package org.polyfrost.polyhitboxes.config.gui

import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.elements.BasicOption

object OptionWidgetGenerator {
    fun getOptionsFor(config: Any): ArrayList<BasicOption> {
        val options = ConfigUtils.getClassOptions(config)
        (config as? SetupOptions)?.apply {
            val fieldOptionMap = options.associateBy { option -> option.field.name }
            OptionSetup(fieldOptionMap, options).setup()
        }
        return options
    }
}

interface SetupOptions {
    fun OptionSetup.setup()
}

class OptionSetup(private val fieldOptionMap: Map<String, BasicOption>, val options: MutableList<BasicOption>) {
    val optionNames: Set<String> = fieldOptionMap.keys

    fun String.dependOn(name: String, condition: () -> Boolean) {
        val option = fieldOptionMap[this] ?: return
        val dependentOption = fieldOptionMap[name] ?: return
        option.addDependency(dependentOption.name, condition)
    }

    infix fun String.dependOn(dependency: String) {
        val option = fieldOptionMap[this] ?: return
        val dependentOption = fieldOptionMap[dependency] ?: return
        option.addDependency(dependentOption.name) {
            dependentOption.get() as? Boolean == true
        }
    }

    fun String.hideIf(condition: () -> Boolean) {
        val option = fieldOptionMap[this] ?: return
        option.addHideCondition(condition)
    }

    infix fun String.hideIf(dependency: String) {
        val option = fieldOptionMap[this] ?: return
        val dependentOption = fieldOptionMap[dependency] ?: return
        option.addHideCondition {
            dependentOption.get() as? Boolean == true
        }
    }

    fun String.onChanged(callback: () -> Unit) {
        val option = fieldOptionMap[this] ?: return
        option.addListener(callback)
    }
}
