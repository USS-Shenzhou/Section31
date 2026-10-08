package cn.ussshenzhou.section31.web;

import cn.ussshenzhou.section31.backend.DataSourceHelper;
import cn.ussshenzhou.section31.backend.PageGenerator;
import cn.ussshenzhou.section31.util.Util;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.util.thread.EffectiveSide;
import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * @author USS_Shenzhou
 */
public class SparkServer {

    public static void init() {
        Thread serverThread = new Thread(() -> {
            try {
                Spark.port(FMLEnvironment.getDist() == Dist.DEDICATED_SERVER ? 25570 : 25571);

                if (FMLEnvironment.isProduction()) {
                    Spark.staticFiles.location("public");
                } else {
                    Spark.staticFiles.externalLocation(FMLPaths.GAMEDIR.get().getParent().toString() + "\\src\\main\\resources\\public");
                }
                Spark.get("/api/all", (req, res) -> {
                    enableGzip(req, res);
                    return DataSourceHelper.getRefreshData();
                });
                Spark.get("/api/init", (req, res) -> {
                    enableGzip(req, res);
                    return DataSourceHelper.getInitData();
                });

                Spark.get("/", (req, res) -> {
                    res.redirect("/section31");
                    return null;
                });
                Spark.get("/section31", (req, res) -> {
                    res.type("text/html");
                    enableGzip(req, res);
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

    /**
     * Spark only compresses a route response when it carries "Content-Encoding: gzip",
     * but keeps that header even if the client does not accept gzip. So it must only be set for clients accepting gzip.
     */
    private static void enableGzip(Request request, Response response) {
        var acceptEncodings = request.raw().getHeaders("Accept-Encoding");
        while (acceptEncodings.hasMoreElements()) {
            if (acceptEncodings.nextElement().contains("gzip")) {
                response.header("Content-Encoding", "gzip");
                return;
            }
        }
    }
}
