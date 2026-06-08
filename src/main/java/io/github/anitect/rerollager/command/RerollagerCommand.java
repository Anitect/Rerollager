package io.github.anitect.rerollager.command;

import io.github.anitect.rerollager.RerollagerPlugin;
import io.github.anitect.rerollager.config.PluginConfig;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/** {@code /rerollager <reload | info>}. */
@NullMarked
public final class RerollagerCommand implements BasicCommand {

    private final RerollagerPlugin plugin;

    public RerollagerCommand(RerollagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        if (args.length == 0) {
            sender.sendRichMessage("<gray>Usage: /rerollager <reload | info></gray>");
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                if (!sender.hasPermission("rerollager.admin")) {
                    sender.sendRichMessage("<red>You don't have permission to do that.</red>");
                    return;
                }
                plugin.reload();
                sender.sendRichMessage("<green>Rerollager reloaded.</green>");
            }
            case "info" -> {
                PluginConfig config = plugin.config();
                sender.sendRichMessage("<gold>Rerollager</gold> <gray>v"
                        + plugin.getPluginMeta().getVersion() + "</gray>");
                sender.sendRichMessage("<gray>Cooldown:</gray> "
                        + (config.cooldownEnabled() ? config.cooldownSeconds() + "s" : "off"));
                sender.sendRichMessage("<gray>Cost:</gray> "
                        + (config.cost().enabled() ? "on" : "off"));
                sender.sendRichMessage("<gray>Lock:</gray> "
                        + (config.lockEnabled() ? "on (" + config.lockItem() + ")" : "off"));
            }
            default -> sender.sendRichMessage("<gray>Usage: /rerollager <reload | info></gray>");
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length <= 1) {
            return List.of("reload", "info");
        }
        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "rerollager.use";
    }
}
