package pro.cloudnode.smp.smpcore;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public final class Configuration {
    private final @NotNull FileConfiguration config;

    public Configuration(final @NotNull FileConfiguration config) {
        this.config = config;
    }
}
