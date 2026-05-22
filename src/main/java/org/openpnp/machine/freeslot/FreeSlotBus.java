package org.openpnp.machine.freeslot;

import org.openpnp.spi.Actuator;

import java.util.Optional;

import org.openpnp.machine.photon.protocol.Packet;

public class FreeSlotBus {
    private final int fromAddress;
    private final Actuator freeSlotActuator;
    private int packetId;

    public FreeSlotBus(int fromAddress, Actuator freeSlotActuator) {
        this.fromAddress = fromAddress;
        this.freeSlotActuator = freeSlotActuator;
        this.packetId = 0;
    }

    private int nextPacketId() {
        int currentPacketId = packetId;
        packetId = (packetId + 1) % 256;

        return currentPacketId;
    }

    public Optional<Packet> send(Packet commandPacket) throws Exception {
        commandPacket.fromAddress = this.fromAddress;
        commandPacket.packetId = nextPacketId();

        // Send the packet
        String responseString = freeSlotActuator.read(commandPacket.toByteString());

        Optional<Packet> optionalPacket = Packet.decode(responseString);

        if (!optionalPacket.isPresent()) {
            return Optional.empty();
        }

        Packet receivedPacket = optionalPacket.get();

        // Is this our packet?
        if (receivedPacket.packetId != commandPacket.packetId) {
            return Optional.empty();
        }

        return optionalPacket;
    }
}
