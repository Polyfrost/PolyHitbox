# PolyHitbox

[![Modrinth downloads](https://img.shields.io/modrinth/dt/Mptc0jo6?style=flat&logo=modrinth&label=Modrinth&color=1bd96a)](https://modrinth.com/mod/hitbox)
[![CurseForge downloads](https://img.shields.io/curseforge/dt/1148007?style=flat&logo=curseforge&label=CurseForge&color=f16436)](https://www.curseforge.com/minecraft/mc-mods/polyhitbox)

PolyHitbox replaces Minecraft's entity hitboxes with ones you can restyle and show or hide for each entity type. It does nothing until you turn on "Enabled" in its settings. While it is on, it hides the vanilla hitboxes and takes over the F3 + B shortcut.

## Features

### Hitbox parts

A hitbox has four parts, and you turn each one on or off.

- The outline of the box. It is on by default.
- Filled sides. They are off by default.
- The eye height, drawn as a ring around the box at eye level. It is on by default.
- The view ray, a line 2 blocks long that points where the entity looks. It is on by default.

Each part has its own color. The outline, eye height and view ray each have a thickness from 0.5 to 3 times the vanilla line width, in steps of 0.25.

### Style

- Draw the lines solid or dashed. A dash factor from 1 to 20 sets the dash length.
- "Draw Over Entity" draws a hitbox on top of its own entity. Blocks and other entities still cover it.
- "Different Color on Hover" gives each part a second color for the entity under your crosshair.
- "Different Color in I-Frames" gives each part another color while an entity is invulnerable after a hit, so you can see when you can damage it again. These colors take priority over the hover colors.

These three options are off by default.

### When hitboxes show

The show condition has four settings.

- Always, the default.
- Toggled. You switch these hitboxes on and off with a keybind. The default is F3 + B, and you can rebind it. PolyHitbox remembers the toggle between game sessions unless you turn "Retain Toggle State" off.
- Hovered. Only the entity under your crosshair shows a hitbox.
- Never.

PolyHitbox hides all hitboxes while you hide the HUD with F1, and you can turn that off. It never draws your own hitbox in first person or a hitbox for an invisible entity. It draws nothing while reduced debug info is on.

### Settings for each entity type

The General tab applies to every entity. Twelve more tabs each cover one entity type: Player, Self, Mob, Monster, Projectile, Arrow, Fireball, Wither Skull, Frames, Armor Stand, Item and XP. In each tab you can override the General show condition, the General styling, or both.

The Arrow tab has one more option that hides the hitboxes of arrows stuck in blocks.

### Accurate hitboxes on 1.8.9

On Minecraft 1.8.9, "Accurate Hitboxes" draws each hitbox at the size attacks register against. It is on by default.

## Compatibility

- PolyHitbox does not load alongside Combat Hitboxes.

## Supported versions

- Minecraft 1.8.9 on Ornithe.
- Minecraft 1.21.1, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11, 26.1, 26.2 and 26.3 on Fabric.

PolyHitbox needs [OneConfig](https://modrinth.com/mod/oneconfig) and Fabric Language Kotlin.

## Support

- Report bugs and ask questions on the [Polyfrost Discord](https://polyfrost.org/discord).
- Check [Polyfrost Status](https://status.polyfrost.org/) if a Polyfrost service is down.

## Building

```sh
./gradlew ":26.3:build"
```

Replace `26.3` with any supported Minecraft version. The build needs JDK 21 and JDK 25 installed.
