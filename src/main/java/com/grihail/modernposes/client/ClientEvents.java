package com.grihail.modernposes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;

import com.grihail.modernposes.PoseState;
import com.grihail.modernposes.Sizes;
import com.grihail.modernposes.api.ModernHands;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public final class ClientEvents {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (e.phase == TickEvent.Phase.START) {
            ClientProxy.ticks++;
            Runnable r;
            while ((r = ClientProxy.QUEUE.poll()) != null) r.run();
            return;
        }
        if (mc.theWorld == null) return;
        // re-assert sizes: entities are recreated (respawn, re-tracking) and vanilla resets size (waking up)
        for (Object o : mc.theWorld.playerEntities) {
            EntityPlayer p = (EntityPlayer) o;
            PoseState s = PoseState.get(p);
            if (s != null && s.pose != null) Sizes.apply(p, s.pose);
            HandClient.tick(p);
        }
        if (!mc.isGamePaused()) ClientPoses.tick(mc);
    }

    /** An item drawn as a model is drawn by HandRender, at 26.3's place. */
    @SubscribeEvent
    public void onSpecials(RenderPlayerEvent.Specials.Pre e) {
        if (ModernHands.heldModel(e.entityPlayer.inventory.getCurrentItem()) != null) {
            e.renderItem = false;
        }
    }

    /** F3 screen: pose and water movement numbers (to diagnose swimming problems). */
    @SubscribeEvent
    public void onDebugText(RenderGameOverlayEvent.Text e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!mc.gameSettings.showDebugInfo || mc.thePlayer == null) return;
        EntityClientPlayerMP p = mc.thePlayer;
        PoseState s = PoseState.get(p);
        if (s == null) return;
        boolean modern = SwimMovement.lastTick >= ClientProxy.ticks - 1;
        double[] flow = WaterFlow.flow(p);
        e.left.add("");
        e.left.add(
            String.format(
                "[Poses] pose=%s swimming=%s sprint=%s water=%s",
                s.pose == null ? "-" : s.pose.name,
                s.swimming,
                p.isSprinting(),
                p.isInWater() ? (modern ? "modern" : "vanilla") : "-"));
        e.left.add(
            String.format(
                "[Poses] eyesIn=%s motion=%.3f %.3f %.3f flow=%.2f %.2f %.2f",
                p.isInsideOfMaterial(net.minecraft.block.material.Material.water),
                p.motionX,
                p.motionY,
                p.motionZ,
                flow[0],
                flow[1],
                flow[2]));
    }

    static boolean own(EntityPlayer p) {
        return p instanceof EntityPlayerSP;
    }
}
