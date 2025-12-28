package pro.cloudnode.smp.smpcore.listener;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.event.player.PlayerServerFullCheckEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import pro.cloudnode.smp.smpcore.Member;

import java.util.Objects;
import java.util.Optional;

public final class PlayerServerFullCheckListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void allowMemberJoinWhenServerFull(final @NotNull PlayerServerFullCheckEvent event) {
        if (event.isAllowed()) return;
        final PlayerProfile profile = event.getPlayerProfile();
        final @NotNull Optional<@NotNull Member> member = Member.get(Objects.requireNonNull(profile.getId()));
        if (member.isEmpty()) return;
        event.allow(true);
    }
}
