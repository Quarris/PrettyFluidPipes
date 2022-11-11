package dev.quarris.ppfluids.client;

import de.ellpeck.prettypipes.pipe.containers.AbstractPipeGui;
import dev.quarris.ppfluids.container.FluidExtractionModuleContainer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class FluidExtractionModuleScreen extends AbstractPipeGui<FluidExtractionModuleContainer> {

    public FluidExtractionModuleScreen(FluidExtractionModuleContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void init() {
        super.init();
        for (AbstractWidget button : this.menu.getFilter().createButtons(this, this.leftPos + this.imageWidth - 7, this.topPos + 17 + 32 + 20)) {
            this.addRenderableWidget(button);
        }

        this.addRenderableWidget(this.menu.getSelector().getButton(this.leftPos + 7, this.topPos + 17 + 32 + 20));
    }
}
