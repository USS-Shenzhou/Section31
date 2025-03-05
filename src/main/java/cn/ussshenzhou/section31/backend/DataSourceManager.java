package cn.ussshenzhou.section31.backend;

import cn.ussshenzhou.section31.backend.provider.MinecraftHelper;
import cn.ussshenzhou.section31.backend.provider.OshiHelper;
import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import spark.Route;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * @author USS_Shenzhou
 */
public class DataSourceManager {
    public static final Gson GSON = new Gson();

    public static String getInitData() {
        Map<String, Object> data = new HashMap<>();
        MetricsManager.getMetrics().values().forEach(list -> list.forEach(metric ->
                data.put(metric.id(), metric.maxProvider().get())
        ));
        return GSON.toJson(data);
    }

    public static String getRefreshData() {
        Map<String, Object> data = new HashMap<>();
        MetricsManager.getMetrics().values().forEach(list -> list.forEach(metric ->
                data.put(metric.id(), metric.provider().get())
        ));
        return GSON.toJson(data);
    }
}
