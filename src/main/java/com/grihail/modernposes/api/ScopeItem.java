package com.grihail.modernposes.api;

/**
 * An item looked through while used (26.3's ItemUseAnimation.SPYGLASS). Its holder's arm lifts it to the eye
 * ({@link ArmPose#SPYGLASS}, {@link ArmAngles#spyglass}); in the third person it is drawn at the head
 * ({@link HeldModel#renderAtEye}), in the first person the hands are not drawn while it is used.
 */
public interface ScopeItem {
}
