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

    public @NotNull Token createToken() throws @NotNull SQLException {
        return Token.create(this);
    }

    public @NotNull HashSet<@NotNull Member> getAlts() {
        final @NotNull HashSet<@NotNull Member> alts = new HashSet<>();
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

    private void delete() {
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

    public static @NotNull Member create(final @NotNull OfflinePlayer player, final @Nullable Member altOwner) {
        final @NotNull Member member = new Member(player.getUniqueId(), null, false, altOwner == null ? null : altOwner.uuid, new Date());
        member.save();
        member.player().setWhitelisted(true);
        return member;
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

    public static @NotNull HashSet<@NotNull Member> get() {
        final @NotNull HashSet<@NotNull Member> members = new HashSet<>();
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

    public static @NotNull HashSet<@NotNull Member> get(int limit, int page) {
        final int offset = (page - 1) * limit;
        final @NotNull HashSet<@NotNull Member> members = new HashSet<>();
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

    public static @NotNull HashSet<@NotNull Member> get(final @NotNull Nation nation) {
        final @NotNull HashSet<@NotNull Member> members = new HashSet<>();
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

    public static @NotNull Set<@NotNull String> getNames() {
        return get().stream().map(m -> m.player().getName()).filter(Objects::nonNull).collect(Collectors.toSet());
    }
}
