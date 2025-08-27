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
     * List the citizens of your nation
     */
    public static @NotNull String NATION_CITIZENS_LIST = "smpcore.nation.citizens.list";

    /**
     * List the member of any nation
     */
    public static @NotNull String NATION_CITIZENS_LIST_OTHER = "smpcore.nation.citizens.list.other";

    /**
     * Kick citizens of your nation
     */
    public static @NotNull String NATION_CITIZENS_KICK = "smpcore.nation.citizens.kick";

    /**
     * Kick citizens of any nation
     */
    public static @NotNull String NATION_CITIZENS_KICK_OTHER = "smpcore.nation.citizens.kick.other";

    /**
     * Request to join a nation
     */
    public static @NotNull String NATION_JOIN_REQUEST = "smpcore.nation.join.request";

    /**
     * Request to join a nation while already in a nation
     */
    public static @NotNull String NATION_JOIN_REQUEST_SWITCH = "smpcore.nation.join.request.switch";

    /**
     * Join a nation without requesting
     */
    public static @NotNull String NATION_JOIN_FORCE = "smpcore.nation.join.force";

    /**
     * Accept invitation to join nation
     */
    public static @NotNull String NATION_INVITE_ACCEPT = "smpcore.nation.invite.accept";

    /**
     * Accept invitation to join nation while already in a nation
     */
    public static @NotNull String NATION_INVITE_ACCEPT_SWITCH = "smpcore.nation.invite.accept.switch";

    /**
     * Send invitation to join your nation
     */
    public static @NotNull String NATION_INVITE = "smpcore.nation.invite";

    /**
     * Send invitation to join any nation
     */
    public static @NotNull String NATION_INVITE_OTHER = "smpcore.nation.invite.other";

    /**
     * Accept request to join your nation
     */
    public static @NotNull String NATION_JOIN_REQUEST_ACCEPT = "smpcore.nation.join.request.accept";

    /**
     * Appoint nation citizen as vice-leader
     */
    public static @NotNull String NATION_VICE_PROMOTE = "smpcore.nation.vice-promote";

    /**
     * Relieve vice-leader of duties
     */
    public static @NotNull String NATION_VICE_DEMOTE = "smpcore.nation.vice-demote";
}
