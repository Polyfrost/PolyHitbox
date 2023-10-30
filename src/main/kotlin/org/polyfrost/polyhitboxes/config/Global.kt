package org.polyfrost.polyhitboxes.config

import cc.polyfrost.oneconfig.config.annotations.Switch
import org.polyfrost.polyhitboxes.config.HitboxConfiguration

class Global : HitboxConfiguration() {
    @Switch(name = "Enable")
    var global = true
}
