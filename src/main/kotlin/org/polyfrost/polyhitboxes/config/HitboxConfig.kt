package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.core.OneColor

interface HitboxConfig {
    var showHitbox: Boolean
    var accurate: Boolean
    var dashedHitbox: Boolean
    var showOutline: Boolean
    var showEyeHeight: Boolean
    var showLookVector: Boolean
    var outlineThickness: Float
    var eyeHeightThickness: Float
    var lookVectorThickness: Float
    var outlineColor: OneColor
    var eyeHeightColor: OneColor
    var lookVectorColor: OneColor
}