package pro.cloudnode.smp.smpcore.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.smpcore.CitizenRequest;
import pro.cloudnode.smp.smpcore.Member;
import pro.cloudnode.smp.smpcore.Messages;
import pro.cloudnode.smp.smpcore.Nation;
import pro.cloudnode.smp.smpcore.Permission;
import pro.cloudnode.smp.smpcore.SMPCore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public final class NationCommand extends Command {

    @Override
    public boolean run(
            final @NotNull CommandSender sender,
            final @NotNull String label,
            @NotNull String @NotNull [] args
    ) {
        if (!sender.hasPermission(Permission.NATION))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());

        final @NotNull Optional<@NotNull Member> member = sender instanceof final @NotNull Player player ? Member.get(
                player) : Optional.empty();

        final @NotNull Optional<@NotNull Nation> nation;
        if (args.length > 0 && args[0].startsWith("id:")) {
            final @NotNull String id = args[0].substring(3);
            nation = Nation.get(id);
            args = Arrays.copyOfRange(args, 1, args.length);
            if (nation.isEmpty())
                return sendMessage(sender, SMPCore.messages().errorNationNotFound(id));
        }
        else
            nation = member.flatMap(Member::nation);

        if (args.length == 0)
            return helpSubCommands(member.orElse(null), nation.orElse(null), sender, label);

        final boolean other = member.isEmpty() || nation.isEmpty() || !nation.get().id.equals(member.get().nationID);
        final @NotNull String command = label + (other && nation.isPresent() ? " id:" + nation.get().id : "") + " "
                + args[0];
        final @NotNull String @NotNull [] argsSubset = Arrays.copyOfRange(args, 1, args.length);
        return switch (args[0]) {
            case "citizens", "members", "subjects", "nationals", "people", "residents", "population", "cits", "pop" -> citizens(member.orElse(null), nation.orElse(null), sender, command, argsSubset);
            case "join", "request", "req" -> join(member.orElse(null), sender, command, argsSubset);
            case "cancel", "reject", "decline", "withdraw", "refuse", "deny" -> memberCancel(member.orElse(null), sender, command, argsSubset);
            case "leave", "abandon", "renounce" -> leave(member.orElse(null), nation.orElse(null), sender);
            default -> helpSubCommands(member.orElse(null), nation.orElse(null), sender, label);
        };
    }

    @Override
    public @Nullable List<@NotNull String> tab(
            final @NotNull CommandSender sender,
            final @NotNull String label,
            @NotNull String @NotNull [] args
    ) {
        return tabComplete(sender, label, args);
    }

    public static @Nullable List<@NotNull String> tabComplete(
            final @NotNull CommandSender sender,
            final @NotNull String label,
            @NotNull String @NotNull [] args
    ) {
        final var list = new ArrayList<@NotNull String>();

        if (!sender.hasPermission(Permission.NATION))
            return list;

        final @NotNull Optional<@NotNull Member> member = sender instanceof final @NotNull Player player ? Member.get(
                player) : Optional.empty();

        final @NotNull Optional<@NotNull Nation> nation;
        if (args.length > 0 && args[0].startsWith("id:")) {
            if (args.length == 1) {
                list.addAll(Nation.get().stream().map(n -> "id:" + n.id).sorted().toList());
                return list;
            }
            final @NotNull String id = args[0].substring(3);
            nation = Nation.get(id);
            args = Arrays.copyOfRange(args, 1, args.length);
            if (nation.isEmpty())
                return list;
        }
        else
            nation = member.flatMap(Member::nation);

        final boolean other = member.isEmpty() || nation.isEmpty() || !nation.get().id.equals(member.get().nationID);

        if (args.length == 1) {
            if (nation.isPresent()) {
                if (
                        (!other && hasAnyPermission(sender, Permission.NATION_CITIZENS_LIST, Permission.NATION_CITIZENS_KICK))
                                || hasAnyPermission(sender, Permission.NATION_CITIZENS_LIST_OTHER, Permission.NATION_CITIZENS_KICK_OTHER)
                )
                    Collections.addAll(list, "citizens", "members", "subjects", "nationals", "people", "residents", "population", "cits", "pop");
                if (sender.hasPermission(Permission.NATION_LEAVE) && member.isPresent() && !other && !nation.get().leaderUUID.equals(member.get().uuid))
                    Collections.addAll(list, "leave", "abandon", "renounce");
            }

            else if (hasAnyPermission(sender, Permission.NATION_JOIN_REQUEST) && (
                    member.isEmpty() || member.get().nationID == null || sender.hasPermission(Permission.NATION_JOIN_REQUEST_SWITCH)
            ))
                Collections.addAll(list, "join", "request", "req", "cancel", "reject", "decline", "withdraw", "refuse", "deny");
        }
        else switch (args[0]) {
            case "citizens", "members", "subjects", "nationals", "people", "residents", "population", "cits", "pop" -> {
                switch (args.length) {
                    case 2 -> {
                        if ((!other && sender.hasPermission(Permission.NATION_CITIZENS_LIST))
                                || sender.hasPermission(Permission.NATION_CITIZENS_LIST_OTHER))
                            Collections.addAll(list, "list", "show", "get");

                        if ((!other && sender.hasPermission(Permission.NATION_CITIZENS_KICK))
                                || sender.hasPermission(Permission.NATION_CITIZENS_KICK_OTHER))
                            Collections.addAll(list, "kick", "remove", "delete", "rm", "del");

                        if ((!other && sender.hasPermission(Permission.NATION_INVITE))
                                || sender.hasPermission(Permission.NATION_INVITE_OTHER))
                            Collections.addAll(list, "invite", "request", "req", "cancel", "reject", "decline", "withdraw", "refuse", "deny");

                        if ((!other && sender.hasPermission(Permission.NATION_CITIZEN_ADD))
                                || sender.hasPermission(Permission.NATION_CITIZEN_ADD_OTHER))
                            Collections.addAll(list, "add");
                    }
                    case 3 -> {
                        switch (args[1]) {
                            case "kick", "remove", "delete", "rm", "del" -> {
                                if (other && !sender.hasPermission(Permission.NATION_CITIZENS_KICK_OTHER))
                                    break;
                                if (!sender.hasPermission(Permission.NATION_CITIZENS_KICK))
                                    break;
                                list.addAll((List<@NotNull String>) nation.get().citizens().stream()
                                                  .filter(c -> !c.uuid.equals(nation.get().leaderUUID))
                                                  .map(c -> c.player().getName())
                                                  .filter(Objects::nonNull).toList());
                            }
                            case "invite", "request", "req" -> {
                                if (other && !sender.hasPermission(Permission.NATION_INVITE_OTHER))
                                    break;
                                if (!sender.hasPermission(Permission.NATION_INVITE))
                                    break;
                                list.addAll((List<@NotNull String>) Member.get().stream()
                                    .filter(m -> !nation.get().id.equals(m.nationID))
                                    .map(c -> c.player().getName())
                                    .filter(Objects::nonNull).toList());
                            }
                            case "cancel", "reject", "decline", "withdraw", "refuse", "deny" -> {
                                if (other && !sender.hasPermission(Permission.NATION_INVITE_OTHER))
                                    break;
                                if (!sender.hasPermission(Permission.NATION_INVITE))
                                    break;
                                list.addAll((List<@NotNull String>) Stream.concat(
                                        CitizenRequest.get(nation.get(), true).stream(),
                                        CitizenRequest.get(nation.get(), false).stream()
                                ).map(req -> req.member().player().getName()).filter(Objects::nonNull).sorted().toList());
                            }
                            case "add" -> {
                                if (other && !sender.hasPermission(Permission.NATION_CITIZEN_ADD_OTHER))
                                    break;
                                if (!sender.hasPermission(Permission.NATION_CITIZEN_ADD))
                                    break;
                                list.addAll((List<@NotNull String>) Member.get().stream()
                                                                          .filter(m -> !nation.get().id.equals(m.nationID))
                                                                          .map(c -> c.player().getName())
                                                                          .filter(Objects::nonNull).toList());
                            }
                        }
                    }
                }
            }
            case "join", "request", "req" -> {
                if (args.length > 2 || member.isEmpty())
                    break;
                if (member.get().nationID != null && !sender.hasPermission(Permission.NATION_JOIN_REQUEST_SWITCH))
                    break;
                if (sender.hasPermission(Permission.NATION_JOIN_REQUEST))
                    list.addAll(Nation.get().stream().map(n -> n.id).filter(n -> !n.equals(member.get().nationID)).sorted().toList());
                else if (sender.hasPermission(Permission.NATION_INVITE_ACCEPT))
                    list.addAll(CitizenRequest.get(member.get(), false).stream().map(req -> req.nationID).sorted().toList());
            }
            case "cancel", "reject", "decline", "withdraw", "refuse", "deny" -> {
                if (args.length > 2 || member.isEmpty())
                    break;
                if (sender.hasPermission(Permission.NATION_INVITE_ACCEPT))
                    list.addAll(Stream.concat(CitizenRequest.get(member.get(), true).stream(), CitizenRequest.get(member.get(), false).stream())
                                      .map(req -> req.nationID).sorted().toList());
                if (sender.hasPermission(Permission.NATION_JOIN_REQUEST))
                    list.addAll(CitizenRequest.get(member.get(), true).stream().map(req -> req.nationID).sorted().toList());
            }
        }

        return list;
    }

    public static boolean helpSubCommands(
            final @Nullable Member member,
            final @Nullable Nation nation,
            final @NotNull CommandSender sender,
            final @NotNull String label
    ) {
        final boolean other = member == null || nation == null || !nation.id.equals(member.nationID);

        final @NotNull String command = label + (other && nation != null ? " id:" + nation.id : "");
        final @NotNull TextComponent.Builder subCommandBuilder = Component.text()
                                                                          .append(SMPCore.messages()
                                                                                         .subCommandHeader(
                                                                                                 "Nation",
                                                                                                 command + " ..."
                                                                                         ))
                                                                          .append(Component.newline());

        if (nation != null) {
            if ((
                    !other && hasAnyPermission(sender, Permission.NATION_CITIZENS_LIST, Permission.NATION_CITIZENS_KICK)
            ) || hasAnyPermission(sender, Permission.NATION_CITIZENS_LIST_OTHER, Permission.NATION_CITIZENS_KICK_OTHER))
                subCommandBuilder.append(Component.newline())
                                 .append(SMPCore.messages().subCommandEntry(command + " citizens ", "citizens"));

            if (sender.hasPermission(Permission.NATION_LEAVE) && member != null && nation.id.equals(member.nationID) && !nation.leaderUUID.equals(member.uuid))
                subCommandBuilder.append(Component.newline())
                                 .append(SMPCore.messages().subCommandEntry(command + " leave ", "leave", "Leave the nation."));
        }

        if (hasAnyPermission(sender, Permission.NATION_JOIN_REQUEST) && (
                member == null || member.nationID == null || sender.hasPermission(Permission.NATION_JOIN_REQUEST_SWITCH)
        )) {
            subCommandBuilder.append(Component.newline()).append(SMPCore.messages().subCommandEntry(
                    command + " join ", "join", new Messages.SubCommandArgument[]{
                            new Messages.SubCommandArgument("nation", true)
                    }, "Request to join a nation."
            ));
            if (member != null && !CitizenRequest.get(member, true).isEmpty())
                subCommandBuilder.append(Component.newline()).append(SMPCore.messages().subCommandEntry(
                        command + " cancel ", "cancel", new Messages.SubCommandArgument[]{
                                new Messages.SubCommandArgument("nation", true)
                        }, "Cancel request to join nation."
                ));

            if (member != null && !CitizenRequest.get(member, false).isEmpty())
                subCommandBuilder.append(Component.newline()).append(SMPCore.messages().subCommandEntry(
                        command + " reject ", "reject", new Messages.SubCommandArgument[]{
                                new Messages.SubCommandArgument("nation", true)
                        }, "Reject invitation to join nation."
                ));
        }

        return sendMessage(sender, subCommandBuilder.build());
    }

    public static boolean citizens(
            final @Nullable Member member,
            final @Nullable Nation nation,
            final @NotNull CommandSender sender,
            final @NotNull String label,
            final @NotNull String @NotNull [] args
    ) {
        if (nation == null)
            return sendMessage(sender, SMPCore.messages().errorNotCitizen());

        if (args.length == 0)
            return citizensSubcommands(member, nation, sender, label);
        final @NotNull String command = label + " " + args[0];
        final @NotNull String @NotNull [] argsSubset = Arrays.copyOfRange(args, 1, args.length);
        return switch (args[0]) {
            case "list", "show", "get" -> listCitizens(member, nation, sender);
            case "kick", "remove", "delete", "rm", "del" -> kickCitizen(member, nation, sender, command, argsSubset);
            case "invite", "request", "req" -> inviteCitizen(member, nation, sender, command, argsSubset);
            case "cancel", "reject", "decline", "withdraw", "refuse", "deny" -> nationCancel(member, nation, sender, command, argsSubset);
            case "add" -> addCitizen(member, nation, sender, command, argsSubset);
            default -> citizensSubcommands(member, nation, sender, label);
        };
    }

    public static boolean citizensSubcommands(
            final @Nullable Member member,
            final @NotNull Nation nation,
            final @NotNull CommandSender sender,
            final @NotNull String label
    ) {
        final boolean other = member == null || !nation.id.equals(member.nationID);

        final @NotNull TextComponent.Builder subCommandBuilder = Component.text()
                                                                          .append(SMPCore.messages()
                                                                                         .subCommandHeader(
                                                                                                 "Nation Citizens",
                                                                                                 label + " ..."
                                                                                         ))
                                                                          .append(Component.newline());

        if ((!other && sender.hasPermission(Permission.NATION_CITIZENS_LIST))
                || sender.hasPermission(Permission.NATION_CITIZENS_LIST_OTHER))
            subCommandBuilder.append(Component.newline())
                             .append(SMPCore.messages()
                                            .subCommandEntry(label + " list ", "list", "List nation citizens."));

        if ((!other && sender.hasPermission(Permission.NATION_CITIZENS_KICK))
                || sender.hasPermission(Permission.NATION_CITIZENS_KICK_OTHER))
            subCommandBuilder.append(Component.newline()).append(SMPCore.messages().subCommandEntry(
                    label + " kick ", "kick", new Messages.SubCommandArgument[]{
                            new Messages.SubCommandArgument("citizen", true)
                    }, "Revoke citizenship."
            ));

        if ((!other && sender.hasPermission(Permission.NATION_INVITE))
                || sender.hasPermission(Permission.NATION_INVITE_OTHER)) {
            subCommandBuilder.append(Component.newline()).append(SMPCore.messages().subCommandEntry(
                    label + " invite ", "invite", new Messages.SubCommandArgument[]{
                            new Messages.SubCommandArgument("member", true)
                    }, "Invite to join nation."
            ));

            if (!CitizenRequest.get(nation, true).isEmpty())
                subCommandBuilder.append(Component.newline()).append(SMPCore.messages().subCommandEntry(
                        label + " reject ", "reject", new Messages.SubCommandArgument[]{
                                new Messages.SubCommandArgument("member", true)
                        }, "Reject request to join nation."
                ));

            if (!CitizenRequest.get(nation, false).isEmpty())
                subCommandBuilder.append(Component.newline()).append(SMPCore.messages().subCommandEntry(
                        label + " cancel ", "cancel", new Messages.SubCommandArgument[]{
                                new Messages.SubCommandArgument("member", true)
                        }, "Cancel invitation to join nation."
                ));
        }

        if ((!other && sender.hasPermission(Permission.NATION_CITIZEN_ADD))
        || sender.hasPermission(Permission.NATION_CITIZEN_ADD_OTHER))
            subCommandBuilder.append(Component.newline()).append(SMPCore.messages().subCommandEntry(
                    label + " add ", "add", new Messages.SubCommandArgument[]{
                            new Messages.SubCommandArgument("member", true)
                    }, "Add member to nation."
            ));

        return sendMessage(sender, subCommandBuilder.build());
    }

    public static boolean listCitizens(
            final @Nullable Member member,
            final @NotNull Nation nation,
            final @NotNull CommandSender sender
    ) {
        final boolean other = member == null || !nation.id.equals(member.nationID);
        if (!sender.hasPermission(Permission.NATION_CITIZENS_LIST)
                || (other && !sender.hasPermission(Permission.NATION_CITIZENS_LIST_OTHER)))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());
        else
            return sendMessage(sender, SMPCore.messages().nationCitizensList(nation, sender, other));
    }

    public static boolean kickCitizen(
            final @Nullable Member member,
            final @NotNull Nation nation,
            final @NotNull CommandSender sender,
            final @NotNull String label,
            final @NotNull String @NotNull [] args
    ) {
        if (!sender.hasPermission(Permission.NATION_CITIZENS_KICK))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());

        if (args.length == 0)
            return sendMessage(sender, SMPCore.messages().usage(label, "<citizen>"));

        final @NotNull OfflinePlayer target = sender.getServer().getOfflinePlayer(args[0]);
        final @NotNull Optional<@NotNull Member> targetMemberOptional = Member.get(target);
        if (targetMemberOptional.isEmpty())
            return sendMessage(sender, SMPCore.messages().errorNotMember(target));

        final @NotNull Member targetMember = targetMemberOptional.get();
        if (targetMember.nationID == null || !targetMember.nationID.equals(nation.id) || (member != null && !nation.id.equals(member.nationID)))
            return sendMessage(sender, SMPCore.messages().errorNotCitizen(targetMember));
        if (targetMember.uuid.equals(nation.leaderUUID) || targetMember.uuid.equals(nation.viceLeaderUUID))
            return sendMessage(sender, SMPCore.messages().errorKickLeadership());

        nation.remove(targetMember);
        return sendMessage(sender, SMPCore.messages().nationCitizensKicked(targetMember));
    }

    public static boolean inviteCitizen(
            final @Nullable Member member,
            final @NotNull Nation nation,
            final @NotNull CommandSender sender,
            final @NotNull String label,
            final @NotNull String @NotNull [] args
    ) {
        if (
                !sender.hasPermission(Permission.NATION_INVITE)
                        || (
                        (member == null || !nation.id.equals(member.nationID))
                                && !sender.hasPermission(Permission.NATION_INVITE_OTHER)
                )
        )
            return sendMessage(sender, SMPCore.messages().errorNoPermission());

        if (args.length < 1)
            return sendMessage(sender, SMPCore.messages().usage(label, "<member>"));

        final var targetPlayer = sender.getServer().getOfflinePlayer(args[0]);
        final @NotNull var target = Member.get(targetPlayer);

        if (target.isEmpty())
            return sendMessage(sender, SMPCore.messages().errorNotMember(targetPlayer));

        if (nation.id.equals(target.get().nationID))
            return sendMessage(sender, SMPCore.messages().errorAlreadyCitizen(target.get()));

        final @NotNull var request = CitizenRequest.get(target.get(), nation);

        if (request.isEmpty() || request.get().expired()) {
            request.ifPresent(CitizenRequest::delete);

            final var newRequest = new CitizenRequest(
                    target.get().uuid,
                    nation.id,
                    false,
                    new Date(),
                    Date.from(Instant.now().plusSeconds(SMPCore.config().joinRequestExpireMinutes() * 60L))
            );
            newRequest.save();
            sendMessage(sender, SMPCore.messages().nationJoinInviteSent(target.get()));
            return newRequest.send();
        }

        if (!request.get().mode)
            return sendMessage(sender, SMPCore.messages().errorAlreadyInvited(target.get()));

        request.get().accept();
        return true;
    }

    public static boolean addCitizen(
            final @Nullable Member member,
            final @NotNull Nation nation,
            final @NotNull CommandSender sender,
            final @NotNull String label,
            final @NotNull String @NotNull [] args
    ) {
        if (
                !sender.hasPermission(Permission.NATION_CITIZEN_ADD)
                        || (
                        (member == null || !nation.id.equals(member.nationID))
                                && !sender.hasPermission(Permission.NATION_CITIZEN_ADD_OTHER)
                )
        )
            return sendMessage(sender, SMPCore.messages().errorNoPermission());

        if (args.length < 1)
            return sendMessage(sender, SMPCore.messages().usage(label, "<member>"));

        final @NotNull var targetPlayer = sender.getServer().getOfflinePlayer(args[0]);
        final @NotNull var target = Member.get(targetPlayer);

        if (target.isEmpty())
            return sendMessage(sender, SMPCore.messages().errorNotMember(targetPlayer));

        if (nation.id.equals(target.get().nationID))
            return sendMessage(sender, SMPCore.messages().errorAlreadyCitizen(target.get()));

        if (target.get().nationID != null && !sender.hasPermission(Permission.NATION_CITIZEN_ADD_SWITCH))
            return sendMessage(sender, SMPCore.messages().errorOtherCitizen(target.get()));

        final var currentNation = target.get().nation().orElseThrow(() -> new IllegalStateException("Could not find nation " + target.get().nationID + " of member " + target.get().uuid));
        if (currentNation.leaderUUID.equals(targetPlayer.getUniqueId()))
            return sendMessage(sender, SMPCore.messages().errorKickLeadership());

        nation.add(target.get());
        return true;
    }

    public static boolean join(
            final @Nullable Member member,
            final @NotNull CommandSender sender,
            final @NotNull String label,
            final @NotNull String @NotNull [] args
    ) {
        if (member == null)
            return sendMessage(sender, SMPCore.messages().errorNotMember());

        if (member.nationID != null) {
            if (!hasAnyPermission(
                    sender,
                    Permission.NATION_JOIN_REQUEST_SWITCH,
                    Permission.NATION_INVITE_ACCEPT_SWITCH
            ))
                return sendMessage(sender, SMPCore.messages().errorAlreadyCitizen());
            final @NotNull Nation nation = member.nation().orElseThrow(() -> new IllegalStateException("Could not find nation " + member.nationID + " of member " + member.uuid));
            if (member.uuid.equals(nation.leaderUUID))
                return sendMessage(sender, SMPCore.messages().errorLeaderLeave());
        }

        if (!hasAnyPermission(sender, Permission.NATION_JOIN_REQUEST, Permission.NATION_INVITE_ACCEPT))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());

        if (args.length < 1)
            return sendMessage(sender, SMPCore.messages().usage(label, "<nation>"));

        final @NotNull Optional<@NotNull Nation> nation = Nation.get(args[0]);

        if (nation.isEmpty())
            return sendMessage(sender, SMPCore.messages().errorNationNotFound(args[0]));

        if (nation.get().id.equals(member.nationID))
            return sendMessage(sender, SMPCore.messages().errorAlreadyCitizen(nation.get()));

        final @NotNull Optional<@NotNull CitizenRequest> request = CitizenRequest.get(member, nation.get());

        if (request.isEmpty() || request.get().expired()) {
            request.ifPresent(CitizenRequest::delete);

            if (sender.hasPermission(Permission.NATION_JOIN_FORCE) && Arrays.asList(args).contains("--force")) {
                nation.get().add(member);
                return true;
            }
            if (!sender.hasPermission(Permission.NATION_JOIN_REQUEST))
                return sendMessage(sender, SMPCore.messages().errorNotInvited(nation.get()));
            final @NotNull CitizenRequest newRequest = new CitizenRequest(
                    member.uuid,
                    nation.get().id,
                    true,
                    new Date(),
                    Date.from(Instant.now().plusSeconds(SMPCore.config().joinRequestExpireMinutes() * 60L))
            );
            newRequest.save();
            sendMessage(sender, SMPCore.messages().nationJoinRequestSent(nation.get()));
            return newRequest.send();
        }

        if (request.get().mode && (!sender.hasPermission(Permission.NATION_JOIN_FORCE) || !Arrays.asList(args).contains("--force")))
            return sendMessage(sender, SMPCore.messages().errorAlreadyRequestedJoin(nation.get()));

        request.get().accept();
        return true;
    }

    public static boolean memberCancel(
            final @Nullable Member member,
            final @NotNull CommandSender sender,
            final @NotNull String label,
            final @NotNull String @NotNull [] args
    ) {
        if (member == null)
            return sendMessage(sender, SMPCore.messages().errorNotMember());

        if (!hasAnyPermission(sender, Permission.NATION_JOIN_REQUEST, Permission.NATION_INVITE_ACCEPT))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());

        if (args.length < 1)
            return sendMessage(sender, SMPCore.messages().usage(label, "<nation>"));

        final var nation = Nation.get(args[0]);
        if (nation.isEmpty())
            return sendMessage(sender, SMPCore.messages().errorNationNotFound(args[0]));

        final var request = CitizenRequest.get(member, nation.get());
        if (request.isEmpty())
            return sendMessage(sender, SMPCore.messages().errorNoRequest(nation.get()));

        request.get().delete();
        if (request.get().mode)
            return sendMessage(sender, SMPCore.messages().nationJoinRequestCancelled(nation.get()));
        return sendMessage(sender, SMPCore.messages().nationJoinInviteRejected(nation.get()));
    }

    public static boolean nationCancel(
            final @Nullable Member member,
            final @NotNull Nation nation,
            final @NotNull CommandSender sender,
            final @NotNull String label,
            final @NotNull String @NotNull [] args
    ) {
        if (
                !sender.hasPermission(Permission.NATION_INVITE)
                        || (
                        (member == null || !nation.id.equals(member.nationID))
                                && !sender.hasPermission(Permission.NATION_INVITE_OTHER)
                )
        )
            return sendMessage(sender, SMPCore.messages().errorNoPermission());

        if (args.length < 1)
            return sendMessage(sender, SMPCore.messages().usage(label, "<member>"));

        final var targetPlayer = sender.getServer().getOfflinePlayer(args[0]);
        final @NotNull var target = Member.get(targetPlayer);

        if (target.isEmpty())
            return sendMessage(sender, SMPCore.messages().errorNotMember(targetPlayer));

        final var request = CitizenRequest.get(target.get(), nation);
        if (request.isEmpty())
            return sendMessage(sender, SMPCore.messages().errorNoRequest(target.get()));

        request.get().delete();
        if (request.get().mode)
            return sendMessage(sender, SMPCore.messages().nationJoinRequestRejected(target.get(), nation));
        return sendMessage(sender, SMPCore.messages().nationJoinInviteCancelled(target.get(), nation));
    }

    public static boolean leave(
            final @Nullable Member member,
            final @Nullable Nation nation,
            final @NotNull CommandSender sender
    ) {
        if (!sender.hasPermission(Permission.NATION_LEAVE))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());
        if (member == null)
            return sendMessage(sender, SMPCore.messages().errorNotMember());
        if (nation == null)
            return sendMessage(sender, SMPCore.messages().errorNotCitizen());
        if (!nation.id.equals(member.nationID))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());
        if (nation.leaderUUID.equals(member.uuid))
            return sendMessage(sender, SMPCore.messages().errorLeaderLeave());
        nation.remove(member);
        return true;
    }
}
