package cn.ussshenzhou.section31.backend;

import cn.ussshenzhou.section31.backend.metric.BasicMetric;
import cn.ussshenzhou.section31.backend.metric.Tab;
import cn.ussshenzhou.section31.backend.metric.StackedMetric;
import cn.ussshenzhou.section31.provider.JvmProvider;
import cn.ussshenzhou.section31.provider.MinecraftClientHelper;
import cn.ussshenzhou.section31.provider.MinecraftServerHelper;
import cn.ussshenzhou.section31.provider.OshiHelper;
import cn.ussshenzhou.section31.provider.NetworkProvider;
import com.google.common.collect.Lists;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static cn.ussshenzhou.section31.util.Util.NO_MAX;

/**
 * @author USS_Shenzhou
 * <p>
 * {@code tab} Tab name. Groups with the same tab name are shown in the same tab, e.g. "JVM".
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
 * <p>
 * <s>{@code important} If true, metric will take 100% width. Otherwise, it will take 50%.</s>
 * <p>
 * {@code importance} metric will take 1/importance with.
 */
public class MetricsManager {
    private static final String ICON_MINECRAFT = """
            <path d="M12 3L20 7.5V16.5L12 21L4 16.5V7.5ZM4 7.5L12 12L20 7.5M12 12V21"/>""";
    private static final String ICON_JVM = """
            <path d="M5 10H16V15A4 4 0 0 1 12 19H9A4 4 0 0 1 5 15ZM16 11H18A2 2 0 0 1 18 15H16M8 4V7M10.5 4V7M13 4V7"/>""";
    private static final String ICON_SYSTEM = """
            <path d="M7 7H17V17H7ZM10 10H14V14H10ZM10 3V7M14 3V7M10 17V21M14 17V21M3 10H7M3 14H7M17 10H21M17 14H21"/>""";

    protected static final LinkedHashMap<String, Tab> METRICS = new LinkedHashMap<>() {{
        var minecraft = new Tab("Minecraft", ICON_MINECRAFT);
        if (FMLEnvironment.getDist() == Dist.DEDICATED_SERVER) {
            minecraft.groups().put("Minecraft Server", Lists.newArrayList(
                    new BasicMetric("player", "Server Players", "", "", "int", MinecraftServerHelper::getPlayers, MinecraftServerHelper::getMaxPlayers, 1),
                    new BasicMetric("mspt", "Server MSPT", "Millisecond per Tick", "", "float", MinecraftServerHelper::getMspt, MinecraftServerHelper::getMaxMspt, 1),

                    new StackedMetric("outbound_size", "Tx Packet Flow", "Outbound packet and size", " ", "net", NetworkProvider.OUTBOUND_PACKET_SIZE::read, NO_MAX, -2),
                    new StackedMetric("inbound_size", "Rx Packet Flow", "Inbound packet and size", " ", "net", NetworkProvider.INBOUND_PACKET_SIZE::read, NO_MAX, -2),
                    new StackedMetric("outbound_count", "Tx Packet Count", "Outbound packet count", " ", "int", NetworkProvider.OUTBOUND_PACKET_COUNT::read, NO_MAX, 2),
                    new StackedMetric("inbound_count", "Rx Packet Count", "Inbound packet count", " ", "int", NetworkProvider.INBOUND_PACKET_COUNT::read, NO_MAX, 2),

                    new StackedMetric("visible_chunks", "Chunks Visible", "", "", "int", MinecraftServerHelper::getVisible, NO_MAX, 2),
                    new StackedMetric("updating_chunks", "Chunks Updating", "", "", "int", MinecraftServerHelper::getUpdating, NO_MAX, 2)
            ));
        } else {
            minecraft.groups().put("Minecraft Client", Lists.newArrayList(
                    new BasicMetric("c_player", "Client Players", "", "", "int", MinecraftClientHelper::getPlayers, NO_MAX, 1),
                    new BasicMetric("c_fps", "FPS", "Frames per Second", "", "int", MinecraftClientHelper::getFPS, NO_MAX, 1),

                    new StackedMetric("c_outbound_size", "Tx Packet Flow", "Outbound packet and size", " ", "net", NetworkProvider.OUTBOUND_PACKET_SIZE::read, NO_MAX, -2),
                    new StackedMetric("c_inbound_size", "Rx Packet Flow", "Inbound packet and size", " ", "net", NetworkProvider.INBOUND_PACKET_SIZE::read, NO_MAX, -2),
                    new StackedMetric("c_outbound_count", "Tx Packet Count", "Outbound packet count", " ", "int", NetworkProvider.OUTBOUND_PACKET_COUNT::read, NO_MAX, 2),
                    new StackedMetric("c_inbound_count", "Rx Packet Count", "Inbound packet count", " ", "int", NetworkProvider.INBOUND_PACKET_COUNT::read, NO_MAX, 2),

                    new StackedMetric("c_chunks", "Chunks Loaded", "", "", "int", MinecraftClientHelper::getLoadedChunks, MinecraftClientHelper::getLoadedChunksMax, 2)
            ));
        }
        put(minecraft.name(), minecraft);

        var jvm = new Tab("JVM", ICON_JVM);
        var memory = new ArrayList<BasicMetric>();
        JvmProvider.getMemoryPoolMetrics(memory);
        jvm.groups().put("Memory", memory);
        var threads = new ArrayList<BasicMetric>();
        JvmProvider.getThreadsMetrics(threads);
        jvm.groups().put("Threads", threads);
        put(jvm.name(), jvm);

        var system = new Tab("System", ICON_SYSTEM);
        system.groups().put("Basic", Lists.newArrayList(
                new BasicMetric("cpu", "CPU", "CPU usage", "", "percent", OshiHelper::getCpu, OshiHelper::getCpuMax, 1),
                new BasicMetric("ram", "RAM", "", " ", "byte", OshiHelper::getRam, OshiHelper::getRamMax, 1),
                new BasicMetric("in", "Network Rx", "Inbound", " ", "net", OshiHelper::getNetRx, OshiHelper::getNetMax, 2),
                new BasicMetric("out", "Network Tx", "Outbound", " ", "net", OshiHelper::getNetTx, OshiHelper::getNetMax, 2)
        ));
        put(system.name(), system);
    }};

    public static Map<String, Tab> getMetrics() {
        return METRICS;
    }

    public static Tab getOrCreateTab(String name) {
        //TODO support icon
        return METRICS.computeIfAbsent(name, newTabName -> new Tab(newTabName, ""));
    }

    public static void forEachMetric(Consumer<BasicMetric> consumer) {
        METRICS.values().forEach(tab -> tab.groups().values().forEach(metrics -> metrics.forEach(consumer)));
    }

    public static int getMetricCount() {
        int count = 0;
        for (var tab : METRICS.values()) {
            for (var metrics : tab.groups().values()) {
                count += metrics.size();
            }
        }
        return count;
    }
}
