package org.openpnp.vision.pipeline.stages;

import java.awt.Color;

import org.openpnp.machine.photon.PhotonFeeder;
import org.openpnp.machine.photon.protocol.PacketBuilder;
import org.openpnp.machine.photon.protocol.PhotonBusInterface;
import org.openpnp.util.UiUtils;
import org.openpnp.vision.pipeline.CvPipeline;
import org.openpnp.vision.pipeline.CvStage;
import org.openpnp.vision.pipeline.Stage;
import org.openpnp.vision.pipeline.stages.convert.ColorConverter;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.convert.Convert;

@Stage(description = "Manage FreeSlotFeeder slot light. Machine must be connected, otherwise error is thrown.")
public class PhotonSlotLight extends CvStage{

    @Attribute(required = false)
    private boolean lit = true;

    @Element(required = false)
    @Convert(ColorConverter.class)
    private Color color = new Color(0xffffff);

    @Attribute(required = false)
    private boolean allSlots = false;

    @Attribute(required = false)
    private int address = 85;

    @Attribute(required = false)
    private int slotX = 1;
    
    @Attribute(required = false)
    private int slotY = 1;

    public boolean getLit() {
        return lit;
    }

    public void setLit(boolean lit) {
        this.lit = lit;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean getAllSlots() {
        return allSlots;
    }

    public void setAllSlots(boolean allSlots) {
        this.allSlots = allSlots;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getSlotX() {
        return slotX;
    }
    
    public void setSlotX(int slotX) {
        this.slotX = slotX;
    }

    public int getSlotY() {
        return slotY;
    }

    public void setSlotY(int slotY) {
        this.slotY = slotY;
    }

    @Override
    public Result process(CvPipeline pipeline) throws Exception{
        if(lit){
            if(allSlots){
                setLight(address, 0x0F, 0x0F, color);
            }
            else{
                setLight(address, slotX, slotY, color);
            }
        }
        else{
            unlit(address, slotX, slotY);
        }

        return null;
    }
    
    private static void setLight(int address, int x, int y, Color color){
        int slotAddress = (y << 4) | x;
        int alpha = convertAlpha(color.getAlpha());
        int[] data = {slotAddress, alpha, color.getRed(), color.getGreen(), color.getBlue()};
        sendCommand(0x10, address, data);
    }
    
    private static void unlit(int address, int x, int y){
        int[] data = {0xFF, 0x00, 0x00, 0x00, 0x00};
        sendCommand(0x10, address, data);
    }
    
    private static PhotonBusInterface getBus(){
        return PhotonFeeder.getBus();
    }
    
    private static void sendCommand(int commandId, int toAddress, int[] data){
        // ? Packet to light up the slot 2, 2
        // ? 55 00 00 06 34 10 22 0F FF FF FF
        UiUtils.submitUiMachineTask(() -> {
            PhotonBusInterface bus = getBus();
            PacketBuilder packet = PacketBuilder.command(commandId, toAddress);
            for(int dataByte : data){
                packet.putByte(dataByte);
            }
            bus.send(packet.toPacket());
        });
    }

    private static int convertAlpha(int a){
        double map = a / 255.0 * 15.0;
        return (int) map;
    }
}