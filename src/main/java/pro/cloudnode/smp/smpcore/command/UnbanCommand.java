package pro.cloudnode.smp.smpcore.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.ban.BanListType;
import org.bukkit.BanEntry;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.smpcore.CachedProfile;
import pro.cloudnode.smp.smpcore.Member;
import pro.cloudnode.smp.smpcore.Permission;
import pro.cloudnode.smp.smpcore.SMPCore;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class UnbanCommand extends Command {
    /**
     * Usage: {@code /<command> <username>}
     */
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission(Permission.BAN)) return sendMessage(sender, SMPCore.messages().errorNoPermission());
        if (args.length < 1) return sendMessage(sender, SMPCore.messages().usage(label, "<username>"));
        final @NotNull OfflinePlayer target = CachedProfile.fetch(args[0]);
        final @NotNull Optional<@NotNull Member> targetMember = Member.get(target);
        if (targetMember.isEmpty()) {
            if (!target.isBanned()) return sendMessage(sender, SMPCore.messages().errorPlayerNotBanned(target));
            SMPCore.getInstance().getServer().getBanList(BanListType.PROFILE).pardon(target.getPlayerProfile());
            return sendMessage(sender, SMPCore.messages().unbannedPlayer(target));
        }
        final @NotNull Member main = targetMember.get().altOwner().orElse(targetMember.get());
        final @NotNull Set<@NotNull Member> alts = main.getAlts();
        main.unban();
        alts.forEach(Member::unban);
        return sendMessage(sender, SMPCore.messages().unbannedMember(main, alts.stream().toList()));
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public @NotNull List<@NotNull String> tab(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length <= 1) {
            final @NotNull Set<@NotNull BanEntry<@NotNull PlayerProfile>> banlist = SMPCore.getInstance().getServer()
                    .getBanList(BanListType.PROFILE).getEntries();
            return banlist.stream().map(BanEntry::getBanTarget).peek(p -> {
                if (!p.completeFromCache()) p.complete();
            }).map(PlayerProfile::getName).filter(Objects::nonNull).toList();
        }
        return List.of();
    }
}
