package dev.quarris.ppfluids.mixins;

import de.ellpeck.prettypipes.misc.DirectionSelector;
import de.ellpeck.prettypipes.misc.ItemFilter;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemFilter.class)
public interface ItemFilterAccessor {

    @Accessor
    void setModified(boolean modified);

}
