package pro.cloudnode.smp.smpcore.command;

import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.smpcore.Member;
import pro.cloudnode.smp.smpcore.Permission;
import pro.cloudnode.smp.smpcore.SMPCore;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class BanCommand extends Command {
    /**
     * Usage: {@code /<command> <username> [duration] [reason]}
     */
    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission(Permission.BAN))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());
        if (args.length < 1)
            return sendMessage(sender, SMPCore.messages().usage(label, "<username> [duration] [reason]"));

        final @Nullable String durationArg = args.length > 1 ? args[1] : null;
        @Nullable Duration duration = null;
        if (durationArg != null && durationArg.matches("(?i)^PT?\\d.*")) try {
            duration = Duration.parse(durationArg);
        }
        catch (DateTimeParseException ignored) {
            return sendMessage(sender, SMPCore.messages().invalidDuration(durationArg));
        }

        if (duration != null && (duration.isNegative() || duration.isZero()))
            return sendMessage(sender, SMPCore.messages().errorDurationZeroOrLess());

        final @Nullable Date banExpiry = duration == null ? null : Date.from(Instant.now().plus(duration));

        final @Nullable String reason = args.length > 1
                ? String.join(" ", Arrays.copyOfRange(args, duration == null ? 1 : 2, args.length))
                : null;
        final @NotNull NamespacedKey banSource;
        if (sender instanceof final @NotNull Player player)
            banSource = new NamespacedKey(SMPCore.getInstance(), "player/" + player.getUniqueId());
        else banSource = new NamespacedKey(SMPCore.getInstance(), "console");

        final @NotNull OfflinePlayer target = SMPCore.getInstance().getServer().getOfflinePlayer(args[0]);
        final @NotNull Optional<@NotNull Member> targetMember = Member.get(target);
        if (targetMember.isEmpty()) {
            SMPCore.runMain(() -> target.ban(reason, banExpiry, banSource.asString()));
            return sendMessage(sender, SMPCore.messages().bannedPlayer(target, duration));
        }
        final @NotNull Member main = targetMember.get().altOwner().orElse(targetMember.get());
        final @NotNull HashSet<@NotNull Member> alts = main.getAlts();

        SMPCore.runMain(() -> main.player().ban(reason, banExpiry, banSource.asString()));
        if (alts.isEmpty()) return sendMessage(sender, SMPCore.messages().bannedMember(main, duration));
        else {
            SMPCore.runMain(() -> {
                for (final @NotNull Member alt : alts)
                    alt.player().ban(reason, banExpiry, banSource.asString());
            });
            return sendMessage(sender, SMPCore.messages().bannedMemberChain(main, alts.stream().toList(), duration));
        }
    }

    @Override
    public @NotNull List<@NotNull String> tab(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length <= 1) return Arrays.stream(SMPCore.getInstance().getServer().getOfflinePlayers()).filter(p -> !p.isBanned()).map(OfflinePlayer::getName).filter(Objects::nonNull).toList();
        return List.of();
    }
}
