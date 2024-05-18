package pro.cloudnode.smp.smpcore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    public @NotNull Component bannedPlayer(final @NotNull OfflinePlayer player) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("banned-player")), Placeholder.unparsed("player", Optional
                        .ofNullable(player.getName()).orElse(player.getUniqueId().toString())));
    }

    public @NotNull Component bannedMember(final @NotNull Member member) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("banned-member")), Placeholder.unparsed("player", Optional
                        .ofNullable(member.player().getName()).orElse(member.player().getUniqueId().toString())));
    }

    public @NotNull Component bannedMemberChain(final @NotNull Member member, final @NotNull List<@NotNull Member> alts) {
        final @NotNull String altsString = alts.stream()
                .map(m -> Optional.ofNullable(m.player().getName()).orElse(m.player().getUniqueId().toString()))
                .collect(Collectors.joining(", "));
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("banned-member-chain")), Placeholder.unparsed("player", Optional
                        .ofNullable(member.player().getName()).orElse(member.player().getUniqueId()
                                .toString())), Placeholder.unparsed("n-alt", String.valueOf(alts.size())), Placeholder.unparsed("alts", altsString));
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

    public @NotNull Component seenOnline(final @NotNull OfflinePlayer player) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("seen.online")), Placeholder.unparsed("player", Optional
                        .ofNullable(player.getName()).orElse(player.getUniqueId().toString())));
    }

    public @NotNull Component seen(final @NotNull Member player, final boolean active, final @NotNull Date lastSeen, final @NotNull Component lastSeenRelative) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString(active ? "seen.active" : "seen.inactive")), Placeholder.unparsed("player", Optional
                        .ofNullable(player.player().getName())
                        .orElse(player.uuid.toString())), Formatter.date("last-seen", lastSeen.toInstant()
                        .atZone(ZoneOffset.UTC)
                        .toLocalDateTime()), Placeholder.component("last-seen-relative", lastSeenRelative));
    }

    public @NotNull Component seen(final @NotNull OfflinePlayer player, final @NotNull Date lastSeen, final @NotNull Component lastSeenRelative) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("seen.non-member")), Placeholder.unparsed("player", Optional
                        .ofNullable(player.getName())
                        .orElse(player.getUniqueId().toString())), Formatter.date("last-seen", lastSeen.toInstant()
                        .atZone(ZoneOffset.UTC)
                        .toLocalDateTime()), Placeholder.component("last-seen-relative", lastSeenRelative));
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
