package com.grihail.modernposes.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The poses a player can take, as a {@link PoseProvider} gives them.
 * <ul>
 * <li>{@code stand} is required; {@code sneak} is taken while holding sneak (or under a low ceiling), {@code swim}
 * while sprint-swimming, {@code crawl} under a ceiling too low for sneaking, {@code sprint} (optional) while
 * sprinting on land — a low running pose, e.g. a fox on four paws through one block holes;</li>
 * <li>{@code fallback}: poses tried in order when the wanted one does not fit (vanilla: stand, sneak, crawl; put a
 * low {@code sprint} last so stopping keeps it while nothing bigger fits);</li>
 * <li>{@code fall_flying}: gliding (on elytra), taken when a {@link PoseTrigger} asks for it. A set without it glides
 * with the sizes of its {@code swim} pose, or of {@code stand} if it has no {@code swim} either (the set adds that
 * {@code fall_flying} itself, so every set has one);</li>
 * <li>{@code modernSwim}: modern water movement and sprint-swimming (needs a {@code swim} pose). False for players
 * that move by their own rules in water.</li>
 * </ul>
 */
public final class PoseSet {

    /** Name of the gliding pose (26.3's Pose.FALL_FLYING). */
    public static final String FALL_FLYING = "fall_flying";

    private static final PoseSize STAND = new PoseSize("stand", 0.6F, 1.8F, 1.62F);

    /** The modern player (Minecraft wiki: Player, Sneaking, Swimming, Crawling). */
    public static final PoseSet VANILLA = new PoseSet(
        Arrays.asList(
            STAND,
            new PoseSize("sneak", 0.6F, 1.5F, 1.27F),
            new PoseSize("swim", 0.6F, 0.6F, 0.4F),
            new PoseSize("crawl", 0.6F, 0.6F, 0.4F),
            new PoseSize(FALL_FLYING, 0.6F, 0.6F, 0.4F)),
        Arrays.asList("stand", "sneak", "crawl"),
        true);

    /**
     * Client only: the provider's data for this player has not arrived yet. The player keeps the pose the server gave
     * and decides nothing until then.
     */
    public static final PoseSet WAIT = new PoseSet(
        Collections.singletonList(STAND),
        Collections.singletonList("stand"),
        false);

    public final Map<String, PoseSize> poses;
    public final List<String> fallback;
    public final boolean modernSwim;

    public PoseSet(List<PoseSize> poses, List<String> fallback, boolean modernSwim) {
        Map<String, PoseSize> m = new LinkedHashMap<>();
        for (PoseSize p : poses) m.put(p.name, p);
        if (!m.containsKey("stand")) throw new IllegalArgumentException("a pose set needs \"stand\"");
        if (!m.containsKey(FALL_FLYING)) { // the only place of this rule: glide with the swim (or stand) sizes
            PoseSize like = m.containsKey("swim") ? m.get("swim") : m.get("stand");
            m.put(FALL_FLYING, new PoseSize(FALL_FLYING, like.width, like.height, like.eye));
        }
        List<String> fb = new ArrayList<>();
        for (String n : fallback == null ? Collections.<String>emptyList() : fallback) {
            if (!m.containsKey(n)) throw new IllegalArgumentException("fallback refers to unknown pose '" + n + "'");
            if (!fb.contains(n)) fb.add(n);
        }
        if (fb.isEmpty()) fb.add("stand");
        this.poses = Collections.unmodifiableMap(m);
        this.fallback = Collections.unmodifiableList(fb);
        this.modernSwim = modernSwim;
    }

    public PoseSize get(String name) {
        return name == null ? null : poses.get(name);
    }

    public PoseSize stand() {
        return poses.get("stand");
    }

    /** Modern water movement and sprint-swimming are on. */
    public boolean canSwim() {
        return modernSwim && poses.containsKey("swim");
    }
}
