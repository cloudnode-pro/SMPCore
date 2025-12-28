package pro.cloudnode.smp.smpcore.listener;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public final class PlayerPostRespawnListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void enforceDefaultGamemode(final @NotNull PlayerPostRespawnEvent event) {
        final Player player = event.getPlayer();

        if (player.hasPermission("minecraft.command.gamemode"))
            return;

        player.setGameMode(player.getServer().getDefaultGameMode());
    }
}
