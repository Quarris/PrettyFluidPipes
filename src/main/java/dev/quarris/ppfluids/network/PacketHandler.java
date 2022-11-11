package dev.quarris.ppfluids.network;

import dev.quarris.ppfluids.ModRef;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class PacketHandler {
    private static final String VERSION = "1";
    private static SimpleChannel channel = NetworkRegistry.newSimpleChannel(
            ModRef.res("channel"),
            () -> VERSION,
            VERSION::equals,
            VERSION::equals
    );

    public static void init() {
        channel.registerMessage(0, FluidButtonPacket.class, FluidButtonPacket::encode, FluidButtonPacket::decode, FluidButtonPacket::handle);
    }

    public static void sendToAllLoaded(Level level, BlockPos pos, Object message) {
        channel.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), message);
    }

    public static void sendTo(Player player, Object message) {
        channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), message);
    }

    public static void sendToServer(Object message) {
        channel.send(PacketDistributor.SERVER.noArg(), message);
    }
}