package com.grihail.modernposes.api;

/** Hitbox and eye height of one pose, in blocks. Width is the square footprint, like vanilla. */
public final class PoseSize {

    /** Vanilla kicks for "Illegal stance" when the eye is less than this above the feet. */
    public static final float MIN_EYE = 0.1F;

    public final String name;
    public final float width, height, eye;

    public PoseSize(String name, float width, float height, float eye) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("pose without a name");
        if (width < 0.1F || width > 4F) throw new IllegalArgumentException(name + ": width must be within 0.1..4");
        if (height < 0.1F || height > 4F) throw new IllegalArgumentException(name + ": height must be within 0.1..4");
        if (eye < MIN_EYE || eye > height)
            throw new IllegalArgumentException(name + ": eye must be within 0.1..height");
        this.name = name;
        this.width = width;
        this.height = height;
        this.eye = eye;
    }

    /** Poses drawn lying down (the swim / crawl rotation of the player model). */
    public boolean isHorizontal() {
        return "swim".equals(name) || "crawl".equals(name);
    }

    /** The gliding pose (drawn by its own rules, not lying like swim / crawl). */
    public boolean isFallFlying() {
        return PoseSet.FALL_FLYING.equals(name);
    }

    @Override
    public String toString() {
        return name + " " + width + "x" + height + " eye " + eye;
    }
}
