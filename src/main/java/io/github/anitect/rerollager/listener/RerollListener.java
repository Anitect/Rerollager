package io.github.anitect.rerollager.listener;

import io.github.anitect.rerollager.RerollagerPlugin;
import io.github.anitect.rerollager.config.PluginConfig;
import io.github.anitect.rerollager.reroll.RerollOutcome;
import io.github.anitect.rerollager.reroll.RerollService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.time.Duration;

/** Turns the sneak + right-click gesture into a reroll attempt and surfaces the result. */
@NullMarked
public final class RerollListener implements Listener {

    private final RerollagerPlugin plugin;

    public RerollListener(RerollagerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return; // PlayerInteractEntityEvent also fires for the off-hand; handle once.
        }
        if (!(event.getRightClicked() instanceof Villager villager)) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return; // both gestures are sneak + right-click; a plain right-click still opens trades.
        }

        PluginConfig config = plugin.config();
        ItemStack hand = player.getInventory().getItemInMainHand();

        // Lock/unlock takes precedence: it requires a specific item, so it can't collide with the
        // default empty-hand reroll. Only meaningful on a rerollable villager.
        if (config.lockEnabled() && matchesItem(hand, config.lockItem())) {
            if (!player.hasPermission("rerollager.lock") || !RerollService.isEligible(villager)) {
                return;
            }
            event.setCancelled(true);
            boolean nowLocked = plugin.lockService().toggle(villager);
            PluginConfig.Messages messages = config.messages();
            send(player, nowLocked ? messages.locked() : messages.unlocked());
            playLockSound(villager, config.lockSound(), nowLocked);
            return;
        }

        // Reroll gesture.
        if (!triggerMatches(player, config)) {
            return; // wrong item in hand: leave vanilla behaviour untouched.
        }
        if (!player.hasPermission("rerollager.use")) {
            return;
        }

        // This gesture is ours: stop the vanilla trade screen from opening.
        event.setCancelled(true);

        RerollOutcome outcome = plugin.rerollService().attempt(player, villager);
        report(player, villager, config, outcome);
    }

    private boolean matchesItem(ItemStack hand, String materialName) {
        if (materialName.isEmpty()) {
            return false;
        }
        Material required = Material.matchMaterial(materialName);
        return required != null && hand.getType() == required;
    }

    private boolean triggerMatches(Player player, PluginConfig config) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        String require = config.triggerRequireItem();
        if (require.isEmpty()) {
            return hand.getType().isAir();
        }
        Material required = Material.matchMaterial(require);
        return required != null && hand.getType() == required;
    }

    private void report(Player player, Villager villager, PluginConfig config, RerollOutcome outcome) {
        PluginConfig.Messages messages = config.messages();
        switch (outcome.status()) {
            case SUCCESS -> {
                send(player, messages.success());
                playSound(villager, config.sound());
            }
            case ON_COOLDOWN -> send(player, messages.onCooldown()
                    .replace("<time>", formatRemaining(outcome.cooldownRemainingMillis())));
            case CANNOT_AFFORD -> send(player, messages.cannotAfford());
            case LOCKED -> send(player, messages.rerollDeniedLocked());
            case NOT_ELIGIBLE -> send(player, messages.notEligible());
            case FAILED -> { /* generation failed (likely a version/internals issue); stay silent. */ }
        }
    }

    private void send(Player player, String miniMessage) {
        if (!miniMessage.isEmpty()) {
            player.sendRichMessage(miniMessage);
        }
    }

    private void playSound(Villager villager, PluginConfig.SoundSpec sound) {
        if (sound.enabled()) {
            villager.getWorld().playSound(villager.getLocation(), sound.key(), sound.volume(), sound.pitch());
        }
    }

    private void playLockSound(Villager villager, PluginConfig.LockSound sound, boolean locked) {
        if (sound.enabled()) {
            String key = locked ? sound.lockKey() : sound.unlockKey();
            villager.getWorld().playSound(villager.getLocation(), key, sound.volume(), sound.pitch());
        }
    }

    private static String formatRemaining(long millis) {
        long seconds = Math.max(1, Duration.ofMillis(millis).toSeconds());
        return seconds < 60 ? seconds + "s" : (seconds / 60) + "m " + (seconds % 60) + "s";
    }
}
