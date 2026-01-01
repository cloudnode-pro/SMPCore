package pro.cloudnode.smp.smpcore.command;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.smpcore.CachedProfile;
import pro.cloudnode.smp.smpcore.Member;
import pro.cloudnode.smp.smpcore.Permission;
import pro.cloudnode.smp.smpcore.SMPCore;

import java.util.List;
import java.util.Optional;

public final class SeenCommand extends Command {

    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission(Permission.SEEN)) return sendMessage(sender, SMPCore.messages().errorNoPermission());

        if (args.length != 1) return sendMessage(sender, SMPCore.messages().usage(label, "<player>"));

        final @NotNull OfflinePlayer player = CachedProfile.get(args[0]);

        if (!player.hasPlayedBefore()) return sendMessage(sender, SMPCore.messages().errorNeverJoined(player));

        final @NotNull Optional<@NotNull Member> member = Member.get(player.getUniqueId());

        if (!sender.hasPermission(Permission.SEEN_STAFF) && member.isPresent() && member.get().staff) return sendMessage(sender, SMPCore.messages().errorCommandOnStaff(label));

        return member.map(m -> sendMessage(sender, SMPCore.messages().seen(m)))
                .orElseGet(() -> sendMessage(sender, SMPCore.messages().seen(player)));
    }

    @Override
    public @NotNull List<@NotNull String> tab(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender.hasPermission(Permission.SEEN_STAFF)) return Member.getNames().stream().toList();
        else return Member.get().stream()
                .filter(m -> !m.staff)
                .map(m -> SMPCore.getName(m.player()))
                .toList();
    }
}
