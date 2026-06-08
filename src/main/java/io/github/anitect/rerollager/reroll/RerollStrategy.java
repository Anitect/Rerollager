package io.github.anitect.rerollager.reroll;

import org.bukkit.entity.Villager;
import org.jspecify.annotations.NullMarked;

/**
 * Regenerates a villager's trade offers. Implementations decide <em>how</em> the offers are
 * produced; the rest of the plugin (trigger, cooldown, cost, persistence) is strategy-agnostic.
 *
 * <p>v1 ships a single NMS-based implementation (see {@code nms.NmsRerollStrategy}); this seam
 * exists so a future clean Paper API or a no-NMS fallback can be swapped in without touching the
 * surrounding code (see docs/adr/0001).
 */
@NullMarked
public interface RerollStrategy {

    /**
     * Regenerate {@code villager}'s offers at its current level, preserving profession, level,
     * accumulated XP, and custom name.
     *
     * @return {@code true} if the offers were regenerated; {@code false} if generation could not
     *         be performed (e.g. no trade pool for the profession).
     */
    boolean reroll(Villager villager);
}
