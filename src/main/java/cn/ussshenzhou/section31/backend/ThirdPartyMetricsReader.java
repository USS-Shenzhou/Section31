package cn.ussshenzhou.section31.backend;

import cn.ussshenzhou.section31.Section31;
import cn.ussshenzhou.section31.backend.metric.BasicMetric;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber
public class ThirdPartyMetricsReader {
    @SubscribeEvent
    public static void receiveAllThirdPartyMetrics(FMLLoadCompleteEvent event) {
        InterModComms.getMessages(Section31.MODID)
                .forEach(message -> {
                    var supplied = message.messageSupplier().get();
                    if (supplied instanceof Map<?, ?> dataMap
                            && dataMap.containsKey("id")
                            && dataMap.containsKey("preferredMaxClass")
                            && dataMap.containsKey("preferredMaxMethod")
                            && dataMap.containsKey("sourceClass")
                            && dataMap.containsKey("sourceMethod")
                    ) {
                        Method maxProvider;
                        Method provider;
                        try {
                            var clazz = Class.forName((String) dataMap.get("preferredMaxClass"));
                            maxProvider = clazz.getDeclaredMethod((String) dataMap.get("preferredMaxMethod"));
                            maxProvider.setAccessible(true);
                            clazz = Class.forName((String) dataMap.get("sourceClass"));
                            provider = clazz.getDeclaredMethod((String) dataMap.get("sourceMethod"));
                            provider.setAccessible(true);
                        } catch (ClassNotFoundException | NoSuchMethodException e) {
                            LogUtils.getLogger().error("Failed to load metric source {} # {} of {}.",
                                    dataMap.get("sourceClass"),
                                    dataMap.get("sourceMethod"),
                                    message.modId()
                            );
                            LogUtils.getLogger().error(e.getMessage());
                            return;
                        }
                        synchronized (MetricsManager.METRICS) {
                            MetricsManager.METRICS.computeIfAbsent(getOrDefault(dataMap, "group", message.modId()), k -> new ArrayList<>())
                                    .add(new BasicMetric(
                                            (String) dataMap.get("id"),
                                            getOrDefault(dataMap, "name", (String) dataMap.get("id")),
                                            getOrDefault(dataMap, "desc", ""),
                                            getOrDefault(dataMap, "maxDesc", ""),
                                            getOrDefault(dataMap, "format", "float"),
                                            () -> {
                                                try {
                                                    return maxProvider.invoke(null);
                                                } catch (IllegalAccessException | InvocationTargetException e) {
                                                    LogUtils.getLogger().error(e.getMessage());
                                                    throw new RuntimeException(e);
                                                }
                                            },
                                            () -> {
                                                try {
                                                    return provider.invoke(null);
                                                } catch (IllegalAccessException | InvocationTargetException e) {
                                                    LogUtils.getLogger().error(e.getMessage());
                                                    throw new RuntimeException(e);
                                                }
                                            },
                                            Integer.parseInt(getOrDefault(dataMap, "important", "true"))
                                    ));
                        }
                    }
                });
    }

    private static String getOrDefault(Map<?, ?> map, Object key, String defaultValue) {
        if (map.containsKey(key)) {
            return (String) map.get(key);
        }
        return defaultValue;
    }
}
