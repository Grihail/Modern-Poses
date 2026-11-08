package com.grihail.modernposes.net;

import com.grihail.modernposes.ModernPosesMod;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Server -> client: the swing another player is starting is its off hand's. */
public class SSwingOff implements IMessage {

    public int entityId;

    public SSwingOff() {}

    public SSwingOff(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
    }

    public static class Handler implements IMessageHandler<SSwingOff, IMessage> {

        @Override
        public IMessage onMessage(SSwingOff msg, MessageContext ctx) {
            ModernPosesMod.proxy.runOnClient(() -> com.grihail.modernposes.client.HandClient.onSwingOff(msg.entityId));
            return null;
        }
    }
}
