package pro.cloudnode.smp.smpcore;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Represents a player profile cached in the database.
 *
 * @param uuid    UUID of the player.
 * @param name    Username of the player.
 * @param fetched Date the profile was fetched from Mojang.
 */
public record CachedProfile(@NotNull UUID uuid, @NotNull String name, @NotNull Date fetched) {
    private static final @NotNull Duration MAX_AGE = Duration.ofDays(7);
    private static final @NotNull Duration STALE_WHILE_REVALIDATE = Duration.ofDays(1);

    private static final @NotNull HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /**
     * Gets a cached profile by UUID.
     *
     * @param uuid UUID to look up in the database.
     */
    public static @NotNull Optional<@NotNull CachedProfile> get(final @NotNull UUID uuid) {
        try (final PreparedStatement stmt = SMPCore.getInstance().conn.prepareStatement(
                "SELECT * FROM `names_cache` WHERE `uuid` = ? LIMIT 1"
        )) {
            stmt.setString(1, uuid.toString());

            final ResultSet rs = stmt.executeQuery();
            if (!rs.next())
                return Optional.empty();

            return Optional.<@NotNull CachedProfile>ofNullable(CachedProfile.from(rs));
        }
        catch (final SQLException e) {
            SMPCore.getInstance().getLogger().log(Level.SEVERE, "could not get cached profile for UUID " + uuid, e);
            return Optional.empty();
        }
    }

    /**
     * Gets a cached profile for the specified OfflinePlayer.
     *
     * @param player OfflinePlayer to look up in the database.
     */
    public static @NotNull Optional<@NotNull CachedProfile> get(final @NotNull OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    /**
     * Fetches a profile from Mojang and caches it.
     *
     * @param uuid UUID of the player to fetch.
     */
    public static @Nullable CachedProfile fetch(final @NotNull UUID uuid) {
        final PluginMeta meta = SMPCore.getInstance().getPluginMeta();

        final HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString()))
                .header("User-Agent", meta.getName() + "/" + meta.getVersion())
                .GET()
                .build();

        try {
            final HttpResponse<String> res = client
                    .send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() >= 400 || res.statusCode() == 204 || res.statusCode() < 200 || res.body() == null) {
                SMPCore.getInstance().getLogger().log(Level.SEVERE, "got HTTP status " + res.statusCode()
                        + " fetching profile for UUID " + uuid + ". Body is" + (res.body() == null ? "" : " not")
                        + " null.");
                return null;
            }

            final JsonObject body = JsonParser.parseString(res.body()).getAsJsonObject();

            final CachedProfile profile = new CachedProfile(uuid, body.get("name").getAsString(), new Date());

            try (final PreparedStatement stmt = SMPCore.getInstance().conn.prepareStatement(
                    "INSERT OR REPLACE INTO `names_cache` (`uuid`, `name`, `fetched`) VALUES (?, ?, ?)"
            )) {
                stmt.setString(1, profile.uuid().toString());
                stmt.setString(2, profile.name());
                stmt.setTimestamp(3, new Timestamp(profile.fetched().getTime()));
                stmt.executeUpdate();
            }
            catch (final SQLException e) {
                SMPCore.getInstance().getLogger().log(Level.SEVERE, "could not save profile for UUID " + uuid, e);
            }

            return profile;
        }
        catch (final IOException | InterruptedException e) {
            SMPCore.getInstance().getLogger().log(Level.SEVERE, "could not fetch profile for UUID " + uuid, e);
            return null;
        }
    }

    /**
     * Fetches a profile from Mojang and caches it.
     *
     * @param player OfflinePlayer to fetch.
     */
    public static @Nullable CachedProfile fetch(final @NotNull OfflinePlayer player) {
        return fetch(player.getUniqueId());
    }

    /**
     * Gets a profile from the cache or fetches it from Mojang if cache stale or missed.
     *
     * @param uuid UUID of the player to fetch.
     * @throws IllegalStateException if the profile could not be fetched.
     */
    public static @NotNull CachedProfile getOrFetch(final @NotNull UUID uuid) throws IllegalStateException {
        final @Nullable CachedProfile profile = get(uuid).orElseGet(() -> fetch(uuid));

        if (profile == null)
            throw new IllegalStateException("profile for UUID " + uuid + " could not be fetched");

        return profile;
    }

    /**
     * Gets a profile from the cache or fetches it from Mojang if cache stale or missed.
     * @param player OfflinePlayer to fetch.
     * @throws IllegalStateException if the profile could not be fetched.
     */
    public static @NotNull CachedProfile getOrFetch(final @NotNull OfflinePlayer player) throws IllegalStateException {
        return getOrFetch(player.getUniqueId());
    }

    /**
     * Deletes stale cached profiles.
     */
    public static void cleanUp() {
        try (final @NotNull PreparedStatement stmt = SMPCore.getInstance().conn.prepareStatement(
                "DELETE FROM `names_cache` WHERE `fetched` < ?"
        )) {
            stmt.setTimestamp(1, new Timestamp(staleWhileRevalidateThreshold().getTime()));
            stmt.execute();
        }
        catch (final SQLException e) {
            SMPCore.getInstance().getLogger().log(Level.SEVERE, "could not clean up cached profiles", e);
        }
    }

    private static @NotNull Date expirationThreshold() {
        return new Date(System.currentTimeMillis() - MAX_AGE.toMillis());
    }

    private static @NotNull Date staleWhileRevalidateThreshold() {
        return new Date(System.currentTimeMillis() - STALE_WHILE_REVALIDATE.toMillis() - MAX_AGE.toMillis());
    }

    private static @Nullable CachedProfile from(final ResultSet rs) throws SQLException {
        final CachedProfile profile = new CachedProfile(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("name"),
                rs.getTimestamp("fetched")
        );

        if (profile.stale()) {
            SMPCore.runAsync(() -> CachedProfile.fetch(profile.uuid()));

            return profile.staleWhileRevalidate() ? profile : null;
        }

        return profile;
    }

    private boolean stale() {
        return fetched.before(expirationThreshold());
    }

    private boolean staleWhileRevalidate() {
        return fetched.before(staleWhileRevalidateThreshold());
    }
}
