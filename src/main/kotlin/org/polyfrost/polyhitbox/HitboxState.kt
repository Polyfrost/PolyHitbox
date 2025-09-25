package org.polyfrost.polyhitbox

import dev.deftu.omnicore.api.data.aabb.OmniAABB
import net.minecraft.util.math.Box

data class HitboxState(
    val offsetX: Double, val offsetY: Double, val offsetZ: Double,
    val entityX: Double, val entityY: Double, val entityZ: Double,
    val lookVecX: Double, val lookVecY: Double, val lookVecZ: Double,
    val eyeHeight: Float,
    val isLiving: Boolean,
    val isTargeted: Boolean,
    val width: Double,
    val collisionSize: Double,
    val boundingBox: OmniAABB,
)