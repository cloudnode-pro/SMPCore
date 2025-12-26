package pro.cloudnode.smp.smpcore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class Messages extends BaseConfig {

    public Messages() {
        super("messages.yml");
    }

    public @NotNull Component reloaded() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("reloaded")));
    }

    public @NotNull Component usage(final @NotNull String label, final @NotNull String args) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("usage")), Placeholder.unparsed("label", label), Placeholder.unparsed("args", args));
    }

    private @NotNull Component formatDuration(@Nullable Duration duration) {
        if (duration == null) return SMPCore.config().relativeTimeDurationIndefinite();

        final long seconds = Math.abs(duration.getSeconds());
        final long days    = seconds / 86_400;
        final long hours   = (seconds % 86_400) / 3_600;
        final long minutes = (seconds % 3_600) / 60;
        final long secs    = seconds % 60;

        final ArrayList<Component> components = new ArrayList<>();

        if (days    > 0) components.add(SMPCore.config().relativeTime(days, ChronoUnit.DAYS));
        if (hours   > 0) components.add(SMPCore.config().relativeTime(hours, ChronoUnit.HOURS));
        if (minutes > 0) components.add(SMPCore.config().relativeTime(minutes, ChronoUnit.MINUTES));
        if (secs    > 0) components.add(SMPCore.config().relativeTime(secs, ChronoUnit.SECONDS));

        final Component joined = Component.join(JoinConfiguration.spaces(), components);

        return SMPCore.config().relativeTimeDuration(joined);
    }

    public @NotNull Component bannedPlayer(final @NotNull OfflinePlayer player, final @Nullable Duration duration) {
        return MiniMessage.miniMessage()
                .deserialize(
                        Objects.requireNonNull(config.getString("banned-player")),
                        Placeholder.unparsed("player", Optional.ofNullable(player.getName()).orElse(player.getUniqueId().toString())),
                        Placeholder.component("duration", formatDuration(duration))
                );
    }

    public @NotNull Component bannedMember(final @NotNull Member member, final @Nullable Duration duration) {
        return MiniMessage.miniMessage()
                .deserialize(
                        Objects.requireNonNull(config.getString("banned-member")),
                        Placeholder.unparsed("player", Optional.ofNullable(member.player().getName()).orElse(member.player().getUniqueId().toString())),
                        Placeholder.component("duration", formatDuration(duration))
                );
    }

    public @NotNull Component bannedMemberChain(final @NotNull Member member, final @NotNull List<@NotNull Member> alts, final @Nullable Duration duration) {
        final @NotNull String altsString = alts.stream()
                .map(m -> Optional.ofNullable(m.player().getName()).orElse(m.player().getUniqueId().toString()))
                .collect(Collectors.joining(", "));
        return MiniMessage.miniMessage()
                .deserialize(
                        Objects.requireNonNull(config.getString("banned-member-chain")),
                        Placeholder.unparsed("player", Optional.ofNullable(member.player().getName()).orElse(member.player().getUniqueId().toString())),
                        Placeholder.unparsed("n-alt", String.valueOf(alts.size())),
                        Placeholder.unparsed("alts", altsString),
                        Placeholder.component("duration", formatDuration(duration))
                );
    }

    public @NotNull Component unbannedPlayer(final @NotNull OfflinePlayer player) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("unbanned-player")), Placeholder.unparsed("player", Optional
                        .ofNullable(player.getName()).orElse(player.getUniqueId().toString())));
    }

    public @NotNull Component unbannedMember(final @NotNull Member member, final @NotNull List<@NotNull Member> alts) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("unbanned-member")), Placeholder.unparsed("player", Optional
                        .ofNullable(member.player().getName()).orElse(member.player().getUniqueId()
                                .toString())), Placeholder.unparsed("n-alts", String.valueOf(alts.size())));
    }

    // subcommands

    public @NotNull Component subCommandHeader(final @NotNull String name, final @NotNull String usage) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("subcommands.header")), Placeholder.unparsed("name", name), Placeholder.unparsed("usage", usage));
    }

    public @NotNull Component subCommandEntry(final @NotNull String command, final @NotNull String label, final @NotNull SubCommandArgument @NotNull [] args) {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("subcommands.entry"))
                .replace("<command>", command), Placeholder.unparsed("label", label), Placeholder.component("args", SubCommandArgument.join(args)));
    }

    public @NotNull Component subCommandEntry(final @NotNull String command, final @NotNull String label) {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("subcommands.entry"))
                .replace("<command>", command), Placeholder.unparsed("label", label), Placeholder.component("args", SubCommandArgument.join(SubCommandArgument.of())));
    }

    public @NotNull Component subCommandEntry(final @NotNull String command, final @NotNull String label, final @NotNull SubCommandArgument @NotNull [] args, final @NotNull String description) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("subcommands.entry-with-description"))
                        .replace("<command>", command), Placeholder.unparsed("label", label), Placeholder.component("args", SubCommandArgument.join(args)), Placeholder.unparsed("description", description));
    }

    public @NotNull Component subCommandEntry(final @NotNull String command, final @NotNull String label, final @NotNull String description) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("subcommands.entry-with-description"))
                        .replace("<command>", command), Placeholder.unparsed("label", label), Placeholder.component("args", SubCommandArgument.join(SubCommandArgument.of())), Placeholder.unparsed("description", description));
    }

    public @NotNull Component subCommandArgumentRequired(final @NotNull String argument) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("subcommands.argument.required")), Placeholder.unparsed("arg", argument));
    }

    public @NotNull Component subCommandArgumentOptional(final @NotNull String argument) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("subcommands.argument.optional")), Placeholder.unparsed("arg", argument));
    }

    // end of subcommands

    public @NotNull Component altsListHeader(final @NotNull Member member) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("alts.list.header")), Placeholder.unparsed("player", Optional
                        .ofNullable(member.player().getName()).orElse(member.player().getUniqueId().toString())));
    }

    public @NotNull Component altsListNone() {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("alts.list.none")));
    }

    public @NotNull Component altsListEntry(final @NotNull Member alt) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("alts.list.entry")), Placeholder.unparsed("alt", Optional
                        .ofNullable(alt.player().getName()).orElse(alt.player().getUniqueId().toString())));
    }

    public @NotNull Component altsConfirmAdd(final @NotNull OfflinePlayer alt, final @NotNull String confirmCommand) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("alts.confirm-add")).replace("<confirm-command>", confirmCommand), Placeholder.unparsed("alt", Optional
                        .ofNullable(alt.getName()).orElse(alt.getUniqueId().toString())));
    }

    public @NotNull Component altsCreated(final @NotNull Member alt) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("alts.created")), Placeholder.unparsed("alt", Optional
                        .ofNullable(alt.player().getName()).orElse(alt.player().getUniqueId().toString())));
    }

    public @NotNull Component altsDeleted(final @NotNull Member alt) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("alts.deleted")), Placeholder.unparsed("alt", Optional
                        .ofNullable(alt.player().getName()).orElse(alt.player().getUniqueId().toString())));
    }

    public @NotNull Component seen(final @NotNull Member member) {
        if (member.player().isOnline()) return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("seen.online")), Placeholder.unparsed("player", Optional
                        .ofNullable(member.player().getName()).orElse(member.uuid.toString())));
        final @NotNull Date lastSeen = new Date(member.player().getLastSeen());
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString(member.isActive() ? "seen.active" : "seen.inactive")), Placeholder.unparsed("player", Optional
                        .ofNullable(member.player().getName())
                        .orElse(member.uuid.toString())), Formatter.date("last-seen", lastSeen.toInstant()
                        .atZone(ZoneOffset.UTC)
                        .toLocalDateTime()), Placeholder.component("last-seen-relative", SMPCore.relativeTime(lastSeen)));
    }

    public @NotNull Component seen(final @NotNull OfflinePlayer player) {
        if (player.isOnline()) return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("seen.online")), Placeholder.unparsed("player", Optional
                        .ofNullable(player.getName()).orElse(player.getUniqueId().toString())));
        final @NotNull Date lastSeen = new Date(player.getLastSeen());
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("seen.non-member")), Placeholder.unparsed("player", Optional
                        .ofNullable(player.getName())
                        .orElse(player.getUniqueId().toString())), Formatter.date("last-seen", lastSeen.toInstant()
                        .atZone(ZoneOffset.UTC)
                        .toLocalDateTime()), Placeholder.component("last-seen-relative", SMPCore.relativeTime(lastSeen)));
    }

    public @NotNull Component time(final @NotNull Date date) {
        final @NotNull Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("time")), Formatter.date("date", calendar
                        .toInstant().atZone(ZoneOffset.UTC)
                        .toLocalDateTime()), Placeholder.unparsed("day", String.valueOf(day)), Placeholder.unparsed("day", String.valueOf(day)), Formatter.choice("day-format", day));
    }

    public @NotNull Component nationCitizensStatus(final @NotNull Member member) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString(member.isActive() ? "nation.citizens.status.active" : "nation.citizens.status.inactive")));
    }

    public @NotNull Component nationCitizensList(final @NotNull Nation nation, final @NotNull Permissible sender, final boolean other) {
        final @NotNull HashSet<@NotNull Member> members = nation.citizens();
        final @NotNull Component header = MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("nation.citizens.list.header"))
                        .replaceAll("<color>", "<#" + nation.color + ">")
                        .replaceAll("</color>", "</#" + nation.color + ">"),
                        Placeholder.unparsed("nation-name", nation.name),
                        Placeholder.unparsed("nation-active-members", members.stream().filter(Member::isActive).count() + ""),
                        Placeholder.unparsed("nation-inactive-members", members.stream().filter(m -> !m.isActive()).count() + ""),
                        Placeholder.unparsed("nation-total-members", members.size() + "")
                );

        final @NotNull List<@NotNull Component> list = new ArrayList<>();

        final @NotNull Member leader = members.stream().filter(m -> m.uuid.equals(nation.leaderUUID)).findFirst().orElseThrow(IllegalStateException::new);
        list.add(nationCitizensListEntry(nation, leader, sender, other));

        final @NotNull Member vice = members.stream().filter(m -> m.uuid.equals(nation.viceLeaderUUID)).findFirst().orElseThrow(IllegalStateException::new);
        if (!vice.uuid.equals(leader.uuid))
            list.add(nationCitizensListEntry(nation, vice, sender, other));

        final @NotNull List<@NotNull Component> citizens = members.stream().filter(m -> !m.uuid.equals(leader.uuid) && !m.uuid.equals(vice.uuid) && m.isActive()).map(m -> nationCitizensListEntry(nation, m, sender, other)).toList();
        list.addAll(citizens);

        final @NotNull List<@NotNull Component> inactive = members.stream().filter(m -> !m.uuid.equals(leader.uuid) && !m.uuid.equals(vice.uuid) && !m.isActive()).map(m -> nationCitizensListEntry(nation, m, sender, other)).toList();
        list.addAll(inactive);

        final @NotNull TextComponent.Builder listComponent = Component.text();
        for (int i = 0; i < list.size(); i++) {
            listComponent.append(list.get(i));
            if (i + 1 < list.size()) listComponent.append(Component.newline());
        }

        return Component.text().append(header).append(Component.newline()).append(listComponent.build()).build();
    }

    private @NotNull Component nationCitizensListEntry(final @NotNull Nation nation, final @NotNull Member member, final @NotNull Permissible sender, final boolean other) {
        if (member.uuid.equals(nation.leaderUUID)) {
            return MiniMessage.miniMessage()
                    .deserialize(Objects.requireNonNull(config.getString("nation.citizens.list.entry.leader"))
                            .replaceAll("<color>", "<#" + nation.color + ">")
                            .replaceAll("</color>", "</#" + nation.color + ">")
                            .replaceAll("<member-name>", Optional.ofNullable(member.player().getName()).orElse(member.uuid.toString())),
                            Placeholder.component("member-status", nationCitizensStatus(member)),
                            Placeholder.component("buttons", nationCitizensListButtons(nation, member, sender, other))
                    );
        }
        if (member.uuid.equals(nation.viceLeaderUUID)) {
            return MiniMessage.miniMessage()
                    .deserialize(Objects.requireNonNull(config.getString("nation.citizens.list.entry.vice"))
                                    .replaceAll("<color>", "<#" + nation.color + ">")
                                    .replaceAll("</color>", "</#" + nation.color + ">")
                                    .replaceAll("<member-name>", Optional.ofNullable(member.player().getName()).orElse(member.uuid.toString())),
                            Placeholder.component("member-status", nationCitizensStatus(member)),
                            Placeholder.component("buttons", nationCitizensListButtons(nation, member, sender, other))
                    );
        }
        if (member.isActive()) {
            return MiniMessage.miniMessage()
                    .deserialize(Objects.requireNonNull(config.getString("nation.citizens.list.entry.citizen"))
                                    .replaceAll("<color>", "<#" + nation.color + ">")
                                    .replaceAll("</color>", "</#" + nation.color + ">")
                                    .replaceAll("<member-name>", Optional.ofNullable(member.player().getName()).orElse(member.uuid.toString())),
                            Placeholder.component("member-status", nationCitizensStatus(member)),
                            Placeholder.component("buttons", nationCitizensListButtons(nation, member, sender, other))
                    );
        }
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("nation.citizens.list.entry.inactive-citizen"))
                                .replaceAll("<color>", "<#" + nation.color + ">")
                                .replaceAll("</color>", "</#" + nation.color + ">")
                                .replaceAll("<member-name>", Optional.ofNullable(member.player().getName()).orElse(member.uuid.toString())),
                        Placeholder.component("member-status", nationCitizensStatus(member)),
                        Placeholder.component("buttons", nationCitizensListButtons(nation, member, sender, other))
                );
    }

    private @NotNull Component nationCitizensListButtons(final @NotNull Nation nation, final @NotNull Member member, final @NotNull Permissible sender, final boolean other) {
        final @NotNull List<@NotNull Component> buttons = new ArrayList<>();
        if (
                ((!other && sender.hasPermission(Permission.NATION_CITIZENS_KICK))
                        || sender.hasPermission(Permission.NATION_CITIZENS_KICK_OTHER))
                        && !(member.uuid.equals(nation.leaderUUID) || member.uuid.equals(nation.viceLeaderUUID))
        )
            buttons.add(nationCitizensListButton("kick", nation, member));
        if (
                ((!other && sender.hasPermission(Permission.NATION_DEMOTE))
                        || sender.hasPermission(Permission.NATION_DEMOTE_OTHER))
                        && member.uuid.equals(nation.viceLeaderUUID)
        )
            buttons.add(nationCitizensListButton("demote", nation, member));

        if (
                ((!other && sender.hasPermission(Permission.NATION_PROMOTE))
                        || sender.hasPermission(Permission.NATION_PROMOTE_OTHER))
                        && nation.viceLeaderUUID.equals(nation.leaderUUID)
                        && !member.uuid.equals(nation.leaderUUID)
                        && member.isActive()
        )
            buttons.add(nationCitizensListButton("promote", nation, member));

        final @NotNull TextComponent.Builder buttonsComponent = Component.text();
        if (!buttons.isEmpty()) buttonsComponent.append(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("nation.citizens.list.buttons.prefix"))));
        for (int i = 0; i < buttons.size(); i++) {
            buttonsComponent.append(buttons.get(i));
            if (i + 1 < buttons.size()) buttonsComponent.append(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("nation.citizens.list.buttons.separator"))));
        }
        if (!buttons.isEmpty()) buttonsComponent.append(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("nation.citizens.list.buttons.suffix"))));

        return Component.text().append(buttonsComponent.build()).build();
    }

    private @NotNull Component nationCitizensListButton(final @NotNull String button, final @NotNull Nation nation, final @NotNull Member member) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("nation.citizens.list.buttons." + button))
                                .replaceAll("<color>", "<#" + nation.color + ">")
                                .replaceAll("</color>", "</#" + nation.color + ">")
                                .replaceAll("<member-name>", Optional.ofNullable(member.player().getName()).orElse(member.uuid.toString())));
    }

    public @NotNull Component nationCitizensKicked(final @NotNull Member member) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("nation.citizens.kicked")), Placeholder.unparsed("player", Optional
                        .ofNullable(member.player().getName()).orElse(member.uuid.toString())));
    }

    public @NotNull Component nationCitizensVicePromoted(final @NotNull Member member) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("nation.citizens.vice.promoted")), Placeholder.unparsed("player", Optional.ofNullable(member.player().getName()).orElse(member.uuid.toString())));
    }

    public @NotNull Component nationCitizensVicePromoted() {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("nation.citizens.vice.promoted-you")));
    }

    public @NotNull Component nationCitizensViceDemoted(final @NotNull Member member) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("nation.citizens.vice.demoted")), Placeholder.unparsed("player", Optional.ofNullable(member.player().getName()).orElse(member.uuid.toString())));
    }

    public @NotNull Component nationCitizensViceDemoted() {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("nation.citizens.vice.demoted-you")));
    }

    public @NotNull Component nationJoinRequestSent(final @NotNull Nation nation) {
        return MiniMessage.miniMessage()
                .deserialize(
                        Objects.requireNonNull(config.getString("nation.join.request-sent"))
                                .replaceAll("<nation-id>", nation.id),
                        Placeholder.unparsed("nation", nation.name)
                );
    }

    public @NotNull Component nationJoinRequestReceived(final @NotNull Member member) {
        return MiniMessage.miniMessage()
                .deserialize(
                        Objects.requireNonNull(config.getString("nation.join.request-received"))
                                .replaceAll("<player>", Optional.ofNullable(member.player().getName()).orElse(member.uuid.toString()))
                );
    }

    public @NotNull Component nationJoinInviteSent(final @NotNull Member member) {
        return MiniMessage.miniMessage()
                .deserialize(
                        Objects.requireNonNull(config.getString("nation.join.invite-sent"))
                                .replaceAll("<player>", Optional.ofNullable(member.player().getName()).orElse(member.uuid.toString()))
                );
    }

    public @NotNull Component nationJoinInviteReceived(final @NotNull Nation nation) {
        return MiniMessage.miniMessage()
                .deserialize(
                        Objects.requireNonNull(config.getString("nation.join.invite-received"))
                                .replaceAll("<nation-id>", nation.id),
                        Placeholder.unparsed("nation", nation.name)
                );
    }

    public @NotNull Component nationJoinJoined(final @NotNull Member member) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("nation.join.joined")), Placeholder.unparsed("player", Optional
                        .ofNullable(member.player().getName()).orElse(member.uuid.toString())));
    }

    public @NotNull Component nationJoinLeft(final @NotNull Member member) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("nation.join.left")), Placeholder.unparsed("player", Optional
                        .ofNullable(member.player().getName()).orElse(member.uuid.toString())));
    }

    public @NotNull Component nationJoinRequestCancelled(final @NotNull Nation nation) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("nation.join.request-cancelled")), Placeholder.unparsed("nation", nation.name));
    }

    public @NotNull Component nationJoinRequestRejected(final @NotNull Member member, final @NotNull Nation nation) {
        return MiniMessage.miniMessage()
                .deserialize(
                        Objects.requireNonNull(config.getString("nation.join.request-rejected")),
                        Placeholder.unparsed("player", Optional.ofNullable(
                                member.player().getName()).orElse(member.uuid.toString())
                        ),
                        Placeholder.unparsed("nation", nation.name)
                );
    }

    public @NotNull Component nationJoinInviteCancelled(final @NotNull Member member, final @NotNull Nation nation) {
        return MiniMessage.miniMessage()
                .deserialize(
                        Objects.requireNonNull(config.getString("nation.join.invite-cancelled")),
                        Placeholder.unparsed("player", Optional.ofNullable(
                                member.player().getName()).orElse(member.uuid.toString())
                        ),
                        Placeholder.unparsed("nation", nation.name)
                );
    }

    public @NotNull Component nationJoinInviteRejected(final @NotNull Nation nation) {
        return MiniMessage.miniMessage()
                .deserialize(
                        Objects.requireNonNull(config.getString("nation.join.invite-rejected")),
                        Placeholder.unparsed("nation", nation.name)
                );
    }

    // errors

    public @NotNull Component errorNoPermission() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.no-permission")));
    }

    public @NotNull Component errorPlayerNotBanned(final @NotNull OfflinePlayer player) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("error.player-not-banned")), Placeholder.unparsed("player", Optional
                        .ofNullable(player.getName()).orElse(player.getUniqueId().toString())));
    }

    public @NotNull Component errorNotMember(final @NotNull OfflinePlayer player) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("error.not-member")), Placeholder.unparsed("player", Optional
                        .ofNullable(player.getName()).orElse(player.getUniqueId().toString())));
    }

    public @NotNull Component errorNotMember() {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("error.not-member-you")));
    }

    public @NotNull Component errorAltAlreadyMember(final @NotNull Member player) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("error.alt-already-member")), Placeholder.unparsed("player", Optional
                        .ofNullable(player.player().getName()).orElse(player.player().getUniqueId().toString())));
    }

    public @NotNull Component errorDisallowedCharacters(final @NotNull HashSet<@NotNull Character> chars) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("error.disallowed-characters")), Placeholder.unparsed("chars", chars.stream().map(String::valueOf).collect(Collectors.joining())));
    }

    public @NotNull Component errorFailedDeleteMember(final @NotNull Member player) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("error.failed-delete-member")), Placeholder.unparsed("player", Optional
                        .ofNullable(player.player().getName()).orElse(player.player().getUniqueId().toString())));
    }

    public @NotNull Component errorAlreadyYourAlt(final @NotNull Member alt) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("error.already-your-alt")), Placeholder.unparsed("alt", Optional
                        .ofNullable(alt.player().getName()).orElse(alt.player().getUniqueId().toString())));
    }

    public @NotNull Component errorMaxAltsReached(final int max) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("error.max-alts-reached")), Placeholder.unparsed("max", String.valueOf(max)));
    }

    public @NotNull Component errorMemberNotAlt(final @NotNull Member player) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("error.member-not-alt")), Placeholder.unparsed("player", Optional
                        .ofNullable(player.player().getName()).orElse(player.player().getUniqueId().toString())));
    }

    public @NotNull Component errorRemoveJoinedAlt(final @NotNull Member player) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("error.remove-joined-alt")), Placeholder.unparsed("player", Optional
                        .ofNullable(player.player().getName()).orElse(player.player().getUniqueId().toString())));
    }

    public @NotNull Component errorNeverJoined(final @NotNull OfflinePlayer player) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("error.never-joined")), Placeholder.unparsed("player", Optional
                        .ofNullable(player.getName()).orElse(player.getUniqueId().toString())));
    }

    public @NotNull Component errorCommandOnStaff(final @NotNull String label) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("error.command-on-staff")), Placeholder.unparsed("command", label));
    }

    public @NotNull Component errorNotCitizen() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.not-citizen-you")));
    }

    public @NotNull Component errorNotCitizen(final @NotNull Member member) {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.not-citizen")), Placeholder
                .unparsed("player", Optional
                        .ofNullable(member.player().getName()).orElse(member.player().getUniqueId().toString())));
    }

    public @NotNull Component errorAlreadyCitizen() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.already-citizen")));
    }

    public @NotNull Component errorAlreadyCitizen(final @NotNull Nation nation) {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.already-citizen-nation")), Placeholder.unparsed("nation", nation.name));
    }

    public @NotNull Component errorAlreadyCitizen(final @NotNull Member member) {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.already-citizen-player")), Placeholder.unparsed("player", Optional.ofNullable(member.player().getName()).orElse(member.player().getUniqueId().toString())));
    }

    public @NotNull Component errorOtherCitizen(final @NotNull Member member) {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.other-citizen")), Placeholder.unparsed("player", Optional.ofNullable(member.player().getName()).orElse(member.player().getUniqueId().toString())));
    }

    public @NotNull Component errorNotPlayer() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.not-player")));
    }

    public @NotNull Component errorKickLeadership() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.kick-leadership")));
    }

    public @NotNull Component errorLeaderLeave() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.leader-leave")));
    }

    public @NotNull Component errorNationNotFound(final @NotNull String nation) {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.nation-not-found")), Placeholder.unparsed("nation", nation));
    }

    public @NotNull Component errorNotInvited(final @NotNull Nation nation) {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.not-invited")), Placeholder.unparsed("nation", nation.name));
    }

    public @NotNull Component errorAlreadyRequestedJoin(final @NotNull Nation nation) {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.already-requested-join")), Placeholder.unparsed("nation", nation.name));
    }

    public @NotNull Component errorAlreadyInvited(final @NotNull Member member) {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.already-invited")), Placeholder.unparsed("player", Optional.ofNullable(member.player().getName()).orElse(member.player().getUniqueId().toString())));
    }

    public @NotNull Component errorNoRequest(final @NotNull Nation nation) {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.no-request-nation")), Placeholder.unparsed("nation", nation.name));
    }

    public @NotNull Component errorNoRequest(final @NotNull Member member) {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.no-request-player")), Placeholder.unparsed("player", Optional.ofNullable(member.player().getName()).orElse(member.player().getUniqueId().toString())));
    }

    public @NotNull Component errorViceConflict() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.vice-conflict")));
    }

    public @NotNull Component errorAlreadyVice(final @NotNull Member member) {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.already-vice")), Placeholder.unparsed("player", Optional.ofNullable(member.player().getName()).orElse(member.player().getUniqueId().toString())));
    }

    public @NotNull Component errorDemoteLeader() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.demote-leader")));
    }

    public @NotNull Component errorDemoteCitizen() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.demote-citizen")));
    }

    public @NotNull Component errorDurationZeroOrLess() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.duration-zero-or-less")));
    }

    public record SubCommandArgument(@NotNull String name, boolean required) {
        public @NotNull Component component() {
            return required ? SMPCore.messages().subCommandArgumentRequired(name) : SMPCore.messages()
                    .subCommandArgumentOptional(name);
        }

        public static @NotNull Component join(final @NotNull SubCommandArgument @NotNull [] args) {
            final @NotNull TextComponent.Builder builder = Component.text().append(Component.text(" "));

            for (int i = 0; i < args.length; ++i) {
                builder.append(args[i].component());
                if (i < args.length - 1) builder.append(Component.text(" "));
            }
            return builder.build();
        }

        public static @NotNull SubCommandArgument @NotNull [] of(final @Nullable SubCommandArgument @NotNull ... args) {
            return Arrays.stream(args).filter(Objects::nonNull).toArray(SubCommandArgument[]::new);
        }
    }
}
