package io.github.anitect.rerollager.reroll.nms;

import io.github.anitect.rerollager.reroll.RerollStrategy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.item.trading.MerchantOffers;
import org.bukkit.craftbukkit.entity.CraftVillager;
import org.bukkit.entity.Villager;
import org.jspecify.annotations.NullMarked;

/**
 * Regenerates a villager's offers at its current level using Minecraft's own data-driven trade
 * generation, preserving profession, level, XP, custom name, and villager type.
 *
 * <p>Since 26.x, villager trades are data-driven: a {@code VillagerProfession} maps each level to a
 * {@code ResourceKey<TradeSet>}, and {@code Villager#updateTrades(ServerLevel, int)} appends the
 * current level's {@code TradeSet} offers to the villager's offer list (the no-arg vanilla path
 * calls it with {@code -1} = the TradeSet's own trade count). Vanilla accumulates all tiers by
 * calling this once per level while leveling up; we reproduce that by clearing the offers and
 * walking levels 1..current, then restoring the original {@link VillagerData}.
 *
 * <p>This is the only NMS-touching component and the one expected to break on Minecraft updates
 * (see docs/adr/0001). XP lives in a separate {@code villagerXp} field and the custom name is a
 * generic entity property, so neither is part of {@code VillagerData} and both survive untouched.
 */
@NullMarked
public final class NmsRerollStrategy implements RerollStrategy {

    /** Sentinel meaning "use the TradeSet's configured number of trades" (vanilla's default). */
    private static final int DEFAULT_TRADE_COUNT = -1;

    @Override
    public boolean reroll(Villager bukkitVillager) {
        net.minecraft.world.entity.npc.villager.Villager handle =
                ((CraftVillager) bukkitVillager).getHandle();

        // Trade generation needs a server level (loot-context driven); should always hold in practice.
        if (!(handle.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        VillagerData original = handle.getVillagerData();
        int level = original.level();
        if (level < 1) {
            return false;
        }

        handle.setOffers(new MerchantOffers());
        try {
            for (int tier = 1; tier <= level; tier++) {
                handle.setVillagerData(original.withLevel(tier));
                handle.updateTrades(serverLevel, DEFAULT_TRADE_COUNT);
            }
        } finally {
            // Restore the exact original level/profession/type; XP and custom name were never touched.
            handle.setVillagerData(original);
        }
        return !handle.getOffers().isEmpty();
    }
}
