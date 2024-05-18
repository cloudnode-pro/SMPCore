package pro.cloudnode.smp.smpcore.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public final class AltsCommand extends Command {
    private final @NotNull Command mainCommand;
    public AltsCommand(final @NotNull Command mainCommand) {
        super();
        this.mainCommand = mainCommand;
    }

    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        return MainCommand.alt(sender, args, label);
    }

    @Override
    public @Nullable List<@NotNull String> tab(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        return mainCommand.tab(sender, label, Stream.concat(Stream.of("alt"), Stream.of(args)).toArray(String[]::new));
    }
}
