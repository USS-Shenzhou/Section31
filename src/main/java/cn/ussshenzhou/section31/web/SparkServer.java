package cn.ussshenzhou.section31.web;

import cn.ussshenzhou.section31.backend.DataSourceManager;
import cn.ussshenzhou.section31.backend.PageGenerator;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import spark.Spark;

/**
 * @author USS_Shenzhou
 */
public class SparkServer {

    public static void init() {
        Thread serverThread = new Thread(() -> {
            try {
                Spark.port(FMLEnvironment.dist.isDedicatedServer() ? 25570 : 25571);

                if (FMLLoader.isProduction()) {
                    Spark.staticFiles.location("/public");
                } else {
                    Spark.staticFiles.externalLocation(FMLPaths.GAMEDIR.get().getParent().toString() + "\\src\\main\\resources\\public");
                }
                Spark.get("/api/all", (req, res) -> DataSourceManager.getRefreshData());
                Spark.get("/api/init", (req, res) -> DataSourceManager.getInitData());

                Spark.get("/", (req, res) -> {
                    res.redirect("/section31");
                    return null;
                });
                Spark.get("/section31", (req, res) -> {
                    res.type("text/html");
                    return PageGenerator.getPage();
                });
                Spark.init();
            } catch (Exception e) {
                LogUtils.getLogger().error(e.getMessage());
            }

        }, "Section31 Server");
        serverThread.setDaemon(true);
        serverThread.start();
    }
}
