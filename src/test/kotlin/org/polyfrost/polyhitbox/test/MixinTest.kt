package org.polyfrost.polyhitbox.test

import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.MixinEnvironment.Option
import org.spongepowered.asm.mixin.transformer.IMixinTransformer

// Audits mixins without launching a full client
// Inspired by https://github.com/SkyblockerMod/Skyblocker
class MixinTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupEnvironment() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `mixins load successfully`() {
        val environment = MixinEnvironment.getCurrentEnvironment()
        Assertions.assertInstanceOf(
            IMixinTransformer::class.java,
            environment.activeTransformer,
        )
        // Refmap remapping is on in dev so Mixin retries failed targets with the descriptor stripped
        // Disable it to match production or a selector with a bad descriptor still resolves by name
        environment.setOption(Option.REFMAP_REMAP, false)
        environment.audit()
    }
}
