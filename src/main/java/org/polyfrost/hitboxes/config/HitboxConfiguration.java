package org.polyfrost.hitboxes.config;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;

public class HitboxConfiguration {
    @Switch(
            name = "Show Hitbox",
            subcategory = "General Options"
    )
    public boolean showHitbox = false;

    @Button(name = "Reset", text = "Reset", subcategory = "General Options")
    public void reset() {
        accurate = false;
        dashedHitbox = false;
        showOutline = true;
        showEyeHeight = true;
        showLookVector = true;
        outlineThickness = 2;
        eyeHeightThickness = 2;
        lookVectorThickness = 2;
        outlineColor = new OneColor(-1);
        eyeHeightColor = new OneColor(0xFFFF0000);
        lookVectorColor = new OneColor(0xFF0000FF);
    }

    @Switch(
            name = "Accurate Hitboxes",
            subcategory = "General Options"
    )
    public boolean accurate = false;

    @Switch(
            name = "Dashed",
            subcategory = "General Options"
    )
    public boolean dashedHitbox = false;

    @Switch(
            name = "Hitbox Outline",
            subcategory = "General Options"
    )
    public boolean showOutline = true;

    @Switch(
            name = "Eye Height",
            subcategory = "General Options"
    )
    public boolean showEyeHeight = true;

    @Switch(
            name = "Look Vector",
            subcategory = "General Options"
    )
    public boolean showLookVector = true;

    @Slider(
            name = "Outline Thickness",
            subcategory = "General Options",
            min = 1,
            max = 5
    )
    public float outlineThickness = 2;

    @Slider(
            name = "Eye Height Thickness",
            subcategory = "General Options",
            min = 1,
            max = 5
    )
    public float eyeHeightThickness = 2;

    @Slider(
            name = "Look Vector Thickness",
            subcategory = "General Options",
            min = 1,
            max = 5
    )
    public float lookVectorThickness = 2;

    @Color(
            name = "Hitbox Outline Color",
            subcategory = "Color Options",
            size = 2
    )
    public OneColor outlineColor = new OneColor(-1);

    @Color(
            name = "Eye Height Color",
            subcategory = "Color Options",
            size = 2
    )
    public OneColor eyeHeightColor = new OneColor(0xFFFF0000);

    @Color(
            name = "Look Vector Color",
            subcategory = "Color Options",
            size = 2
    )
    public OneColor lookVectorColor = new OneColor(0xFF0000FF);

}
