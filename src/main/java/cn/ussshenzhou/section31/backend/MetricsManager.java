package cn.ussshenzhou.section31.backend;

import cn.ussshenzhou.section31.backend.metric.BasicMetric;
import cn.ussshenzhou.section31.backend.metric.StackedMetric;
import cn.ussshenzhou.section31.provider.JvmHelper;
import cn.ussshenzhou.section31.provider.MinecraftHelper;
import cn.ussshenzhou.section31.provider.OshiHelper;
import cn.ussshenzhou.section31.provider.network.NetworkDataProvider;
import com.google.common.collect.Lists;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

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
 * <p>
 * {@code important} If true, metric will take 100% width. Otherwise, it will take 50%.
 */
public class MetricsManager {
    public static final Supplier<Integer> NO_MAX = () -> 0;
    protected static final LinkedHashMap<String, ArrayList<BasicMetric>> METRICS = new LinkedHashMap<>() {{
        put("Minecraft in-game", Lists.newArrayList(
                new BasicMetric("player", "Players", "", "", "int", MinecraftHelper::getPlayers, MinecraftHelper::getMaxPlayers, 1),
                new BasicMetric("mspt", "MSPT", "Millisecond per Tick", "", "float", MinecraftHelper::getMspt, MinecraftHelper::getMaxMspt, 1),
                new StackedMetric("outbound_size", "Tx Packet Flow", "Outbound packet and size", " ", "net", NetworkDataProvider.OUTBOUND_PACKET_SIZE::read, NO_MAX, -1),
                new StackedMetric("inbound_size", "Rx Packet Flow", "Inbound packet and size", " ", "net", NetworkDataProvider.INBOUND_PACKET_SIZE::read, NO_MAX, -1),
                new StackedMetric("outbound_count", "Tx Packet Count", "Outbound packet count", " ", "int", NetworkDataProvider.OUTBOUND_PACKET_COUNT::read, NO_MAX, 2),
                new StackedMetric("inbound_count", "Rx Packet Count", "Inbound packet count", " ", "int", NetworkDataProvider.INBOUND_PACKET_COUNT::read, NO_MAX, 2)
        ));
        var jvmList = JvmHelper.getMemoryPoolMetrics();
        put("Java Virtual Machine", jvmList);
        put("System/Hardware", Lists.newArrayList(
                new BasicMetric("cpu", "CPU", "CPU usage", "", "percent", OshiHelper::getCpu, OshiHelper::getCpuMax, 1),
                new BasicMetric("ram", "RAM", "", " ", "byte", OshiHelper::getRam, OshiHelper::getRamMax, 1),
                new BasicMetric("in", "Network Rx", "Inbound", " ", "net", OshiHelper::getNetRx, OshiHelper::getNetMax, 2),
                new BasicMetric("out", "Network Tx", "Outbound", " ", "net", OshiHelper::getNetTx, OshiHelper::getNetMax, 2)
        ));
    }};

    public static Map<String, ArrayList<BasicMetric>> getMetrics() {
        return METRICS;
    }
}
