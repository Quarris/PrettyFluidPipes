package dev.quarris.ppfluids.pipenetwork;

import com.mojang.blaze3d.vertex.PoseStack;
import de.ellpeck.prettypipes.Utility;
import de.ellpeck.prettypipes.network.PipeItem;
import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.IPipeConnectable;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import dev.quarris.ppfluids.ModConfig;
import dev.quarris.ppfluids.client.FluidBlobRenderer;
import dev.quarris.ppfluids.items.FluidItem;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.Random;

public class FluidPipeItem extends PipeItem {

    public static final ResourceLocation FLUID_TYPE = new ResourceLocation("prettypipes", "pipe_fluid");

    private long ticksExisted;

    public FluidPipeItem(ItemStack stack, float speed) {
        super(FLUID_TYPE, stack, speed);
    }

    public FluidPipeItem(CompoundTag nbt) {
        this(FLUID_TYPE, nbt);
    }

    public FluidPipeItem(ResourceLocation type, CompoundTag nbt) {
        super(type, nbt);
        this.ticksExisted = nbt.getLong("TicksExisted");
    }

    public FluidStack getFluidContent() {
        IFluidHandlerItem tank = FluidUtil.getFluidHandler(this.stack).orElse(null);
        if (tank != null) {
            return tank.getFluidInTank(0);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public void updateInPipe(PipeBlockEntity currPipe) {
        super.updateInPipe(currPipe);
        this.ticksExisted++;
    }

    @Override
    public void drop(Level level, ItemStack stack) {
        if (ModConfig.dropFluidContainers.get()) {
            super.drop(level, stack);
        }
    }

    public long getTicksExisted() {
        return this.ticksExisted;
    }

    @Override
    protected void onPathObstructed(PipeBlockEntity currPipe, boolean tryReturn) {
        if (currPipe.getLevel().isClientSide())
            return;

        PipeNetwork network = PipeNetwork.get(currPipe.getLevel());
        if (tryReturn) {
            // first time: we try to return to our input chest
            if (!this.retryOnObstruction && network.routeItemToLocation(currPipe.getBlockPos(), this.destInventory, this.getStartPipe(), this.startInventory, this.stack, speed -> this)) {
                this.retryOnObstruction = true;
                return;
            }
            // second time: we arrived at our input chest, it is full, so we try to find a different goal location
            FluidStack remain = PipeNetworkUtil.routeFluid(currPipe.getLevel(), currPipe.getBlockPos(), this.destInventory, FluidItem.getFluidCopyFromItem(this.stack), (stack, speed) -> this, false);
            if (!remain.isEmpty())
                this.drop(currPipe.getLevel(), FluidItem.createItemFromFluid(remain, false));
        } else {
            // if all re-routing attempts fail, we drop
            this.drop(currPipe.getLevel(), this.stack);
        }
    }

    @Override
    protected ItemStack store(PipeBlockEntity currPipe) {
        if (currPipe instanceof FluidPipeBlockEntity) {
            FluidPipeBlockEntity currFluidPipe = (FluidPipeBlockEntity) currPipe;
            Direction dir = Utility.getDirectionFromOffset(this.getDestInventory(), this.getDestPipe());
            IPipeConnectable connectable = currFluidPipe.getPipeConnectable(dir);
            if (connectable != null) {
                return connectable.insertItem(currFluidPipe.getBlockPos(), dir, this.stack, false);
            } else {
                IFluidHandler handler = currFluidPipe.getFluidHandler(dir);
                if (handler == null)
                    return this.stack;

                return FluidItem.insertFluid(handler, this.stack, false);
            }
        }

        return super.store(currPipe);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        tag.putLong("TicksExisted", this.ticksExisted);
        return tag;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PipeBlockEntity tile, PoseStack matrixStack, Random random, float partialTicks, int light, int overlay, MultiBufferSource buffer) {
        FluidBlobRenderer.render(this, matrixStack, random, partialTicks, light, overlay, buffer);
    }
}
