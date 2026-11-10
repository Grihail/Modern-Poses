package com.grihail.modernposes;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

/** config/modernposes.cfg, with Forge's reader: writes the file with its comments and reads the client options. */
public final class Config {

    private static final String RESTART = "Restart the game after a change. The server and its clients must use the same values.";

    /** Options.attackIndicator: crosshair, hotbar or off. */
    public static String attackIndicator = "crosshair";

    private Config() {}

    public static void load(File file) {
        Configuration c = new Configuration(file);
        c.setCategoryComment("poses", "Modern player poses. " + RESTART);
        Features.sneaking = c.getBoolean(
            "sneaking",
            "poses",
            true,
            "Sneaking 1.5 blocks high, so you fit under slabs. Off: 1.7.10's sneaking.");
        Features.swimming = c.getBoolean(
            "swimming",
            "poses",
            true,
            "Swimming by sprinting in water, and modern water movement. Off: 1.7.10's swimming.");
        Features.crawling = c.getBoolean(
            "crawling",
            "poses",
            true,
            "Crawling through 1 block high gaps; a low ceiling makes you crawl.");

        c.setCategoryComment("hands", "Hands. " + RESTART);
        Features.offhand = c.getBoolean(
            "offhand",
            "hands",
            true,
            "The off hand: its inventory slot, the swap key, using and drawing its item. Turned off, an item already in "
                + "the off hand stays saved with the player and comes back when it is turned on again.");

        c.setCategoryComment("combat", "Melee combat of 26.3. " + RESTART);
        Features.attackCooldown = c.getBoolean(
            "attackCooldown",
            "combat",
            true,
            "Attack charge and attack speed, damage by charge, modern critical, sprint and sweeping attacks, "
                + "knockback, the attack indicator, attack sounds and particles.");
        Features.weaponStats = c.getBoolean(
            "weaponStats",
            "combat",
            true,
            "Modern damage and attack speed of swords, axes, pickaxes, shovels and hoes.");
        Features.noSwordBlocking = c
            .getBoolean("noSwordBlocking", "combat", true, "Swords no longer block. Off: they block as in 1.7.10.");
        Features.armor = c.getBoolean(
            "armor",
            "combat",
            true,
            "Modern armor: toughness, armor wear, protection enchantments without randomness, armor tooltips.");
        Features.enchantments = c.getBoolean(
            "enchantments",
            "combat",
            true,
            "Modern Sharpness and Bane of Arthropods, and the Sweeping Edge enchantment (needs attackCooldown).");
        Features.potions = c.getBoolean(
            "potions",
            "combat",
            true,
            "Modern Strength and Weakness; Haste and Mining Fatigue change the attack speed.");
        Features.food = c
            .getBoolean("food", "combat", true, "Fast healing on a full hunger bar, modern golden apple effects.");

        attackIndicator = c.getString(
            "attackIndicator",
            "client",
            "crosshair",
            "Where the attack charge is shown.",
            new String[] { "crosshair", "hotbar", "off" });
        if (c.hasChanged()) {
            c.save();
        }
    }
}
