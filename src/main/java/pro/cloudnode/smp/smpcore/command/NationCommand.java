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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
            case "citizens" -> citizens(member.orElse(null), nation.orElse(null), sender, command, argsSubset);
            case "join" -> join(member.orElse(null), sender, command, argsSubset);
            default -> helpSubCommands(member.orElse(null), nation.orElse(null), sender, label);
        };
    }

    @Override
    public @Nullable List<@NotNull String> tab(
            final @NotNull CommandSender sender,
            final @NotNull String label,
            final @NotNull String @NotNull [] args
    ) {
        return List.of();
    }

    public boolean helpSubCommands(
            final @Nullable Member member,
            final @Nullable Nation nation,
            final @NotNull CommandSender sender,
            final @NotNull String label
    ) {
        final boolean other = member == null || nation == null || !nation.id.equals(member.nationID);

        final @NotNull String command = "/" + label + (other && nation != null ? " id:" + nation.id : "");
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
        }

        if (hasAnyPermission(sender, Permission.NATION_JOIN_REQUEST) && (
                member == null || member.nationID == null || sender.hasPermission(Permission.NATION_JOIN_REQUEST_SWITCH)
        )) {
            subCommandBuilder.append(Component.newline()).append(SMPCore.messages().subCommandEntry(
                    command + " join ", "join", new Messages.SubCommandArgument[]{
                            new Messages.SubCommandArgument("nation", true)
                    }, "Request to join a nation."
            ));
        }

        return sendMessage(sender, subCommandBuilder.build());
    }

    public boolean citizens(
            final @Nullable Member member,
            final @Nullable Nation nation,
            final @NotNull CommandSender sender,
            final @NotNull String label,
            final @NotNull String @NotNull [] args
    ) {
        if (nation == null)
            return sendMessage(sender, SMPCore.messages().errorNotCitizen());

        if (args.length == 0)
            return citizensSubcommand(member, nation, sender, "/" + label);
        final @NotNull String command = label + " " + args[0];
        final @NotNull String @NotNull [] argsSubset = Arrays.copyOfRange(args, 1, args.length);
        return switch (args[0]) {
            case "list" -> listMembers(member, nation, sender);
            case "kick" -> kickMember(member, nation, sender, command, argsSubset);
            default -> citizensSubcommand(member, nation, sender, "/" + label);
        };
    }

    public boolean citizensSubcommand(
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

        return sendMessage(sender, subCommandBuilder.build());
    }

    public boolean listMembers(
            final @Nullable Member member,
            final @NotNull Nation nation,
            final @NotNull CommandSender sender
    ) {
        if (!sender.hasPermission(Permission.NATION_CITIZENS_LIST_OTHER) && (
                member == null || !nation.id.equals(member.nationID)
                        || !sender.hasPermission(Permission.NATION_CITIZENS_LIST)
        ))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());
        else
            return sendMessage(sender, SMPCore.messages().nationCitizensList(nation, sender));
    }

    public boolean kickMember(
            final @Nullable Member member,
            final @NotNull Nation nation,
            final @NotNull CommandSender sender,
            final @NotNull String label,
            final @NotNull String @NotNull [] args
    ) {
        if (!sender.hasPermission(Permission.NATION_CITIZENS_KICK_OTHER) && (
                member == null || !nation.id.equals(member.nationID)
                        || !sender.hasPermission(Permission.NATION_CITIZENS_KICK)
        ))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());

        if (args.length == 0)
            return sendMessage(sender, SMPCore.messages().usage(label, "<citizen>"));

        final @NotNull OfflinePlayer target = sender.getServer().getOfflinePlayer(args[0]);
        final @NotNull Optional<@NotNull Member> targetMemberOptional = Member.get(target);
        if (targetMemberOptional.isEmpty())
            return sendMessage(sender, SMPCore.messages().errorNotMember(target));

        final @NotNull Member targetMember = targetMemberOptional.get();
        if (targetMember.nationID == null || !targetMember.nationID.equals(member.nationID))
            return sendMessage(sender, SMPCore.messages().errorNotCitizen(targetMember));
        if (targetMember.uuid.equals(nation.leaderUUID) || targetMember.uuid.equals(nation.viceLeaderUUID))
            return sendMessage(sender, SMPCore.messages().errorKickLeadership());

        nation.remove(targetMember);
        return sendMessage(sender, SMPCore.messages().nationCitizensKicked(targetMember));
    }

    public boolean join(
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
            return newRequest.send();
        }

        if (request.get().mode && (!sender.hasPermission(Permission.NATION_JOIN_FORCE) || !Arrays.asList(args).contains("--force")))
            return sendMessage(sender, SMPCore.messages().errorAlreadyRequestedJoin(nation.get()));

        request.get().accept();
        return true;
    }
}
