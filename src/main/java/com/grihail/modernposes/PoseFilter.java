package com.grihail.modernposes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.grihail.modernposes.api.PoseSet;
import com.grihail.modernposes.api.PoseSize;

/** The poses turned off in the config are taken out of every set, the mod's own and other mods'. */
public final class PoseFilter {

    /** PoseSet has no equals: one filtered copy per set object. */
    private static final Map<PoseSet, PoseSet> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    private PoseFilter() {}

    public static PoseSet apply(PoseSet set) {
        if (set == null || set == PoseSet.WAIT || Features.sneaking && Features.swimming && Features.crawling) {
            return set;
        }
        PoseSet filtered = CACHE.get(set);
        if (filtered == null) {
            filtered = filter(set);
            CACHE.put(set, filtered);
        }
        return filtered;
    }

    private static PoseSet filter(PoseSet set) {
        List<PoseSize> poses = new ArrayList<>();
        for (PoseSize p : set.poses.values()) {
            if (!off(p.name)) {
                poses.add(p); // fall_flying is in the set already, with the sizes it had
            }
        }
        boolean swim = set.modernSwim && Features.swimming;
        if (poses.size() == set.poses.size() && swim == set.modernSwim) {
            return set;
        }
        List<String> fallback = new ArrayList<>();
        for (String n : set.fallback) {
            if (!off(n)) {
                fallback.add(n);
            }
        }
        return new PoseSet(poses, fallback, swim);
    }

    private static boolean off(String pose) {
        return "sneak".equals(pose) && !Features.sneaking || "swim".equals(pose) && !Features.swimming
            || "crawl".equals(pose) && !Features.crawling;
    }
}
