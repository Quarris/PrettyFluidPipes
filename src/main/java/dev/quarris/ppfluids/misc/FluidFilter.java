package dev.quarris.ppfluids.misc;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.quarris.ppfluids.ModContent;
import dev.quarris.ppfluids.container.FluidFilterSlot;
import dev.quarris.ppfluids.network.ButtonPacket;
import dev.quarris.ppfluids.pipe.FluidPipeTileEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FluidFilter extends ItemStackHandler implements INBTSerializable<CompoundNBT> {
    protected FluidPipeTileEntity pipe;
    protected ItemStack moduleItem;
    protected NonNullList<FluidStack> filters;
    public boolean canPopulateFromTanks;
    public boolean isWhitelist;
    public boolean canModifyWhitelist = true;
    private boolean modified;

    public FluidFilter(int size, ItemStack moduleItem, FluidPipeTileEntity pipe) {
        super(size);
        this.filters = NonNullList.withSize(size, FluidStack.EMPTY);
        this.pipe = pipe;
        this.moduleItem = moduleItem;
        if (moduleItem.hasTag() && moduleItem.getTag().contains("filter")) {
            this.deserializeNBT(moduleItem.getTag().getCompound("filter"));
        }
    }

    public boolean isAllowed(ItemStack stack) {
        if (stack.getItem() == ModContent.FLUID_ITEM) {
            return FluidUtil.getFluidContained(stack).map(this::isAllowed).orElse(false);
        }
        return false;
    }

    public boolean isAllowed(FluidStack stack) {
        return this.isFiltered(stack) == this.isWhitelist;
    }

    private boolean isFiltered(FluidStack stack) {
        for (FluidStack fluid : this.filters) {
            if (fluid.isFluidEqual(stack)) {
                return true;
            }
        }
        return false;
    }

    public void setFilter(int slot, FluidStack filter) {
        this.filters.set(slot, filter == null ? FluidStack.EMPTY : filter);
        this.setStackInSlot(slot, filter == null ? ItemStack.EMPTY : FluidUtil.getFilledBucket(filter));
        this.modified = true;
    }

    public FluidStack getFilter(int slot) {
        return this.filters.get(slot);
    }

    public List<Slot> createContainerSlots(int x, int y) {
        List<Slot> slots = new ArrayList<>();

        for(int i = 0; i < this.size(); ++i) {
            slots.add(new FluidFilterSlot(this, i, x + i % 9 * 18, y + i / 9 * 18));
        }

        return slots;
    }

    @OnlyIn(Dist.CLIENT)
    public List<Widget> createScreenButtons(final Screen gui, int x, int y) {
        List<Widget> buttons = new ArrayList<>();
        if (this.canModifyWhitelist) {  // Allowed/Disallowed button
            Supplier<TranslationTextComponent> whitelistText = () -> new TranslationTextComponent("info.prettypipes." + (this.isWhitelist ? "whitelist" : "blacklist"));
            buttons.add(new Button(x, y, 70, 20, whitelistText.get(), (button) -> {
                ButtonPacket.sendAndExecute(this.pipe.getPos(), ButtonPacket.ButtonResult.FILTER_CHANGE, 0);
                button.setMessage(whitelistText.get());
            }));
        }

        if (this.canPopulateFromTanks) { // Populate button
            buttons.add(new Button(x + 72, y, 70, 20, new TranslationTextComponent("info.prettypipes.populate"), (button) -> {
                ButtonPacket.sendAndExecute(this.pipe.getPos(), ButtonPacket.ButtonResult.FILTER_CHANGE, 1);
            }) {
                public void renderToolTip(MatrixStack matrix, int x, int y) {
                    gui.renderTooltip(matrix, (new TranslationTextComponent("info.prettypipes.populate.description")).mergeStyle(TextFormatting.GRAY), x, y);
                }
            });
        }

        return buttons;
    }

    public void onButtonPacket(int id) {
        if (id == 0 && this.canModifyWhitelist) {
            this.isWhitelist = !this.isWhitelist;
            this.modified = true;
            this.save();
        } else if (id == 1 && this.canPopulateFromTanks) {
            for(Direction dir : Direction.values()) {
                IFluidHandler handler = this.pipe.getAdjacentFluidHandler(dir);
                if (handler != null) {
                    for(int i = 0; i < handler.getTanks(); ++i) {
                        FluidStack stack = handler.getFluidInTank(i);
                        if (!stack.isEmpty() && !this.isFiltered(stack)) {
                            FluidStack copy = stack.copy();
                            copy.setAmount(1);
                            for (int j = 0; j < this.size(); j++) {
                                if (this.getFilter(j).isEmpty()) {
                                    this.setFilter(j, copy);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public int size() {
        return this.filters.size();
    }

    public void save() {
        if (this.modified) {
            this.moduleItem.getOrCreateTag().put("filter", this.serializeNBT());
            this.modified = false;
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        ListNBT fluidList = new ListNBT();
        for (FluidStack fluid : this.filters) {
            fluidList.add(fluid.writeToNBT(new CompoundNBT()));
        }
        nbt.put("fluids", fluidList);
        if (this.canModifyWhitelist) {
            nbt.putBoolean("whitelist", this.isWhitelist);
        }

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        ListNBT fluidList = nbt.getList("fluids", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < fluidList.size(); i++) {
            CompoundNBT fluidNBT = fluidList.getCompound(i);
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(fluidNBT);
            this.filters.set(i, fluid);
            this.setStackInSlot(i, FluidUtil.getFilledBucket(fluid));
        }
        if (this.canModifyWhitelist) {
            this.isWhitelist = nbt.getBoolean("whitelist");
        }
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public interface IFluidFilteredContainer {
        FluidFilter getFilter();
    }
}
