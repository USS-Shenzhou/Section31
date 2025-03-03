package cn.ussshenzhou.section31.backend.provider;

import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;

/**
 * @author USS_Shenzhou
 */
public class MinecraftHelper {
    private static MinecraftServer mcServer = null;

    private static synchronized void tryInit() {
        if (mcServer != null) {
            if (mcServer.isShutdown() || mcServer.isStopped()) {
                mcServer = null;
            }
            return;
        }
        try {
            var server = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);
            if (server instanceof MinecraftServer minecraftServer) {
                mcServer = minecraftServer;
            }
        } catch (NullPointerException ignored) {
        }
    }

    public static int getPlayers() {
        if (mcServer == null) {
            tryInit();
            return 0;
        }
        return mcServer.getPlayerCount();
    }

    public static int getMaxPlayers() {
        if (mcServer == null) {
            tryInit();
            return 0;
        }
        return mcServer.getMaxPlayers();
    }

    public static float getMspt() {
        if (mcServer == null) {
            tryInit();
            return 0;
        }
        return mcServer.getTickTimesNanos()[mcServer.getTickCount() % 100] / 1000_000f;
    }

    public static float getMaxMspt() {
        tryInit();
        return 50;
    }
}
