plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.3" /* [SC] DO NOT EDIT */

stonecutter tasks {
    order("publishModrinth")
}

stonecutter handlers {
    inherit("aw", "classtweaker")
}

stonecutter parameters {
    replacements {
        string(eval(current.version, "< 26.1")) {
            replace(
                "classTweaker v1 official",
                "classTweaker v1 named",
            )
        }
        string(eval(current.version, "= 1.8.9")) {
            replace(
                "com.mojang.blaze3d.platform.InputConstants",
                "org.polyfrost.oneconfig.internal.legacy.InputConstants",
            )
            replace(
                "import com.mojang.blaze3d.vertex.VertexConsumer",
                "import com.mojang.blaze3d.vertex.BufferBuilder as VertexConsumer",
            )
            replace(
                "net.minecraft.util.Mth",
                "org.polyfrost.polyhitbox.legacy.Mth",
            )
            replace(
                "net.minecraft.world.entity.projectile.Fireball",
                "org.polyfrost.polyhitbox.legacy.Fireball",
            )
        }
    }
}
