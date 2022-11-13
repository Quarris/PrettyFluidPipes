package dev.quarris.ppfluids.mixins;

import de.ellpeck.prettypipes.pipe.ConnectionType;
import de.ellpeck.prettypipes.pipe.IPipeConnectable;
import de.ellpeck.prettypipes.pipe.PipeBlock;
import dev.quarris.ppfluids.ModRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = PipeBlock.class, remap = false)
public class PipeBlockMixin {

    @Inject(method = "getConnectionType", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void addTankConnection(Level world, BlockPos pos, Direction direction, BlockState state, CallbackInfoReturnable<ConnectionType> cir, BlockPos offset, Direction opposite, BlockEntity tile, IPipeConnectable connectable) {
        if (tile.getCapability(ModRef.Capabilities.FLUID, opposite).isPresent()) {
            cir.setReturnValue(ConnectionType.CONNECTED);
        }
    }
}
