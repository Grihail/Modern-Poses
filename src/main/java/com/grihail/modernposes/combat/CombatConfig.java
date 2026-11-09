package com.grihail.modernposes.combat;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

/** config/modernposes.cfg. */
public final class CombatConfig {

    /** Options.attackIndicator: crosshair, hotbar or off. */
    public static String attackIndicator = "crosshair";

    private CombatConfig() {}

    public static void load(File file) {
        Configuration c = new Configuration(file);
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
