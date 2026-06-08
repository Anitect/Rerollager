package io.github.anitect.rerollager.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Immutable snapshot of config.yml. Rebuilt on {@code /rerollager reload}. */
@NullMarked
public record PluginConfig(
        String triggerRequireItem,
        boolean consumeRequiredItem,
        boolean cooldownEnabled,
        int cooldownSeconds,
        boolean lockEnabled,
        String lockItem,
        Cost cost,
        Messages messages,
        SoundSpec sound,
        LockSound lockSound,
        boolean updateCheck,
        boolean metricsEnabled) {

    public enum XpMode { LEVELS, POINTS }

    public record Cost(
            boolean enabled,
            Map<Material, Integer> items,
            boolean xpEnabled,
            XpMode xpMode,
            int xpAmount,
            boolean scaleByLevel,
            List<Double> levelMultipliers,
            boolean scaleByProfession,
            Map<String, Double> professionMultipliers) {}

    public record Messages(String notEligible, String onCooldown, String cannotAfford, String success,
                           String locked, String unlocked, String rerollDeniedLocked) {}

    public record SoundSpec(boolean enabled, String key, float volume, float pitch) {}

    public record LockSound(boolean enabled, String lockKey, String unlockKey, float volume, float pitch) {}

    public static PluginConfig from(FileConfiguration c) {
        return new PluginConfig(
                c.getString("trigger.require-item", ""),
                c.getBoolean("trigger.consume-required-item", false),
                c.getBoolean("cooldown.enabled", true),
                c.getInt("cooldown.seconds", 30),
                c.getBoolean("lock.enabled", true),
                c.getString("lock.item", "TRIPWIRE_HOOK"),
                readCost(c),
                new Messages(
                        c.getString("messages.not-eligible", ""),
                        c.getString("messages.on-cooldown",
                                "<red>You must wait <time> before rerolling this villager again.</red>"),
                        c.getString("messages.cannot-afford",
                                "<red>You can't afford to reroll this villager.</red>"),
                        c.getString("messages.success", "<green>Trades rerolled.</green>"),
                        c.getString("messages.locked", "<green>Trades locked.</green>"),
                        c.getString("messages.unlocked", "<yellow>Trades unlocked.</yellow>"),
                        c.getString("messages.reroll-denied-locked",
                                "<red>This villager's trades are locked. Unlock them first.</red>")),
                new SoundSpec(
                        c.getBoolean("feedback.sound.enabled", true),
                        c.getString("feedback.sound.key", "minecraft:entity.villager.yes"),
                        (float) c.getDouble("feedback.sound.volume", 1.0),
                        (float) c.getDouble("feedback.sound.pitch", 1.0)),
                new LockSound(
                        c.getBoolean("feedback.lock-sound.enabled", true),
                        c.getString("feedback.lock-sound.lock-key", "minecraft:block.chain.place"),
                        c.getString("feedback.lock-sound.unlock-key", "minecraft:block.chain.break"),
                        (float) c.getDouble("feedback.lock-sound.volume", 1.0),
                        (float) c.getDouble("feedback.lock-sound.pitch", 1.0)),
                c.getBoolean("updates.check-on-startup", true),
                c.getBoolean("metrics.enabled", true));
    }

    private static Cost readCost(FileConfiguration c) {
        Map<Material, Integer> items = new LinkedHashMap<>();
        ConfigurationSection itemsSection = c.getConfigurationSection("cost.items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                Material material = Material.matchMaterial(key);
                if (material != null) {
                    items.put(material, Math.max(1, itemsSection.getInt(key)));
                }
            }
        }

        XpMode xpMode = "points".equalsIgnoreCase(c.getString("cost.xp.mode", "levels"))
                ? XpMode.POINTS : XpMode.LEVELS;

        Map<String, Double> professionMultipliers = new HashMap<>();
        ConfigurationSection professionSection =
                c.getConfigurationSection("cost.scaling.profession-multipliers");
        if (professionSection != null) {
            for (String key : professionSection.getKeys(false)) {
                professionMultipliers.put(key.toLowerCase(Locale.ROOT),
                        professionSection.getDouble(key, 1.0));
            }
        }

        List<Double> levelMultipliers = c.getDoubleList("cost.scaling.level-multipliers");

        return new Cost(
                c.getBoolean("cost.enabled", false),
                items,
                c.getBoolean("cost.xp.enabled", false),
                xpMode,
                c.getInt("cost.xp.amount", 5),
                c.getBoolean("cost.scaling.by-level", false),
                levelMultipliers,
                c.getBoolean("cost.scaling.by-profession", false),
                professionMultipliers);
    }
}
