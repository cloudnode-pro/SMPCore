package pro.cloudnode.smp.smpcore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class Configuration extends BaseConfig {
    public Configuration() {
        super("config.yml");
    }

    /**
     * REST API HTTP server port
     */
    public int apiPort() {
        return config.getInt("api.port");
    }

    /**
     * Number of days since last seen after which player is considered inactive
     */
    public int membersInactiveDays() {
        return config.getInt("members.inactive-days");
    }

    /**
     * Maximum number of alts you can have
     */
    public int altsMax() {
        return config.getInt("alts.max");
    }

    public @NotNull Component relativeTime(final int t, final @NotNull ChronoUnit unit) {
        final @NotNull String formatString = Objects.requireNonNull(config.getString("relative-time." + switch (unit) {
            case SECONDS -> "seconds";
            case MINUTES -> "minutes";
            case HOURS -> "hours";
            case DAYS -> "days";
            case MONTHS -> "months";
            case YEARS -> "years";
            default -> {
                throw new IllegalStateException("No relative time format for ChronoUnit " + unit);
            }
        }));
        return MiniMessage.miniMessage()
                .deserialize(formatString, Formatter.number("t", t), Formatter.choice("format", Math.abs(t)));
    }

    public @NotNull Component relativeTimeFuture(final @NotNull Component relativeTime) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("relative-time.future")), Placeholder.component("t", relativeTime));
    }

    public @NotNull Component relativeTimePast(final @NotNull Component relativeTime) {
        return MiniMessage.miniMessage()
                .deserialize(Objects.requireNonNull(config.getString("relative-time.past")), Placeholder.component("t", relativeTime));
    }
}
