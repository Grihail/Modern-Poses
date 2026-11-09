package com.grihail.modernposes.client;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

import com.grihail.modernposes.api.StabItem;
import com.grihail.modernposes.combat.Combat;
import com.grihail.modernposes.net.CPunch;
import com.grihail.modernposes.net.PoseNet;

/** The client's half of 26.3's Minecraft.startAttack. */
public final class CombatClient {

    private CombatClient() {}

    /** Minecraft.func_147116_af, before vanilla's run. A swing at the air starts the charge over on both sides. */
    public static void leftClick(Minecraft mc, EntityClientPlayerMP p, int missCounter) {
        if (missCounter > 0) {
            return;
        }
        ItemStack held = p.inventory.getCurrentItem();
        if (held != null && held.getItem() instanceof StabItem) {
            return; // a stab is its item's business (26.3: PiercingWeapon returns early)
        }
        MovingObjectPosition hit = mc.objectMouseOver;
        if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            return;
        }
        boolean block = hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
            && mc.theWorld.getBlock(hit.blockX, hit.blockY, hit.blockZ)
                .getMaterial() != Material.air;
        if (!block) {
            Combat.reset(p);
        }
        PoseNet.NET.sendToServer(new CPunch());
    }
}
