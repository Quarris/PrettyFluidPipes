package dev.quarris.ppfluids.registry;

import de.ellpeck.prettypipes.Utility;
import de.ellpeck.prettypipes.items.IModule;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import de.ellpeck.prettypipes.pipe.containers.AbstractPipeContainer;
import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.container.FluidExtractionModuleContainer;
import dev.quarris.ppfluids.container.FluidFilterModuleContainer;
import dev.quarris.ppfluids.container.FluidRetrievalModuleContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MenuSetup {

    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, ModRef.ID);

    public static final DeferredHolder<MenuType<?>, MenuType<FluidFilterModuleContainer>> FLUID_FILTER_CONTAINER = REGISTRY.register("fluid_filter", MenuSetup::createPipeContainer);
    public static final DeferredHolder<MenuType<?>, MenuType<FluidExtractionModuleContainer>> FLUID_EXTRACTION_CONTAINER = REGISTRY.register("fluid_extraction", MenuSetup::createPipeContainer);
    public static final DeferredHolder<MenuType<?>, MenuType<FluidRetrievalModuleContainer>> FLUID_RETRIEVAL_CONTAINER = REGISTRY.register("fluid_retrieval", MenuSetup::createPipeContainer);


    public static void init(IEventBus bus) {
        REGISTRY.register(bus);
    }

    private static <T extends AbstractPipeContainer<?>> MenuType<T> createPipeContainer() {
        return IMenuTypeExtension.create((windowId, inv, data) -> {
            PipeBlockEntity tile = Utility.getBlockEntity(PipeBlockEntity.class, inv.player.level(), data.readBlockPos());
            int moduleIndex = data.readInt();
            ItemStack moduleStack = tile.modules.getStackInSlot(moduleIndex);
            return (T) ((IModule) moduleStack.getItem()).getContainer(moduleStack, tile, windowId, inv, inv.player, moduleIndex);
        });
    }
}
