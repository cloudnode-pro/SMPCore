package pro.cloudnode.smp.smpcore.listener;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.ban.BanListType;
import org.bukkit.BanEntry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.smpcore.SMPCore;

public final class PlayerPreLoginListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void formatBanScreen(final @NotNull AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return;

        @Nullable BanEntry<PlayerProfile> banEntry = SMPCore.getInstance().getServer()
                .getBanList(BanListType.PROFILE).getBanEntry(event.getPlayerProfile());

        if (banEntry == null)
            return;

        SMPCore.messages().banScreen(banEntry)
                .ifPresent(reason -> event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, reason));
    }
}
