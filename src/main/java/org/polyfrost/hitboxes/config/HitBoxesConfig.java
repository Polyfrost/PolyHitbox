package org.polyfrost.hitboxes.config;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.ConfigUtils;
import cc.polyfrost.oneconfig.config.data.*;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import net.minecraft.client.Minecraft;
import org.polyfrost.hitboxes.HitBoxes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.*;

import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings("unused")
public class HitBoxesConfig extends cc.polyfrost.oneconfig.config.Config {

    @Exclude
    public static HashMap<Class<? extends Entity>, HitboxConfigWrapper> configMap = new HashMap<>();

    @Button(name = "Reset All", text = "Reset")
    public void reset() {

        for (HitboxConfigWrapper wrapper : configMap.values()) {
            HitboxConfiguration hitboxConfig = wrapper.getOrNull(this);
            if (hitboxConfig == null) return;
            hitboxConfig.reset();
        }

        passive.reset();
        hostile.reset();
        projectile.reset();
        other.reset();
    }

    @Page(name = "Passive Entities", location = PageLocation.TOP)
    public Global passive = new Global();

    @Hitbox(EntityArmorStand.class)
    @Page(name = "Armor Stand", location = PageLocation.TOP, category = "Passive Entities")
    public HitboxConfiguration armorStand = new HitboxConfiguration();

    @Hitbox(EntityBat.class)
    @Page(name = "Bat", location = PageLocation.TOP, category = "Passive Entities")
    public HitboxConfiguration bat = new HitboxConfiguration();

    @Hitbox(EntityChicken.class)
    @Page(name = "Chicken", location = PageLocation.TOP, category = "Passive Entities")
    public HitboxConfiguration chicken = new HitboxConfiguration();

    @Hitbox(EntityCow.class)
    @Page(name = "Cow", location = PageLocation.TOP, category = "Passive Entities")
    public HitboxConfiguration cow = new HitboxConfiguration();

    @Hitbox(EntityHorse.class)
    @Page(name = "Horse", location = PageLocation.TOP, category = "Passive Entities")
    public HitboxConfiguration horse = new HitboxConfiguration();

    @Hitbox(EntityMooshroom.class)
    @Page(name = "Mooshroom", location = PageLocation.TOP, category = "Passive Entities")
    public HitboxConfiguration mooshroom = new HitboxConfiguration();

    @Hitbox(EntityOcelot.class)
    @Page(name = "Ocelot", location = PageLocation.TOP, category = "Passive Entities")
    public HitboxConfiguration ocelot = new HitboxConfiguration();

    @Hitbox(EntityPig.class)
    @Page(name = "Pig", location = PageLocation.TOP, category = "Passive Entities")
    public HitboxConfiguration pig = new HitboxConfiguration();

    @Hitbox(EntityPlayer.class)
    @Page(name = "Player", location = PageLocation.TOP, category = "Passive Entities")
    public HitboxConfiguration player = new HitboxConfiguration();

    @Hitbox(EntityRabbit.class)
    @Page(name = "Rabbit", location = PageLocation.TOP, category = "Passive Entities")
    public HitboxConfiguration rabbit = new HitboxConfiguration();

    @Hitbox(EntitySheep.class)
    @Page(name = "Sheep", location = PageLocation.TOP, category = "Passive Entities")
    public HitboxConfiguration sheep = new HitboxConfiguration();

    @Hitbox(EntitySquid.class)
    @Page(name = "Squid", location = PageLocation.TOP, category = "Passive Entities")
    public HitboxConfiguration squid = new HitboxConfiguration();

    @Hitbox(EntityVillager.class)
    @Page(name = "Villager", location = PageLocation.TOP, category = "Passive Entities")
    public HitboxConfiguration villager = new HitboxConfiguration();

    @Hitbox(EntityWolf.class)
    @Page(name = "Wolf", location = PageLocation.TOP, category = "Passive Entities")
    public HitboxConfiguration wolf = new HitboxConfiguration();

    // hostile
    @Page(name = "Hostile Entities", location = PageLocation.TOP)
    public Global hostile = new Global();

    @Hitbox(EntityCaveSpider.class)
    @Page(name = "Cave Spider", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    public HitboxConfiguration caveSpider = new HitboxConfiguration();

    @Hitbox(EntityCreeper.class)
    @Page(name = "Creeper", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    public HitboxConfiguration creeper = new HitboxConfiguration();

    @Hitbox(EntityGiantZombie.class)
    @Page(name = "Giant", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    public HitboxConfiguration giant = new HitboxConfiguration();

    @Hitbox(EntityGuardian.class)
    @Page(name = "Guardian", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    public HitboxConfiguration guardian = new HitboxConfiguration();

    @Hitbox(EntityIronGolem.class)
    @Page(name = "Iron Golem", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    public HitboxConfiguration ironGolem = new HitboxConfiguration();

    @Hitbox(EntitySilverfish.class)
    @Page(name = "Silverfish", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    public HitboxConfiguration silverfish = new HitboxConfiguration();

    @Page(name = "Skeleton", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    public HitboxConfiguration skeleton = new HitboxConfiguration();

    @Hitbox(EntitySlime.class)
    @Page(name = "Slime", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    public HitboxConfiguration slime = new HitboxConfiguration();

    @Hitbox(EntitySnowman.class)
    @Page(name = "Snow Golem", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    public HitboxConfiguration snowGolem = new HitboxConfiguration();

    @Hitbox(EntitySpider.class)
    @Page(name = "Spider", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    public HitboxConfiguration spider = new HitboxConfiguration();

    @Hitbox(EntityWitch.class)
    @Page(name = "Witch", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    public HitboxConfiguration witch = new HitboxConfiguration();

    @Hitbox(EntityZombie.class)
    @Page(name = "Zombie", location = PageLocation.TOP, subcategory = "Overworld", category = "Hostile Entities")
    public HitboxConfiguration zombie = new HitboxConfiguration();

// nether

    @Hitbox(EntityBlaze.class)
    @Page(name = "Blaze", location = PageLocation.TOP, subcategory = "The Nether", category = "Hostile Entities")
    public HitboxConfiguration blaze = new HitboxConfiguration();

    @Hitbox(EntityGhast.class)
    @Page(name = "Ghast", location = PageLocation.TOP, subcategory = "The Nether", category = "Hostile Entities")
    public HitboxConfiguration ghast = new HitboxConfiguration();

    @Hitbox(EntityMagmaCube.class)
    @Page(name = "Magma Cube", location = PageLocation.TOP, subcategory = "The Nether", category = "Hostile Entities")
    public HitboxConfiguration magmaCube = new HitboxConfiguration();

    @Page(name = "Wither Skeleton", location = PageLocation.TOP, subcategory = "The Nether", category = "Hostile Entities")
    public HitboxConfiguration witherSkeleton = new HitboxConfiguration();

    @Hitbox(EntityPigZombie.class)
    @Page(name = "Zombie Pigman", location = PageLocation.TOP, subcategory = "The Nether", category = "Hostile Entities")
    public HitboxConfiguration zombiePigman = new HitboxConfiguration();

// end

    @Hitbox(EntityEnderman.class)
    @Page(name = "Enderman", location = PageLocation.TOP, subcategory = "The End", category = "Hostile Entities")
    public HitboxConfiguration enderman = new HitboxConfiguration();

    @Hitbox(EntityEndermite.class)
    @Page(name = "Endermite", location = PageLocation.TOP, subcategory = "The End", category = "Hostile Entities")
    public HitboxConfiguration endermite = new HitboxConfiguration();

    @Hitbox(EntityDragon.class)
    @Page(name = "Ender Dragon", location = PageLocation.TOP, subcategory = "The End", category = "Hostile Entities")
    public HitboxConfiguration enderDragon = new HitboxConfiguration();

    @Page(name = "Projectile Entities", location = PageLocation.TOP)
    public Global projectile = new Global();

    @Hitbox(EntityArrow.class)
    @Page(name = "Arrow", location = PageLocation.TOP, category = "Projectiles")
    public HitboxConfiguration arrow = new HitboxConfiguration();

    @Hitbox(EntityEgg.class)
    @Page(name = "Egg", location = PageLocation.TOP, category = "Projectiles")
    public HitboxConfiguration egg = new HitboxConfiguration();

    @Hitbox(EntityFireball.class)
    @Page(name = "Fireball", location = PageLocation.TOP, category = "Projectiles")
    public HitboxConfiguration fireball = new HitboxConfiguration();

    @Hitbox(EntityFishHook.class)
    @Page(name = "Fish Hook", location = PageLocation.TOP, category = "Projectiles")
    public HitboxConfiguration fishHook = new HitboxConfiguration();

    @Hitbox(EntityPotion.class)
    @Page(name = "Potion", location = PageLocation.TOP, category = "Projectiles")
    public HitboxConfiguration potion = new HitboxConfiguration();

    @Hitbox(EntitySnowball.class)
    @Page(name = "Snowball", location = PageLocation.TOP, category = "Projectiles")
    public HitboxConfiguration snowball = new HitboxConfiguration();

    @Page(name = "Others", location = PageLocation.TOP)
    public Global other = new Global();

    @Page(name = "Self", location = PageLocation.TOP, category = "Others")
    public HitboxConfiguration self = new HitboxConfiguration();

    @Hitbox(EntityItem.class)
    @Page(name = "Item", location = PageLocation.TOP, category = "Others")
    public HitboxConfiguration item = new HitboxConfiguration();

    @Hitbox(EntityXPOrb.class)
    @Page(name = "XP Orb", location = PageLocation.TOP, category = "Others")
    public HitboxConfiguration xpOrb = new HitboxConfiguration();

    public HitBoxesConfig() {
        super(new Mod(HitBoxes.NAME, ModType.UTIL_QOL), HitBoxes.MODID + ".json");
        initialize();
        setupConditions();
    }

    /**
     * Sets the mod conditions, this will later be setup differently so that it is more efficient, this is it for now though.
     */
    private void setupConditions() {
        for (Map.Entry<String, BasicOption> entry : optionNames.entrySet()) {
            if (entry.getKey().contains("showHitbox")) continue;
            if (entry.getKey().contains("global")) continue;
            Object parent = entry.getValue().getParent();
            if (!(parent instanceof HitboxConfiguration)) continue;
            String page = entry.getKey().split("\\.")[0];
            entry.getValue().addDependency(page + ".showHitbox", () -> ((HitboxConfiguration) parent).showHitbox);
        }

        for (Field declaredField : getClass().getDeclaredFields()) {
            Hitbox hitboxAnnotation = ConfigUtils.findAnnotation(declaredField, Hitbox.class);
            Page pageAnnotation = ConfigUtils.findAnnotation(declaredField, Page.class);
            if (hitboxAnnotation == null || pageAnnotation == null) continue;
            configMap.put(hitboxAnnotation.value(), new HitboxConfigWrapper(declaredField, pageAnnotation.category()));
        }
    }

    public HitboxConfiguration getEntityType(Entity entity) {
        HitboxConfigWrapper hitboxConfigField = configMap.get(entity.getClass());

        if (hitboxConfigField != null)
            return hitboxConfigField.getCategoryOrDefault(this);

        if (entity instanceof EntitySkeleton) {
            if (((EntitySkeleton) entity).getSkeletonType() == 1)
                return this.witherSkeleton;
            return this.skeleton;
        }

        if (entity.equals(Minecraft.getMinecraft().thePlayer)) return this.self;

        for (Map.Entry<Class<? extends Entity>, HitboxConfigWrapper> entry : configMap.entrySet()) {
            if (entry.getKey().isInstance(entity))
                return entry.getValue().getCategoryOrDefault(this);
        }

        return self;
    }
}
