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
                "classTweaker v2 official",
                "classTweaker v2 named",
            )
        }
    }
}
