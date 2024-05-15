package pro.cloudnode.smp.smpcore.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.smpcore.Member;
import pro.cloudnode.smp.smpcore.Messages;
import pro.cloudnode.smp.smpcore.Permission;
import pro.cloudnode.smp.smpcore.SMPCore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public final class MainCommand extends Command {
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) return pluginInfo(sender);
        final @NotNull String @NotNull [] argsSubset = Arrays.copyOfRange(args, 1, args.length);
        return switch (args[0]) {
            case "reload" -> reload(sender);
            case "alt" -> alt(sender, argsSubset, label + " " + args[0]);
            default -> sendMessage(sender, MiniMessage.miniMessage()
                    .deserialize("<red>(!) Unrecognised command <gray><command>", Placeholder.unparsed("command", args[0])));
        };
    }

    @Override
    public @Nullable List<@NotNull String> tab(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        final @NotNull ArrayList<@NotNull String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission(Permission.RELOAD)) suggestions.add("reload");
            if (sender.hasPermission(Permission.ALT)) suggestions.add("alt");
        }
        else if (args.length > 1) switch (args[0]) {
            case "alt" -> {
                if (args.length == 2) {
                    if (sender.hasPermission(Permission.ALT)) suggestions.add("list");
                    if (sender.hasPermission(Permission.ALT_ADD)) suggestions.add("add");
                }
                else switch (args[1]) {
                    case "list" -> {
                        if (args.length == 3 && sender.hasPermission(Permission.ALT_OTHER)) suggestions.addAll(Member.getNames());
                    }
                    case "add" -> {
                        if (args.length == 4 && sender.hasPermission(Permission.ALT_ADD_OTHER)) suggestions.addAll(Member.getNames());
                    }
                }
            }
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

    /**
     * <li>{@code alt} - show list of subcommands
     * <li>{@code alt list [player]} - show list of alts
     * <li>{@code alt add <username> [player]} - add an alt
     */
    public static boolean alt(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission(Permission.ALT)) return sendMessage(sender, SMPCore.messages().errorNoPermission());

        final @NotNull String command = "/" + label;

        final @NotNull TextComponent.Builder subCommandBuilder = Component.text()
                .append(SMPCore.messages().subCommandHeader("Alt", command + " ...")).append(Component.newline())
                .append(SMPCore.messages()
                        .subCommandEntry(command + " list ", "list", Messages.SubCommandArgument.of(sender.hasPermission(Permission.ALT_OTHER) ? new Messages.SubCommandArgument("player", false) : null)));
        if (sender.hasPermission(Permission.ALT_ADD)) subCommandBuilder.append(Component.newline()).append(SMPCore.messages()
                .subCommandEntry(command + " add ", "add", Messages.SubCommandArgument.of(new Messages.SubCommandArgument("username", true), sender.hasPermission(Permission.ALT_ADD_OTHER) ? new Messages.SubCommandArgument("owner", false) : null)));

        if (args.length == 0) return sendMessage(sender, subCommandBuilder.build());
        else switch (args[0]) {
            case "list" -> {
                final @NotNull OfflinePlayer target;
                if (!(sender instanceof final @NotNull Player player)) {
                    if (args.length == 1) return sendMessage(sender, SMPCore.messages().usage(label, "list <player>"));
                    target = sender.getServer().getOfflinePlayer(args[1]);
                }
                else {
                    if (args.length > 1 && player.hasPermission(Permission.ALT_OTHER)) target = player.getServer().getOfflinePlayer(args[1]);
                    else target = player;
                }
                final @NotNull Optional<@NotNull Member> targetMember = Member.get(target);
                if (targetMember.isEmpty()) {
                    if (sender instanceof final @NotNull Player player && target.getUniqueId().equals(player.getUniqueId())) return sendMessage(sender, SMPCore.messages().errorNotMember());
                    else return sendMessage(sender, SMPCore.messages().errorNotMember(target));
                }

                final @NotNull Member member = targetMember.get().altOwner().orElse(targetMember.get());
                final @NotNull HashSet<@NotNull Member> alts = member.getAlts();

                sendMessage(sender, SMPCore.messages().altsHeader(member));
                if (alts.isEmpty()) return sendMessage(sender, SMPCore.messages().altsNone());
                for (final @NotNull Member alt : alts)
                    sendMessage(sender, SMPCore.messages().altsEntry(alt));
                return true;
            }
            case "add" -> {
                return sendMessage(sender, Component.text("add"));
            }
            default -> {
                return sendMessage(sender, subCommandBuilder.build());
            }
        }
    }
}
