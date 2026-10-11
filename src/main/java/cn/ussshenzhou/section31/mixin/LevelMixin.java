package cn.ussshenzhou.section31.mixin;

import cn.ussshenzhou.section31.provider.MsptMapProvider;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author USS_Shenzhou
 */
@Mixin(Level.class)
public abstract class LevelMixin {

    @WrapOperation(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/TickingBlockEntity;tick()V"))
    private void section31TimeBlockEntity(TickingBlockEntity ticker, Operation<Void> original) {
        //noinspection ConstantValue
        if (!((Object) this instanceof ServerLevel)) {
            original.call(ticker);
            return;
        }
        BlockPos pos = ticker.getPos();
        long start = System.nanoTime();
        original.call(ticker);
        MsptMapProvider.add(((Level) (Object) this).dimension(), MsptMapProvider.Type.BLOCK_ENTITY,SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), System.nanoTime() - start);
    }
}
