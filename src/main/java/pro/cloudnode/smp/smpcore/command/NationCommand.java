package pro.cloudnode.smp.smpcore.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.smpcore.Member;
import pro.cloudnode.smp.smpcore.Nation;
import pro.cloudnode.smp.smpcore.Permission;
import pro.cloudnode.smp.smpcore.SMPCore;

import java.util.List;
import java.util.Optional;

public final class NationCommand extends Command {

    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> tab(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }

    public boolean listMembers(@NotNull CommandSender sender) {
        if (!sender.hasPermission(Permission.NATION) || !sender.hasPermission(Permission.NATION_MEMBERS_LIST))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());
        if (!(sender instanceof final @NotNull Player player))
            return sendMessage(sender, SMPCore.messages().errorNotPlayer());
        final @NotNull Optional<@NotNull Member> member = Member.get(player);
        if (member.isEmpty())
            return sendMessage(sender, SMPCore.messages().errorNotMember());
        final @NotNull Optional<@NotNull Nation> nation = member.get().nation();
        if (nation.isEmpty())
            return sendMessage(sender, SMPCore.messages().errorNotInNation());

        return sendMessage(sender, SMPCore.messages().nationMembersList(nation.get()));
    }
}
