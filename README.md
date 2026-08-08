<img align="right" src="src/main/resources/assets/polyhitbox/polyhitbox.svg" alt="PolyHitbox Icon"/>

# PolyHitbox

![Compact Powered by OneConfig](https://polyfrost.org/img/compact_vector.svg)  ![Dev Workflow Status](https://img.shields.io/github/v/release/Polyfrost/PolyHitbox.svg?style=for-the-badge&color=1452cc&label=release)

A hitbox modification mod

## Features

- Show Condition - Always / Debug (F3+B) / Hovered / Never
- Line Style - Normal / Dashed
- Hovered Color
- Sides
- Outline
- Eye Height
- View Ray
- Different color when hovered
- Different color during invulnerability frames (i-frames)
- Hide with the HUD (F1)

## For mod developers

Other mods can recolour hitboxes per entity through `org.polyfrost.polyhitbox.api.HitboxColors`:

```java
HitboxColors.register((context, argb) ->
    context.getElement() == HitboxElement.OUTLINE
        ? myColorFor(context.getEntity(), argb)
        : argb);
```

Providers run on the render thread, once per element per visible entity per frame, after the
configured hover and i-frame colours have been applied. `context.has(HitboxCondition.HOVERED)` and
`context.has(HitboxCondition.IFRAME)` report the entity's actual state regardless of whether the
matching colour option is enabled; more conditions may be added to `HitboxCondition` later.

The context is only valid for the duration of the call — read what you need, don't retain it. A
provider that throws is unregistered and the failure is logged, so a broken integration degrades to
its own feature going away rather than breaking hitbox rendering.

Everything in `org.polyfrost.polyhitbox.api` is stable. Nothing else is — in particular
`render.HitboxRenderer` is private, is rewritten between patch releases, and injecting into it will
break your users' game rather than just your feature.

## Gallery

![settings-page.png](images/settings-page.png)
![settings-page-2.png](images/settings-page-2.png)

