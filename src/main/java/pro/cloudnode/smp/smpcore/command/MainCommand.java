package pro.cloudnode.smp.smpcore.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.smpcore.Member;
import pro.cloudnode.smp.smpcore.Messages;
import pro.cloudnode.smp.smpcore.Nation;
import pro.cloudnode.smp.smpcore.Permission;
import pro.cloudnode.smp.smpcore.SMPCore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public final class MainCommand extends Command {
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) return pluginInfo(sender);
        final @NotNull String @NotNull [] argsSubset = Arrays.copyOfRange(args, 1, args.length);
        return switch (args[0]) {
            case "reload" -> reload(sender);
            case "alt" -> alt(sender, argsSubset, label + " " + args[0]);
            case "time", "date" -> time(sender);
            case "member", "members" -> member(sender, argsSubset, label + " " + args[0]);
            default -> sendMessage(sender, MiniMessage.miniMessage()
                    .deserialize("<red>(!) Unrecognised command <gray><command>", Placeholder.unparsed("command", args[0])));
        };
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public @Nullable List<@NotNull String> tab(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        final @NotNull ArrayList<@NotNull String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission(Permission.RELOAD)) suggestions.add("reload");
            if (sender.hasPermission(Permission.ALT)) suggestions.add("alt");
            if (sender.hasPermission(Permission.TIME)) suggestions.addAll(List.of("time", "date"));
            if (sender.hasPermission(Permission.MEMBER)) suggestions.addAll(List.of("member", "members"));
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
            case "member", "members" -> {
                if (args.length == 2) {
                    if (sender.hasPermission(Permission.MEMBER)) suggestions.add("add");
                    if (sender.hasPermission(Permission.MEMBER_LIST)) suggestions.add("list");
                    if (sender.hasPermission(Permission.MEMBER_REMOVE)) suggestions.add("remove");
                    if (sender.hasPermission(Permission.MEMBER_SET_STAFF)) suggestions.add("staff");
                }
                else switch (args[1]) {
                    case "add" -> {
                        if (args.length == 3 && sender.hasPermission(Permission.MEMBER_ADD))
                            suggestions.addAll(
                                    sender.getServer().getOnlinePlayers().stream()
                                            .filter(p -> Member.get(p).isEmpty())
                                            .map(Player::getName).toList()
                            );
                    }
                    case "remove" -> {
                        if (args.length == 3 && sender.hasPermission(Permission.MEMBER_REMOVE))
                            suggestions.addAll(Member.getNames());
                    }
                    case "staff" -> {
                        if (sender.hasPermission(Permission.MEMBER_SET_STAFF)) {
                            if (args.length == 3)
                                suggestions.addAll(Member.getNames());
                            else if (args.length == 4)
                                suggestions.addAll(List.of("true", "false"));
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
     * <li>{@code /command alt} - show list of subcommands
     * <li>{@code /command alt list [player]} - show list of alts
     * <li>{@code /command alt add <username> [player]} - add an alt
     */
    public static boolean alt(final @NotNull CommandSender sender, final @NotNull String @NotNull [] originalArgs, final @NotNull String label) {
        if (!sender.hasPermission(Permission.ALT)) return sendMessage(sender, SMPCore.messages().errorNoPermission());

        final @NotNull String command = label;

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
                final @NotNull Set<@NotNull Member> alts = member.getAlts();

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

                if (removeMember(sender, altMember.get()))
                    return true;

                return sendMessage(sender, SMPCore.messages().altsDeleted(altMember.get()));
            }
            default -> {
                return sendMessage(sender, subCommandBuilder.build());
            }
        }
    }


    /**
     * Usage: {@code /<command> time}
     */
    public static boolean time(final @NotNull CommandSender sender) {
        if (!sender.hasPermission(Permission.TIME))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());
        return sendMessage(sender, SMPCore.messages().time(SMPCore.gameTime()));
    }

    /**
     * <li>{@code /<command> member} - Member Sub Commands</li>
     * <li>{@code /<command> member add <username>} - Add member to the server.</li>
     * <li>{@code /<command> member list} - List server members.</li>
     * <li>{@code /<command> member remove <member>} - Revoke server membership.</li>
     * <li>{@code /<command> member staff <member> <true|false>} - Set member staff status.</li>
     */
    public static boolean member(
            final @NotNull CommandSender sender,
            final @NotNull String @NotNull [] args,
            final @NotNull String label
    ) {
        if (!sender.hasPermission(Permission.MEMBER))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());

        if (args.length > 0) switch (args[0]) {
            case "add": {
                if (!sender.hasPermission(Permission.MEMBER_ADD))
                    return sendMessage(sender, SMPCore.messages().errorNoPermission());

                if (args.length != 2)
                    return sendMessage(sender, SMPCore.messages().usage(label, "member add <username>"));

                final OfflinePlayer target = sender.getServer().getOfflinePlayer(args[1]);

                final Optional<Member> existing = Member.get(target);
                if (existing.isPresent())
                    return sendMessage(sender, SMPCore.messages().errorAlreadyMember(existing.get()));

                final Member member = new Member(target);
                member.save();
                SMPCore.runMain(() -> member.player().setWhitelisted(true));

                return sendMessage(sender, SMPCore.messages().membersAdded(member));
            }

            case "list": {
                if (!sender.hasPermission(Permission.MEMBER_LIST))
                    return sendMessage(sender, SMPCore.messages().errorNoPermission());

                sendMessage(sender, SMPCore.messages().membersListHeader());

                final Set<Member> members = Member.get();

                if (members.isEmpty())
                    return sendMessage(sender, SMPCore.messages().membersListNone());

                for (final Member member : members)
                    sendMessage(sender, SMPCore.messages().membersListEntry(member));

                return true;
            }

            case "remove": {
                if (!sender.hasPermission(Permission.MEMBER_REMOVE))
                    return sendMessage(sender, SMPCore.messages().errorNoPermission());

                if (args.length != 2)
                    return sendMessage(sender, SMPCore.messages().usage(label, "member remove <member>"));

                final OfflinePlayer target = sender.getServer().getOfflinePlayer(args[1]);
                final Optional<Member> member = Member.get(target);

                if (member.isEmpty())
                    return sendMessage(sender, SMPCore.messages().errorNotMember(target));

                if (removeMember(sender, member.get()))
                    return true;

                return sendMessage(sender, SMPCore.messages().membersDeleted(target));
            }

            case "staff": {
                if (!sender.hasPermission(Permission.MEMBER_SET_STAFF))
                    return sendMessage(sender, SMPCore.messages().errorNoPermission());

                if (args.length != 3)
                    return sendMessage(sender, SMPCore.messages().usage(label, "member staff " + (args.length > 1 ? args[1] : "<member>") + " <true|false>"));

                final OfflinePlayer target = sender.getServer().getOfflinePlayer(args[1]);

                final boolean requestedStatus;
                switch (args[2].toLowerCase()) {
                    case "true", "yes" -> requestedStatus = true;
                    case "false", "no" -> requestedStatus = false;
                    default -> {
                        return sendMessage(sender, SMPCore.messages().usage(label, "member staff " + args[1] + " <true|false>"));
                    }
                }

                final Optional<Member> member = Member.get(target);

                if (member.isEmpty())
                    return sendMessage(sender, SMPCore.messages().errorNotMember(target));

                if (member.get().staff == requestedStatus)
                    return sendMessage(sender, SMPCore.messages().errorAlreadyStaff(member.get()));

                member.get().staff = requestedStatus;
                member.get().save();

                final ConsoleCommandSender console = sender.getServer().getConsoleSender();

                if (member.get().staff) {
                    member.get().nation().ifPresent(nation -> nation.getTeam().removePlayer(member.get().player()));

                    Member.getStaffTeam().addPlayer(member.get().player());
                }
                else {
                    Member.getStaffTeam().removePlayer(member.get().player());

                    member.get().nation().ifPresent(nation -> nation.getTeam().addPlayer(member.get().player()));
                }

                SMPCore.config().staffCommands(member.get().staff, member.get().player());

                return sendMessage(sender, SMPCore.messages().membersSetStaff(member.get()));
            }
        }

        final TextComponent.Builder subCommandBuilder = Component.text()
                .append(SMPCore.messages().subCommandHeader("Member", label + " ..."))
                .append(Component.newline());

        if (sender.hasPermission(Permission.MEMBER_ADD))
            subCommandBuilder.append(Component.newline()).append(SMPCore.messages().subCommandEntry(
                    label + " add ",
                    "add",
                    Messages.SubCommandArgument.of(new Messages.SubCommandArgument("username", true)),
                    "Add member to the server."
            ));

        if (sender.hasPermission(Permission.MEMBER_LIST))
            subCommandBuilder.append(Component.newline()).append(SMPCore.messages().subCommandEntry(
                    label + " list ", "list", "List server members."
            ));

        if (sender.hasPermission(Permission.MEMBER_REMOVE))
            subCommandBuilder.append(Component.newline()).append(SMPCore.messages().subCommandEntry(
                    label + " remove ",
                    "remove",
                    Messages.SubCommandArgument.of(new Messages.SubCommandArgument("member", true)),
                    "Remove member from the server."
            ));

        if (sender.hasPermission(Permission.MEMBER_SET_STAFF))
            subCommandBuilder.append(Component.newline()).append(SMPCore.messages().subCommandEntry(
                    label + " staff ",
                    "staff",
                    Messages.SubCommandArgument.of(
                            new Messages.SubCommandArgument("member", true),
                            new Messages.SubCommandArgument("true|false", true)
                    ),
                    "Set member staff status."
            ));

        return sendMessage(sender, subCommandBuilder.build());
    }

    /**
     * @return Whether the action was prevented.
     */
    private static boolean removeMember(final @NotNull Audience audience, final @NotNull Member target) {
        final Optional<Nation> nation = target.nation();
        final OfflinePlayer player = target.player();

        if (nation.isPresent()) {
            if (nation.get().leaderUUID.equals(player.getUniqueId())) {
                sendMessage(audience, SMPCore.messages().errorRemoveMemberLeader(target, nation.get()));
                return true;
            }

            if (nation.get().viceLeaderUUID.equals(player.getUniqueId())) {
                nation.get().viceLeaderUUID = nation.get().leaderUUID;
                nation.get().save();
            }

            nation.get().getTeam().removePlayer(player);
        }

        if (target.staff) {
            Member.getStaffTeam().removePlayer(player);

            SMPCore.config().staffCommands(false, player);
        }

        if (!target.delete())
            return sendMessage(audience, SMPCore.messages().errorFailedDeleteMember(target));

        SMPCore.runMain(() -> target.player().setWhitelisted(false));

        return false;
    }
}
