package dev.quarris.ppfluids.pipenetwork;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import de.ellpeck.prettypipes.Utility;
import de.ellpeck.prettypipes.network.PipeItem;
import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.IPipeConnectable;
import de.ellpeck.prettypipes.pipe.PipeTileEntity;
import dev.quarris.ppfluids.client.FluidBlobRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import dev.quarris.ppfluids.items.FluidItem;
import dev.quarris.ppfluids.pipe.FluidPipeTileEntity;

import java.util.Random;

public class FluidPipeItem extends PipeItem {

    public static final ResourceLocation TYPE = new ResourceLocation("prettypipes", "pipe_fluid");

    public FluidPipeItem(ItemStack stack, float speed) {
        super(TYPE, stack, speed);
    }

    public FluidPipeItem(CompoundNBT nbt) {
        this(TYPE, nbt);
    }

    public FluidPipeItem(ResourceLocation type, CompoundNBT nbt) {
        super(type, nbt);
    }

    public FluidStack getFluidContent() {
        IFluidHandlerItem tank = FluidUtil.getFluidHandler(this.stack).orElse(null);
        if (tank != null) {
            return tank.getFluidInTank(0);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public void drop(World world, ItemStack stack) {
        super.drop(world, stack);
    }

    @Override
    protected void onPathObstructed(PipeTileEntity currPipe, boolean tryReturn) {
        if (currPipe.getWorld().isRemote)
            return;
        PipeNetwork network = PipeNetwork.get(currPipe.getWorld());
        if (tryReturn) {
            // first time: we try to return to our input chest
            if (!this.retryOnObstruction && network.routeItemToLocation(currPipe.getPos(), this.destInventory, this.getStartPipe(), this.startInventory, this.stack, speed -> this)) {
                this.retryOnObstruction = true;
                return;
            }
            // second time: we arrived at our input chest, it is full, so we try to find a different goal location
            FluidStack remain = PipeNetworkUtil.routeFluid(currPipe.getWorld(), currPipe.getPos(), this.destInventory, FluidItem.getFluidCopyFromItem(this.stack), (stack, speed) -> this, false);
            if (!remain.isEmpty())
                this.drop(currPipe.getWorld(), FluidItem.createItemFromFluid(remain, false));
        } else {
            // if all re-routing attempts fail, we drop
            this.drop(currPipe.getWorld(), this.stack);
        }
    }

    @Override
    protected ItemStack store(PipeTileEntity currPipe) {
        if (currPipe instanceof FluidPipeTileEntity) {
            FluidPipeTileEntity currFluidPipe = (FluidPipeTileEntity) currPipe;
            Direction dir = Utility.getDirectionFromOffset(this.getDestInventory(), this.getDestPipe());
            IPipeConnectable connectable = currFluidPipe.getPipeConnectable(dir);
            if (connectable != null) {
                return connectable.insertItem(currFluidPipe.getPos(), dir, this.stack, false);
            } else {
                IFluidHandler handler = currFluidPipe.getAdjacentFluidHandler(dir);
                if (handler == null)
                    return this.stack;

                return FluidItem.insertFluid(handler, this.stack, false);
            }
        }

        return super.store(currPipe);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PipeTileEntity tile, MatrixStack matrixStack, Random random, float partialTicks, int light, int overlay, IRenderTypeBuffer buffer) {
        FluidBlobRenderer.render(this, matrixStack, random, partialTicks, light, overlay, buffer);
    }
}
