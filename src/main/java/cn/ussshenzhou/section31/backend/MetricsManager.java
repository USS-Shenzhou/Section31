package cn.ussshenzhou.section31.backend;

import cn.ussshenzhou.section31.backend.metric.BasicMetric;
import cn.ussshenzhou.section31.backend.metric.StackedMetric;
import cn.ussshenzhou.section31.provider.JvmHelper;
import cn.ussshenzhou.section31.provider.MinecraftClientHelper;
import cn.ussshenzhou.section31.provider.MinecraftServerHelper;
import cn.ussshenzhou.section31.provider.OshiHelper;
import cn.ussshenzhou.section31.provider.network.NetworkDataProvider;
import cn.ussshenzhou.section31.util.Util;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.ussshenzhou.section31.util.Util.NO_MAX;

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
    protected static final LinkedHashMap<String, ArrayList<BasicMetric>> METRICS = new LinkedHashMap<>() {{
        if (Util.IS_SERVER) {
            put("Minecraft Server", Lists.newArrayList(
                    new BasicMetric("player", "Server Players", "", "", "int", MinecraftServerHelper::getPlayers, MinecraftServerHelper::getMaxPlayers, 1),
                    new BasicMetric("mspt", "Server MSPT", "Millisecond per Tick", "", "float", MinecraftServerHelper::getMspt, MinecraftServerHelper::getMaxMspt, 1),

                    new StackedMetric("outbound_size", "Tx Packet Flow", "Outbound packet and size", " ", "net", NetworkDataProvider.OUTBOUND_PACKET_SIZE::read, NO_MAX, -1),
                    new StackedMetric("inbound_size", "Rx Packet Flow", "Inbound packet and size", " ", "net", NetworkDataProvider.INBOUND_PACKET_SIZE::read, NO_MAX, -1),
                    new StackedMetric("outbound_count", "Tx Packet Count", "Outbound packet count", " ", "int", NetworkDataProvider.OUTBOUND_PACKET_COUNT::read, NO_MAX, 2),
                    new StackedMetric("inbound_count", "Rx Packet Count", "Inbound packet count", " ", "int", NetworkDataProvider.INBOUND_PACKET_COUNT::read, NO_MAX, 2),

                    new StackedMetric("visible_chunks", "Chunks Visible", "", "", "int", MinecraftServerHelper::getVisible, NO_MAX, 2),
                    new StackedMetric("updating_chunks", "Chunks Updating", "", "", "int", MinecraftServerHelper::getUpdating, NO_MAX, 2)
            ));
        } else {
            put("Minecraft Client", Lists.newArrayList(
                    new BasicMetric("c_player", "Client Players", "", "", "int", MinecraftClientHelper::getPlayers, NO_MAX, 1),
                    new BasicMetric("c_fps", "FPS", "Frames per Second", "", "int", MinecraftClientHelper::getFPS, NO_MAX, 1),

                    new StackedMetric("c_outbound_size", "Tx Packet Flow", "Outbound packet and size", " ", "net", NetworkDataProvider.OUTBOUND_PACKET_SIZE::read, NO_MAX, -1),
                    new StackedMetric("c_inbound_size", "Rx Packet Flow", "Inbound packet and size", " ", "net", NetworkDataProvider.INBOUND_PACKET_SIZE::read, NO_MAX, -1),
                    new StackedMetric("c_outbound_count", "Tx Packet Count", "Outbound packet count", " ", "int", NetworkDataProvider.OUTBOUND_PACKET_COUNT::read, NO_MAX, 2),
                    new StackedMetric("c_inbound_count", "Rx Packet Count", "Inbound packet count", " ", "int", NetworkDataProvider.INBOUND_PACKET_COUNT::read, NO_MAX, 2),

                    new StackedMetric("c_chunks", "Chunks Loaded", "", "", "int", MinecraftClientHelper::getLoadedChunks, MinecraftClientHelper::getLoadedChunksMax, 2)
            ));
        }

        var jvmList = new ArrayList<BasicMetric>();
        JvmHelper.getMemoryPoolMetrics(jvmList);
        JvmHelper.getThreadsMetrics(jvmList);
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
