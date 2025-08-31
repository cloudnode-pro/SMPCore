package pro.cloudnode.smp.smpcore.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class TimeCommand extends Command {

    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        return MainCommand.time(sender);
    }

    @Override
    public @NotNull List<@NotNull String> tab(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
