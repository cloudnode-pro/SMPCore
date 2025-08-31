package pro.cloudnode.smp.smpcore;

import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.smpcore.command.Command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class CitizenRequest {
    /**
     * Member UUID
     */
    public final @NotNull UUID uuid;

    /**
     * Nation ID
     */
    public final @NotNull String nationID;

    /**
     * Request mode. True if the member requests citizenship, false if the nation invites the member.
     */
    public final boolean mode;

    /**
     * Request creation timestamp
     */
    public final @NotNull Date created;

    /**
     * Request expiration timestamp
     */
    public final @NotNull Date expires;

    /**
     * @param uuid     Member UUID
     * @param nationID Nation ID
     * @param mode     Request mode. True if the member requests citizenship, false if the nation invites the member.
     * @param created  Request creation timestamp
     * @param expires  Request expiration timestamp
     */
    public CitizenRequest(
            final @NotNull UUID uuid,
            final @NotNull String nationID,
            final boolean mode,
            final @NotNull Date created,
            final @NotNull Date expires
    ) {
        this.uuid = uuid;
        this.nationID = nationID;
        this.mode = mode;
        this.created = created;
        this.expires = expires;
    }

    /**
     * Checks whether this request has expired
     */
    public boolean expired() {
        return new Date().after(expires);
    }

    /**
     * Gets the member
     */
    public @NotNull Member member() {
        return Member.get(uuid).orElseThrow(() -> new IllegalArgumentException("Member not found"));
    }

    /**
     * Gets the nation
     */
    public @NotNull Nation nation() {
        return Nation.get(nationID).orElseThrow(() -> new IllegalArgumentException("Nation not found"));
    }

    public boolean send() {
        final @NotNull Member member = member();
        final @Nullable Player player = member.player().getPlayer();
        final @NotNull Nation nation = nation();

        // invited
        if (!mode) {
            if (player != null)
                player.sendMessage(SMPCore.messages().nationJoinInviteReceived(nation));
            return true;
        }

        // requested
        return Command.sendMessage(
                Audience.audience(
                        nation().onlinePlayers().stream()
                                .filter(p -> p.hasPermission(Permission.NATION_INVITE))
                                .toList()
                ),
                SMPCore.messages().nationJoinRequestReceived(member)
        );
    }

    /**
     * Accepts the request
     */
    public void accept() {
        nation().add(member());
        delete();
    }

    /**
     * Rejects the request
     */
    public void reject() {
        delete();
    }

    public CitizenRequest(final @NotNull ResultSet rs) throws SQLException {
        this(
                UUID.fromString(rs.getString("member")),
                rs.getString("nation"),
                rs.getBoolean("mode"),
                rs.getTimestamp("created"),
                rs.getTimestamp("expires")
        );
    }

    public void save() {
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db().getConnection();
                final @NotNull PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO `citizen_requests` (`member`, `nation`, `mode`, `created`, `expires`) VALUES (?,"
                                + " ?, ?, ?, ?)")
        ) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, nationID);
            stmt.setBoolean(3, mode);
            stmt.setTimestamp(4, new Timestamp(created.getTime()));
            stmt.setTimestamp(5, new Timestamp(expires.getTime()));
            stmt.executeUpdate();
        }
        catch (final @NotNull SQLException e) {
            SMPCore.getInstance()
                   .getLogger()
                   .log(
                           Level.SEVERE,
                           "could not save citizen request for member: " + uuid + ", nation: " + nationID,
                           e
                   );
        }
    }

    public void delete() {
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db().getConnection();
                final @NotNull PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM `citizen_requests` WHERE `member` = ? AND `nation` = ?")
        ) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, nationID);
            stmt.executeUpdate();
        }
        catch (final @NotNull SQLException e) {
            SMPCore.getInstance()
                   .getLogger()
                   .log(
                           Level.SEVERE,
                           "could not delete citizen request for member: " + uuid + ", nation: " + nationID,
                           e
                   );
        }
    }

    /**
     * Gets a citizen request
     *
     * @param member Member
     * @param nation Nation
     */
    public static @NotNull Optional<@NotNull CitizenRequest> get(
            final @NotNull Member member,
            final @NotNull Nation nation
    ) {
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db().getConnection();
                final @NotNull PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM `citizen_requests` WHERE `member` = ? AND `nation` = ? LIMIT 1")
        ) {
            stmt.setString(1, member.uuid.toString());
            stmt.setString(2, nation.id);
            final @NotNull ResultSet rs = stmt.executeQuery();
            if (!rs.next())
                return Optional.empty();
            return Optional.of(new CitizenRequest(rs));
        }
        catch (final @NotNull SQLException e) {
            SMPCore.getInstance().getLogger().log(
                    Level.SEVERE,
                    "could not get citizen request for member: " + member.uuid + ", nation: " + nation.id,
                    e
            );
            return Optional.empty();
        }
    }

    /**
     * Gets all requests for joining the nation
     *
     * @param nation Nation
     * @param mode   Request mode (true for citizen requests, false for nation invitations)
     */
    public static @NotNull List<@NotNull CitizenRequest> get(final @NotNull Nation nation, final boolean mode) {
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db().getConnection();
                final @NotNull PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM `citizen_requests` WHERE `nation` = ? AND `mode` = ? ORDER BY `created`")
        ) {
            stmt.setString(1, nation.id);
            stmt.setBoolean(2, mode);
            final @NotNull ResultSet rs = stmt.executeQuery();
            final @NotNull List<@NotNull CitizenRequest> requests = new ArrayList<>();
            while (rs.next())
                requests.add(new CitizenRequest(rs));
            return requests;
        }
        catch (final @NotNull SQLException e) {
            SMPCore.getInstance()
                   .getLogger()
                   .log(Level.SEVERE, "could not get citizen requests for nation: " + nation.id + ", mode: " + mode, e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets all requests for joining any nation for the member
     *
     * @param member Member
     * @param mode   Request mode (true for citizen requests, false for nation invitations)
     */
    public static @NotNull List<@NotNull CitizenRequest> get(final @NotNull Member member, final boolean mode) {
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db().getConnection();
                final @NotNull PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM `citizen_requests` WHERE `member` = ? AND `mode` = ? ORDER BY `created`")
        ) {
            stmt.setString(1, member.uuid.toString());
            stmt.setBoolean(2, mode);
            final @NotNull ResultSet rs = stmt.executeQuery();
            final @NotNull List<@NotNull CitizenRequest> requests = new ArrayList<>();
            while (rs.next())
                requests.add(new CitizenRequest(rs));
            return requests;
        }
        catch (final @NotNull SQLException e) {
            SMPCore.getInstance()
                   .getLogger()
                   .log(
                           Level.SEVERE,
                           "could not get citizen requests for member: " + member.uuid + ", mode: " + mode,
                           e
                   );
            return new ArrayList<>();
        }
    }

    /**
     * Deletes multiple requests
     *
     * @param requests Requests
     */
    public static void delete(final @NotNull List<@NotNull CitizenRequest> requests) {
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db().getConnection();
                final @NotNull PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM `citizen_requests` WHERE `member` = ? AND `nation` = ?")
        ) {
            conn.setAutoCommit(false);
            for (final @NotNull CitizenRequest request : requests) {
                stmt.setString(1, request.uuid.toString());
                stmt.setString(2, request.nationID);
                stmt.addBatch();
            }

            stmt.executeBatch();
            conn.commit();
        }
        catch (final @NotNull SQLException e) {
            SMPCore.getInstance().getLogger().log(
                    Level.SEVERE,
                    "could not delete citizen requests: "
                            + requests.stream()
                                      .map(r -> "(" + r.nationID + ", " + r.uuid + ")")
                                      .collect(Collectors.joining(", ")),
                    e
            );
        }
    }
}
