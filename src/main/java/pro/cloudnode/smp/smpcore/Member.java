package pro.cloudnode.smp.smpcore;

import io.papermc.paper.ban.BanListType;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class Member {
    public final @NotNull UUID uuid;
    public @Nullable String nationID;
    @SuppressWarnings("CanBeFinal")
    public boolean staff;
    public final @Nullable UUID altOwnerUUID;
    public final @NotNull Date added;

    private Member(final @NotNull UUID uuid, final @Nullable String nationID, final boolean staff, final @Nullable UUID altOwnerUUID, final @NotNull Date added) {
        this.uuid = uuid;
        this.nationID = nationID;
        this.staff = staff;
        this.altOwnerUUID = altOwnerUUID;
        this.added = added;
    }

    public Member(final @NotNull OfflinePlayer player, final @Nullable Member altOwner) {
        this(player.getUniqueId(), null, false, altOwner == null ? null : altOwner.uuid, new Date());
    }

    private Member(final @NotNull ResultSet rs) throws @NotNull SQLException {
        this(UUID.fromString(rs.getString("uuid")), rs.getString("nation"), rs.getBoolean("staff"), rs.getString("alt_owner") == null ? null : UUID.fromString(rs.getString("alt_owner")), rs.getTimestamp("added"));
    }

    public @NotNull OfflinePlayer player() {
        return SMPCore.getInstance().getServer().getOfflinePlayer(uuid);
    }

    public boolean isActive() {
        return new Date().getTime() - player().getLastSeen() < (long) SMPCore.config().membersInactiveDays() * 24 * 60 * 60 * 1000;
    }

    public boolean isAlt() {
        return altOwnerUUID != null;
    }

    public @NotNull Optional<@NotNull Member> altOwner() {
        return altOwnerUUID == null ? Optional.empty() : Member.get(altOwnerUUID);
    }

    public @NotNull Optional<@NotNull Nation> nation() {
        return nationID == null ? Optional.empty() : Nation.get(nationID);
    }

    public @NotNull Set<@NotNull Token> tokens() {
        return Token.get(this);
    }

    public @NotNull Set<@NotNull Member> getAlts() {
        final @NotNull Set<@NotNull Member> alts = new HashSet<>();
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db()
                        .getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `members` WHERE `alt_owner` = ?")
        ) {
            stmt.setString(1, uuid.toString());
            final @NotNull ResultSet rs = stmt.executeQuery();
            while (rs.next()) alts.add(new Member(rs));
        }
        catch (final @NotNull SQLException e) {
            SMPCore.getInstance().getLogger().log(Level.SEVERE, "could not get alts for " + player().getName(), e);
        }

        return alts;
    }

    public void unban() {
        SMPCore.getInstance().getServer().getBanList(BanListType.PROFILE).pardon(player().getPlayerProfile());
    }

    public void save() {
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db()
                        .getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("INSERT OR REPLACE INTO `members` (`uuid`, `nation`, `staff`, `alt_owner`, `added`) VALUES (?, ?, ?, ?, ?)")
        ) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, nationID == null ? null : nationID);
            stmt.setBoolean(3, staff);
            stmt.setString(4, altOwnerUUID == null ? null : altOwnerUUID.toString());
            stmt.setTimestamp(5, new java.sql.Timestamp(added.getTime()));
            stmt.executeUpdate();
        }
        catch (final @NotNull SQLException e) {
            SMPCore.getInstance().getLogger().log(Level.SEVERE, "could not save member " + uuid, e);
        }
    }

    /**
     * Removes only from database
     */
    private void remove() {
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db()
                        .getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("DELETE FROM `members` WHERE `uuid` = ?")
        ) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        }
        catch (final @NotNull SQLException e) {
            SMPCore.getInstance().getLogger().log(Level.SEVERE, "could not delete member UUID " + uuid, e);
        }
    }

    /**
     * Member deletion procedure, i.e. membership revocation
     *
     * <p>Will not be deleted if:</p>
     * <ul>
     *   <li>has alts (delete alts first)</li>
     *   <li>is leader of a nation (change leader or delete nation)</li>
     * </ul>
     * <p>If vice leader of a nation, will set nation's leader to both leader and vice leader</p>
     *
     * @return whether the member was deleted
     */
    public boolean delete() {
        if (!getAlts().isEmpty()) return false;
        final @NotNull OfflinePlayer player = player();
        SMPCore.runMain(() -> player.setWhitelisted(false));
        final @NotNull Optional<@NotNull Nation> nation = nation();
        if (nation.isPresent()) {
            if (nation.get().leaderUUID.equals(player.getUniqueId())) return false;
            if (nation.get().viceLeaderUUID.equals(player.getUniqueId())) {
                nation.get().viceLeaderUUID = nation.get().leaderUUID;
                nation.get().save();
            }
            SMPCore.runMain(() -> nation.get().getTeam().removePlayer(player));
        }
        tokens().forEach(Token::delete);
        remove();
        return true;
    }

    public static @NotNull Optional<@NotNull Member> get(final @NotNull OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    public static @NotNull Optional<@NotNull Member> get(final @NotNull UUID uuid) {
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db()
                        .getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `members` WHERE `uuid` = ? LIMIT 1")
        ) {
            stmt.setString(1, uuid.toString());
            final @NotNull ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return Optional.empty();
            return Optional.of(new Member(rs));
        }
        catch (final @NotNull SQLException e) {
            SMPCore.getInstance().getLogger().log(Level.SEVERE, "could not get member for UUID " + uuid, e);
            return Optional.empty();
        }
    }

    public static @NotNull Set<@NotNull Member> get() {
        final @NotNull Set<@NotNull Member> members = new HashSet<>();
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db()
                        .getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `members`")
        ) {
            final @NotNull ResultSet rs = stmt.executeQuery();
            while (rs.next()) members.add(new Member(rs));
        }
        catch (final @NotNull SQLException e) {
            SMPCore.getInstance().getLogger().log(Level.SEVERE, "could not get members", e);
        }
        return members;
    }

    public static @NotNull Set<@NotNull Member> get(int limit, int page) {
        final int offset = (page - 1) * limit;
        final @NotNull Set<@NotNull Member> members = new HashSet<>();
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db()
                        .getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `members` LIMIT ? OFFSET ?")
        ) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            final @NotNull ResultSet rs = stmt.executeQuery();
            while (rs.next()) members.add(new Member(rs));
        }
        catch (final @NotNull SQLException e) {
            SMPCore.getInstance().getLogger().log(Level.SEVERE, "could not get members", e);
        }
        return members;
    }

    public static @NotNull Set<@NotNull Member> get(final @NotNull Nation nation) {
        final @NotNull Set<@NotNull Member> members = new HashSet<>();
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db()
                        .getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `members` WHERE `nation` = ?")
        ) {
            stmt.setString(1, nation.id);
            final @NotNull ResultSet rs = stmt.executeQuery();
            while (rs.next()) members.add(new Member(rs));
        }
        catch (final @NotNull SQLException e) {
            SMPCore.getInstance().getLogger().log(Level.SEVERE, "could not get members for nation " + nation.id, e);
        }
        return members;
    }

    public static int count() {
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db()
                        .getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as `n` FROM `members`")
        ) {
            final @NotNull ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt("n");
        }
        catch (final @NotNull SQLException e) {
            SMPCore.getInstance().getLogger().log(Level.SEVERE, "could not count members", e);
            return 0;
        }
    }

    public static @NotNull Set<@NotNull String> getNames() {
        return get().stream().map(m -> m.player().getName()).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public static @NotNull Set<@NotNull String> getAltNames() {
        return get().stream().filter(Member::isAlt).map(m -> m.player().getName()).filter(Objects::nonNull).collect(Collectors.toSet());
    }
}
