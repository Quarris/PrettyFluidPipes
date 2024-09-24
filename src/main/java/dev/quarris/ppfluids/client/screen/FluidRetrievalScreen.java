package dev.quarris.ppfluids.client.screen;

import de.ellpeck.prettypipes.pipe.containers.AbstractPipeGui;
import dev.quarris.ppfluids.container.FluidRetrievalContainer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FluidRetrievalScreen extends AbstractPipeGui<FluidRetrievalContainer> {

    public FluidRetrievalScreen(FluidRetrievalContainer screenContainer, Inventory inv, Component titleIn) {
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
