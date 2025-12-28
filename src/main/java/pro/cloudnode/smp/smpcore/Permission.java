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
     * Leave your nation
     */
    public static @NotNull String NATION_LEAVE = "smpcore.nation.leave";

    /**
     * Add member to your nation
     */
    public static @NotNull String NATION_CITIZEN_ADD = "smpcore.nation.citizens.add";

    /**
     * Add member to any nation
     */
    public static @NotNull String NATION_CITIZEN_ADD_OTHER = "smpcore.nation.citizens.add.other";

    /**
     * Add member to nation even if they are already in another
     */
    public static @NotNull String NATION_CITIZEN_ADD_SWITCH = "smpcore.nation.citizens.add.switch";

    /**
     * Appoint nation citizen as vice-leader of your nation
     */
    public static @NotNull String NATION_PROMOTE = "smpcore.nation.citizens.promote";

    /**
     * Appoint nation citizen as vice-leader of any nation
     */
    public static @NotNull String NATION_PROMOTE_OTHER = "smpcore.nation.citizens.promote.other";

    /**
     * Relieve vice-leader of your nation of their duties
     */
    public static @NotNull String NATION_DEMOTE = "smpcore.nation.citizens.demote";

    /**
     * Relieve vice-leader of any nation of their duties
     */
    public static @NotNull String NATION_DEMOTE_OTHER = "smpcore.nation.citizens.demote.other";

    /**
     * Bypass death ban.
     */
    public static final @NotNull String DEATHBAN_BYPASS = "smpcore.deathban.bypass";
}
