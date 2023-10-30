package org.polyfrost.hitboxes.config;

import cc.polyfrost.oneconfig.config.annotations.Switch;

public class Global extends HitboxConfiguration{
    @Switch(name = "Enable")
    public boolean global = true;
}
