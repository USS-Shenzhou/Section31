package cn.ussshenzhou.section31.mixin;

import cn.ussshenzhou.section31.provider.MsptMapProvider;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockEventData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author USS_Shenzhou
 */
@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @WrapMethod(method = "tickBlock")
    private void section31TimeBlockTick(BlockPos pos, Block type, Operation<Void> original) {
        long start = System.nanoTime();
        original.call(pos, type);
        MsptMapProvider.add(((ServerLevel) (Object) this).dimension(), MsptMapProvider.Type.BLOCK_TICK, SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), System.nanoTime() - start);
    }

    @WrapMethod(method = "tickFluid")
    private void section31TimeFluidTick(BlockPos pos, Fluid type, Operation<Void> original) {
        long start = System.nanoTime();
        original.call(pos, type);
        MsptMapProvider.add(((ServerLevel) (Object) this).dimension(), MsptMapProvider.Type.FLUID_TICK, SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), System.nanoTime() - start);
    }

    @WrapMethod(method = "tickChunk")
    private void section31TimeRandomTick(LevelChunk chunk, int tickSpeed, Operation<Void> original) {
        long start = System.nanoTime();
        original.call(chunk, tickSpeed);
        ChunkPos chunkPos = chunk.getPos();
        MsptMapProvider.add(((ServerLevel) (Object) this).dimension(), MsptMapProvider.Type.RANDOM_TICK, chunkPos.x(), chunkPos.z(), System.nanoTime() - start);
    }

    @WrapMethod(method = "doBlockEvent")
    private boolean section31TimeBlockEvent(BlockEventData eventData, Operation<Boolean> original) {
        long start = System.nanoTime();
        boolean result = original.call(eventData);
        BlockPos pos = eventData.pos();
        MsptMapProvider.add(((ServerLevel) (Object) this).dimension(), MsptMapProvider.Type.BLOCK_EVENT, SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), System.nanoTime() - start);
        return result;
    }

    @WrapMethod(method = "tickNonPassenger")
    private void section31TimeEntity(Entity entity, Operation<Void> original) {
        ChunkPos chunkPos = entity.chunkPosition();
        long start = System.nanoTime();
        original.call(entity);
        MsptMapProvider.add(((ServerLevel) (Object) this).dimension(), MsptMapProvider.Type.ENTITY, chunkPos.x(), chunkPos.z(), System.nanoTime() - start);
    }
}
