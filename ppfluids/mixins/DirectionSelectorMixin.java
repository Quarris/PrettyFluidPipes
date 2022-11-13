package old.dev.quarris.ppfluids.mixins;

import de.ellpeck.prettypipes.misc.DirectionSelector;
import old.dev.quarris.ppfluids.misc.FluidDirectionSelector;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DirectionSelector.class, remap = false)
public class DirectionSelectorMixin {

    @Inject(method = "isDirectionValid", at = @At("HEAD"), cancellable = true)
    private void isDirectionValid(Direction dir, CallbackInfoReturnable<Boolean> cir) {
        DirectionSelectorAccessor accessor = (DirectionSelectorAccessor) this;
        if (accessor instanceof FluidDirectionSelector) {
            if (FluidDirectionSelector.isDirectionValid(accessor, dir)) {
                cir.setReturnValue(true);
            }
        }
    }
}
