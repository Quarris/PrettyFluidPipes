package dev.quarris.ppfluids.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.quarris.ppfluids.pipenetwork.FluidPipeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Random;

public class FluidBlobRenderer {

    private static final FluidBlobModel MODEL = new FluidBlobModel(FluidBlobModel.createBlobLayer().bakeRoot());

    public static void render(FluidPipeItem item, PoseStack matrix, Random random, float partialTicks, int light, int overlay, MultiBufferSource buffer) {
        long time = item.getTicksExisted();
        float lastSizeOffset = (float) (Math.sin(Math.toRadians((time - 1) * 10)) + 1) / 30;
        float thisSizeOffset = (float) (Math.sin(Math.toRadians(time * 10)) + 1) / 30;
        float sizeOffset = (Mth.lerp(partialTicks, lastSizeOffset, thisSizeOffset));
        FluidStack fluidStack = item.getFluidContent();
        Fluid fluid = fluidStack.getFluid();
        FluidState renderState = fluid.defaultFluidState();
        IClientFluidTypeExtensions attributes = IClientFluidTypeExtensions.of(renderState);
        float size = Mth.lerp(Math.min(1, fluidStack.getAmount() / 2000f), 0.1f - sizeOffset, 0.25f + sizeOffset);
        int color = attributes.getTintColor(renderState, Minecraft.getInstance().level, item.getCurrentPipe());
        float r = ((color >> 16) & 0xFF) / 255f; // red
        float g = ((color >> 8) & 0xFF) / 255f; // green
        float b = ((color >> 0) & 0xFF) / 255f; // blue
        float a = ((color >> 24) & 0xFF) / 255f; // alpha

        float tx = Mth.lerp(partialTicks, item.lastX, item.x);
        float ty = Mth.lerp(partialTicks, item.lastY, item.y);
        float tz = Mth.lerp(partialTicks, item.lastZ, item.z);

        TextureAtlasSprite sprite = getFluidStillSprite(fluid);
        VertexConsumer vbuf = sprite.wrap(buffer.getBuffer(RenderType.entityTranslucent(sprite.atlasLocation())));

        matrix.pushPose();
        matrix.translate(tx, ty, tz);
        matrix.scale(size, size, size);
        MODEL.renderToBuffer(matrix, vbuf, light, overlay, r, g, b, a);
        matrix.popPose();
    }

    private static TextureAtlasSprite getFluidStillSprite(Fluid fluid) {
        IClientFluidTypeExtensions attributes = IClientFluidTypeExtensions.of(fluid);
        return Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(attributes.getStillTexture());
    }

    public static class FluidBlobModel extends Model {
        private ModelPart blob;

        public FluidBlobModel(ModelPart root) {
            super(RenderType::entityTranslucent);
            this.blob = root.getChild("blob");
        }

        public static LayerDefinition createBlobLayer() {
            MeshDefinition mesh = new MeshDefinition();
            mesh.getRoot().addOrReplaceChild("blob", CubeListBuilder.create().addBox(-8, -8, -8, 16, 16, 16), PartPose.ZERO);
            return LayerDefinition.create(mesh, 64, 64);
        }

        @Override
        public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
            this.blob.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        }
    }

}
