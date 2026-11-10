package com.grihail.modernposes.client;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;

import com.grihail.modernposes.CommonProxy;
import com.grihail.modernposes.Features;

import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    /** Client tick counter. */
    public static long ticks;
    public static final Queue<Runnable> QUEUE = new ConcurrentLinkedQueue<>();

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        register(new ClientEvents());
        register(new HandClient());
        register(new CombatHud());
        if (Features.offhand) {
            cpw.mods.fml.client.registry.ClientRegistry.registerKeyBinding(HandClient.SWAP);
        }
    }

    @Override
    public void checkFeatures(int serverMask) {
        int differ = serverMask ^ com.grihail.modernposes.Features.mask();
        if (differ == 0) {
            return;
        }
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < com.grihail.modernposes.Features.NAMES.length; i++) {
            if ((differ & 1 << i) != 0) {
                names.append(names.length() == 0 ? "" : ", ")
                    .append(com.grihail.modernposes.Features.NAMES[i]);
            }
        }
        net.minecraft.util.ChatComponentTranslation msg = new net.minecraft.util.ChatComponentTranslation(
            "modernposes.config.mismatch",
            names.toString());
        msg.getChatStyle()
            .setColor(net.minecraft.util.EnumChatFormatting.YELLOW);
        net.minecraft.client.Minecraft.getMinecraft().ingameGUI.getChatGUI()
            .printChatMessage(msg);
    }

    @Override
    public boolean isLocalPlayer(EntityPlayer p) {
        return p instanceof EntityPlayerSP;
    }

    @Override
    public boolean handleMovement(EntityPlayer p, float strafe, float forward) {
        return p instanceof EntityPlayerSP && SwimMovement.tick((EntityPlayerSP) p, strafe, forward);
    }

    @Override
    public void runOnClient(Runnable r) {
        QUEUE.add(r);
    }
}
