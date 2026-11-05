package com.grihail.modernposes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import com.grihail.modernposes.Sizes;

/**
 * Camera at the pose's eye: vanilla puts it at feet + 1.62 whatever the hitbox is; {@link #eyeOffset} moves it (and
 * the third person rays) to feet + (smoothed eye height), so pose changes glide like in modern Minecraft.
 */
public final class PoseCamera {

    public static float partialTicks;

    private PoseCamera() {}

    /**
     * Replaces orientCamera's {@code f1 = yOffset - 1.62} (vanilla: the eye is always 1.62 above the feet) with
     * {@code yOffset - eye}: the smoothed eye (it jumps down at once when a ceiling is right above), so the camera and
     * the third person rays never start inside the ceiling the box fits under.
     */
    public static float eyeOffset(float vanilla) {
        EntityLivingBase e = Minecraft.getMinecraft().renderViewEntity;
        if (!(e instanceof EntityPlayer) || e.isPlayerSleeping() || !Sizes.hasPose(e)) return vanilla;
        float eye = ClientPoses.eye(e, partialTicks);
        if (Float.isNaN(eye)) eye = e.yOffset;
        return e.yOffset - Math.max(0.05F, eye);
    }
}
