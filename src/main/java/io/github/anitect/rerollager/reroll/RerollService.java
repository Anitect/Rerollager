package io.github.anitect.rerollager.reroll;

import io.github.anitect.rerollager.RerollagerPlugin;
import io.github.anitect.rerollager.config.PluginConfig;
import io.github.anitect.rerollager.cooldown.CooldownService;
import io.github.anitect.rerollager.cost.CostService;
import io.github.anitect.rerollager.lock.LockService;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.jspecify.annotations.NullMarked;

/** Orchestrates a reroll attempt: eligibility -> lock -> cooldown -> cost -> strategy -> bookkeeping. */
@NullMarked
public final class RerollService {

    private static final String BYPASS_LOCK = "rerollager.bypass.lock";
    private static final String BYPASS_COOLDOWN = "rerollager.bypass.cooldown";
    private static final String BYPASS_COST = "rerollager.bypass.cost";

    private final RerollagerPlugin plugin;
    private final RerollStrategy strategy;
    private final LockService locks;
    private final CooldownService cooldowns;
    private final CostService costs;

    public RerollService(RerollagerPlugin plugin, RerollStrategy strategy,
                         LockService locks, CooldownService cooldowns, CostService costs) {
        this.plugin = plugin;
        this.strategy = strategy;
        this.locks = locks;
        this.cooldowns = cooldowns;
        this.costs = costs;
    }

    /** A rerollable villager: an adult with a real profession and at least one trade. */
    public static boolean isEligible(Villager villager) {
        if (!villager.isAdult()) {
            return false;
        }
        Villager.Profession profession = villager.getProfession();
        if (profession.equals(Villager.Profession.NONE) || profession.equals(Villager.Profession.NITWIT)) {
            return false;
        }
        return !villager.getRecipes().isEmpty();
    }

    public RerollOutcome attempt(Player player, Villager villager) {
        if (!isEligible(villager)) {
            return RerollOutcome.of(RerollOutcome.Status.NOT_ELIGIBLE);
        }

        PluginConfig config = plugin.config();

        if (config.lockEnabled() && locks.isLocked(villager) && !player.hasPermission(BYPASS_LOCK)) {
            return RerollOutcome.of(RerollOutcome.Status.LOCKED);
        }

        boolean gated = config.cooldownEnabled() && !player.hasPermission(BYPASS_COOLDOWN);
        boolean charging = config.cost().enabled() && !player.hasPermission(BYPASS_COST);

        if (gated) {
            long remaining = cooldowns.remaining(villager, player, config.cooldownSeconds());
            if (remaining > 0) {
                return RerollOutcome.onCooldown(remaining);
            }
        }
        if (charging && !costs.canAfford(player, villager, config)) {
            return RerollOutcome.of(RerollOutcome.Status.CANNOT_AFFORD);
        }

        if (!strategy.reroll(villager)) {
            return RerollOutcome.of(RerollOutcome.Status.FAILED);
        }

        // Only charge / stamp after a confirmed successful reroll.
        if (charging) {
            costs.charge(player, villager, config);
        }
        if (gated) {
            cooldowns.stamp(villager, player);
        }
        return RerollOutcome.of(RerollOutcome.Status.SUCCESS);
    }
}
