package org.polyfrost.polyhitboxes.config.tree

class HitboxTree { // todo: remove boilerplate
    @Hitbox("All")
    var all = AllHitbox()

    class AllHitbox : HitboxNode() {
        @Hitbox("Player")
        var player = Player()

        class Player : HitboxNode() {
            @Hitbox("Self")
            var self = HitboxNode()
        }

        @Hitbox("Mob")
        var mob = Mob()

        class Mob : HitboxNode() {
            @Hitbox("Self")
            var self = HitboxNode()
        }

        @Hitbox("Other")
        var other = Other()

        class Other : HitboxNode() {
            @Hitbox("Self")
            var self = HitboxNode()
        }
    }
}