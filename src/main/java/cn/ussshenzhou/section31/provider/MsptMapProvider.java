package cn.ussshenzhou.section31.provider;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class MsptMapProvider {

    public static void add(ResourceKey<Level> dimension, Type type, int chunkX, int chunkZ, long ns) {

    }

    public enum Type {
        BLOCK_TICK,
        FLUID_TICK,
        SPAWNING,
        RANDOM_TICK,
        BLOCK_EVENT,
        ENTITY,
        BLOCK_ENTITY
    }
}
