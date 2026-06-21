package cn.ussshenzhou.section31.provider;

import cn.ussshenzhou.section31.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Optional;

/**
 * @author USS_Shenzhou
 */
public class MinecraftClientHelper {
    private static Minecraft client() {
        return Minecraft.getInstance();
    }

    private static Optional<Level> level() {
        return Optional.ofNullable(client().level);
    }

    public static int getPlayers() {
        return level().map(l -> l.players().size()).orElse(0);
    }

    public static float getFPS() {
        return client().getFps();
    }

    public static Map<String, Integer> getLoadedChunks() {
        return level().map(l -> Map.of(
                l.dimension().identifier().toString(),
                l.getChunkSource().getLoadedChunksCount()
        )).orElse(Util.NONE);
    }

    public static int getLoadedChunksMax() {
        return level().map(l ->
                ((ClientChunkCache) l.getChunkSource()).storage.chunks.length()
        ).orElse(0);
    }
}
