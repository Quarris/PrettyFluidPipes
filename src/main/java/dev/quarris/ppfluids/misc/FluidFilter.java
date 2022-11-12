package dev.quarris.ppfluids.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import de.ellpeck.prettypipes.PrettyPipes;
import de.ellpeck.prettypipes.misc.ItemFilter;
import de.ellpeck.prettypipes.packets.PacketButton;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import dev.quarris.ppfluids.ModContent;
import dev.quarris.ppfluids.container.FluidFilterSlot;
import dev.quarris.ppfluids.mixins.ItemFilterAccessor;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class FluidFilter extends ItemFilter {

    protected FluidPipeBlockEntity pipe;

    public FluidFilter(int size, ItemStack moduleItem, PipeBlockEntity pipe, boolean isDefaultWhitelist) {
        super(size, moduleItem, pipe);
        this.pipe = (FluidPipeBlockEntity) pipe;
        this.isWhitelist = isDefaultWhitelist;
        this.load();
    }

    public FluidFilter(int size, ItemStack moduleItem, FluidPipeBlockEntity pipe) {
        this(size, moduleItem, pipe, false);
    }

    @Override
    public List<Slot> getSlots(int x, int y) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < this.getSlots(); ++i) {
            slots.add(new FluidFilterSlot(this, i, x + i % 9 * 18, y + i / 9 * 18));
        }
        return slots;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<AbstractWidget> getButtons(Screen gui, int x, int y, boolean rightAligned) {
        List<AbstractWidget> buttons = new ArrayList<>();
        if (this.canModifyWhitelist) {  // Allowed/Disallowed button
            Supplier<MutableComponent> whitelistText = () -> new TranslatableComponent("info.prettypipes." + (this.isWhitelist ? "whitelist" : "blacklist"));
            buttons.add(new Button(x - 20, y, 20, 20, whitelistText.get(), button -> {
                PacketButton.sendAndExecute(this.pipe.getBlockPos(), PacketButton.ButtonResult.FILTER_CHANGE, 0);
                button.setMessage(whitelistText.get());
            }) {
                @Override
                public void renderToolTip(PoseStack matrix, int x, int y) {
                    gui.renderTooltip(matrix, new TranslatableComponent("info.ppfluids." + (FluidFilter.this.isWhitelist ? "whitelist" : "blacklist") + ".description").withStyle(ChatFormatting.GRAY), x, y);
                }
            });
        }

        if (this.canPopulateFromInventories) { // Populate button
            buttons.add(new Button(x - 42, y, 20, 20, new TranslatableComponent("info." + PrettyPipes.ID + ".populate"), button -> PacketButton.sendAndExecute(this.pipe.getBlockPos(), PacketButton.ButtonResult.FILTER_CHANGE, 1)) {
                @Override
                public void renderToolTip(PoseStack matrix, int x, int y) {
                    gui.renderTooltip(matrix, new TranslatableComponent("info." + PrettyPipes.ID + ".populate.description").withStyle(ChatFormatting.GRAY), x, y);
                }
            });
        }

        return buttons;
    }

    @Override
    public void onButtonPacket(IFilteredContainer menu, int id) {
        if (id == 0 && this.canModifyWhitelist) {
            ItemFilterAccessor accessor = (ItemFilterAccessor) this;
            this.isWhitelist = !this.isWhitelist;
            accessor.setModified(true);
            this.save();
        } else if (id == 1 && this.canPopulateFromInventories) {
            List<FluidFilter> filters = this.pipe.getFluidFilters();
            boolean changed = false;
            for (Direction dir : Direction.values()) {
                IFluidHandler handler = this.pipe.getFluidHandler(dir);
                if (handler == null)
                    continue;

                for (int i = 0; i < handler.getTanks(); ++i) {
                    FluidStack stack = handler.getFluidInTank(i);
                    if (stack.isEmpty() || this.isFluidFiltered(stack))
                        continue;

                    ItemStack fluidBucket = FluidUtil.getFilledBucket(stack);
                    for (FluidFilter filter : filters) {
                        if (ItemHandlerHelper.insertItem(filter, fluidBucket, false).isEmpty()) {
                            changed = true;
                            filter.save();
                            break;
                        }
                    }
                }
            }
            if (changed)
                menu.onFilterPopulated();
        }
    }

    public boolean isPipeItemAllowed(ItemStack stack) {
        if (stack.getItem() == ModContent.FLUID_ITEM.get()) {
            return FluidUtil.getFluidContained(stack).map(this::isPipeFluidAllowed).orElse(false);
        }
        return false;
    }

    public boolean isPipeFluidAllowed(FluidStack stack) {
        return this.isFluidFiltered(stack) == this.isWhitelist;
    }

    private boolean isItemFiltered(ItemStack stack) {
        Optional<FluidStack> fluid = FluidUtil.getFluidContained(stack);
        return fluid.filter(this::isFluidFiltered).isPresent();

    }

    private boolean isFluidFiltered(FluidStack fluidStack) {
        for (ItemStackHandler handler : this.pipe.getFluidFilters()) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack filter = handler.getStackInSlot(i);
                Optional<FluidStack> filteredFluid = FluidUtil.getFluidContained(filter);
                if (!filteredFluid.isPresent())
                    continue;

                if (filteredFluid.get().equals(fluidStack)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        // Deprecated deserialization
        if (nbt.contains("fluids")) {
            ListTag fluidList = nbt.getList("fluids", CompoundTag.TAG_COMPOUND);
            int size = Math.min(fluidList.size(), this.getSlots());
            for (int i = 0; i < size; i++) {
                CompoundTag fluidNBT = fluidList.getCompound(i);
                FluidStack fluid = FluidStack.loadFluidStackFromNBT(fluidNBT);
                this.setStackInSlot(i, FluidUtil.getFilledBucket(fluid));
            }
            if (this.canModifyWhitelist) {
                this.isWhitelist = nbt.getBoolean("whitelist");
            }
            ((ItemFilterAccessor) this).setModified(true);
            return;
        }

        super.deserializeNBT(nbt);

    }

    public interface IFluidFilteredContainer extends IFilteredContainer {
        FluidFilter getFilter();

        default void onFilterPopulated() {
        }
    }
}
