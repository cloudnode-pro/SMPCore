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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public final class MainCommand extends Command {
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) return pluginInfo(sender);
        final @NotNull String @NotNull [] argsSubset = Arrays.copyOfRange(args, 1, args.length);
        return switch (args[0]) {
            case "reload" -> reload(sender);
            case "alt" -> alt(sender, argsSubset, label + " " + args[0]);
            case "time", "date" -> time(sender, argsSubset, label + " " + args[0]);
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
            if (sender.hasPermission(Permission.TIME)) suggestions.addAll(List.of("time", "date"));
        }
        else if (args.length > 1) switch (args[0]) {
            case "alt" -> {
                if (args.length == 2) {
                    if (sender.hasPermission(Permission.ALT)) suggestions.add("list");
                    if (sender.hasPermission(Permission.ALT_ADD)) suggestions.add("add");
                    if (sender.hasPermission(Permission.ALT_REMOVE)) suggestions.add("remove");
                }
                else switch (args[1]) {
                    case "list" -> {
                        if (args.length == 3 && sender.hasPermission(Permission.ALT_OTHER)) suggestions.addAll(Member.getNames());
                    }
                    case "add" -> {
                        if (args.length == 4 && sender.hasPermission(Permission.ALT_ADD_OTHER)) suggestions.addAll(Member.getNames());
                    }
                    case "remove" -> {
                        if (args.length == 3) {
                            if (sender.hasPermission(Permission.ALT_REMOVE_OTHER)) suggestions.addAll(Member.getAltNames());
                            if (sender instanceof final @NotNull Player player) {
                                final @NotNull Optional<@NotNull Member> member = Member.get(player);
                                member.ifPresent(value -> suggestions.addAll(value.getAlts().stream()
                                        .map(m -> m.player().getName()).filter(Objects::nonNull).toList()));
                            }
                        }
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
    public static boolean alt(final @NotNull CommandSender sender, final @NotNull String @NotNull [] originalArgs, final @NotNull String label) {
        if (!sender.hasPermission(Permission.ALT)) return sendMessage(sender, SMPCore.messages().errorNoPermission());

        final @NotNull String command = "/" + label;

        final @NotNull TextComponent.Builder subCommandBuilder = Component.text()
                .append(SMPCore.messages().subCommandHeader("Alt", command + " ...")).append(Component.newline())
                .append(SMPCore.messages()
                        .subCommandEntry(command + " list ", "list", Messages.SubCommandArgument.of(sender.hasPermission(Permission.ALT_OTHER) ? new Messages.SubCommandArgument("player", !(sender instanceof Player)) : null)));
        if (sender.hasPermission(Permission.ALT_ADD)) subCommandBuilder.append(Component.newline()).append(SMPCore.messages()
                .subCommandEntry(command + " add ", "add", Messages.SubCommandArgument.of(new Messages.SubCommandArgument("username", true), sender.hasPermission(Permission.ALT_ADD_OTHER) ? new Messages.SubCommandArgument("owner", !(sender instanceof Player)) : null)));
        if (sender.hasPermission(Permission.ALT_REMOVE)) subCommandBuilder.append(Component.newline()).append(SMPCore.messages(
                ).subCommandEntry(command + " remove ", "remove", Messages.SubCommandArgument.of(new Messages.SubCommandArgument("alt", true))));

        if (originalArgs.length == 0) return sendMessage(sender, subCommandBuilder.build());
        else switch (originalArgs[0]) {
            case "list" -> {
                final @NotNull OfflinePlayer target;
                if (!(sender instanceof final @NotNull Player player)) {
                    if (originalArgs.length == 1) return sendMessage(sender, SMPCore.messages().usage(label, "list <player>"));
                    target = sender.getServer().getOfflinePlayer(originalArgs[1]);
                }
                else {
                    if (originalArgs.length > 1 && player.hasPermission(Permission.ALT_OTHER)) target = player.getServer().getOfflinePlayer(originalArgs[1]);
                    else target = player;
                }
                final @NotNull Optional<@NotNull Member> targetMember = Member.get(target);
                if (targetMember.isEmpty()) {
                    if (sender instanceof final @NotNull Player player && target.getUniqueId().equals(player.getUniqueId())) return sendMessage(sender, SMPCore.messages().errorNotMember());
                    else return sendMessage(sender, SMPCore.messages().errorNotMember(target));
                }

                final @NotNull Member member = targetMember.get().altOwner().orElse(targetMember.get());
                final @NotNull HashSet<@NotNull Member> alts = member.getAlts();

                sendMessage(sender, SMPCore.messages().altsListHeader(member));
                if (alts.isEmpty()) return sendMessage(sender, SMPCore.messages().altsListNone());
                for (final @NotNull Member alt : alts)
                    sendMessage(sender, SMPCore.messages().altsListEntry(alt));
                return true;
            }
            case "add" -> {
                if (!sender.hasPermission(Permission.ALT_ADD)) return sendMessage(sender, SMPCore.messages().errorNoPermission());

                final @NotNull LinkedList<@NotNull String> tempArgs = new LinkedList<>(Arrays.asList(originalArgs));
                final boolean confirm = tempArgs.contains("--confirm");
                final @NotNull String @NotNull [] args;
                if (confirm) {
                    tempArgs.remove("--confirm");
                    args = tempArgs.toArray(new String[0]);
                }
                else args = originalArgs;

                if (args.length == 1) {
                    if (!(sender instanceof Player)) return sendMessage(sender, SMPCore.messages().usage(label, "add <username> <owner>"));
                    if (sender.hasPermission(Permission.ALT_ADD_OTHER)) return sendMessage(sender, SMPCore.messages().usage(label, "add <username> [owner]"));
                    return sendMessage(sender, SMPCore.messages().usage(label, "add <username>"));
                }

                final @NotNull OfflinePlayer target;
                if (!(sender instanceof final @NotNull Player player)) {
                    if (args.length == 2) return sendMessage(sender, SMPCore.messages().usage(label, "add <username> <owner>"));
                    target = sender.getServer().getOfflinePlayer(args[2]);
                }
                else {
                    if (args.length > 2 && player.hasPermission(Permission.ALT_ADD_OTHER)) target = player.getServer().getOfflinePlayer(args[2]);
                    else target = player;
                }
                final @NotNull Optional<@NotNull Member> tempTargetMember = Member.get(target);
                if (tempTargetMember.isEmpty()) {
                    if (sender instanceof final @NotNull Player player && target.getUniqueId().equals(player.getUniqueId())) return sendMessage(sender, SMPCore.messages().errorNotMember());
                    else return sendMessage(sender, SMPCore.messages().errorNotMember(target));
                }

                final @NotNull Member targetMember = tempTargetMember.get().altOwner().orElse(tempTargetMember.get());

                if (SMPCore.ifDisallowedCharacters(args[1], Pattern.compile("[^A-Za-z\\d._]+"), s -> sendMessage(sender, SMPCore.messages().errorDisallowedCharacters(s))))
                    return true;

                if (!sender.hasPermission(Permission.ALT_MAX_BYPASS) && targetMember.getAlts().size() >= SMPCore.config().altsMax())
                    return sendMessage(sender, SMPCore.messages().errorMaxAltsReached(SMPCore.config().altsMax()));

                final @NotNull OfflinePlayer altPlayer = sender.getServer().getOfflinePlayer(args[1]);
                final @NotNull Optional<@NotNull Member> altMember = Member.get(altPlayer);
                if (altMember.isPresent()) {
                    if (altMember.get().isAlt() && Objects.requireNonNull(altMember.get().altOwnerUUID).equals(target.getUniqueId()))
                        return sendMessage(sender, SMPCore.messages().errorAlreadyYourAlt(altMember.get()));
                    if (!altMember.get().isAlt() || altMember.get().player().hasPlayedBefore())
                        return sendMessage(sender, SMPCore.messages().errorAltAlreadyMember(altMember.get()));
                }

                if (!confirm) return sendMessage(sender, SMPCore.messages().altsConfirmAdd(altPlayer, command + " " + String.join(" ", args) + " --confirm"));
                if (altMember.isPresent() && !altMember.get().delete()) return sendMessage(sender, SMPCore.messages().errorFailedDeleteMember(altMember.get()));

                final @NotNull Member alt = new Member(altPlayer, targetMember);
                alt.save();
                SMPCore.runMain(() -> alt.player().setWhitelisted(true));

                return sendMessage(sender, SMPCore.messages().altsCreated(alt));
            }
            case "remove" -> {
                if (!sender.hasPermission(Permission.ALT_REMOVE))
                    return sendMessage(sender, SMPCore.messages().errorNoPermission());

                if (originalArgs.length == 1)
                    return sendMessage(sender, SMPCore.messages().usage(label, "remove <alt>"));

                final @NotNull OfflinePlayer altPlayer = sender.getServer().getOfflinePlayer(originalArgs[1]);
                final @NotNull Optional<@NotNull Member> altMember = Member.get(altPlayer);
                if (altMember.isEmpty()) return sendMessage(sender, SMPCore.messages().errorNotMember(altPlayer));

                final @NotNull Optional<@NotNull Member> altOwner = altMember.get().altOwner();
                if (altOwner.isEmpty())
                    return sendMessage(sender, SMPCore.messages().errorMemberNotAlt(altMember.get()));

                if (!sender.hasPermission(Permission.ALT_REMOVE_OTHER)) {
                    final @NotNull Optional<@NotNull Member> member = sender instanceof final @NotNull Player player ? Member.get(player) : Optional.empty();
                    if (member.isEmpty()) return sendMessage(sender, SMPCore.messages().errorNotMember());
                    if (!altOwner.get().uuid.equals(member.get().uuid)) return sendMessage(sender, SMPCore.messages().errorNoPermission());
                }

                if (!sender.hasPermission(Permission.ALT_REMOVE_JOINED) && altPlayer.hasPlayedBefore())
                    return sendMessage(sender, SMPCore.messages().errorRemoveJoinedAlt(altMember.get()));

                if (!altMember.get().delete())
                    return sendMessage(sender, SMPCore.messages().errorFailedDeleteMember(altMember.get()));

                return sendMessage(sender, SMPCore.messages().altsDeleted(altMember.get()));
            }
            default -> {
                return sendMessage(sender, subCommandBuilder.build());
            }
        }
    }

    public static boolean time(final @NotNull CommandSender sender, final @NotNull String @NotNull [] args, final @NotNull String label) {
        if (!sender.hasPermission(Permission.TIME))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());
        return sendMessage(sender, SMPCore.messages().time(SMPCore.gameTime()));
    }
}
