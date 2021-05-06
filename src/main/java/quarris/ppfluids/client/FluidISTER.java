package quarris.ppfluids.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

public class FluidISTER extends ItemStackTileEntityRenderer {

    @Override
    public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        System.out.println("Rendering Item");
        IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack).orElse(null);
        if (handler != null) {
            Fluid fluid = handler.getFluidInTank(0).getFluid();
            Item item = fluid.getDefaultState().getBlockState().getBlock().asItem();
            if (item != Items.AIR) {
                Minecraft.getInstance().getItemRenderer().renderItem(new ItemStack(item), ItemCameraTransforms.TransformType.GROUND, combinedLightIn, combinedOverlayIn, matrixStack, buffer);
            }
        }
    }


}
