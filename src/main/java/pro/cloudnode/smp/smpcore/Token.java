package pro.cloudnode.smp.smpcore;

import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.smpcore.exception.MemberNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public final class Token {
    public final @NotNull UUID token;
    public final @NotNull UUID memberUUID;
    public final @NotNull Date created;
    public final @NotNull Date lastUsed;

    public Token(final @NotNull UUID token, final @NotNull UUID memberUUID, final @NotNull Date created, final @NotNull Date lastUsed) {
        this.token = token;
        this.memberUUID = memberUUID;
        this.created = created;
        this.lastUsed = lastUsed;
    }

    public Token(final @NotNull ResultSet rs) throws @NotNull SQLException {
        this(
                UUID.fromString(rs.getString("token")),
                UUID.fromString(rs.getString("player")),
                rs.getTimestamp("created"),
                rs.getTimestamp("last_used")
        );
    }

    public void save() throws @NotNull SQLException {
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db()
                        .getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("INSERT OR REPLACE INTO `tokens` (`token`, `player`, `created`, `last_used`) VALUES (?, ?, ?, ?)")
        ) {
            stmt.setString(1, token.toString());
            stmt.setString(2, memberUUID.toString());
            stmt.setString(3, created.toString());
            stmt.setString(4, lastUsed.toString());
            stmt.executeUpdate();
        }
    }

    public void delete() throws @NotNull SQLException {
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db()
                        .getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("DELETE FROM `tokens` WHERE `token` = ?")
        ) {
            stmt.setString(1, token.toString());
            stmt.executeUpdate();
        }
    }

    public static @NotNull Optional<@NotNull Token> get(final @NotNull UUID token) throws @NotNull SQLException, @NotNull MemberNotFoundException {
        try (
                final @NotNull Connection conn = SMPCore.getInstance().db()
                        .getConnection(); final @NotNull PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `tokens` WHERE `token` = ? LIMIT 1")
        ) {
            stmt.setString(1, token.toString());
            final @NotNull ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return Optional.empty();
            return Optional.of(new Token(rs));
        }
    }

    public static @NotNull Token create(final @NotNull Member member) throws @NotNull SQLException {
        final @NotNull Token token = new Token(UUID.randomUUID(), member.uuid, new Date(), new Date());
        token.save();
        return token;
    }
}
