package pro.cloudnode.smp.smpcore;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class BaseConfig {
    protected @NotNull YamlConfiguration config;
    protected final @NotNull String path;

    protected BaseConfig(final @NotNull String path) {
        this.path = path;
        this.config = load();
    }

    protected final @NotNull YamlConfiguration load() {
        if (!file().exists()) saveDefault();
        return YamlConfiguration.loadConfiguration(file());
    }

    public final void reload() {
        config = load();
    }

    private @NotNull File file() {
        return new File(SMPCore.getInstance().getDataFolder(), path);
    }

    public final void saveDefault() {
        if (!file().exists())
            SMPCore.getInstance().saveResource(path, false);
    }
}
