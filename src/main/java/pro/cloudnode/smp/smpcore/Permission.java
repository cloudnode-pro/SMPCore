package pro.cloudnode.smp.smpcore;

import org.jetbrains.annotations.NotNull;

public final class Permission {
    public static @NotNull String RELOAD = "smpcore.reload";
    public static @NotNull String BAN = "smpcore.ban";
    /**
     * See your own alts
     */
    public static @NotNull String ALT = "smpcore.alt";

    /**
     * See someone else's alts
     */
    public static @NotNull String ALT_OTHER = "smpcore.alt.other";

    /**
     * Add an alt
     */
    public static @NotNull String ALT_ADD = "smpcore.alt.add";

    /**
     * Add an alt for someone else
     */
    public static @NotNull String ALT_ADD_OTHER = "smpcore.alt.add.other";

    /**
     * Bypass the maximum alts limit
     */
    public static @NotNull String ALT_MAX_BYPASS = "smpcore.alt.bypass.max";

    /**
     * Remove an alt
     */
    public static @NotNull String ALT_REMOVE = "smpcore.alt.remove";

    /**
     * Remove someone else's alt
     */
    public static @NotNull String ALT_REMOVE_OTHER = "smpcore.alt.remove.other";

    /**
     * Remove an alt that has joined the server
     */
    public static @NotNull String ALT_REMOVE_JOINED = "smpcore.alt.remove.joined";
}
