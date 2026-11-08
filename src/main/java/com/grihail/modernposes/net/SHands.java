package com.grihail.modernposes.net;

import net.minecraft.item.ItemStack;

import com.grihail.modernposes.ModernPosesMod;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Server -> client: another player's off hand, and whether the item in use is that hand's. */
public class SHands implements IMessage {

    public int entityId;
    public ItemStack stack;
    public boolean usedOff;

    public SHands() {}

    public SHands(int entityId, ItemStack stack, boolean usedOff) {
        this.entityId = entityId;
        this.stack = stack;
        this.usedOff = usedOff;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeBoolean(this.usedOff);
        ByteBufUtils.writeItemStack(buf, this.stack);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.usedOff = buf.readBoolean();
        this.stack = ByteBufUtils.readItemStack(buf);
    }

    public static class Handler implements IMessageHandler<SHands, IMessage> {

        @Override
        public IMessage onMessage(SHands msg, MessageContext ctx) {
            ModernPosesMod.proxy.runOnClient(() -> com.grihail.modernposes.client.HandClient.onHands(msg));
            return null;
        }
    }
}
