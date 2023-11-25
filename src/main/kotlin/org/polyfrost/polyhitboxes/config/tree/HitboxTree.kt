package org.polyfrost.polyhitboxes.config.tree

import cc.polyfrost.oneconfig.libs.universal.wrappers.UPlayer
import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.dsl.drawText
import cc.polyfrost.oneconfig.utils.dsl.nanoVG
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.IProjectile
import net.minecraft.entity.monster.IMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityArrow

object HitboxMainTree { // todo: finish this
    var savedMap = HashMap<String, HitboxProfile>()
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

const val BRANCH_HEIGHT = 32

open class NamedHitbox(
    val name: String,
    val checkEntity: (Entity) -> Boolean,
) : HitboxProvider {
    override val savedHitbox: HitboxProfile
        get() = HitboxMainTree.savedMap.computeIfAbsent(name) { HitboxProfile() }

    private val readHitbox: HitboxProfile?
        get() = HitboxMainTree.savedMap[name]

    override fun findHitbox(entity: Entity): HitboxProfile? = readHitbox?.takeIf { !it.inherit && checkEntity(entity) }
    override fun find(index: Int) = takeIf { index == 0 }
    override fun size() = 1

    override fun drawBranch(vg: Long, x: Int, y: Int, inputHandler: InputHandler) = nanoVG(vg) {
        drawText(name, x + 14, y + BRANCH_HEIGHT / 2, 0xFFFFFFFF.toInt(), 14, Fonts.MEDIUM)
    }

    fun withChildren(vararg children: HitboxProvider) = ParentNamedHitbox(this, listOf(*children))
}

class ParentNamedHitbox(
    val hitbox: NamedHitbox,
    private val children: List<HitboxProvider>,
) : HitboxProvider by hitbox {
    private var expanded = true

    private val expandedList: List<HitboxProvider>
        get() = if (expanded) {
            listOf(hitbox) + children
        } else {
            listOf(hitbox)
        }

    override fun size() = expandedList.sumOf { it.size() }

    override fun find(index: Int): NamedHitbox? {
        var indexDec = index
        for (child in expandedList) {
            child.find(indexDec)?.let { return it }
            indexDec -= child.size()
        }
        return null
    }

    override fun drawBranch(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {
        if (inputHandler.isAreaClicked(x.toFloat(), y.toFloat(), 10f, BRANCH_HEIGHT.toFloat())) {
            expanded = !expanded
        }

        vg.drawText(if (expanded) "v" else ">", x, y + BRANCH_HEIGHT / 2, 0xFFAAAAAA.toInt(), 14, Fonts.SEMIBOLD)
        hitbox.drawBranch(vg, x, y, inputHandler)

        if (!expanded) return
        var y2 = y + hitbox.size() * BRANCH_HEIGHT
        for (child in children) {
            child.drawBranch(vg, x + 10, y2, inputHandler)
            y2 += child.size() * BRANCH_HEIGHT
        }
    }

    override fun findHitbox(entity: Entity): HitboxProfile? =
        expandedList.lastNotNullOfOrNull {
            it.findHitbox(entity)
        }
}

interface HitboxProvider {
    val savedHitbox: HitboxProfile
    fun findHitbox(entity: Entity): HitboxProfile?

    fun drawBranch(vg: Long, x: Int, y: Int, inputHandler: InputHandler)
    fun size(): Int
    fun find(index: Int): NamedHitbox?
}

inline fun <T, R : Any> List<T>.lastNotNullOfOrNull(transform: (T) -> R?): R? {
    val iterator = listIterator(size)
    while (iterator.hasPrevious()) {
        return transform(iterator.previous()) ?: continue
    }
    return null
}

