package com.grihail.modernposes;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * What parts of the mod are on (config/modernposes.cfg). Read once at startup: by {@link MixinPlugin} before the game
 * loads (a part that is off gets none of its mixins), then by {@link Config} with Forge's own reader.
 * <p>
 * No Minecraft or Forge class may be used here: this runs while mixins are being set up.
 */
public final class Features {

    public static boolean sneaking = true, swimming = true, crawling = true;
    public static boolean offhand = true;
    public static boolean attackCooldown = true, weaponStats = true, noSwordBlocking = true, armor = true,
        enchantments = true, potions = true, food = true;

    /** The config keys, in the order of {@link #mask}'s bits. */
    public static final String[] NAMES = { "poses.sneaking", "poses.swimming", "poses.crawling", "hands.offhand",
        "combat.attackCooldown", "combat.weaponStats", "combat.noSwordBlocking", "combat.armor", "combat.enchantments",
        "combat.potions", "combat.food" };

    private Features() {}

    /** The features as bits, to compare a client's with its server's. */
    public static int mask() {
        boolean[] on = { sneaking, swimming, crawling, offhand, attackCooldown, weaponStats, noSwordBlocking, armor,
            enchantments, potions, food };
        int m = 0;
        for (int i = 0; i < on.length; i++) {
            if (on[i]) {
                m |= 1 << i;
            }
        }
        return m;
    }

    /** The feature a mixin belongs to is on (the names MixinPlugin maps the mixins to). */
    static boolean isOn(String feature) {
        switch (feature) {
            case "offhand":
                return offhand;
            case "attackCooldown":
                return attackCooldown;
            case "weaponStats":
                return weaponStats;
            case "noSwordBlocking":
                return noSwordBlocking;
            case "armor":
                return armor;
            case "enchantments":
                return enchantments;
            case "potions":
                return potions;
            case "food":
                return food;
            default:
                throw new IllegalArgumentException("unknown feature " + feature);
        }
    }

    /** Forge's config format, read by hand: {@code category {}} blocks of {@code B:name=value} lines. */
    static void readEarly(File file) {
        if (!file.isFile()) {
            return; // first start: everything on; Config writes the file
        }
        Map<String, String> values = new HashMap<>();
        String category = "";
        try {
            for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                String t = line.trim();
                if (t.isEmpty() || t.startsWith("#")) {
                    continue;
                }
                if (t.endsWith("{")) {
                    category = t.substring(0, t.length() - 1)
                        .trim()
                        .replace("\"", "")
                        .toLowerCase();
                } else if (t.equals("}")) {
                    category = "";
                } else {
                    int eq = t.indexOf('=');
                    if (eq > 2 && t.charAt(1) == ':') {
                        values.put(
                            category + "."
                                + t.substring(2, eq)
                                    .trim(),
                            t.substring(eq + 1)
                                .trim());
                    }
                }
            }
        } catch (IOException e) {
            return; // unreadable: the defaults, as Forge's reader would then write
        }
        sneaking = flag(values, "poses.sneaking");
        swimming = flag(values, "poses.swimming");
        crawling = flag(values, "poses.crawling");
        offhand = flag(values, "hands.offhand");
        attackCooldown = flag(values, "combat.attackCooldown");
        weaponStats = flag(values, "combat.weaponStats");
        noSwordBlocking = flag(values, "combat.noSwordBlocking");
        armor = flag(values, "combat.armor");
        enchantments = flag(values, "combat.enchantments");
        potions = flag(values, "combat.potions");
        food = flag(values, "combat.food");
    }

    private static boolean flag(Map<String, String> values, String key) {
        return !"false".equalsIgnoreCase(values.get(key));
    }
}
