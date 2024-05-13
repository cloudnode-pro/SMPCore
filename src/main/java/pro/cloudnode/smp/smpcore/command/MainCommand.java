package pro.cloudnode.smp.smpcore.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.smpcore.Permission;
import pro.cloudnode.smp.smpcore.SMPCore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MainCommand extends Command {
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) return pluginInfo(sender);
        final @NotNull String @NotNull [] argsSubset = Arrays.copyOfRange(args, 1, args.length);
        return switch (args[0]) {
            case "reload" -> reload(sender);
            default -> sendMessage(sender, MiniMessage.miniMessage()
                    .deserialize("<red>(!) Unrecognised command <gray><command>", Placeholder.unparsed("command", args[0])));
        };
    }

    @Override
    public @NotNull List<@NotNull String> tab(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        final @NotNull ArrayList<@NotNull String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission(Permission.RELOAD)) suggestions.add("reload");
        }
        return suggestions;
    }

    /**
     * Usage: {@code /<command>}
     */
    public static boolean pluginInfo(final @NotNull CommandSender sender) {
        final @NotNull SMPCore plugin = SMPCore.getInstance();
        return sendMessage(sender, MiniMessage.miniMessage()
                .deserialize("<green><name></green> <white>v<version> by</white> <gray><author></gray>", Placeholder.unparsed("name", plugin
                        .getPluginMeta().getName()), Placeholder.unparsed("version", plugin.getPluginMeta()
                        .getVersion()), Placeholder.unparsed("author", String.join(", ", plugin.getPluginMeta()
                        .getAuthors()))));
    }

    /**
     * Usage: {@code /<command> reload}
     */
    public static boolean reload(final @NotNull CommandSender sender) {
        if (!sender.hasPermission(Permission.RELOAD))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());
        SMPCore.getInstance().reload();
        return sendMessage(sender, SMPCore.messages().reloaded());
    }
}
