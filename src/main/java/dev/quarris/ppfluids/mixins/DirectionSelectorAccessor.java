package dev.quarris.ppfluids.mixins;

import de.ellpeck.prettypipes.misc.DirectionSelector;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DirectionSelector.class)
public interface DirectionSelectorAccessor {

    @Accessor
    ItemStack getStack();

    @Accessor
    PipeBlockEntity getPipe();

    @Accessor
    Direction getDirection();

}
