package pro.cloudnode.smp.smpcore.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.smpcore.SMPCore;

import java.util.Arrays;
import java.util.List;

public abstract class Command implements TabCompleter, CommandExecutor {
    @SuppressWarnings("SameReturnValue")
    public static boolean sendMessage(final @NotNull Audience recipient, final @NotNull Component message) {
        recipient.sendMessage(message);
        return true;
    }

    public abstract boolean run(final @NotNull CommandSender sender, final @NotNull String label, final @NotNull String @NotNull [] args);
    public abstract @Nullable List<@NotNull String> tab(final @NotNull CommandSender sender, final @NotNull String label, final @NotNull String @NotNull [] args);

    @Override
    public final boolean onCommand(final @NotNull CommandSender sender, final @NotNull org.bukkit.command.Command command, final @NotNull String label, @NotNull String @NotNull [] args) {
        SMPCore.runAsync(() -> {
            final boolean ignored = run(sender, label, args);
        });
        return true;
    }

    @Override
    public final @Nullable List<@NotNull String> onTabComplete(final @NotNull CommandSender sender, final @NotNull org.bukkit.command.Command command, final @NotNull String label, final @NotNull String @NotNull [] args) {
        final @Nullable List<@NotNull String> tab = tab(sender, label, args);
        if (args.length > 0 && tab != null) return tab.stream().filter(e -> e.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).toList();
        else return tab;
    }

    /**
     * Check for the presence of the permissions (using OR).
     *
     * @param permissible Permissible to check
     * @param permissions Permissions to check
     */
    public static boolean hasAnyPermission(final @NotNull Permissible permissible, final @NotNull String @NotNull ... permissions) {
        return Arrays.stream(permissions).anyMatch(permissible::hasPermission);
    }
}
