package dev.quarris.ppfluids.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import de.ellpeck.prettypipes.PrettyPipes;
import de.ellpeck.prettypipes.packets.PacketButton;
import dev.quarris.ppfluids.container.FluidFilterSlot;
import dev.quarris.ppfluids.network.FluidButtonPacket;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import dev.quarris.ppfluids.registry.ItemSetup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

public class FluidFilter extends ItemStackHandler {

    protected ItemStack moduleItem;
    protected FluidPipeBlockEntity pipe;
    public boolean isWhitelist;

    public boolean canPopulateFromTanks;
    public boolean canModifyWhitelist = true;
    private boolean modified;

    public FluidFilter(int size, ItemStack moduleItem, FluidPipeBlockEntity pipe, boolean isDefaultWhitelist) {
        super(size);
        this.pipe = pipe;
        this.moduleItem = moduleItem;
        this.isWhitelist = isDefaultWhitelist;
        this.load();
    }

    public FluidFilter(int size, ItemStack moduleItem, FluidPipeBlockEntity pipe) {
        this(size, moduleItem, pipe, false);
    }

    public List<Slot> createSlots(int x, int y) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < this.getSlots(); ++i) {
            slots.add(new FluidFilterSlot(this, i, x + i % 9 * 18, y + i / 9 * 18));
        }
        return slots;
    }

    @OnlyIn(Dist.CLIENT)
    public List<AbstractWidget> createButtons(final Screen gui, int x, int y) {
        List<AbstractWidget> buttons = new ArrayList<>();
        if (this.canModifyWhitelist) {  // Allowed/Disallowed button
            Supplier<String> whitelistText = () -> "info." + PrettyPipes.ID + "." + (this.isWhitelist ? "whitelist" : "blacklist");
            buttons.add(Button.builder(Component.translatable(whitelistText.get()), button -> {
                    FluidButtonPacket.sendAndExecute(this.pipe.getBlockPos(), FluidButtonPacket.ButtonResult.FILTER_CHANGE, 0);
                    button.setMessage(Component.translatable(whitelistText.get()));
                })
                .bounds(x - 20, y, 20, 20)
                .tooltip(Tooltip.create(Component.translatable(whitelistText.get() + ".description").withStyle(ChatFormatting.GRAY)))
                .build());
        }

        if (this.canPopulateFromTanks) { // Populate button
            buttons.add(Button.builder(Component.translatable("info." + PrettyPipes.ID + ".populate"), button -> FluidButtonPacket.sendAndExecute(this.pipe.getBlockPos(), FluidButtonPacket.ButtonResult.FILTER_CHANGE, 1))
                .bounds(x - 42, y, 20, 20)
                .tooltip(Tooltip.create(Component.translatable("info." + PrettyPipes.ID + ".populate.description").withStyle(ChatFormatting.GRAY)))
                .build());

        }

        return buttons;
    }

    public void onButtonPacket(IFluidFilteredContainer menu, int id) {
        if (id == 0 && this.canModifyWhitelist) {
            this.isWhitelist = !this.isWhitelist;
            this.modified = true;
            this.save();
        } else if (id == 1 && this.canPopulateFromTanks) {
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

                    for (FluidFilter filter : filters) {
                        if (filter.addFilter(stack)) {
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
        if (stack.getItem() == ItemSetup.FLUID_HOLDER.get()) {
            return FluidUtil.getFluidContained(stack).map(this::isPipeFluidAllowed).orElse(false);
        }
        return false;
    }

    public boolean isPipeFluidAllowed(FluidStack stack) {
        return this.isFluidFiltered(stack) == this.isWhitelist;
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

    private boolean addFilter(FluidStack stack) {
        if (this.isFluidFiltered(stack)) { // If already filtered no need to add another one
            return false;
        }

        for (int slot = 0; slot < this.getSlots(); slot++) {
            if (this.getStackInSlot(slot).isEmpty()) {
                this.setFilter(slot, stack);
                this.modified = true;
                return true;
            }
        }

        return false;
    }

    public void setFilter(int slot, FluidStack filter) {
        this.setStackInSlot(slot, filter.isEmpty() ? ItemStack.EMPTY : FluidUtil.getFilledBucket(filter));
        this.modified = true;
    }

    public void save() {
        if (this.modified) {
            this.moduleItem.getOrCreateTag().put("filter", this.serializeNBT());
            this.pipe.setChanged();
            this.modified = false;
        }
    }

    @Override
    protected void onContentsChanged(int slot) {
        this.modified = true;
    }

    public void load() {
        if (this.moduleItem.hasTag() && this.moduleItem.getTag().contains("filter")) {
            this.deserializeNBT(this.moduleItem.getTag().getCompound("filter"));
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT();
        if (this.canModifyWhitelist) {
            nbt.putBoolean("whitelist", this.isWhitelist);
        }

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        if (this.canModifyWhitelist) {
            this.isWhitelist = nbt.getBoolean("whitelist");
        }
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }


    public interface IFluidFilteredContainer {
        FluidFilter getFilter();

        default void onFilterPopulated() {
        }
    }
}
