package pro.cloudnode.smp.smpcore.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.json.JsonMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.cloudnode.smp.smpcore.SMPCore;
import pro.cloudnode.smp.smpcore.api.routes.Members;
import pro.cloudnode.smp.smpcore.api.routes.Nations;

import java.lang.reflect.Type;

public class REST {
    private final @NotNull Javalin javalin;

    public REST(final int port) {
        javalin = Javalin.create(config -> {
            config.jsonMapper(new Mapper());
        }).start(port);
    }

    private static void info(final @NotNull Context ctx) {
        final @NotNull JsonObject obj = new JsonObject();
        obj.addProperty("version", SMPCore.getInstance().getPluginMeta().getVersion());
        obj.addProperty("time", SMPCore.gameTime().getTime());
        ctx.json(obj);
    }

    public void stop() {
        javalin.stop();
    }

    private void configureRoutes(final @NotNull JavalinConfig config) {
        config.routes.before(ctx -> {
            final @Nullable String origin = ctx.header("Origin");
            ctx.header("Access-Control-Allow-Origin", origin == null ? "*" : origin);
            ctx.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "*");
            ctx.header("Access-Control-Allow-Credentials", "true");
            ctx.header("Access-Control-Max-Age", "3600");
        });


        config.routes.get("/", REST::info);

        final var members = new Members(this);
        config.routes.get("/members", members::list);
        config.routes.get("/members/{uuid}", members::get);

        final var nations = new Nations(this);
        config.routes.get("/nations", nations::list);
        config.routes.get("/nations/{id}", nations::get);
    }

    public void e404(final @NotNull Context ctx) {
        ctx.status(404);
        final @NotNull JsonObject obj = new JsonObject();
        obj.addProperty("error", "not found");
        ctx.json(obj);
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
