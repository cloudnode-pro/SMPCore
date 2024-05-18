package pro.cloudnode.smp.smpcore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class Rest {
    final @NotNull Javalin javalin = Javalin.create(config -> {
        config.jsonMapper(new Mapper());
    });

    private void e404 (final @NotNull io.javalin.http.Context ctx) {
        ctx.status(404);
        final @NotNull JsonObject obj = new JsonObject();
        obj.addProperty("error", "not found");
        ctx.json(obj);
    }

    private @NotNull JsonObject getMemberObject(final @NotNull Member member) {
        final @NotNull JsonObject obj = new JsonObject();
        final @NotNull OfflinePlayer player = member.player();
        obj.addProperty("uuid", member.uuid.toString());
        obj.addProperty("name", player.getName());
        obj.addProperty("nation", member.nationID);
        obj.addProperty("staff", member.staff);
        obj.addProperty("online", player.isOnline());
        obj.addProperty("whitelisted", player.isWhitelisted());
        obj.addProperty("banned", player.isBanned());
        obj.addProperty("altOwner", member.altOwnerUUID == null ? null : member.altOwnerUUID.toString());
        obj.addProperty("added", member.added.getTime());
        obj.addProperty("lastSeen", player.getLastSeen());
        obj.addProperty("firstSeen", player.getFirstPlayed());
        obj.addProperty("active", member.isActive());
        return obj;
    }

    private @NotNull JsonObject getNationObject(final @NotNull Nation nation) {
        final @NotNull JsonObject obj = new JsonObject();
        obj.addProperty("id", nation.id);
        obj.addProperty("name", nation.name);
        obj.addProperty("shortName", nation.shortName);
        obj.addProperty("color", nation.color);
        obj.addProperty("leader", nation.leaderUUID.toString());
        obj.addProperty("viceLeader", nation.viceLeaderUUID.toString());
        obj.addProperty("members", nation.members().size());
        obj.addProperty("founded", nation.founded.getTime());
        obj.addProperty("bank", nation.bank);
        return obj;
    }

    public Rest(final int port) {
        javalin.before(ctx -> {
           final @Nullable String origin = ctx.header("Origin");
           ctx.header("Access-Control-Allow-Origin", origin == null ? "*" : origin);
           ctx.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
           ctx.header("Access-Control-Allow-Headers", "*");
           ctx.header("Access-Control-Allow-Credentials", "true");
           ctx.header("Access-Control-Max-Age", "3600");
        });

        javalin.get("/members", ctx -> {
            final @Nullable String filter = ctx.queryParam("filter");
            final @Nullable String limitString = ctx.queryParam("limit");
            final @Nullable String pageString = ctx.queryParam("page");
            final @Nullable String include = ctx.queryParam("include");

            final @Nullable Integer limit;
            if (limitString == null) limit = null;
            else {
                @Nullable Integer t = null;
                try {
                    t = Integer.parseInt(limitString);
                }
                catch (final @NotNull NumberFormatException ignored) {}
                limit = t;
            }

            final int page;
            if (pageString == null) page = 1;
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

            final @NotNull HashSet<@NotNull Member> members = limit == null ? Member.get() : Member.get(limit, page);
            final @NotNull JsonArray arr = new JsonArray();
            for (final @NotNull Member member : members) {
                if (filter != null) {
                    if (filter.equals("online") && !member.player().isOnline()) continue;
                    if (filter.equals("offline") && member.player().isOnline()) continue;
                    if (filter.equals("banned") && !member.player().isBanned()) continue;
                }
                final @NotNull JsonObject m = getMemberObject(member);
                if (include != null) {
                    switch (include) {
                        case "nation" -> {
                            final @NotNull Optional<@NotNull Nation> optionalNation = member.nation();
                            if (optionalNation.isEmpty()) m.add("nation", null);
                            else m.add("nation", getNationObject(optionalNation.get()));
                        }
                    }
                }
                arr.add(m);
            }
            ctx.json(arr);
        });

        javalin.get("/members/{uuid}", ctx -> {
            final @NotNull UUID uuid;
            try {
                uuid = UUID.fromString(ctx.pathParam("uuid"));
            }
            catch (final @NotNull IllegalArgumentException e) {
                e404(ctx);
                return;
            }
            final @NotNull OfflinePlayer offlinePlayer = SMPCore.getInstance().getServer()
                    .getOfflinePlayer(uuid);
            final @NotNull Optional<@NotNull Member> member = Member.get(offlinePlayer);
            if (member.isEmpty()) {
                e404(ctx);
                return;
            }
            final @NotNull HashSet<@NotNull Member> alts = member.get().getAlts();
            final @NotNull JsonObject obj = getMemberObject(member.get());
            final @NotNull JsonArray altsArray = new JsonArray();
            for (final @NotNull Member alt : alts) {
                final @NotNull JsonObject altObj = new JsonObject();
                final @NotNull OfflinePlayer player = alt.player();
                altObj.addProperty("uuid", alt.uuid.toString());
                altObj.addProperty("name", player.getName());
                altObj.addProperty("nation", alt.nationID);
                altObj.addProperty("added", alt.added.getTime());
                altObj.addProperty("lastSeen", player.getLastSeen());
                altsArray.add(altObj);
            }
            obj.add("alts", altsArray);
            ctx.json(obj);
        });

        javalin.get("/nations", ctx -> {
            final @NotNull HashSet<@NotNull Nation> nations = Nation.get();
            final @NotNull JsonArray arr = new JsonArray();
            for (final @NotNull Nation nation : nations)
                arr.add(getNationObject(nation));
            ctx.json(arr);
        });

        javalin.get("/nations/{id}", ctx -> {
            final @Nullable String include = ctx.queryParam("include");

            final @NotNull Optional<@NotNull Nation> nation = Nation.get(ctx.pathParam("id"));
            if (nation.isEmpty()) {
                e404(ctx);
                return;
            }
            final @NotNull JsonObject obj = getNationObject(nation.get());

            if (include != null) {
                switch (include) {
                    case "members" -> {
                        final @NotNull JsonArray arr = new JsonArray();
                        final @NotNull HashSet<@NotNull Member> members = nation.get().members();
                        for (final @NotNull Member member : members)
                            arr.add(getMemberObject(member));
                        obj.add("members", arr);
                    }
                }
            }

            ctx.json(obj);
        });

        javalin.start(port);
    }

    public static final class Mapper implements JsonMapper {
        private final @NotNull Gson gson = new GsonBuilder().serializeNulls().create();

        @Override
        public @NotNull String toJsonString(final @NotNull Object obj, final @NotNull Type type) {
            return gson.toJson(obj, type);
        }

        @Override
        public <T> @NotNull T fromJsonString(final @NotNull String json, final @NotNull Type targetType) {
            return gson.fromJson(json, targetType);
        }
    }
}
