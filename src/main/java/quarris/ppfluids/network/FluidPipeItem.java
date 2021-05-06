package quarris.ppfluids.network;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import de.ellpeck.prettypipes.Utility;
import de.ellpeck.prettypipes.network.PipeItem;
import de.ellpeck.prettypipes.pipe.IPipeConnectable;
import de.ellpeck.prettypipes.pipe.PipeTileEntity;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import quarris.ppfluids.items.FluidItem;
import quarris.ppfluids.pipe.FluidPipeTileEntity;

import java.util.Random;

public class FluidPipeItem extends PipeItem {

    public static final ResourceLocation TYPE = new ResourceLocation("prettypipes", "pipe_fluid");
    private final ResourceLocation modelTexture;
    private final FluidBlobModel model;

    public FluidPipeItem(ItemStack stack, float speed) {
        super(TYPE, stack, speed);
        FluidStack fluidStack = this.getFluidContent();
        Fluid fluid = fluidStack.getFluid();
        float size = MathHelper.lerp(Math.min(1, fluidStack.getAmount() / 2000f), 0.2f, 1f);
        TextureAtlasSprite sprite = getFluidStillSprite(fluid);
        this.model = new FluidBlobModel(sprite, size);
        this.modelTexture = sprite.getAtlasTexture().getTextureLocation();
    }

    public FluidPipeItem(CompoundNBT nbt) {
        this(TYPE, nbt);
    }

    public FluidPipeItem(ResourceLocation type, CompoundNBT nbt) {
        super(type, nbt);
        FluidStack fluidStack = this.getFluidContent();
        Fluid fluid = fluidStack.getFluid();
        float size = MathHelper.lerp(Math.min(1, fluidStack.getAmount() / 2000f), 0.2f, 1f);
        TextureAtlasSprite sprite = getFluidStillSprite(fluid);
        this.model = new FluidBlobModel(sprite, size);
        this.modelTexture = sprite.getAtlasTexture().getTextureLocation();
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
        // Drop fluid??
        super.drop(world, stack);
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

    @Override
    public void render(PipeTileEntity tile, MatrixStack matrixStack, Random random, float partialTicks, int light, int overlay, IRenderTypeBuffer buffer) {
        FluidStack fluidStack = this.getFluidContent();
        Fluid fluid = fluidStack.getFluid();
        int color = fluid.getAttributes().getColor(fluidStack);
        float r = ((color >> 16) & 0xFF) / 255f; // red
        float g = ((color >> 8) & 0xFF) / 255f; // green
        float b = ((color >> 0) & 0xFF) / 255f; // blue
        float a = ((color >> 24) & 0xFF) / 255f; // alpha

        matrixStack.translate(this.x, this.y, this.z);
        this.model.render(matrixStack, buffer.getBuffer(RenderType.getEntityTranslucent(this.modelTexture)), light, overlay, r, g, b, a);
    }

    private static TextureAtlasSprite getFluidStillSprite(Fluid fluid) {
        return Minecraft.getInstance()
                .getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE)
                .apply(fluid.getAttributes().getStillTexture());
    }

    public static class FluidBlobModel extends Model {
        private TextureAtlasSprite sprite;
        private ModelRenderer blob;

        public FluidBlobModel(TextureAtlasSprite sprite, float size) {
            super(RenderType::getEntityCutout);
            this.sprite = sprite;
            this.blob = new ModelRenderer(this);
            this.blob.setTextureOffset(0, 0).addBox(-8, -8, -8, 16, 16, 16, -8 + size * 2);
        }

        @Override
        public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
            this.blob.render(matrixStackIn, sprite.wrapBuffer(bufferIn), packedLightIn, packedOverlayIn, red, green, blue, alpha);
        }
    }
}
