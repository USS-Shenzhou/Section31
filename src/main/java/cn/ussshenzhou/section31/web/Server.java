package cn.ussshenzhou.section31.web;

import cn.ussshenzhou.section31.backend.MinecraftHelper;
import cn.ussshenzhou.section31.backend.OshiHelper;
import com.google.gson.Gson;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import spark.Route;
import spark.Spark;

import java.util.HashMap;
import java.util.Map;

/**
 * @author USS_Shenzhou
 */
public class Server {
    public static final Gson GSON = new Gson();
    public static final Route REFRESH = (req, res) -> {
        Map<String, Object> data = new HashMap<>();

        data.put("player", MinecraftHelper.getPlayers());
        data.put("mspt", MinecraftHelper.getMspt());
        data.put("cpu", OshiHelper.getCpu());
        data.put("ram", OshiHelper.getRam());
        data.put("in", OshiHelper.getNetRx());
        data.put("out", OshiHelper.getNetTx());

        return GSON.toJson(data);
    };
    public static final Route INIT = (req, res) -> {
        Map<String, Object> data = new HashMap<>();

        data.put("player", MinecraftHelper.getMaxPlayers());
        data.put("mspt", MinecraftHelper.getMaxMspt());
        data.put("cpu", OshiHelper.getCpuMax());
        data.put("ram", OshiHelper.getRamMax());
        data.put("in", OshiHelper.getNetMax());
        data.put("out", OshiHelper.getNetMax());

        return GSON.toJson(data);
    };

    public static void init() {
        Thread serverThread = new Thread(() -> {
            Spark.port(25570);
            if (FMLLoader.isProduction()) {
                Spark.staticFiles.location("/public");
            } else {
                Spark.staticFiles.externalLocation(FMLPaths.GAMEDIR.get().getParent().toString() + "\\src\\main\\resources\\public");
            }
            Spark.get("/api/all", REFRESH);
            Spark.get("/api/init", INIT);

            Spark.get("/", (req, res) -> {
                res.redirect("/section31.html");
                return null;
            });
        }, "Section31 Server");
        serverThread.setDaemon(true);
        serverThread.start();
    }
}
