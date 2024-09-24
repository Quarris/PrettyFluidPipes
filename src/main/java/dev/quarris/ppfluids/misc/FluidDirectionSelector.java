package dev.quarris.ppfluids.misc;

import de.ellpeck.prettypipes.PrettyPipes;
import de.ellpeck.prettypipes.misc.DirectionSelector;
import de.ellpeck.prettypipes.packets.PacketButton;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import dev.quarris.ppfluids.mixins.DirectionSelectorAccessor;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.util.List;

public class FluidDirectionSelector extends DirectionSelector {

    public FluidDirectionSelector(ItemStack stack, PipeBlockEntity pipe) {
        super(stack, pipe);
    }

    @Override
    public AbstractWidget getButton(int x, int y) {
        DirectionSelectorAccessor accessor = (DirectionSelectorAccessor) this;
        return new ExtendedButton(x, y, 100, 20, Component.translatable("info." + PrettyPipes.ID + ".populate"), button ->
            PacketButton.sendAndExecute(accessor.getPipe().getBlockPos(), PacketButton.ButtonResult.DIRECTION_SELECTOR, List.of())) {
            @Override
            public Component getMessage() {
                PipeBlockEntity pipe = accessor.getPipe();
                Direction dir = accessor.getDirection();
                MutableComponent msg = Component.translatable("dir." + PrettyPipes.ID + "." + (dir != null ? dir.getName() : "all"));
                if (dir != null && pipe instanceof FluidPipeBlockEntity fluidPipe) {
                    MutableComponent blockName = fluidPipe.getFluidHandler(dir) != null ? pipe.getLevel().getBlockState(pipe.getBlockPos().relative(dir)).getBlock().getName() : null;
                    if (blockName != null)
                        msg = msg.append(" (").append(blockName).append(")");
                }
                return msg;
            }
        };
    }

    public static boolean isDirectionValid(DirectionSelectorAccessor selector, Direction dir) {
        if (!(selector.getPipe() instanceof FluidPipeBlockEntity fluidPipe))
            return false;

        if (dir == null)
            return true;

        if (fluidPipe.getFluidHandler(dir) == null)
            return false;

        return fluidPipe.streamModules()
            .filter(p -> p.getLeft() != selector.getStack())
            .map(p -> (DirectionSelectorAccessor) p.getRight().getDirectionSelector(p.getLeft(), fluidPipe))
            .noneMatch(p -> p != null && p.getDirection() == dir);
    }

}
