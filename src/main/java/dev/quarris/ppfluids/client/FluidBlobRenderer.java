package dev.quarris.ppfluids.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import dev.quarris.ppfluids.pipenetwork.FluidPipeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class FluidBlobRenderer {

    private static final FluidBlobModel MODEL = new FluidBlobModel();

    public static void render(FluidPipeItem item, MatrixStack matrixStack, Random random, float partialTicks, int light, int overlay, IRenderTypeBuffer buffer) {
        FluidStack fluidStack = item.getFluidContent();
        Fluid fluid = fluidStack.getFluid();
        float size = MathHelper.lerp(Math.min(1, fluidStack.getAmount() / 2000f), 0.1f, 0.25f);
        int color = fluid.getAttributes().getColor(fluidStack);
        float r = ((color >> 16) & 0xFF) / 255f; // red
        float g = ((color >> 8) & 0xFF) / 255f; // green
        float b = ((color >> 0) & 0xFF) / 255f; // blue
        float a = ((color >> 24) & 0xFF) / 255f; // alpha

        TextureAtlasSprite sprite = getFluidStillSprite(fluid);

        IVertexBuilder vbuf = sprite.wrapBuffer(buffer.getBuffer(RenderType.getEntityTranslucent(sprite.getAtlasTexture().getTextureLocation())));

        matrixStack.push();
        matrixStack.translate(item.x, item.y, item.z);
        matrixStack.scale(size, size, size);
        MODEL.render(matrixStack, vbuf, light, overlay, r, g, b, a);
        matrixStack.pop();
    }

    private static TextureAtlasSprite getFluidStillSprite(Fluid fluid) {
        return Minecraft.getInstance()
                .getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE)
                .apply(fluid.getAttributes().getStillTexture());
    }

    public static class FluidBlobModel extends Model {
        private ModelRenderer blob;

        public FluidBlobModel() {
            super(RenderType::getEntityCutout);
            this.blob = new ModelRenderer(this);
            this.blob.setTextureOffset(0, 0).addBox(-8, -8, -8, 16, 16, 16);
        }

        @Override
        public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
            this.blob.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        }
    }

}
