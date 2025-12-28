package pro.cloudnode.smp.smpcore;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.smpcore.command.AltsCommand;
import pro.cloudnode.smp.smpcore.command.BanCommand;
import pro.cloudnode.smp.smpcore.command.CitizensCommand;
import pro.cloudnode.smp.smpcore.command.Command;
import pro.cloudnode.smp.smpcore.command.MainCommand;
import pro.cloudnode.smp.smpcore.command.NationCommand;
import pro.cloudnode.smp.smpcore.command.SeenCommand;
import pro.cloudnode.smp.smpcore.command.TimeCommand;
import pro.cloudnode.smp.smpcore.command.UnbanCommand;
import pro.cloudnode.smp.smpcore.listener.PlayerJoinListener;
import pro.cloudnode.smp.smpcore.listener.PlayerDeathListener;
import pro.cloudnode.smp.smpcore.listener.PlayerPostRespawnListener;
import pro.cloudnode.smp.smpcore.listener.PlayerPreLoginListener;
import pro.cloudnode.smp.smpcore.listener.PlayerServerFullCheckListener;
import pro.cloudnode.smp.smpcore.listener.ServerListPingListener;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SMPCore extends JavaPlugin {
    public static @NotNull SMPCore getInstance() {
        return getPlugin(SMPCore.class);
    }

    public final @NotNull HikariConfig hikariConfig = new HikariConfig();
    private HikariDataSource dbSource;

    public @NotNull HikariDataSource db() {
        return dbSource;
    }

    private @Nullable Configuration config;
    private @Nullable Messages messages;

    public static @NotNull Configuration config() {
        return Objects.requireNonNull(getInstance().config);
    }

    public static @NotNull Messages messages() {
        return Objects.requireNonNull(getInstance().messages);
    }

    private @Nullable Rest rest;

    @Override
    public void onEnable() {
        config = new Configuration();
        messages = new Messages();
        config.saveDefault();
        messages.saveDefault();
        config.load();

        reload();
        initDatabase();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new ServerListPingListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerServerFullCheckListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerPreLoginListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerPostRespawnListener(), this);

        final @NotNull Map<@NotNull String, @NotNull Command> commands = new HashMap<>() {{
            put("smpcore", new MainCommand());
            put("ban", new BanCommand());
            put("unban", new UnbanCommand());
            put("seen", new SeenCommand());
            put("time", new TimeCommand());
            put("nation", new NationCommand());
            put("citizens", new CitizensCommand());
        }};
        commands.put("alts", new AltsCommand(commands.get("smpcore")));
        for (final @NotNull Map.Entry<@NotNull String, @NotNull Command> entry : commands.entrySet())
            Objects.requireNonNull(getServer().getPluginCommand(entry.getKey())).setExecutor(entry.getValue());
    }

    @Override
    public void onDisable() {
        db().close();
        if (rest != null) rest.javalin.stop();
    }

    public void reload() {
        Objects.requireNonNull(config);
        config.reload();
        if (messages != null) messages.reload();
        setupDatabase();
        if (rest != null) rest.javalin.stop();
        rest = new Rest(config.apiPort());
    }

    private void disable() {
        getServer().getPluginManager().disablePlugin(this);
    }

    private void setupDatabase() {
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + getDataFolder().getAbsolutePath() + "/smp.db");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "true");

        dbSource = new HikariDataSource(hikariConfig);
    }

    private void initDatabase() {
        final @NotNull String setup;
        try (final @Nullable InputStream in = getClassLoader().getResourceAsStream("init.sql")) {
            setup = new String(Objects.requireNonNull(in).readAllBytes());
        }
        catch (final @NotNull IOException e) {
            getLogger().log(Level.SEVERE, "db init: could not read db setup file", e);
            disable();
            return;
        }
        final @NotNull String @NotNull [] queries = setup.split(";");
        for (@NotNull String q : queries) {
            final @NotNull String query = q.stripTrailing().stripIndent().replaceAll("^\\s+(?:--.+)*", "");
            if (query.isBlank()) continue;
            try (
                    final @NotNull Connection conn = db().getConnection();
                    final @NotNull PreparedStatement stmt = conn.prepareStatement(query)
            ) {
                stmt.executeUpdate();
            }
            catch (final @NotNull SQLException e) {
                getLogger().log(Level.SEVERE, "db init: could not execute query: " + query, e);
                disable();
                return;
            }
        }
    }

    public static void runAsync(final @NotNull Runnable runnable) {
        getInstance().getServer().getScheduler().runTaskAsynchronously(getInstance(), runnable);
    }

    public static void runMain(final @NotNull Runnable runnable) {
        getInstance().getServer().getScheduler().runTask(getInstance(), runnable);
    }

    public static @NotNull Set<@NotNull Character> getDisallowedCharacters(final @NotNull String source, final @NotNull Pattern pattern) {
        final @NotNull Matcher matcher = pattern.matcher(source);
        final @NotNull Set<@NotNull Character> chars = new HashSet<>();
        while (matcher.find())
            for (char c : matcher.group().toCharArray())
                chars.add(c);
        return chars;
    }

    public static boolean ifDisallowedCharacters(final @NotNull String source, final @NotNull Pattern pattern, final @NotNull Consumer<@NotNull Set<@NotNull Character>> consumer) {
        final @NotNull Set<@NotNull Character> chars = getDisallowedCharacters(source, pattern);
        if (!chars.isEmpty()) {
            consumer.accept(chars);
            return true;
        }
        return false;
    }

    public static @NotNull Component relativeTime(final @NotNull Date date1, final @NotNull Date date2) {
        final long diff = date1.getTime() - date2.getTime();
        final long abs = Math.abs(diff);
        final double seconds = Math.floor(abs / 1000.0);
        final double minutes = Math.floor(seconds / 60.0);
        final double hours = Math.floor(minutes / 60.0);
        final double days = Math.floor(hours / 24.0);
        final double months = Math.floor(days / 30.0);
        final double years = Math.floor(months / 12.0);

        final @NotNull Component t;
        if (years > 0) t = SMPCore.config().relativeTime(years, ChronoUnit.YEARS);
        else if (months > 0) t = SMPCore.config().relativeTime((int) months, ChronoUnit.MONTHS);
        else if (days > 0) t = SMPCore.config().relativeTime((int) days, ChronoUnit.DAYS);
        else if (hours > 0) t = SMPCore.config().relativeTime((int) hours, ChronoUnit.HOURS);
        else if (minutes > 0) t = SMPCore.config().relativeTime((int) minutes, ChronoUnit.MINUTES);
        else t = SMPCore.config().relativeTime((int) seconds, ChronoUnit.SECONDS);

        return diff < 0 ? SMPCore.config().relativeTimePast(t) : SMPCore.config().relativeTimeFuture(t);
    }

    public static @NotNull Component relativeTime(final @NotNull Date date) {
        return relativeTime(date, new Date());
    }

    private static @NotNull World overworld() {
        return Objects.requireNonNull(getInstance().getServer().getWorlds().stream()
                .filter(w -> w.getEnvironment() == World.Environment.NORMAL).findFirst().orElse(null));
    }

    public static @NotNull Date gameTime() {
        return gameTime(overworld().getFullTime());
    }

    public static @NotNull Date gameTime(final long ticks) {
        return new Date(ticks * 3600 + 21600000);
    }
}
