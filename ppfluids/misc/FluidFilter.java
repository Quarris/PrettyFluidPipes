package old.dev.quarris.ppfluids.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import de.ellpeck.prettypipes.PrettyPipes;
import de.ellpeck.prettypipes.packets.PacketButton;
import old.dev.quarris.ppfluids.ModContent;
import old.dev.quarris.ppfluids.container.FluidFilterSlot;
import old.dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
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
    //protected NonNullList<FluidStack> filters;

    public boolean canPopulateFromTanks;
    public boolean canModifyWhitelist = true;
    private boolean modified;

    public FluidFilter(int size, ItemStack moduleItem, FluidPipeBlockEntity pipe, boolean isDefaultWhitelist) {
        super(size);
        this.pipe = pipe;
        this.moduleItem = moduleItem;
        this.isWhitelist = isDefaultWhitelist;
        //this.filters = NonNullList.withSize(size, FluidStack.EMPTY);
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
            Supplier<Component> whitelistText = () -> Component.translatable("info.prettypipes." + (this.isWhitelist ? "whitelist" : "blacklist"));
            buttons.add(new Button(x - 20, y, 20, 20, whitelistText.get(), button -> {
                PacketButton.sendAndExecute(this.pipe.getBlockPos(), PacketButton.ButtonResult.FILTER_CHANGE, 0);
                button.setMessage(whitelistText.get());
            }) {
                @Override
                public void renderToolTip(PoseStack matrix, int x, int y) {
                    gui.renderTooltip(matrix, Component.translatable(whitelistText.get() + ".description").withStyle(ChatFormatting.GRAY), x, y);
                }
            });
        }

        if (this.canPopulateFromTanks) { // Populate button
            buttons.add(new Button(x - 42, y, 20, 20, Component.translatable("info." + PrettyPipes.ID + ".populate"), button -> PacketButton.sendAndExecute(this.pipe.getBlockPos(), PacketButton.ButtonResult.FILTER_CHANGE, 1)) {
                @Override
                public void renderToolTip(PoseStack matrix, int x, int y) {
                    gui.renderTooltip(matrix, Component.translatable("info." + PrettyPipes.ID + ".populate.description").withStyle(ChatFormatting.GRAY), x, y);
                }
            });
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

    /*public void setFilter(int slot, FluidStack filter) {
        this.filters.set(slot, filter == null ? FluidStack.EMPTY : filter);
        this.setStackInSlot(slot, filter == null ? ItemStack.EMPTY : FluidUtil.getFilledBucket(filter));
        this.modified = true;
    }*/

    /*public FluidStack getFilter(int slot) {
        return this.filters.get(slot);
    }*/

    /*public int size() {
        return this.filters.size();
    }*/

    public void save() {
        if (this.modified) {
            this.moduleItem.getOrCreateTag().put("filter", this.serializeNBT());
            this.modified = false;
        }
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

    /*public void setModified(boolean modified) {
        this.modified = modified;
    }*/

    public interface IFluidFilteredContainer {
        FluidFilter getFilter();

        default void onFilterPopulated() {
        }
    }
}
