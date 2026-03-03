package pro.cloudnode.smp.smpcore.api.routes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.smpcore.Member;
import pro.cloudnode.smp.smpcore.Nation;
import pro.cloudnode.smp.smpcore.SMPCore;
import pro.cloudnode.smp.smpcore.api.REST;

import java.util.Optional;
import java.util.Set;

public final class Nations {
    private final @NotNull REST rest;

    public Nations(final @NotNull REST rest) {
        this.rest = rest;
    }

    public static @NotNull JsonObject map(final @NotNull Nation nation) {
        final @NotNull JsonObject obj = new JsonObject();
        obj.addProperty("id", nation.id);
        obj.addProperty("name", nation.name);
        obj.addProperty("shortName", nation.shortName);
        obj.addProperty("color", nation.color);
        obj.addProperty("leader", nation.leaderUUID.toString());
        obj.addProperty("viceLeader", nation.viceLeaderUUID.toString());
        obj.addProperty("members", nation.citizens().size());
        obj.addProperty("founded", nation.founded.getTime());
        obj.addProperty("foundedGameTicks", nation.foundedTicks);
        obj.addProperty("foundedGameDate", SMPCore.gameTime(nation.foundedTicks).getTime());
        obj.addProperty("bank", nation.bank);
        return obj;
    }

    public void list(final @NotNull Context ctx) {
        final @NotNull Set<@NotNull Nation> nations = Nation.get();
        final @NotNull JsonArray arr = new JsonArray();
        for (final @NotNull Nation nation : nations)
            arr.add(map(nation));
        ctx.json(arr);
    }

    public void get(final @NotNull Context ctx) {
        final @Nullable String include = ctx.queryParam("include");

        final @NotNull Optional<@NotNull Nation> nation = Nation.get(ctx.pathParam("id"));
        if (nation.isEmpty()) {
            rest.e404(ctx);
            return;
        }
        final @NotNull JsonObject obj = map(nation.get());

        if (include != null) {
            switch (include) {
                case "members" -> {
                    final @NotNull JsonArray arr = new JsonArray();
                    final @NotNull Set<@NotNull Member> citizens = nation.get().citizens();
                    for (final @NotNull Member member : citizens)
                        arr.add(Members.map(member));
                    obj.add("members", arr);
                }
            }
        }

        ctx.json(obj);
    }
}
