package pro.cloudnode.smp.smpcore.command;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.smpcore.Member;
import pro.cloudnode.smp.smpcore.Permission;
import pro.cloudnode.smp.smpcore.SMPCore;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public final class SeenCommand extends Command {

    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission(Permission.SEEN)) return sendMessage(sender, SMPCore.messages().errorNoPermission());

        if (args.length != 1) return sendMessage(sender, SMPCore.messages().usage(label, "<player>"));

        final @NotNull OfflinePlayer player = sender.getServer().getOfflinePlayer(args[0]);
        if (player.isOnline()) return sendMessage(sender, SMPCore.messages().seenOnline(player));

        if (!player.hasPlayedBefore()) return sendMessage(sender, SMPCore.messages().errorNeverJoined(player));

        final @NotNull Optional<@NotNull Member> member = Member.get(player.getUniqueId());

        return member.map(value -> sendMessage(sender, SMPCore.messages()
                        .seen(value, value.isActive(), new Date(player.getLastSeen()), SMPCore.relativeTime(new Date(player.getLastSeen())))))
                .orElseGet(() -> sendMessage(sender, SMPCore.messages().seen(player, new Date(player.getLastSeen()), SMPCore.relativeTime(new Date(player.getLastSeen())))));
    }

    @Override
    public @NotNull List<@NotNull String> tab(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        return Member.getNames().stream().toList();
    }
}
