package com.grihail.modernposes;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.launchwrapper.Launch;

import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** A part of the mod that is off in the config gets none of its mixins, so it leaves the game's code alone. */
public class MixinPlugin implements IMixinConfigPlugin {

    /** Mixin (relative to the mixin package) to the feature it belongs to; mixins not listed are always applied. */
    private static final Map<String, String> FEATURE = new HashMap<>();

    static {
        feature(
            "offhand",
            "MixinContainerPlayer",
            "MixinInventoryPlayer",
            "MixinEntityPlayerOffhand",
            "MixinNetHandlerPlayServerOffhand",
            "client.MixinEntityOtherPlayerMP",
            "client.MixinGuiContainerCreative",
            "client.MixinGuiInventory",
            "client.MixinMinecraftOffhand",
            "client.MixinPlayerControllerMPOffhand",
            "client.MixinItemRendererOffhand",
            "client.MixinNetHandlerPlayClientOffhand");
        feature(
            "attackCooldown",
            "MixinEntityPlayerCombat",
            "MixinEntityLivingBaseCombat",
            "client.MixinMinecraftCombat",
            "client.MixinPlayerControllerMPCombat",
            "client.MixinItemRendererCombat");
        feature("weaponStats", "MixinItemSword", "MixinItemTool");
        feature("noSwordBlocking", "MixinItemSwordBlocking");
        feature("armor", "MixinEntityPlayerArmor", "MixinEntityLivingBaseArmor");
        feature("enchantments", "MixinEnchantmentDamage");
        feature("potions", "MixinPotionAttackDamage");
        feature("food", "MixinFoodStats", "MixinItemAppleGold");
    }

    private static void feature(String name, String... mixins) {
        for (String m : mixins) {
            FEATURE.put(m, name);
        }
    }

    private String mixinPackage;

    @Override
    public void onLoad(String mixinPackage) {
        this.mixinPackage = mixinPackage + ".";
        Features.readEarly(new File(new File(Launch.minecraftHome, "config"), "modernposes.cfg"));
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String name = mixinClassName.startsWith(this.mixinPackage)
            ? mixinClassName.substring(this.mixinPackage.length())
            : mixinClassName;
        String feature = FEATURE.get(name);
        if (feature == null || Features.isOn(feature)) {
            return true;
        }
        LogManager.getLogger("modernposes")
            .info("{} is off in the config: {} not applied", feature, name);
        return false;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
