package dev.quarris.ppfluids.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import de.ellpeck.prettypipes.entities.PipeFrameEntity;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import dev.quarris.ppfluids.pipenetwork.FluidNetworkLocation;
import dev.quarris.ppfluids.pipenetwork.PipeNetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PipeFrameEntity.class)
public abstract class PipeFrameEntityMixin extends ItemFrame {

    @Shadow public abstract InteractionResult interact(Player player, InteractionHand hand);

    @Shadow @Final private static EntityDataAccessor<Integer> AMOUNT;

    public PipeFrameEntityMixin(EntityType<? extends ItemFrame> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lde/ellpeck/prettypipes/network/PipeNetwork;getOrderedNetworkItems(Lnet/minecraft/core/BlockPos;)Ljava/util/List;"),
        cancellable = true
    )
    private void ppfluids_setFluidPipeFrameAmount(CallbackInfo ci, @Local(ordinal = 0) BlockPos attached, @Local(ordinal = 1) BlockPos node, @Local ItemStack itemStack) {
        if (!(this.level().getBlockEntity(attached) instanceof FluidPipeBlockEntity)) {
            return;
        }

        FluidStack fluidStack = FluidUtil.getFluidContained(itemStack).orElse(FluidStack.EMPTY);
        if (fluidStack.isEmpty()) {
            return;
        }

        List<FluidNetworkLocation> fluids = PipeNetworkUtil.getOrderedNetworkFluids(this.level(), node);
        int amount = fluids.stream().mapToInt((i) -> i.getFluidAmount(this.level(), fluidStack)).sum();
        this.entityData.set(AMOUNT, amount);
        ci.cancel();
    }

}
