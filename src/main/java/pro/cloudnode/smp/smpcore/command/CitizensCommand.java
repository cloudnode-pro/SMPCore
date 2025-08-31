package pro.cloudnode.smp.smpcore.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.smpcore.Member;
import pro.cloudnode.smp.smpcore.Nation;
import pro.cloudnode.smp.smpcore.Permission;
import pro.cloudnode.smp.smpcore.SMPCore;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class CitizensCommand extends Command {
    @Override
    public boolean run(
            final @NotNull CommandSender sender,
            final @NotNull String label,
            @NotNull String @NotNull [] args
    ) {
        if (!sender.hasPermission(Permission.NATION))
            return sendMessage(sender, SMPCore.messages().errorNoPermission());

        final @NotNull Optional<@NotNull Member> member = sender instanceof final @NotNull Player player ? Member.get(
                player) : Optional.empty();

        final @NotNull Optional<@NotNull Nation> nation;
        if (args.length > 0 && args[0].startsWith("id:")) {
            final @NotNull String id = args[0].substring(3);
            nation = Nation.get(id);
            args = Arrays.copyOfRange(args, 1, args.length);
            if (nation.isEmpty())
                return sendMessage(sender, SMPCore.messages().errorNationNotFound(id));
        }
        else
            nation = member.flatMap(Member::nation);

        if (nation.isEmpty())
            return sendMessage(sender, SMPCore.messages().errorNotCitizen());

        if (args.length == 0)
            return NationCommand.citizensSubcommands(member.orElse(null), nation.get(), sender, label);

        return NationCommand.citizens(member.orElse(null), nation.get(), sender, label, args);
    }

    @Override
    public @Nullable List<@NotNull String> tab(
            final @NotNull CommandSender sender,
            final @NotNull String label,
            final @NotNull String @NotNull [] args
    ) {
        return NationCommand.tabComplete(sender, label, Stream.concat(Stream.of("citizens"), Arrays.stream(args)).toArray(String[]::new));
    }
}
