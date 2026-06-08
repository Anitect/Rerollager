package io.github.anitect.rerollager;

import io.github.anitect.rerollager.command.RerollagerCommand;
import io.github.anitect.rerollager.config.PluginConfig;
import io.github.anitect.rerollager.cooldown.CooldownService;
import io.github.anitect.rerollager.cost.CostService;
import io.github.anitect.rerollager.listener.RerollListener;
import io.github.anitect.rerollager.lock.LockService;
import io.github.anitect.rerollager.reroll.RerollService;
import io.github.anitect.rerollager.reroll.RerollStrategy;
import io.github.anitect.rerollager.reroll.nms.NmsRerollStrategy;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class RerollagerPlugin extends JavaPlugin {

    private volatile PluginConfig config;
    private LockService lockService;
    private RerollService rerollService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.config = PluginConfig.from(getConfig());

        // The version-fragile reroll engine is the only NMS-touching component (see docs/adr/0001).
        RerollStrategy strategy = new NmsRerollStrategy();
        this.lockService = new LockService(this);
        CooldownService cooldowns = new CooldownService(this);
        CostService costs = new CostService();
        this.rerollService = new RerollService(this, strategy, lockService, cooldowns, costs);

        warnIfTriggerCollision();

        getServer().getPluginManager().registerEvents(new RerollListener(this), this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(
                        "rerollager",
                        "Rerollager admin & info commands",
                        List.of("rrl"),
                        new RerollagerCommand(this)));

        // TODO(v1): bStats metrics behind config.metricsEnabled().
        // TODO(v1): Modrinth update check behind config.updateCheck().

        getSLF4JLogger().info("Enabled. Sneak + right-click a professional villager to reroll its trades.");
    }

    /** Reload config.yml and rebuild the immutable config snapshot. */
    public void reload() {
        reloadConfig();
        this.config = PluginConfig.from(getConfig());
    }

    public PluginConfig config() {
        return config;
    }

    public LockService lockService() {
        return lockService;
    }

    public RerollService rerollService() {
        return rerollService;
    }

    /** Lock and reroll share the sneak+right-click gesture, distinguished by item-in-hand; warn if they'd clash. */
    private void warnIfTriggerCollision() {
        String lockItem = config.lockItem();
        String rerollItem = config.triggerRequireItem();
        if (config.lockEnabled() && !lockItem.isEmpty() && lockItem.equalsIgnoreCase(rerollItem)) {
            getSLF4JLogger().warn("lock.item and trigger.require-item are both '{}'; the lock gesture "
                    + "will take precedence and rerolls via that item won't fire. Use different items.", lockItem);
        }
    }
}
