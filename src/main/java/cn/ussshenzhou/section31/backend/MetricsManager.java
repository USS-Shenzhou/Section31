package cn.ussshenzhou.section31.backend;

import cn.ussshenzhou.section31.Section31;
import cn.ussshenzhou.section31.backend.provider.JvmHelper;
import cn.ussshenzhou.section31.backend.provider.MinecraftHelper;
import cn.ussshenzhou.section31.backend.provider.OshiHelper;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author USS_Shenzhou
 * <p>
 * {@code group} Group name.
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
 * {@code format} int/percent/float/net/byte
 * <p>
 * {@code sourceClass} Full class name of your provider.
 * <p>
 * {@code sourceMethod} Static method name of your provider.
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class MetricsManager {
    private static final LinkedHashMap<String, ArrayList<Metric>> METRICS = new LinkedHashMap<>() {{
        put("Minecraft in-game", Lists.newArrayList(
                new Metric("player", "Players", "", "", "int", MinecraftHelper::getPlayers, MinecraftHelper::getMaxPlayers),
                new Metric("mspt", "MSPT", "Millisecond per Tick", "", "float", MinecraftHelper::getMspt, MinecraftHelper::getMaxMspt)
        ));
        var jvmList = JvmHelper.getMemoryPoolMetrics();
        put("Java Virtual Machine", jvmList);
        put("System/Hardware", Lists.newArrayList(
                new Metric("cpu", "CPU", "CPU usage", "", "percent", OshiHelper::getCpu, OshiHelper::getCpuMax),
                new Metric("ram", "RAM", "", " GiB", "float", OshiHelper::getRam, OshiHelper::getRamMax),
                new Metric("in", "Network Rx", "Inbound", " ", "net", OshiHelper::getNetRx, OshiHelper::getNetMax),
                new Metric("out", "Network Tx", "Outbound", " ", "net", OshiHelper::getNetTx, OshiHelper::getNetMax)
        ));
    }};

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
                        synchronized (METRICS) {
                            METRICS.computeIfAbsent(getOrDefault(dataMap, "group", message.modId()), k -> new ArrayList<>())
                                    .add(new Metric(
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
                                            }
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

    public static Map<String, ArrayList<Metric>> getMetrics() {
        return METRICS;
    }
}
