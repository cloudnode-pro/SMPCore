package pro.cloudnode.smp.smpcore.api.routes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.http.Context;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.smpcore.CachedProfile;
import pro.cloudnode.smp.smpcore.Member;
import pro.cloudnode.smp.smpcore.Nation;
import pro.cloudnode.smp.smpcore.SMPCore;
import pro.cloudnode.smp.smpcore.api.REST;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class Members {
    private final @NotNull REST rest;

    public Members(final @NotNull REST rest) {
        this.rest = rest;
    }

    public static @NotNull JsonObject map(final @NotNull Member member) {
        final @NotNull JsonObject obj = new JsonObject();
        final @NotNull OfflinePlayer player = member.player();
        obj.addProperty("uuid", member.uuid.toString());
        obj.addProperty("name", CachedProfile.getName(player));
        obj.addProperty("nation", member.nationID);
        obj.addProperty("staff", member.staff);
        obj.addProperty("online", !member.staff && player.isOnline());
        obj.addProperty("whitelisted", player.isWhitelisted());
        obj.addProperty("banned", player.isBanned());
        obj.addProperty("altOwner", member.altOwnerUUID == null ? null : member.altOwnerUUID.toString());
        obj.addProperty("added", member.added.getTime());
        obj.addProperty("lastSeen", member.staff ? 0 : player.getLastSeen());
        obj.addProperty("firstSeen", player.getFirstPlayed());
        obj.addProperty("active", member.isActive());
        return obj;
    }

    public void list(final @NotNull Context ctx) {
        final @Nullable String filter = ctx.queryParam("filter");
        final @Nullable String limitString = ctx.queryParam("limit");
        final @Nullable String pageString = ctx.queryParam("page");
        final @Nullable String include = ctx.queryParam("include");

        final @Nullable Integer limit;
        if (limitString == null)
            limit = null;
        else {
            @Nullable Integer t = null;
            try {
                t = Integer.parseInt(limitString);
            }
            catch (final @NotNull NumberFormatException ignored) {}
            limit = t;
        }

        final int page;
        if (pageString == null)
            page = 1;
        else {
            int t;
            try {
                t = Integer.parseInt(pageString);
            }
            catch (final @NotNull NumberFormatException ignored) {
                t = 1;
            }
            page = t;
        }

        final @NotNull Set<@NotNull Member> members = limit == null ? Member.get() : Member.get(limit, page);
        final @NotNull JsonArray arr = new JsonArray();
        for (final @NotNull Member member : members) {
            if (filter != null)
                switch (filter) {
                    case "online":
                        if (member.staff || !member.player().isOnline())
                            continue;
                    case "offline":
                        if (!member.staff && member.player().isOnline())
                            continue;
                    case "banned":
                        if (!member.player().isBanned())
                            continue;
                }
            final @NotNull JsonObject m = map(member);
            if (include != null) {
                switch (include) {
                    case "nation" -> {
                        final @NotNull Optional<@NotNull Nation> optionalNation = member.nation();
                        if (optionalNation.isEmpty())
                            m.add("nation", null);
                        else
                            m.add("nation", Nations.map(optionalNation.get()));
                    }
                }
            }
            arr.add(m);
        }
        ctx.json(arr);
    }

    public void get(final @NotNull Context ctx) {
        final @NotNull UUID uuid;
        try {
            uuid = UUID.fromString(ctx.pathParam("uuid"));
        }
        catch (final @NotNull IllegalArgumentException e) {
            rest.e404(ctx);
            return;
        }
        final @NotNull OfflinePlayer offlinePlayer = SMPCore.getInstance().getServer().getOfflinePlayer(uuid);
        final @NotNull Optional<@NotNull Member> member = Member.get(offlinePlayer);
        if (member.isEmpty()) {
            rest.e404(ctx);
            return;
        }
        final @NotNull Set<@NotNull Member> alts = member.get().getAlts();
        final @NotNull JsonObject obj = map(member.get());
        final @NotNull JsonArray altsArray = new JsonArray();
        for (final @NotNull Member alt : alts) {
            altsArray.add(map(alt));
        }
        obj.add("alts", altsArray);
        ctx.json(obj);
    }
}
