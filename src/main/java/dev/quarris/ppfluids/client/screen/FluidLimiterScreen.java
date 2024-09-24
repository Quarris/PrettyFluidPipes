package dev.quarris.ppfluids.client.screen;

import de.ellpeck.prettypipes.pipe.containers.AbstractPipeGui;
import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.container.FluidLimiterContainer;
import dev.quarris.ppfluids.item.FluidLimiterModuleItem;
import dev.quarris.ppfluids.network.FluidButtonPayload;
import dev.quarris.ppfluids.registry.DataComponentSetup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.function.Function;
import java.util.function.Supplier;

public class FluidLimiterScreen extends AbstractPipeGui<FluidLimiterContainer> {

    public FluidLimiterScreen(FluidLimiterContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void init() {
        super.init();
        var textField = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 7, this.topPos + 17 + 32 + 10, 100, 20, Component.translatable("info." + ModRef.ID + ".max_stack_size")) {
            @Override
            public void insertText(String textToWrite) {
                var ret = new StringBuilder();
                for (var c : textToWrite.toCharArray()) {
                    if (Character.isDigit(c))
                        ret.append(c);
                }
                super.insertText(ret.toString());
            }

        });
        var data = this.menu.moduleStack.getOrDefault(DataComponentSetup.FLUID_LIMITER, FluidLimiterModuleItem.LimiterData.DEFAULT);
        textField.setValue(String.valueOf(data.maxAmount()));
        textField.setMaxLength(10);
        textField.setResponder(s -> {
            if (s.isEmpty())
                return;
            var amount = Integer.parseInt(s);
            FluidButtonPayload.sendAndExecute(this.menu.tile.getBlockPos(), FluidButtonPayload.ButtonResult.LIMITER_AMOUNT, amount);
        });
        Function<FluidLimiterModuleItem.LimiterData, Component> buttonText = d -> Component.literal(d.useBucketMeasure() ? "B" : "mB");
        Function<FluidLimiterModuleItem.LimiterData, Component> buttonTooltip = d -> Component.translatable("info." + ModRef.ID + ".limit_buckets_" + (d.useBucketMeasure() ? "on" : "off"));

        this.addRenderableWidget(Button.builder(buttonText.apply(data), b -> {
                FluidButtonPayload.sendAndExecute(this.menu.tile.getBlockPos(), FluidButtonPayload.ButtonResult.LIMITER_MEASURE);
                var limiterData = this.menu.moduleStack.getOrDefault(DataComponentSetup.FLUID_LIMITER, FluidLimiterModuleItem.LimiterData.DEFAULT);
                b.setMessage(buttonText.apply(limiterData));
                b.setTooltip(Tooltip.create(buttonTooltip.apply(limiterData)));
            }).tooltip(Tooltip.create(buttonTooltip.apply(data)))
            .bounds(this.leftPos + 7 + 100 + 4, this.topPos + 17 + 32 + 10, 20, 20).build());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(this.font, Component.translatable("info." + ModRef.ID + ".limited_amount"), 7, 17 + 32, 4210752, false);
    }
}
