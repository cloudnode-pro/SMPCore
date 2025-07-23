package pro.cloudnode.smp.smpcore.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.smpcore.Member;
import pro.cloudnode.smp.smpcore.Messages;
import pro.cloudnode.smp.smpcore.Nation;
import pro.cloudnode.smp.smpcore.Permission;
import pro.cloudnode.smp.smpcore.SMPCore;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class NationCommand extends Command {

    @Override
    public boolean run(final @NotNull CommandSender sender, final @NotNull String label, final @NotNull String @NotNull [] args) {
        if (!sender.hasPermission(Permission.NATION))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());
        if (!(sender instanceof final @NotNull Player player))
            return sendMessage(sender, SMPCore.messages().errorNotPlayer());
        final @NotNull Optional<@NotNull Member> member = Member.get(player);
        if (member.isEmpty())
            return sendMessage(sender, SMPCore.messages().errorNotMember());

        if (args.length == 0) return helpSubCommands(member.get(), sender, label);
        final @NotNull String command = label + " " + args[0];
        final @NotNull String @NotNull [] argsSubset = Arrays.copyOfRange(args, 1, args.length);
        switch (args[0]) {
            case "members": return members(member.get(), sender, command, argsSubset);
            default: return helpSubCommands(member.get(), sender, label);
        }
    }

    @Override
    public @Nullable List<@NotNull String> tab(final @NotNull CommandSender sender, final @NotNull String label, final @NotNull String @NotNull [] args) {
        return List.of();
    }

    public boolean helpSubCommands(final @NotNull Member member, final @NotNull CommandSender sender, final @NotNull String label) {
        final @NotNull String command = "/" + label;
        final @NotNull TextComponent.Builder subCommandBuilder = Component.text()
                .append(SMPCore.messages().subCommandHeader("Nation", command + " ...")).append(Component.newline());
        if (member.nationID != null && (
                sender.hasPermission(Permission.NATION_MEMBERS_LIST)
                || sender.hasPermission(Permission.NATION_MEMBERS_KICK)
        )) subCommandBuilder.append(Component.newline()).append(SMPCore.messages()
                .subCommandEntry(command + " members ", "members"));
        return sendMessage(sender, subCommandBuilder.build());
    }

    public boolean members(final @NotNull Member member, final @NotNull CommandSender sender, final @NotNull String label, final @NotNull String @NotNull [] args) {
        if (args.length == 0) return membersSubcommand(sender, "/" + label);
        final @NotNull String command = label + " " + args[0];
        final @NotNull String @NotNull [] argsSubset = Arrays.copyOfRange(args, 1, args.length);
        return switch (args[0]) {
            case "list" -> listMembers(member, sender);
            case "kick" -> kickMember(member, sender, command, argsSubset);
            default -> membersSubcommand(sender, "/" + label);
        };
    }

    public boolean membersSubcommand(final @NotNull CommandSender sender, final @NotNull String label) {
        final @NotNull TextComponent.Builder subCommandBuilder = Component.text()
                .append(SMPCore.messages().subCommandHeader("Nation Members", label + " ...")).append(Component.newline());
        if (sender.hasPermission(Permission.NATION_MEMBERS_LIST)) subCommandBuilder.append(Component.newline()).append(SMPCore.messages()
                .subCommandEntry(label + " list ", "list"));
        if (sender.hasPermission(Permission.NATION_MEMBERS_KICK)) subCommandBuilder.append(Component.newline()).append(SMPCore.messages()
                .subCommandEntry(label + " kick ", "kick", Messages.SubCommandArgument.of(new Messages.SubCommandArgument("member", true)), "remove a member from the nation"));
        return sendMessage(sender, subCommandBuilder.build());
    }

    public boolean listMembers(final @NotNull Member member, final @NotNull CommandSender sender) {
        if (!sender.hasPermission(Permission.NATION_MEMBERS_LIST))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());

        final @NotNull Optional<@NotNull Nation> nation = member.nation();
        if (nation.isEmpty())
            return sendMessage(sender, SMPCore.messages().errorNotInNation());

        return sendMessage(sender, SMPCore.messages().nationMembersList(nation.get(), sender));
    }

    public boolean kickMember(final @NotNull Member member, final @NotNull CommandSender sender, final @NotNull String label, final @NotNull String @NotNull [] args) {
        if (!sender.hasPermission(Permission.NATION_MEMBERS_KICK))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());
        final @NotNull Optional<@NotNull Nation> nation = member.nation();
        if (nation.isEmpty())
            return sendMessage(sender, SMPCore.messages().errorNotInNation());
        if (args.length == 0) return sendMessage(sender, SMPCore.messages().usage(label, "kick <member>"));
        final @NotNull OfflinePlayer target = sender.getServer().getOfflinePlayer(args[0]);
        final @NotNull Optional<@NotNull Member> targetMemberOptional = Member.get(target);
        if (targetMemberOptional.isEmpty())
            return sendMessage(sender, SMPCore.messages().errorNotMember(target));
        final @NotNull Member targetMember = targetMemberOptional.get();
        if (targetMember.nationID == null || !targetMember.nationID.equals(member.nationID))
            return sendMessage(sender, SMPCore.messages().errorMemberNotYourNation(targetMember));
        if (targetMember.uuid.equals(nation.get().leaderUUID) || targetMember.uuid.equals(nation.get().viceLeaderUUID))
            return sendMessage(sender, SMPCore.messages().errorKickLeadership());

        nation.get().remove(targetMember);
        return sendMessage(sender, SMPCore.messages().nationMembersKicked(targetMember));
    }
}
