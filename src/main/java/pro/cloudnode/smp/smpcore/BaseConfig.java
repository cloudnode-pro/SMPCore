package pro.cloudnode.smp.smpcore;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;

public abstract class BaseConfig {
    protected final @NotNull YamlConfiguration config;
    protected final @NotNull String path;

    protected BaseConfig(final @NotNull String path) {
        this.path = path;
        this.config = new YamlConfiguration();
        load();
    }

    protected final void load() {
        if (!file().exists()) saveDefault();
        try {
            this.config.load(file());
        }
        catch (final @NotNull Exception ignored) {
        }

        this.config.addDefaults(YamlConfiguration.loadConfiguration(resource()));
    }

    public final void reload() {
        load();
    }

    private @NotNull File file() {
        return new File(SMPCore.getInstance().getDataFolder(), path);
    }

    public final void saveDefault() {
        if (!file().exists()) SMPCore.getInstance().saveResource(path, false);
    }

    private @NotNull Reader resource() {
        return new BufferedReader(new InputStreamReader(Objects.requireNonNull(SMPCore.getInstance().getResource(path))));
    }
}
