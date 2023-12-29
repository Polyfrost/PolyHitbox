package org.polyfrost.polyhitboxes.config.data

import com.google.common.collect.ImmutableMap

class ConfigAdapter : MutableMap<HitboxCategory, HitboxConfig> by ImmutableMap.of() {
    override fun isEmpty() = false

    override val size: Int
        get() = HitboxCategory.entries.size

    override val entries: MutableSet<MutableMap.MutableEntry<HitboxCategory, HitboxConfig>>
        get() = HitboxCategory.entries.associateWithTo(mutableMapOf()) { category -> category.config }.entries

    override fun put(key: HitboxCategory, value: HitboxConfig): HitboxConfig {
        val last = key.config
        key.config = value
        return last
    }
}