package cn.ussshenzhou.section31.provider;

import cn.ussshenzhou.section31.util.Util;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author USS_Shenzhou
 */
public class MinecraftServerHelper {

    private static MinecraftServer mcServer = null;

    private static synchronized void checkServer() {
        if (mcServer != null) {
            if (mcServer.isShutdown() || mcServer.isStopped()) {
                mcServer = null;
            }
            return;
        }
        mcServer = ServerLifecycleHooks.getCurrentServer();
    }

    public static int getPlayers() {
        return getMcServer().map(MinecraftServer::getPlayerCount).orElse(0);
    }

    public static int getMaxPlayers() {
        return getMcServer().map(MinecraftServer::getMaxPlayers).orElse(0);
    }

    public static float getMspt() {
        return getMcServer().map(s -> s.getTickTimesNanos()[mcServer.getTickCount() % 100] / 1000_000f).orElse(0f);
    }

    public static float getMaxMspt() {
        return 50;
    }

    public static Optional<MinecraftServer> getMcServer() {
        checkServer();
        return Optional.ofNullable(mcServer);
    }

    public static Map<String, Integer> getVisible() {
        return getMcServer()
                .map(s -> s.levels
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey().identifier().toString(),
                                e -> e.getValue().getChunkSource().chunkMap.visibleChunkMap.size()
                        ))
                ).orElse(Util.NONE);
    }

    public static Map<String, Integer> getUpdating() {
        return getMcServer()
                .map(s -> s.levels
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey().identifier().toString(),
                                e -> e.getValue().getChunkSource().chunkMap.updatingChunkMap.size()
                        ))
                ).orElse(Util.NONE);
    }
}
