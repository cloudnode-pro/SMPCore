package pro.cloudnode.smp.smpcore.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.smpcore.Member;

public final class PlayerSlotsListener implements Listener {
    /**
     * Change the max players number in server list ping
     */
    @EventHandler
    public void onServerListPing(final @NotNull ServerListPingEvent event) {
        event.setMaxPlayers(Member.count());
    }

    /**
     * If the player is a member, but the server thinks it's full, allow them to join
     */
    @EventHandler
    public void onPlayerLogin(final @NotNull PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL)
            event.allow();
    }
}
