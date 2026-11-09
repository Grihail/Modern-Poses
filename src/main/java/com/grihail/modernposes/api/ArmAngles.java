package com.grihail.modernposes.api;

/**
 * 26.3's arm poses as plain math (HumanoidModel.poseBlockingArm, the bow's pose, the attack swing), in radians with
 * ModelBiped's axes, so that any model of a body gets the same movement. Nothing here touches a model.
 */
public final class ArmAngles {

    private static final float PI = (float) Math.PI;
    private static final float RAD = PI / 180.0F;

    /** The part of the arm's walking swing that stays while it is raised. */
    public static final float BLOCK_SWING_KEPT = 0.5F;

    private ArmAngles() {}

    /**
     * An arm raised to block.
     *
     * @param baseX    the arm's x rotation before the pose (its walking swing)
     * @param headX    the head's x rotation
     * @param headY    the head's y rotation
     * @param rightArm the right arm
     * @return x and y rotation of the arm
     */
    public static float[] blocking(float baseX, float headX, float headY, boolean rightArm) {
        return new float[] { baseX * BLOCK_SWING_KEPT - 0.9424779F + clamp(headX, -PI * 4.0F / 9.0F, 0.43633232F),
            (rightArm ? -30.0F : 30.0F) * RAD + clamp(headY, -PI / 6.0F, PI / 6.0F) };
    }

    /**
     * Both arms aiming a bow.
     *
     * @param leftHolds the bow is in the left arm
     * @return y rotation of the right arm, y rotation of the left, x rotation of both
     */
    public static float[] bowAim(float headX, float headY, boolean leftHolds) {
        return leftHolds ? new float[] { -0.1F + headY - 0.4F, 0.1F + headY, -PI / 2.0F + headX }
            : new float[] { -0.1F + headY, 0.1F + headY + 0.4F, -PI / 2.0F + headX };
    }

    /**
     * The attack swing of one arm (HumanoidModel.setupAttackAnimation), from the swing's progress 0..1.
     *
     * @return the body's y rotation; what to add to the swinging arm's x, y and z rotation
     */
    public static float[] swing(float progress, float headX, boolean leftArm) {
        float bodyY = sin(sqrt(progress) * PI * 2.0F) * 0.2F * (leftArm ? -1.0F : 1.0F);
        float k = 1.0F - progress;
        k = 1.0F - k * k * k * k;
        float aa = sin(k * PI);
        float bb = sin(progress * PI) * -(headX - 0.7F) * 0.75F;
        return new float[] { bodyY, -(aa * 1.2F + bb), bodyY * 2.0F, sin(progress * PI) * -0.4F };
    }

    /**
     * The arm lifting a spyglass to the eye (HumanoidModel.poseRightArm / poseLeftArm: SPYGLASS).
     *
     * @return x and y rotation of the arm
     */
    public static float[] spyglass(float headX, float headY, boolean crouching, boolean rightArm) {
        float x = clamp(headX - 1.9198622F - (crouching ? PI / 12.0F : 0.0F), -2.4F, 3.3F);
        return new float[] { x, headY + (rightArm ? -PI / 12.0F : PI / 12.0F) };
    }

    private static float sin(float a) {
        return (float) Math.sin(a);
    }

    private static float sqrt(float a) {
        return (float) Math.sqrt(a);
    }

    private static float clamp(float v, float min, float max) {
        return v < min ? min : Math.min(v, max);
    }
}
