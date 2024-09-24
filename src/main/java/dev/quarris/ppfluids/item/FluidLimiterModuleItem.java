package dev.quarris.ppfluids.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.ellpeck.prettypipes.items.IModule;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import de.ellpeck.prettypipes.pipe.containers.AbstractPipeContainer;
import de.ellpeck.prettypipes.pipe.modules.stacksize.StackSizeModuleItem;
import dev.quarris.ppfluids.container.FluidLimiterContainer;
import dev.quarris.ppfluids.registry.DataComponentSetup;
import dev.quarris.ppfluids.registry.MenuSetup;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class FluidLimiterModuleItem extends FluidModuleItem {

    public FluidLimiterModuleItem(String name, Properties properties) {
        super(name, properties);
    }

    @Override
    public int getMaxInsertionAmount(ItemStack module, PipeBlockEntity pipe, FluidStack fluid, IFluidHandler destination) {
        var data = module.getOrDefault(DataComponentSetup.FLUID_LIMITER, LimiterData.DEFAULT);
        var max = data.maxAmount;
        if (data.useBucketMeasure)
            max *= 1000;
        var storedAmount = 0;
        for (var i = 0; i < destination.getTanks(); i++) {
            var storedFluid = destination.getFluidInTank(i);
            if (storedFluid.isEmpty())
                continue;
            storedAmount += storedFluid.getAmount();
            if (storedAmount >= max)
                return 0;
        }
        return max - storedAmount;
    }

    @Override
    public boolean isCompatible(ItemStack itemStack, PipeBlockEntity pipeBlockEntity, IModule iModule) {
        return !(iModule instanceof FluidLimiterModuleItem);
    }

    @Override
    public boolean hasContainer(ItemStack itemStack, PipeBlockEntity pipeBlockEntity) {
        return true;
    }

    @Override
    public AbstractPipeContainer<?> getContainer(ItemStack module, PipeBlockEntity tile, int windowId, Inventory inv, Player player, int moduleIndex) {
        return new FluidLimiterContainer(MenuSetup.FLUID_LIMITER.get(), windowId, player, tile.getBlockPos(), moduleIndex);
    }

    public record LimiterData(int maxAmount, boolean useBucketMeasure) {
        public static final LimiterData DEFAULT = new LimiterData(8000, false);
        public static final Codec<LimiterData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("max_amount").forGetter(LimiterData::maxAmount),
            Codec.BOOL.fieldOf("use_bucket_measure").forGetter(LimiterData::useBucketMeasure)
        ).apply(i, LimiterData::new));
    }
}
