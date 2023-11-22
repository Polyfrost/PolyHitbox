package org.polyfrost.polyhitboxes.config.tree

import cc.polyfrost.oneconfig.libs.universal.wrappers.UPlayer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.IProjectile
import net.minecraft.entity.monster.IMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityArrow
import java.util.*

object HitboxMainConfig { // todo: finish this
    var savedMap = HashMap<String, HitboxNode>()
    val all = NamedHitbox("All") { true }.withChildren(
        typedHitbox<EntityPlayer>("Player").withChildren(
            NamedHitbox("Self") { it == UPlayer.getPlayer() }
        ),
        typedHitbox<EntityLiving>("Mob").withChildren(
            typedHitbox<IMob>("Hostile")
        ),
        typedHitbox<IProjectile>("Projectile").withChildren(
            typedHitbox<EntityArrow>("Arrow")
        ),
    )
}

private inline fun <reified T> typedHitbox(name: String) = NamedHitbox(name) { it is T }

open class NamedHitbox(
    val name: String,
    val checkEntity: (Entity) -> Boolean,
) : HitboxProvider {
    override val savedHitbox: HitboxNode
        get() = HitboxMainConfig.savedMap.computeIfAbsent(name) { HitboxNode() }

    private val readHitbox: HitboxNode?
        get() = HitboxMainConfig.savedMap[name]

    override fun findHitbox(entity: Entity): HitboxNode? = readHitbox?.takeIf { it.override && checkEntity(entity) }

    fun withChildren(vararg children: HitboxProvider) = ParentNamedHitbox(this, children)
}

class ParentNamedHitbox(
    private val hitbox: HitboxProvider,
    private val children: Array<out HitboxProvider>,
) : HitboxProvider by hitbox {
    override fun findHitbox(entity: Entity): HitboxNode? =
        children.firstNotNullOfOrNull {
            it.findHitbox(entity)
        } ?: hitbox.findHitbox(entity)
}

interface HitboxProvider {
    val savedHitbox: HitboxNode
    fun findHitbox(entity: Entity): HitboxNode?
}

