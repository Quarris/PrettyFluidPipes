package dev.quarris.ppfluids;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ModRef {

    public static final String ID = "ppfluids";

    public static ResourceLocation res(String name) {
        return new ResourceLocation(ID, name);
    }

    public static class Capabilities {
        public static final Capability<IFluidHandler> FLUID = CapabilityManager.get(new CapabilityToken<>() {});
    }
}
