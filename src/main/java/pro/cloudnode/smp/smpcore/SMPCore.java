package pro.cloudnode.smp.smpcore;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;

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

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reload();
        initDatabase();
    }

    @Override
    public void onDisable() {
        db().close();
    }

    public static void reload() {
        getInstance().reloadConfig();
        getInstance().config = new Configuration(getInstance().getConfig());
        getInstance().setupDatabase();
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
}
