package dev.quarris.ppfluids.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.quarris.ppfluids.ModContent;
import dev.quarris.ppfluids.container.FluidFilterSlot;
import dev.quarris.ppfluids.network.ButtonPacket;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FluidFilter extends ItemStackHandler implements INBTSerializable<CompoundTag> {
    protected FluidPipeBlockEntity pipe;
    protected ItemStack moduleItem;
    protected NonNullList<FluidStack> filters;
    public boolean canPopulateFromTanks;
    public boolean isWhitelist;
    public boolean canModifyWhitelist = true;
    private boolean modified;

    public FluidFilter(int size, ItemStack moduleItem, FluidPipeBlockEntity pipe) {
        super(size);
        this.filters = NonNullList.withSize(size, FluidStack.EMPTY);
        this.pipe = pipe;
        this.moduleItem = moduleItem;
        if (moduleItem.hasTag() && moduleItem.getTag().contains("filter")) {
            this.deserializeNBT(moduleItem.getTag().getCompound("filter"));
        }
    }

    public boolean isAllowed(ItemStack stack) {
        if (stack.getItem() == ModContent.FLUID_ITEM.get()) {
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
    public List<AbstractWidget> createScreenButtons(final Screen gui, int x, int y) {
        List<AbstractWidget> buttons = new ArrayList<>();
        if (this.canModifyWhitelist) {  // Allowed/Disallowed button
            Supplier<TranslatableComponent> whitelistText = () -> new TranslatableComponent("info.prettypipes." + (this.isWhitelist ? "whitelist" : "blacklist"));
            buttons.add(new Button(x, y, 70, 20, whitelistText.get(), (button) -> {
                ButtonPacket.sendAndExecute(this.pipe.getBlockPos(), ButtonPacket.ButtonResult.FILTER_CHANGE, 0);
                button.setMessage(whitelistText.get());
            }));
        }

        if (this.canPopulateFromTanks) { // Populate button
            buttons.add(new Button(x + 72, y, 70, 20, new TranslatableComponent("info.prettypipes.populate"), (button) -> {
                ButtonPacket.sendAndExecute(this.pipe.getBlockPos(), ButtonPacket.ButtonResult.FILTER_CHANGE, 1);
            }) {
                public void renderToolTip(PoseStack matrix, int x, int y) {
                    gui.renderTooltip(matrix, (new TranslatableComponent("info.prettypipes.populate.description")).withStyle(ChatFormatting.GRAY), x, y);
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
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag fluidList = new ListTag();
        for (FluidStack fluid : this.filters) {
            fluidList.add(fluid.writeToNBT(new CompoundTag()));
        }
        nbt.put("fluids", fluidList);
        if (this.canModifyWhitelist) {
            nbt.putBoolean("whitelist", this.isWhitelist);
        }

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag fluidList = nbt.getList("fluids", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < fluidList.size(); i++) {
            CompoundTag fluidNBT = fluidList.getCompound(i);
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
