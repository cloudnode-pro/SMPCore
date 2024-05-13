package pro.cloudnode.smp.smpcore.exception;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public final class MemberNotFoundException extends Exception {
    public final @NotNull OfflinePlayer offlinePlayer;
    public MemberNotFoundException(final @NotNull OfflinePlayer offlinePlayer) {
        super("Member not found: " + offlinePlayer);
        this.offlinePlayer = offlinePlayer;
    }
}
