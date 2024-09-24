package dev.quarris.ppfluids.network;

import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.misc.FluidFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record FluidButtonPayload(
    BlockPos pos,
    int result,
    List<Integer> data
) implements CustomPacketPayload {

    public static final Type<FluidButtonPayload> TYPE = new Type<>(ModRef.res("fluid_button"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidButtonPayload> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, FluidButtonPayload::pos,
        ByteBufCodecs.INT, FluidButtonPayload::result,
        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.INT), FluidButtonPayload::data,
        FluidButtonPayload::new);

    public FluidButtonPayload(BlockPos pos, ButtonResult result, List<Integer> data) {
        this(pos, result.ordinal(), data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void onMessage(final FluidButtonPayload message, IPayloadContext ctx) {
        var player = ctx.player();
        ButtonResult.values()[message.result].action.accept(message.pos, message.data, player);
    }

    public static void sendAndExecute(BlockPos pos, ButtonResult result, int... data) {
        List<Integer> dataList = new ArrayList<>();
        for (int d : data) {
            dataList.add(d);
        }
        PacketDistributor.sendToServer(new FluidButtonPayload(pos, result, dataList));
        result.action.accept(pos, dataList, Minecraft.getInstance().player);
    }

    public enum ButtonResult {
        FILTER_CHANGE((pos, data, player) -> {
            FluidFilter.IFluidFilteredContainer container = (FluidFilter.IFluidFilteredContainer) player.containerMenu;
            FluidFilter filter = container.getFilter();
            filter.onButtonPacket(container, data.get(0));
        });

        public final TriConsumer<BlockPos, List<Integer>, Player> action;

        ButtonResult(TriConsumer<BlockPos, List<Integer>, Player> action) {
            this.action = action;
        }
    }
}
