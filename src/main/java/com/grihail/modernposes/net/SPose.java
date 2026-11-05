package com.grihail.modernposes.net;

import com.grihail.modernposes.ModernPosesMod;
import com.grihail.modernposes.api.PoseSize;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Server -> client: player {@code entityId} is now in this pose (with its sizes, so other players' boxes are right
 * without knowing their poses). Sent to the other players on every change, and to the player itself when the server
 * rejected its request ({@code correction}) or changed the pose on its own (login, another set of poses).
 */
public class SPose implements IMessage {

    public int entityId;
    public PoseSize pose;
    public boolean correction;

    public SPose() {}

    public SPose(int entityId, PoseSize pose, boolean correction) {
        this.entityId = entityId;
        this.pose = pose;
        this.correction = correction;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(correction);
        ByteBufUtils.writeUTF8String(buf, pose.name);
        buf.writeFloat(pose.width);
        buf.writeFloat(pose.height);
        buf.writeFloat(pose.eye);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        correction = buf.readBoolean();
        pose = new PoseSize(ByteBufUtils.readUTF8String(buf), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public static class Handler implements IMessageHandler<SPose, IMessage> {

        @Override
        public IMessage onMessage(SPose msg, MessageContext ctx) {
            ModernPosesMod.proxy.runOnClient(() -> com.grihail.modernposes.client.ClientPoses.onPose(msg));
            return null;
        }
    }
}
