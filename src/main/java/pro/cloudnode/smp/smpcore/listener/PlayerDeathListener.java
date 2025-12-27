package pro.cloudnode.smp.smpcore.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.smpcore.Permission;
import pro.cloudnode.smp.smpcore.SMPCore;
import pro.cloudnode.smp.smpcore.command.BanCommand;

import java.util.Optional;

public final class PlayerDeathListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void banPlayer(final @NotNull PlayerDeathEvent event) {
        if (!SMPCore.config().deathBanEnabled())
            return;

        final @NotNull Player player = event.getPlayer();

        if (player.hasPermission(Permission.DEATHBAN_BYPASS))
            return;

        player.spigot().respawn();
        player.setGameMode(SMPCore.getInstance().getServer().getDefaultGameMode());

        final Component reason = Optional.ofNullable(event.deathMessage())
                .orElse(SMPCore.config().deathBanMessage());

        BanCommand.ban(
                player,
                PlainTextComponentSerializer.plainText().serialize(reason),
                SMPCore.config().deathBanProgression(player.getStatistic(Statistic.DEATHS)),
                null
        );
    }
}
