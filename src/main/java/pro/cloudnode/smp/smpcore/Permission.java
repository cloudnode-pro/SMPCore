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

    public static @NotNull String SEEN = "smpcore.seen";

    /**
     * Allow using `/seen` on staff
     */
    public static @NotNull String SEEN_STAFF = "smpcore.seen.staff";

    /**
     * Allow seeing the game time and date
     */
    public static @NotNull String TIME = "smpcore.time";

    /**
     * Access to the {@code /nation} command
     */
    public static @NotNull String NATION = "smpcore.nation";

    /**
     * List the members of your nation
     */
    public static @NotNull String NATION_MEMBERS_LIST = "smpcore.nation.members.list";

    /**
     * List the member of any nation
     */
    public static @NotNull String NATION_MEMBERS_LIST_OTHER = "smpcore.nation.members.list.other";

    /**
     * Kick members of your nation
     */
    public static @NotNull String NATION_MEMBERS_KICK = "smpcore.nation.members.kick";

    /**
     * Kick members of any nation
     */
    public static @NotNull String NATION_MEMBERS_KICK_OTHER = "smpcore.nation.members.kick.other";

    /**
     * Appoint nation citizen as vice-leader
     */
    public static @NotNull String NATION_VICE_PROMOTE = "smpcore.nation.vice-promote";

    /**
     * Relieve vice-leader of duties
     */
    public static @NotNull String NATION_VICE_DEMOTE = "smpcore.nation.vice-demote";
}
