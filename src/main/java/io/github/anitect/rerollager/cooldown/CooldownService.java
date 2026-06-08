package io.github.anitect.rerollager.cooldown;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

/**
 * Per-(player, villager) cooldown stored on the villager's PersistentDataContainer: a nested
 * container mapping each player's key to the epoch-millis of their last reroll. It travels with
 * the entity, survives restarts, and is removed when the villager dies (see docs/adr/0002).
 */
@NullMarked
public final class CooldownService {

    private final Plugin plugin;
    private final NamespacedKey rootKey;

    public CooldownService(Plugin plugin) {
        this.plugin = plugin;
        this.rootKey = new NamespacedKey(plugin, "cooldowns");
    }

    /** Milliseconds remaining before {@code player} may reroll {@code villager} again; 0 if ready. */
    public long remaining(Villager villager, Player player, int cooldownSeconds) {
        PersistentDataContainer map =
                villager.getPersistentDataContainer().get(rootKey, PersistentDataType.TAG_CONTAINER);
        if (map == null) {
            return 0L;
        }
        Long last = map.get(playerKey(player), PersistentDataType.LONG);
        if (last == null) {
            return 0L;
        }
        long elapsed = System.currentTimeMillis() - last;
        long window = cooldownSeconds * 1000L;
        return elapsed >= window ? 0L : window - elapsed;
    }

    /** Record now as {@code player}'s most recent reroll of {@code villager}. */
    public void stamp(Villager villager, Player player) {
        PersistentDataContainer pdc = villager.getPersistentDataContainer();
        PersistentDataContainer map = pdc.get(rootKey, PersistentDataType.TAG_CONTAINER);
        if (map == null) {
            map = pdc.getAdapterContext().newPersistentDataContainer();
        }
        map.set(playerKey(player), PersistentDataType.LONG, System.currentTimeMillis());
        pdc.set(rootKey, PersistentDataType.TAG_CONTAINER, map);
    }

    private NamespacedKey playerKey(Player player) {
        // UUIDs are lowercase with hyphens, both valid in a NamespacedKey value.
        return new NamespacedKey(plugin, "p_" + player.getUniqueId());
    }
}
