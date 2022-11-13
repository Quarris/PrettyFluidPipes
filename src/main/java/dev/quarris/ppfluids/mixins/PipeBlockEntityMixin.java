package dev.quarris.ppfluids.mixins;

import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import dev.quarris.ppfluids.ModRef;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = PipeBlockEntity.class, remap = false)
public abstract class PipeBlockEntityMixin {

    @Shadow public abstract <T> T getNeighborCap(Direction dir, Capability<T> cap);

    @Inject(method = "canHaveModules", at = @At(value = "INVOKE", target = "Lde/ellpeck/prettypipes/pipe/PipeBlockEntity;getItemHandler(Lnet/minecraft/core/Direction;)Lnet/minecraftforge/items/IItemHandler;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void checkFluidModules(CallbackInfoReturnable<Boolean> cir, Direction[] var1, int var2, int var3, Direction dir) {
        if (this.getFluidHandler(dir) != null) {
            cir.setReturnValue(true);
        }
    }

    public IFluidHandler getFluidHandler(Direction dir) {
        IFluidHandler handler = this.getNeighborCap(dir, ModRef.Capabilities.FLUID);
        if (handler != null)
            return handler;

        return null;
    }

}
