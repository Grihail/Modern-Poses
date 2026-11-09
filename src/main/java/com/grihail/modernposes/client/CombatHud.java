package com.grihail.modernposes.client;

import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import org.lwjgl.opengl.GL11;

import com.grihail.modernposes.combat.ArmorRules;
import com.grihail.modernposes.combat.Combat;
import com.grihail.modernposes.combat.CombatConfig;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/** 26.3's attack indicator (Hud) and the attributes in an item's tooltip (ItemAttributeModifiers). */
public final class CombatHud {

    private static final ResourceLocation CROSS_FULL = gui("crosshair_attack_indicator_full"),
        CROSS_BACK = gui("crosshair_attack_indicator_background"),
        CROSS_PROGRESS = gui("crosshair_attack_indicator_progress"),
        BAR_BACK = gui("hotbar_attack_indicator_background"), BAR_PROGRESS = gui("hotbar_attack_indicator_progress");

    private static ResourceLocation gui(String name) {
        return new ResourceLocation("modernposes", "textures/gui/" + name + ".png");
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) {
            return;
        }
        int width = e.resolution.getScaledWidth(), height = e.resolution.getScaledHeight();
        if (e.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS
            && "crosshair".equals(CombatConfig.attackIndicator)) {
            crosshair(mc, width, height);
        } else
            if (e.type == RenderGameOverlayEvent.ElementType.HOTBAR && "hotbar".equals(CombatConfig.attackIndicator)) {
                hotbar(mc, width, height);
            }
    }

    private static void crosshair(Minecraft mc, int width, int height) {
        float scale = Combat.strength(mc.thePlayer, 0.0F);
        boolean full = mc.pointedEntity instanceof EntityLivingBase && scale >= 1.0F
            && Combat.delay(mc.thePlayer) > 5.0F
            && mc.pointedEntity.isEntityAlive();
        if (!full && scale >= 1.0F) {
            return;
        }
        int x = width / 2 - 8, y = height / 2 - 7 + 16;
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, 1, 0);
        if (full) {
            blit(mc, CROSS_FULL, x, y, 0, 16, 16, 16, 16);
        } else {
            blit(mc, CROSS_BACK, x, y, 0, 16, 4, 16, 4);
            blit(mc, CROSS_PROGRESS, x, y, 0, (int) (scale * 17.0F), 4, 16, 4);
        }
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glDisable(GL11.GL_BLEND);
        mc.getTextureManager()
            .bindTexture(Gui.icons);
    }

    private static void hotbar(Minecraft mc, int width, int height) {
        float scale = Combat.strength(mc.thePlayer, 0.0F);
        if (scale >= 1.0F) {
            return;
        }
        int x = width / 2 + 91 + 6, y = height - 20;
        int progress = (int) (scale * 19.0F);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        blit(mc, BAR_BACK, x, y, 0, 18, 18, 18, 18);
        blit(mc, BAR_PROGRESS, x, y + 18 - progress, 18 - progress, 18, progress, 18, 18);
        GL11.glDisable(GL11.GL_BLEND);
        mc.getTextureManager()
            .bindTexture(Gui.icons);
    }

    private static void blit(Minecraft mc, ResourceLocation tex, int x, int y, int v, int w, int h, int texW,
        int texH) {
        mc.getTextureManager()
            .bindTexture(tex);
        Gui.func_146110_a(x, y, 0.0F, v, w, h, texW, texH);
    }

    // ------------------------------------------------------------------ tooltips

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent e) {
        if (e.entityPlayer == null) {
            return;
        }
        weapon(e.itemStack, e.toolTip, e.entityPlayer);
        armor(e.itemStack, e.toolTip);
    }

    /** 1.7.10's "+6 Attack Damage" becomes 26.3's " 7 Attack Damage" under "When in Main Hand:". */
    @SuppressWarnings("unchecked")
    private static void weapon(ItemStack stack, List<String> tip, EntityPlayer p) {
        String speedKey = Combat.ATTACK_SPEED.getAttributeUnlocalizedName();
        String damageKey = SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName();
        AttributeModifier speed = find(
            stack.getAttributeModifiers()
                .get(speedKey),
            Combat.SPEED_MODIFIER);
        if (speed == null) {
            return;
        }
        AttributeModifier damage = find(
            stack.getAttributeModifiers()
                .get(damageKey),
            Combat.DAMAGE_MODIFIER);
        int at = removeVanilla(tip, damageKey, damage);
        int speedAt = removeVanilla(tip, speedKey, speed);
        at = at < 0 ? speedAt : (speedAt < 0 ? at : Math.min(at, speedAt));
        if (at < 0) {
            at = tip.size();
        }
        tip.add(at++, EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.modifiers.mainhand"));
        double baseDamage = p.getEntityAttribute(SharedMonsterAttributes.attackDamage)
            .getBaseValue();
        tip.add(at++, line(baseDamage + (damage == null ? 0.0D : damage.getAmount()), damageKey));
        tip.add(
            at,
            line(
                p.getEntityAttribute(Combat.ATTACK_SPEED)
                    .getBaseValue() + speed.getAmount(),
                speedKey));
    }

    private static AttributeModifier find(Collection<?> modifiers, java.util.UUID id) {
        for (Object o : modifiers) {
            if (id.equals(((AttributeModifier) o).getID())) {
                return (AttributeModifier) o;
            }
        }
        return null;
    }

    /** ItemStack.getTooltip's line of this modifier taken out. @return where it was, or -1 */
    private static int removeVanilla(List<String> tip, String key, AttributeModifier m) {
        if (m == null || m.getAmount() == 0.0D) {
            return -1;
        }
        boolean plus = m.getAmount() > 0.0D;
        String line = (plus ? EnumChatFormatting.BLUE : EnumChatFormatting.RED)
            + StatCollector.translateToLocalFormatted(
                "attribute.modifier." + (plus ? "plus" : "take") + ".0",
                ItemStack.field_111284_a.format(Math.abs(m.getAmount())),
                StatCollector.translateToLocal("attribute.name." + key));
        int at = tip.indexOf(line);
        if (at >= 0) {
            tip.remove(at);
        }
        return at;
    }

    private static String line(double amount, String key) {
        return EnumChatFormatting.DARK_GREEN + " "
            + new java.text.DecimalFormat("#.##", java.text.DecimalFormatSymbols.getInstance(java.util.Locale.ROOT))
                .format(amount)
            + " "
            + StatCollector.translateToLocal("attribute.name." + key);
    }

    private static final String[] SLOTS = { "head", "chest", "legs", "feet" };

    /** 1.7.10 does not list armor points at all. */
    private static void armor(ItemStack stack, List<String> tip) {
        if (!(stack.getItem() instanceof ItemArmor)) {
            return;
        }
        ItemArmor armor = (ItemArmor) stack.getItem();
        if (armor.damageReduceAmount <= 0 || armor.armorType < 0 || armor.armorType >= SLOTS.length) {
            return;
        }
        tip.add("");
        tip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.modifiers." + SLOTS[armor.armorType]));
        tip.add(
            EnumChatFormatting.BLUE + "+"
                + armor.damageReduceAmount
                + " "
                + StatCollector.translateToLocal("attribute.name.generic.armor"));
        float tough = ArmorRules.toughness(stack);
        if (tough > 0.0F) {
            tip.add(
                EnumChatFormatting.BLUE + "+"
                    + ItemStack.field_111284_a.format(tough)
                    + " "
                    + StatCollector.translateToLocal("attribute.name.generic.armorToughness"));
        }
    }
}
