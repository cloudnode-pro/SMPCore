package pro.cloudnode.smp.smpcore.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.smpcore.Member;

public final class ServerListPingListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void changeMaxPlayers(final @NotNull ServerListPingEvent event) {
        event.setMaxPlayers(Member.count());
    }
}
