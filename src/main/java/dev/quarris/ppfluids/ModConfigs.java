package dev.quarris.ppfluids;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfigs {

    public static ModConfigSpec.BooleanValue dropFluidContainers;

    public static ModConfigSpec register(ModConfigSpec.Builder builder) {
        dropFluidContainers = builder.comment(
                "Set to true if you want the fluid to drop fluid containers when there is no way to store the fluid in the network.",
                "Enabling this might cause high number of item entities to spawn accidentally by users."
        ).define("dropFluid", false);
        return builder.build();
    }
}
