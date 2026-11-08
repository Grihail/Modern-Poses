package com.grihail.modernposes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import org.lwjgl.input.Keyboard;

import com.grihail.modernposes.api.RaisedBlockItem;
import com.grihail.modernposes.hands.HandUse;
import com.grihail.modernposes.hands.Hands;
import com.grihail.modernposes.net.CSwapHands;
import com.grihail.modernposes.net.PoseNet;
import com.grihail.modernposes.net.SHands;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;

/**
 * The client's side of the off hand: the right click tries the main hand, then the off hand (26.3's startUseItem), the
 * swap key, what the server says of the other players.
 */
public final class HandClient {

    public static final KeyBinding SWAP = new KeyBinding(
        "key.modernposes.swapHands",
        Keyboard.KEY_F,
        "key.categories.inventory");

    /** The sprite of the empty off hand slot. */
    public static IIcon emptySlot;

    private static boolean inside;
    private static boolean offPass;
    private static boolean consumed;
    private static boolean suppressMainUse;

    /**
     * Minecraft.func_147121_ag. {@code vanilla} is that method itself, which is run for the main hand and, if the main
     * hand did nothing, again with the hands' stacks swapped.
     *
     * @return true if vanilla's own run must be skipped (it ran here)
     */
    public static boolean rightClick(EntityClientPlayerMP p, Runnable vanilla) {
        if (inside) {
            return false;
        }
        Hands h = Hands.of(p);
        h.setUsedOff(false);
        ItemStack off = h.get();
        if (off == null) {
            return false;
        }
        ItemStack main = p.inventory.getCurrentItem();
        inside = true;
        consumed = false;
        // A shield in the off hand comes before the old blocking with a sword in the main hand
        suppressMainUse = off.getItem() instanceof RaisedBlockItem && main != null
            && main.getItemUseAction() == EnumAction.block
            && !(main.getItem() instanceof RaisedBlockItem);
        try {
            vanilla.run();
            suppressMainUse = false;
            if (consumed || p.isUsingItem()) {
                return true;
            }
            int swing = p.swingProgressInt;
            boolean swinging = p.isSwingInProgress;
            offPass = true;
            HandUse.swap(p);
            try {
                vanilla.run();
            } finally {
                HandUse.swap(p);
                offPass = false;
            }
            h.setUsedOff(p.isUsingItem() && p.getItemInUse() == h.get());
            if (p.isSwingInProgress && (!swinging || p.swingProgressInt != swing)) {
                h.swingOff = true;
                h.swingGrace = 3;
            }
        } finally {
            inside = false;
            suppressMainUse = false;
        }
        return true;
    }

    /** The second run of the right click, for the off hand. */
    public static boolean isOffPass() {
        return offPass;
    }

    /** The hand of this run did something: the next hand is not tried. */
    public static void markConsumed(boolean did) {
        consumed |= did;
    }

    /** The main hand's item must not be used in this run. */
    public static boolean suppressMainUse() {
        return suppressMainUse;
    }

    /** Client tick: a swing that ended is no hand's. */
    public static void tick(EntityPlayer p) {
        Hands h = Hands.of(p);
        if (h.swingGrace > 0) {
            h.swingGrace--;
        } else if (!p.isSwingInProgress) {
            h.swingOff = false;
        }
    }

    /** EntityOtherPlayerMP.onUpdate: another player's item in use, when it is the off hand's. */
    public static void remoteUse(EntityPlayer p) {
        Hands h = Hands.of(p);
        ItemStack off = h.get();
        if (h.wantsOff() && p.isEating() && off != null) {
            if (p.getItemInUse() != off) {
                p.setItemInUse(off, off.getMaxItemUseDuration());
            }
            h.remoteUse = true;
        } else if (h.remoteUse) {
            p.clearItemInUse();
            h.remoteUse = false;
        }
    }

    public static void onHands(SHands msg) {
        Entity e = world(msg.entityId);
        if (e instanceof EntityPlayer) {
            Hands h = Hands.of((EntityPlayer) e);
            h.set(msg.stack);
            h.setUsedOff(msg.usedOff);
        }
    }

    public static void onSwingOff(int entityId) {
        Entity e = world(entityId);
        if (e instanceof EntityPlayer) {
            Hands h = Hands.of((EntityPlayer) e);
            h.swingOff = true;
            h.swingGrace = 3;
        }
    }

    private static Entity world(int id) {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.theWorld == null ? null : mc.theWorld.getEntityByID(id);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent e) {
        if (SWAP.isPressed() && Minecraft.getMinecraft().thePlayer != null) {
            PoseNet.NET.sendToServer(new CSwapHands());
        }
    }

    /** A click a mod took for itself (cancelling the event) is a click used: the off hand does not repeat it. */
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (inside && e.world.isRemote && e.isCanceled()) {
            consumed = true;
        }
    }

    @SubscribeEvent
    public void onStitch(TextureStitchEvent.Pre e) {
        if (e.map.getTextureType() == 1) {
            emptySlot = e.map.registerIcon("modernposes:empty_slot_shield");
        }
    }
}
