package org.polyfrost.hitboxes;

import org.polyfrost.hitboxes.config.HitBoxesConfig;

@net.minecraftforge.fml.common.Mod(modid = HitBoxes.MODID, name = HitBoxes.NAME, version = HitBoxes.VERSION)
public class HitBoxes {
    public static final String MODID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";

    @net.minecraftforge.fml.common.Mod.Instance(MODID)
    public static HitBoxes INSTANCE;
    public HitBoxesConfig config;

    @net.minecraftforge.fml.common.Mod.EventHandler
    public void onFMLInitialization(net.minecraftforge.fml.common.event.FMLInitializationEvent event) {
        config = new HitBoxesConfig();
    }

}
