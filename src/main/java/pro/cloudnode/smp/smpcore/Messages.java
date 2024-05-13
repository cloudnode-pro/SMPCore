package pro.cloudnode.smp.smpcore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

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

    public @NotNull Component errorNoPermission() {
        return MiniMessage.miniMessage().deserialize(Objects.requireNonNull(config.getString("error.no-permission")));
    }

    public @NotNull Component errorPlayerNotBanned(final @NotNull OfflinePlayer player) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("error.player-not-banned")), Placeholder.unparsed("player", Optional
                        .ofNullable(player.getName()).orElse(player.getUniqueId().toString())));
    }
}
