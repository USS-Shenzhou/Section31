package cn.ussshenzhou.section31.provider.network;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * @author USS_Shenzhou
 */
public class NetworkDataProvider {

    public static final SlidingWindowCollector OUTBOUND_PACKET_SIZE = new SlidingWindowCollector();
    public static final SlidingWindowCollector OUTBOUND_PACKET_COUNT = new SlidingWindowCollector();
    public static final SlidingWindowCollector INBOUND_PACKET_SIZE = new SlidingWindowCollector();
    public static final SlidingWindowCollector INBOUND_PACKET_COUNT = new SlidingWindowCollector();

    public static void encode(Packet<?> packet, int size) {
        OUTBOUND_PACKET_SIZE.put(getType(packet), size);
        OUTBOUND_PACKET_COUNT.put(getType(packet), 1);
    }

    public static void decode(Packet<?> packet, int size) {
        INBOUND_PACKET_SIZE.put(getType(packet), size);
        INBOUND_PACKET_COUNT.put(getType(packet), 1);
    }

    public static Identifier getType(Packet<?> packet) {
        if (packet instanceof ClientboundCustomPayloadPacket(CustomPacketPayload payload)) {
            return payload.type().id();
        } else if (packet instanceof ServerboundCustomPayloadPacket(CustomPacketPayload payload)) {
            return payload.type().id();
        } else {
            return packet.type().id();
        }
    }
}
