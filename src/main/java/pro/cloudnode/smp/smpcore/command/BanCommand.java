package pro.cloudnode.smp.smpcore.command;

import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.smpcore.Member;
import pro.cloudnode.smp.smpcore.Permission;
import pro.cloudnode.smp.smpcore.SMPCore;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class BanCommand extends Command {
    private static void ban(
            final @NotNull OfflinePlayer player,
            final @Nullable String reason,
            final @Nullable Date expiry,
            final @Nullable String source
    ) {
        SMPCore.runMain(() -> player.ban(reason, expiry, source));
    }

    /**
     * Bans a player and all their alts.
     *
     * @return List of all banned members, with the main account as the first element.
     * <ul>
     *    <li>Empty list: the banned player was not a member.</li>
     *    <li>One element: the banned member had no alts.</li>
     *    <li>Multiple elements: the main account, followed by their alts.</li>
     * </ul>
     */
    public static @NotNull List<@NotNull Member> ban(
            final @NotNull OfflinePlayer player,
            final @Nullable String reason,
            final @Nullable Duration duration,
            final @Nullable OfflinePlayer source
    ) {
        final String banSource = new NamespacedKey(
                SMPCore.getInstance(),
                source == null ? "console" : "player/" + source.getUniqueId()
        ).asString();

        final @Nullable Date banExpiry = duration == null
                ? null
                : Date.from(Instant.now().plus(duration));

        final Optional<Member> targetMember = Member.get(player);
        if (targetMember.isEmpty()) {
            ban(player, reason, banExpiry, banSource);
            return List.of();
        }

        final Member main = targetMember.get().altOwner().orElse(targetMember.get());
        final HashSet<Member> alts = main.getAlts();

        ban(main.player(), reason, banExpiry, banSource);
        final List<Member> bannedMembers = new ArrayList<>();
        bannedMembers.add(main);
        if (alts.isEmpty()) return bannedMembers;

        for (final Member alt : alts) {
            ban(alt.player(), reason, banExpiry, banSource);
            bannedMembers.add(alt);
        }
        return bannedMembers;
    }

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

        final @Nullable String reason = args.length > 1
                ? String.join(" ", Arrays.copyOfRange(args, duration == null ? 1 : 2, args.length))
                : null;

        final @NotNull OfflinePlayer target = SMPCore.getInstance().getServer().getOfflinePlayer(args[0]);

        final List<Member> banned = ban(
                target,
                reason,
                duration,
                sender instanceof final OfflinePlayer player ? player : null
        );

        if (banned.isEmpty())
            return sendMessage(sender, SMPCore.messages().bannedPlayer(target, duration));
        if (banned.size() == 1)
            return sendMessage(sender, SMPCore.messages().bannedMember(banned.get(0), duration));
        return sendMessage(sender, SMPCore.messages().bannedMemberChain(
                banned.get(0),
                banned.subList(1, banned.size()), duration)
        );
    }

    @Override
    public @NotNull List<@NotNull String> tab(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length <= 1) return Arrays.stream(SMPCore.getInstance().getServer().getOfflinePlayers()).filter(p -> !p.isBanned()).map(OfflinePlayer::getName).filter(Objects::nonNull).toList();
        return List.of();
    }
}
