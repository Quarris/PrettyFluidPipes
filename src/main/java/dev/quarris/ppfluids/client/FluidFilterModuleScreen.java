package dev.quarris.ppfluids.client;

import de.ellpeck.prettypipes.pipe.containers.AbstractPipeGui;
import dev.quarris.ppfluids.container.FluidFilterModuleContainer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class FluidFilterModuleScreen extends AbstractPipeGui<FluidFilterModuleContainer> {

    public FluidFilterModuleScreen(FluidFilterModuleContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    protected void init() {
        super.init();
        for (AbstractWidget button : this.menu.getFilter().getButtons(this, this.leftPos + this.imageWidth - 7, this.topPos + 17 + 32 + 20, false)) {
            this.addRenderableWidget(button);
        }

        this.addRenderableWidget(this.menu.getSelector().getButton(this.leftPos + 7, this.topPos + 17 + 32 + 20));
    }
}
