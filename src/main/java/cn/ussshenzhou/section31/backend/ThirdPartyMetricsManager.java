package cn.ussshenzhou.section31.backend;

import cn.ussshenzhou.section31.Section31;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author USS_Shenzhou
 * <p>
 * {@code id} ID for identification. Should be unique, like modid_name.
 * <p>
 * {@code name} Name to display.
 * <p>
 * {@code desc} Description for this metric.
 * <p>
 * {@code maxDesc} Units for max-value display. Should be like " GB".
 * <p>
 * {@code preferredMaxClass} Full class name of your (Preferred) max value provider.
 * <p>
 * {@code preferredMaxMethod} Static method name of your (Preferred) max value provider.
 * <p>
 * {@code format} int/percent/float/net
 * <p>
 * {@code sourceClass} Full class name of your provider.
 * <p>
 * {@code sourceMethod} Static method name of your provider.
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ThirdPartyMetricsManager {
    public static volatile boolean READY = false;
    private static final ConcurrentHashMap<String, ArrayList<ThirdPartyMetrics>> THIRD_PARTY_METRICS = new ConcurrentHashMap<>();

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
                        THIRD_PARTY_METRICS.computeIfAbsent(message.modId(), k -> new ArrayList<>())
                                .add(new ThirdPartyMetrics(
                                        (String) dataMap.get("id"),
                                        getOrDefault(dataMap, "name", (String) dataMap.get("id")),
                                        getOrDefault(dataMap, "desc", ""),
                                        getOrDefault(dataMap, "maxDesc", ""),
                                        getOrDefault(dataMap, "format", "float"),
                                        maxProvider,
                                        provider
                                ));
                    }
                });
        READY = true;
    }

    private static String getOrDefault(Map<?, ?> map, Object key, String defaultValue) {
        if (map.containsKey(key)) {
            return (String) map.get(key);
        }
        return defaultValue;
    }

    public static ConcurrentHashMap<String, ArrayList<ThirdPartyMetrics>> getThirdPartyMetrics(){
        if(!READY){
            LogUtils.getLogger().error("Section 31 has not been initialized yet.");
        }
        return THIRD_PARTY_METRICS;
    }


    public record ThirdPartyMetrics(String id,
                                    String name,
                                    String desc,
                                    String maxDesc,
                                    String format,
                                    Method maxProvider,
                                    Method provider) {
    }
}
