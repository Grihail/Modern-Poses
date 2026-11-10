package com.grihail.modernposes.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.grihail.modernposes.Features;
import com.grihail.modernposes.hands.Hands;

/**
 * The player's two hands, for other mods: 26.3's off hand (a slot of its own, swapped with the main hand by a key,
 * used when the main hand does nothing), which hand is in use, what each arm is doing. Works on both sides for every
 * player the side knows.
 * <p>
 * A mod that draws its own body reads {@link #armPose} and {@link ArmAngles}, and draws what is held with
 * {@link #renderHeld}.
 */
public final class ModernHands {

    private static final Map<Item, HeldModel> MODELS = new ConcurrentHashMap<>();

    private ModernHands() {}

    /** What the off hand holds, or null. */
    public static ItemStack getOffhand(EntityPlayer p) {
        return Hands.of(p)
            .get();
    }

    /** Server: puts a stack in the off hand; it reaches the clients by itself. */
    public static void setOffhand(EntityPlayer p, ItemStack stack) {
        if (Features.offhand) {
            Hands.of(p)
                .set(stack);
        }
    }

    /** What a hand holds, or null. */
    public static ItemStack getHeld(EntityPlayer p, Hand hand) {
        return hand == Hand.OFF ? getOffhand(p) : p.inventory.getCurrentItem();
    }

    /** Puts a stack in a hand (null empties it). */
    public static void setHeld(EntityPlayer p, Hand hand, ItemStack stack) {
        if (hand == Hand.OFF) {
            setOffhand(p, stack);
        } else {
            p.inventory.mainInventory[p.inventory.currentItem] = stack;
        }
    }

    /** The hand whose item is in use, or null when none is. */
    public static Hand usedHand(EntityPlayer p) {
        if (!p.isUsingItem()) {
            return null;
        }
        return Hands.of(p)
            .usedOff() ? Hand.OFF : Hand.MAIN;
    }

    /** The hand of the swing going on (the main one when none is). */
    public static Hand swingingHand(EntityPlayer p) {
        return Hands.of(p).swingOff ? Hand.OFF : Hand.MAIN;
    }

    /** What the arm of a hand is doing. The main hand is the right arm, the off hand the left. */
    public static ArmPose armPose(EntityPlayer p, Hand hand) {
        ItemStack stack = getHeld(p, hand);
        if (stack == null) {
            return ArmPose.EMPTY;
        }
        if (usedHand(p) == hand && stack.getItem() instanceof ScopeItem) {
            return ArmPose.SPYGLASS;
        }
        if (stack.getItem() instanceof KineticItem) {
            return ArmPose.SPEAR;
        }
        if (usedHand(p) == hand) {
            EnumAction action = stack.getItemUseAction();
            if (action == EnumAction.block) {
                return stack.getItem() instanceof RaisedBlockItem ? ArmPose.BLOCK : ArmPose.SWORD_BLOCK;
            }
            if (action == EnumAction.bow) {
                return ArmPose.BOW_AND_ARROW;
            }
        }
        return ArmPose.ITEM;
    }

    /** Ticks the item in use has been used (with the frame's part), 0 when that hand uses nothing. */
    public static float useTicks(EntityPlayer p, Hand hand, float partialTicks) {
        if (usedHand(p) != hand || p.getItemInUse() == null) {
            return 0.0F;
        }
        return p.getItemInUse()
            .getMaxItemUseDuration() - p.getItemInUseCount()
            + partialTicks;
    }

    /** The swing's progress 0..1 of a hand (0 when the other hand swings or none does). */
    public static float swingProgress(EntityPlayer p, Hand hand, float partialTicks) {
        return swingingHand(p) == hand ? p.getSwingProgress(partialTicks) : 0.0F;
    }

    /** Draws an item as a model when held (call during init on the client). */
    public static void registerHeldModel(Item item, HeldModel model) {
        MODELS.put(item, model);
    }

    /** The model that draws an item when held, or null (1.7.10's item drawing). */
    public static HeldModel heldModel(ItemStack stack) {
        return stack == null ? null : MODELS.get(stack.getItem());
    }

    /**
     * Client: draws what a hand holds, as the player's model does, with the matrix at the arm's pivot (after
     * ModelRenderer.postRender of the arm, or at the hand of another body).
     */
    public static void renderHeld(EntityPlayer p, Hand hand, float partialTicks) {
        com.grihail.modernposes.client.HandRender.atArm(p, hand, partialTicks);
    }
}
