package cn.ussshenzhou.section31.mixin;

import cn.ussshenzhou.section31.provider.MsptMapProvider;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

/**
 * @author USS_Shenzhou
 */
@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {

    @WrapMethod(method = "tickSpawningChunk")
    private void section31TimeSpawning(LevelChunk chunk, long timeDiff, List<MobCategory> spawningCategories, NaturalSpawner.SpawnState spawnCookie, Operation<Void> original) {
        long start = System.nanoTime();
        original.call(chunk, timeDiff, spawningCategories, spawnCookie);
        ChunkPos chunkPos = chunk.getPos();
        MsptMapProvider.add(((ServerChunkCache) (Object) this).level.dimension(), MsptMapProvider.Type.SPAWNING, chunkPos.x(), chunkPos.z(), System.nanoTime() - start);
    }
}
