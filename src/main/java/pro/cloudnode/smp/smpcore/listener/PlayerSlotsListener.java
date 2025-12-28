package pro.cloudnode.smp.smpcore.listener;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.event.player.PlayerServerFullCheckEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.smpcore.Member;

import java.util.Objects;
import java.util.Optional;

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
    @EventHandler(priority = EventPriority.HIGHEST)
    public void allowMemberJoinWhenServerFull(final @NotNull PlayerServerFullCheckEvent event) {
        if (event.isAllowed()) return;
        final PlayerProfile profile = event.getPlayerProfile();
        final @NotNull Optional<@NotNull Member> member = Member.get(Objects.requireNonNull(profile.getId()));
        if (member.isEmpty()) return;
        event.allow(true);
    }
}
