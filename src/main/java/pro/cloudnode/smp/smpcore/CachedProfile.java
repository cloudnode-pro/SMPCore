package pro.cloudnode.smp.smpcore;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.Bukkit;
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
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a player profile cached in the database.
 *
 * @param uuid    UUID of the player.
 * @param name    Username of the player.
 * @param fetched Date the profile was fetched from Mojang.
 */
@SuppressWarnings("ProfileCache")
public record CachedProfile(@NotNull UUID uuid, @NotNull String name, @NotNull Date fetched) {
    private static final @NotNull Duration MAX_AGE = Duration.ofDays(7);
    static final @NotNull Duration STALE_WHILE_REVALIDATE = Duration.ofDays(1);

    private static final @NotNull HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private static final @NotNull Logger logger = Logger
            .getLogger(SMPCore.getInstance().getLogger().getName() + "/ProfileCache");

    static {
        logger.setParent(SMPCore.getInstance().getLogger());
    }

    /**
     * Gets a cached profile by UUID.
     *
     * @param uuid UUID to look up in the database.
     */
    public static @NotNull Optional<@NotNull CachedProfile> getOffline(final @NotNull UUID uuid) {
        try (final PreparedStatement stmt = SMPCore.getInstance().conn.prepareStatement(
                "SELECT * FROM `cached_profiles` WHERE `uuid` = ? LIMIT 1"
        )) {
            stmt.setString(1, uuid.toString());

            final ResultSet rs = stmt.executeQuery();
            if (!rs.next())
                return Optional.empty();

            return Optional.<@NotNull CachedProfile>ofNullable(CachedProfile.from(rs));
        }
        catch (final SQLException e) {
            logger.log(Level.SEVERE, "could not get profile for UUID " + uuid, e);
            return Optional.empty();
        }
    }

    /**
     * Gets a cached profile for the specified OfflinePlayer.
     *
     * @param player OfflinePlayer to look up in the database.
     */
    public static @NotNull Optional<@NotNull CachedProfile> getOffline(final @NotNull OfflinePlayer player) {
        return getOffline(player.getUniqueId());
    }

    /**
     * Fetches a profile from Mojang and caches it.
     *
     * @param uuid UUID of the player to fetch.
     */
    private static @Nullable CachedProfile fetch(final @NotNull UUID uuid) {
        logger.fine("Miss for UUID " + uuid);

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
                logger.log(Level.SEVERE, "got HTTP status " + res.statusCode()
                        + " for UUID " + uuid + ". Body is" + (res.body() == null ? "" : " not")
                        + " null.");
                return null;
            }

            final JsonObject body = JsonParser.parseString(res.body()).getAsJsonObject();

            return new CachedProfile(uuid, body.get("name").getAsString(), new Date())
                    .save();
        }
        catch (final IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "could not fetch profile for UUID " + uuid, e);
            return null;
        }
    }

    /**
     * Fetches a profile from Mojang and caches it.
     *
     * @param player OfflinePlayer to fetch.
     */
    private static @Nullable CachedProfile fetch(final @NotNull OfflinePlayer player) {
        return fetch(player.getUniqueId());
    }

    /**
     * Gets a profile from the cache or fetches it from Mojang if cache stale or missed.
     *
     * @param uuid UUID of the player to fetch.
     * @throws IllegalStateException if the profile could not be fetched.
     */
    public static @NotNull CachedProfile get(final @NotNull UUID uuid) throws IllegalStateException {
        final @Nullable CachedProfile profile = getOffline(uuid).orElseGet(() -> fetch(uuid));

        if (profile == null)
            throw new IllegalStateException("profile for UUID " + uuid + " could not be fetched");

        return profile;
    }

    /**
     * Gets a profile from the cache or fetches it from Mojang if cache stale or missed.
     * @param player OfflinePlayer to fetch.
     * @throws IllegalStateException if the profile could not be fetched.
     */
    public static @NotNull CachedProfile get(final @NotNull OfflinePlayer player) throws IllegalStateException {
        return get(player.getUniqueId());
    }

    /**
     * Get OfflinePlayer by name if cached.
     *
     * @param name Username of the player to look up.
     */
    public static @Nullable OfflinePlayer getOffline(final @NotNull String name) {
        final @Nullable OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(name);
        if (player != null)
            return player;
        
        try (final PreparedStatement stmt = SMPCore.getInstance().conn.prepareStatement(
                "SELECT * from `cached_profiles` where `name` = ? LIMIT  1"
        )) {
            stmt.setString(1, name);
            
            final ResultSet rs = stmt.executeQuery();
            if (!rs.next())
                return null;

            final @Nullable CachedProfile profile = CachedProfile.from(rs);
            if (profile == null)
                return null;

            return Bukkit.getOfflinePlayer(profile.uuid());
        }
        catch (final SQLException e) {
            logger.log(Level.SEVERE, "could not get cached profile for name " + name, e);
            return null;
        }
    }

    /**
     * Get OfflinePlayer by name, fetching from Mojang if necessary.
     *
     * @param name Username of the player to look up.
     */
    public static @NotNull OfflinePlayer get(final @NotNull String name) {
        final OfflinePlayer player = Optional.<@NotNull OfflinePlayer>ofNullable(getOffline(name))
                .orElseGet(() -> {
                    logger.fine("Miss for name " + name);
                    return Bukkit.getOfflinePlayer(name);
                });

        CachedProfile.from(player);

        return player;
    }

    /**
     * Deletes stale cached profiles.
     */
    public static void cleanUp() {
        logger.fine("Cleaning up stale cached profiles...");
        try (final @NotNull PreparedStatement stmt = SMPCore.getInstance().conn.prepareStatement(
                "DELETE FROM `cached_profiles` WHERE `fetched` < ?"
        )) {
            stmt.setTimestamp(1, new Timestamp(staleWhileRevalidateThreshold().getTime()));
            stmt.execute();
        }
        catch (final SQLException e) {
            logger.log(Level.SEVERE, "could not clean up cached profiles", e);
        }
    }

    public static @NotNull String getName(final @NotNull OfflinePlayer player) {
        final @Nullable String name = player.getName();
        if (name != null)
            return name;

        final PlayerProfile profile = player.getPlayerProfile();

        if (profile.completeFromCache(true)) {
            final @Nullable String profileName = profile.getName();
            if (profileName != null && !profileName.isEmpty())
                return profileName;
        }

        try {
            return get(player).name();
        }
        catch (IllegalStateException e) {
            logger.log(Level.SEVERE, "Failed to fetch", e);
            return player.getUniqueId().toString();
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
            logger.fine("Profile cache is stale for " + profile.uuid + " (" + Duration.between(profile.fetched.toInstant(), Instant.now()) + " old). Revalidation scheduled.");
            SMPCore.runAsync(() -> CachedProfile.fetch(profile.uuid()));

            if (profile.staleWhileRevalidate()) {
                logger.fine("Serving stale cache for " + profile.uuid + " while revalidating.");
                return profile;
            }
            return null;
        }

        return profile;
    }

    private static @Nullable CachedProfile from(final @NotNull OfflinePlayer player) {
        if (player.getName() == null)
            return null;

        return new CachedProfile(player.getUniqueId(), player.getName(), new Date())
                .save();
    }

    private boolean stale() {
        return fetched.before(expirationThreshold());
    }

    private boolean staleWhileRevalidate() {
        return fetched.before(staleWhileRevalidateThreshold());
    }

    private @NotNull CachedProfile save() {
        SMPCore.runAsync(() -> {
            try (final PreparedStatement stmt = SMPCore.getInstance().conn.prepareStatement(
                    "INSERT OR REPLACE INTO `cached_profiles` (`uuid`, `name`, `fetched`) VALUES (?, ?, ?)"
            )) {
                stmt.setString(1, uuid().toString());
                stmt.setString(2, name());
                stmt.setTimestamp(3, new Timestamp(fetched().getTime()));
                stmt.executeUpdate();
            } catch (final SQLException e) {
                logger.log(Level.SEVERE, "could not save profile for UUID " + uuid, e);
            }
        });

        return this;
    }
}
