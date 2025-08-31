package pro.cloudnode.smp.smpcore;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Stream;

public final class Nation {
    /**
     * 2-character country code
     */
    public final @NotNull String id;

    /**
     * Full legal name, e.g. "The Federal Republic of FooBar". This name is only used in formal contexts.
     * <p>
     * Max length: 128
     */
    public @NotNull String name;

    /**
     * Nation's short name, e.g. "FooBar". This name is used almost everywhere.
     * <p>
     * Max length: 16
     */
    public @NotNull String shortName;

    /**
     * Nation's colour in hex, e.g. {@code 22c55e} (without #).
     * <p>
     * Max length: 6
     */
    public @NotNull String color;

    /**
     * Nation leader's UUID
     */
    public @NotNull UUID leaderUUID;

    /**
     * Nation vice-leader's UUID
     */
    public @NotNull UUID viceLeaderUUID;

    /**
     * Date of foundation
     */
    public final @NotNull Date founded;

    /**
     * In-game absolute ticks when this nation was founded (based on {@link org.bukkit.World#getFullTime()})
     */
    public final long foundedTicks;

    /**
     * National bank account of the nation
     */
    public final @NotNull String bank;

    /**
     * @param id         See {@link #id}
     * @param name       See {@link #name}
     * @param shortName  See {@link #shortName}
     * @param colour     See {@link #color}
     * @param leaderUUID     See {@link #leaderUUID}
     * @param viceLeaderUUID See {@link #viceLeaderUUID}
     * @param founded    See {@link #founded}
     * @param foundedTicks See {@link #foundedTicks}
     * @param bank       See {@link #bank}
     */
    public Nation(final @NotNull String id, final @NotNull String name, final @NotNull String shortName, final @NotNull String colour, final @NotNull UUID leaderUUID, final @NotNull UUID viceLeaderUUID, final @NotNull Date founded, final long foundedTicks, final @NotNull String bank) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.color = colour;
        this.leaderUUID = leaderUUID;
        this.viceLeaderUUID = viceLeaderUUID;
        this.founded = founded;
        this.foundedTicks = foundedTicks;
        this.bank = bank;
    }

    public @NotNull HashSet<@NotNull Member> citizens() {
        return Member.get(this);
    }

    public @NotNull Member vice() {
        return Member.get(viceLeaderUUID).orElseThrow(() -> new IllegalStateException("vice leader uuid " + viceLeaderUUID + " of nation " + id + " not found"));
    }

    public @NotNull List<@NotNull Player> onlinePlayers() {
        final Stream<@NotNull Player> stream = citizens().stream()
                                                                .map(Member::player)
                                                                .map(OfflinePlayer::getPlayer)
                                                                .filter(Objects::nonNull);
        return stream.toList();
    }

    public @NotNull Team createTeam() {
        final @NotNull Optional<@NotNull Team> optionalTeam = Optional.ofNullable(SMPCore.getInstance().getServer().getScoreboardManager().getMainScoreboard().getTeam(id));
        optionalTeam.ifPresent(Team::unregister);
        final @NotNull Team team = SMPCore.getInstance().getServer().getScoreboardManager().getMainScoreboard().registerNewTeam(id);
        team.setAllowFriendlyFire(false);
        team.setCanSeeFriendlyInvisibles(true);
        team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
        team.displayName(Component.text(shortName).color(TextColor.color(Integer.decode("0x" + color))).hoverEvent(HoverEvent.showText(Component.text(name))));
        team.prefix(Component.text(shortName + " ").color(TextColor.color(Integer.decode("0x" + color))).hoverEvent(HoverEvent.showText(Component.text(name))));
        for (final @NotNull Member member : citizens()) try {
            team.addPlayer(member.player());
        }
        catch (final @NotNull IllegalArgumentException ignored) {}
        return team;
    }

    public @NotNull Team getTeam() {
        final @NotNull Optional<@NotNull Team> optionalTeam = Optional.ofNullable(SMPCore.getInstance().getServer().getScoreboardManager().getMainScoreboard().getTeam(id));
        final @NotNull Team team;
        team = optionalTeam.orElseGet(this::createTeam);
        return team;
    }

    public void add(final @NotNull Member member) {
        member.nation().ifPresent(nation -> nation.remove(member));
        member.nationID = id;
        member.save();
        getTeam().addPlayer(member.player());

        Audience.audience(onlinePlayers()).sendMessage(SMPCore.messages().nationJoinJoined(member));

        CitizenRequest.get(member, this).ifPresent(CitizenRequest::delete);
        CitizenRequest.delete(CitizenRequest.get(member, true));
    }

    public void remove(final @NotNull Member member) {
        if (!id.equals(member.nationID))
            throw new IllegalStateException("Member " + member.uuid + " is not in nation " + id);
        if (member.uuid.equals(leaderUUID))
            throw new IllegalStateException("Cannot remove leader " + member.uuid + " from nation " + id);

        Audience.audience(onlinePlayers()).sendMessage(SMPCore.messages().nationJoinLeft(member));

        if (member.uuid.equals(viceLeaderUUID)) {
            this.viceLeaderUUID = this.leaderUUID;
            save();
        }

        member.nationID = null;
        member.save();
        getTeam().removePlayer(member.player());
    }

    public Nation(final @NotNull ResultSet rs) throws @NotNull SQLException {
        this(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("short_name"),
                rs.getString("color"),
                UUID.fromString(rs.getString("leader")),
                UUID.fromString(rs.getString("vice")),
                rs.getTimestamp("founded"),
                rs.getLong("founded_ticks"),
                rs.getString("bank")
        );
    }

    public void save() {
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db()
                        .getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("INSERT OR REPLACE INTO `nations` (`id`, `name`, `short_name`, `color`, `leader`, `vice`, `founded`, `founded_ticks`, `bank`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
        ) {
            stmt.setString(1, id);
            stmt.setString(2, name);
            stmt.setString(3, shortName);
            stmt.setString(4, color);
            stmt.setString(5, leaderUUID.toString());
            stmt.setString(6, viceLeaderUUID.toString());
            stmt.setTimestamp(7, new java.sql.Timestamp(founded.getTime()));
            stmt.setLong(8, foundedTicks);
            stmt.setString(9, bank);
            stmt.executeUpdate();
        }
        catch (final @NotNull SQLException e) {
            SMPCore.getInstance().getLogger().log(Level.SEVERE, "could not save nation " + id, e);
        }
    }

    public static @NotNull Optional<@NotNull Nation> get(final @NotNull Team team) {
        return get(team.getName());
    }

    public static @NotNull Optional<@NotNull Nation> get(final @NotNull String id) {
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db()
                        .getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `nations` WHERE `id` = ? LIMIT 1")
        ) {
            stmt.setString(1, id);
            final @NotNull ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return Optional.empty();
            return Optional.of(new Nation(rs));
        }
        catch (final @NotNull SQLException e) {
            SMPCore.getInstance().getLogger().log(Level.SEVERE, "could not get nation " + id, e);
            return Optional.empty();
        }
    }

    public static @NotNull HashSet<@NotNull Nation> get() {
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db()
                        .getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `nations`")
        ) {
            final @NotNull ResultSet rs = stmt.executeQuery();
            final @NotNull HashSet<@NotNull Nation> nations = new HashSet<>();
            while (rs.next()) nations.add(new Nation(rs));
            return nations;
        }
        catch (final @NotNull SQLException e) {
            SMPCore.getInstance().getLogger().log(Level.SEVERE, "could not get nations", e);
            return new HashSet<>();
        }
    }
}
