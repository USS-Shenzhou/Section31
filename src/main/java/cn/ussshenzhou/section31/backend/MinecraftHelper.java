package cn.ussshenzhou.section31.backend;

import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;

import java.lang.ref.WeakReference;

/**
 * @author USS_Shenzhou
 */
public class MinecraftHelper {
    private static final WeakReference<MinecraftServer> mcServer = new WeakReference<>(null);

    private static void tryInit() {
        if (mcServer.get() != null) {
            return;
        }
        try {
            var server = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);
            if (server instanceof MinecraftServer minecraftServer) {
                mcServer.refersTo(minecraftServer);
            }
        } catch (NullPointerException ignored) {
        }
    }

    public static int getPlayers() {
        if (mcServer.get() == null) {
            tryInit();
            return 0;
        }
        //noinspection DataFlowIssue
        return mcServer.get().getPlayerCount();
    }

    public static int getMaxPlayers() {
        if (mcServer.get() == null) {
            tryInit();
            return 0;
        }
        //noinspection DataFlowIssue
        return mcServer.get().getMaxPlayers();
    }

    public static float getMspt() {
        if (mcServer.get() == null) {
            tryInit();
            return 0;
        }
        //noinspection DataFlowIssue
        return mcServer.get().getTickTimesNanos()[mcServer.get().getTickCount() % 100];
    }

    public static float getMaxMspt() {
        tryInit();
        return 50;
    }
}
