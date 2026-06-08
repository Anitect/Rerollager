package io.github.anitect.rerollager.cost;

import io.github.anitect.rerollager.config.PluginConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Computes and applies the per-reroll cost (items and/or XP), with optional scaling. */
@NullMarked
public final class CostService {

    public boolean canAfford(Player player, Villager villager, PluginConfig config) {
        PluginConfig.Cost cost = config.cost();
        double multiplier = multiplier(villager, cost);

        for (Map.Entry<Material, Integer> entry : effectiveItems(config, multiplier).entrySet()) {
            if (countItems(player, entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        if (cost.xpEnabled()) {
            int amount = scaled(cost.xpAmount(), multiplier);
            return cost.xpMode() == PluginConfig.XpMode.LEVELS
                    ? player.getLevel() >= amount
                    : player.getTotalExperience() >= amount;
        }
        return true;
    }

    public void charge(Player player, Villager villager, PluginConfig config) {
        PluginConfig.Cost cost = config.cost();
        double multiplier = multiplier(villager, cost);

        for (Map.Entry<Material, Integer> entry : effectiveItems(config, multiplier).entrySet()) {
            player.getInventory().removeItem(new ItemStack(entry.getKey(), entry.getValue()));
        }
        if (cost.xpEnabled()) {
            int amount = scaled(cost.xpAmount(), multiplier);
            if (cost.xpMode() == PluginConfig.XpMode.LEVELS) {
                player.setLevel(Math.max(0, player.getLevel() - amount));
            } else {
                // TODO: setTotalExperience does not perfectly resync the on-screen level bar;
                //       replace with a precise points-deduction helper before relying on POINTS mode.
                player.setTotalExperience(Math.max(0, player.getTotalExperience() - amount));
            }
        }
    }

    /** Configured item costs plus the consumed required-item, each scaled and merged. */
    private Map<Material, Integer> effectiveItems(PluginConfig config, double multiplier) {
        Map<Material, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<Material, Integer> entry : config.cost().items().entrySet()) {
            result.merge(entry.getKey(), scaled(entry.getValue(), multiplier), Integer::sum);
        }
        if (config.consumeRequiredItem() && !config.triggerRequireItem().isEmpty()) {
            Material required = Material.matchMaterial(config.triggerRequireItem());
            if (required != null) {
                result.merge(required, 1, Integer::sum);
            }
        }
        return result;
    }

    private double multiplier(Villager villager, PluginConfig.Cost cost) {
        double multiplier = 1.0;
        if (cost.scaleByLevel()) {
            List<Double> multipliers = cost.levelMultipliers();
            if (!multipliers.isEmpty()) {
                int index = Math.max(0, Math.min(multipliers.size() - 1, villager.getVillagerLevel() - 1));
                multiplier *= multipliers.get(index);
            }
        }
        if (cost.scaleByProfession()) {
            String profession = villager.getProfession().getKey().getKey().toLowerCase(Locale.ROOT);
            multiplier *= cost.professionMultipliers().getOrDefault(profession, 1.0);
        }
        return multiplier;
    }

    private static int scaled(int base, double multiplier) {
        return Math.max(0, (int) Math.ceil(base * multiplier));
    }

    private static int countItems(Player player, Material material) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == material) {
                total += stack.getAmount();
            }
        }
        return total;
    }
}
