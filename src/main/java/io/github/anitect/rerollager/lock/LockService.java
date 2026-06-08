package io.github.anitect.rerollager.lock;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

/**
 * Per-villager "trades locked" flag stored on the villager's PersistentDataContainer. Same
 * rationale as the cooldown store (see docs/adr/0002): it travels with the entity, survives
 * restarts, and is removed when the villager dies. A locked villager refuses rerolls.
 */
@NullMarked
public final class LockService {

    private final NamespacedKey key;

    public LockService(Plugin plugin) {
        this.key = new NamespacedKey(plugin, "locked");
    }

    public boolean isLocked(Villager villager) {
        Byte flag = villager.getPersistentDataContainer().get(key, PersistentDataType.BYTE);
        return flag != null && flag != 0;
    }

    public void setLocked(Villager villager, boolean locked) {
        if (locked) {
            villager.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        } else {
            villager.getPersistentDataContainer().remove(key);
        }
    }

    /** Flip the lock state. @return the new state (true = now locked). */
    public boolean toggle(Villager villager) {
        boolean next = !isLocked(villager);
        setLocked(villager, next);
        return next;
    }
}
