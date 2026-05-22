

package org.openpnp.machine.freeslot;

import javax.swing.Action;

import org.apache.commons.io.IOUtils;
import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.photon.protocol.Packet;
import org.openpnp.machine.photon.protocol.PacketBuilder;
import org.openpnp.machine.reference.ReferenceActuator;
import org.openpnp.machine.reference.ReferenceFeeder;
import org.openpnp.machine.reference.driver.GcodeDriver;
import org.openpnp.machine.reference.feeder.AdvancedLoosePartFeeder;
import org.openpnp.model.Configuration;
import org.openpnp.model.Length;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.spi.Actuator;
import org.openpnp.spi.Machine;
import org.openpnp.spi.Nozzle;
import org.openpnp.spi.PropertySheetHolder;
import org.openpnp.spi.Driver;
import org.openpnp.util.UiUtils;
import org.openpnp.vision.pipeline.CvPipeline;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;


public class FreeSlotFeeder extends AdvancedLoosePartFeeder {
    public static final String ACTUATOR_DATA_NAME = "FreeSlotFeederData";
    static String actuatorName = "FREESLOTFEEDER";
    private static FreeSlotBus freeSlotBus;
    
    @Attribute(required = false)
    protected int slotX = 1;

    @Attribute(required = false)
    protected int slotY = 1;

    @Attribute(required = false)
    protected int pitch = 4;

    @Attribute(required = false)
    protected double widthX = 19.7;

    @Attribute(required = false)
    protected double widthY = 11.2;

    public FreeSlotFeeder(){
        super();
        getPipeline().setFeeder(this);
        getTrainingPipeline().setFeeder(this);
    }

    @Override
    public Location getPickLocation() throws Exception {
        return location;
    }

    @Override
    public void feed(Nozzle nozzle) throws Exception {
        Actuator actuator = nozzle.getHead().getActuatorByName(actuatorName);
        if (actuator == null) {
            actuator = Configuration.get().getMachine().getActuatorByName(actuatorName);
        }
        if (actuator == null) {
            throw new Exception("Feed failed. Unable to find an actuator named " + actuatorName);
        }
        actuator.actuate(String.format("%d %d %d", slotX, slotY, pitch));
    }

    public void litUp(){
        int slotAddress = (slotY << 4) | slotX;
        int[] data = new int[] {slotAddress, 0x0F, 0xFF, 0xFF, 0xFF};
        sendCommand(0x10, 0x55, data);
    }

    public void litUp(int a, int r, int g, int b){
        int slotAddress = (slotY << 4) | slotX;
        int alpha = a / 0x0F; // Convert from 0-255 to 0-15
        int[] data = new int[] { slotAddress, alpha, r, g, b };
        sendCommand(0x10, 0x55, data);
    }

    public void unlit() {
        int[] data = new int[] { 0xFF, 0x00, 0x00, 0x00, 0x00 };
        sendCommand(0x10, 0x55, data);
    }

    public static void litAll(){
        int[] data = new int[] {0xFF, 0x0F, 0xFF, 0xFF, 0xFF};
        sendCommand(0x10, 0x55, data);
    }

    public static void litAll(int a, int r, int g, int b){
        int alpha = a / 0x0F; // Convert from 0-255 to 0-15
        int[] data = new int[] { 0xFF, alpha, r, g, b };
        sendCommand(0x10, 0x55, data);
    }

    public static void sendCommand(int commandId, int toAddress, int[] data){
        // ? Packet to light up the slot 2, 2:
        // ? 55 00 00 06 34 10 22 0F FF FF FF
        UiUtils.submitUiMachineTask(() -> {
            FreeSlotBus bus = getBus();
            PacketBuilder packet = PacketBuilder.command(commandId, toAddress);
            for(int dataByte : data){
                packet.putByte(dataByte);
            }
            bus.send(packet.toPacket());
        });
    }

    public static void setBus(FreeSlotBus bus) {
        freeSlotBus = bus;
    }
    
    public static FreeSlotBus getBus() {
        populateFreeSlotBus();
        return freeSlotBus;
    }

    private static void populateFreeSlotBus() {
        if (freeSlotBus != null) {
            return;
        }

        freeSlotBus = new FreeSlotBus(0, getDataActuator());
    }

    static Actuator getDataActuator() {
        Machine machine = Configuration.get().getMachine();

        Actuator actuator = machine.getActuatorByName(ACTUATOR_DATA_NAME);

        if (actuator == null) {
            actuator = createDefaultActuator(machine);
        }

        return actuator;
    }

    private static Actuator createDefaultActuator(Machine machine) {
        Actuator actuator;
        actuator = new ReferenceActuator();
        actuator.setName(ACTUATOR_DATA_NAME);

        for (Driver driver : machine.getDrivers()) {
            if(! (driver instanceof GcodeDriver)) {
                continue;
            }
            GcodeDriver gcodeDriver = (GcodeDriver) driver;
            gcodeDriver.setCommand(actuator, GcodeDriver.CommandType.ACTUATOR_READ_COMMAND, "M485 {Value}");
            gcodeDriver.setCommand(actuator, GcodeDriver.CommandType.ACTUATOR_READ_REGEX, "rs485-reply: (?<Value>.*)");
            break;  // Only set this on 1 GCodeDriver
        }

        try {
            machine.addActuator(actuator);
        } catch (Exception exception) {
            exception.printStackTrace(); // TODO Probably need to log this, figure out why it can happen first
        }
        return actuator;
    }

	@Override
    public Wizard getConfigurationWizard() {
        return new FreeSlotFeederConfigurationWizard(this);
    }

    @Override
    public String getPropertySheetHolderTitle() {
        return getClass().getSimpleName() + " " + getName();
    }

    @Override
    public PropertySheetHolder[] getChildPropertySheetHolders() {
        return null;
    }

    @Override
    public Action[] getPropertySheetHolderActions() {
        return null;
    }
    
    public int getSlotX() {
        return slotX;
    }

    public void setSlotX(int slotX) {
        this.slotX = slotX;
        firePropertyChange("slotX", null, slotX);
    }

    public int getSlotY() {
        return slotY;
    }

    public void setSlotY(int slotY) {
        this.slotY = slotY;
        firePropertyChange("slotY", null, slotY);
    }

    public int getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
        firePropertyChange("pitch", null, pitch);
    }

    public double getWidthX() {
        return widthX;
    }

    public void setWidthX(double widthX) {
        this.widthX = widthX;
        firePropertyChange("widthX", null, widthX);
    }

    public double getWidthY() {
        return widthY;
    }

    public void setWidthY(double widthY) {
        this.widthY = widthY;
        firePropertyChange("widthY", null, widthY);
    }
}
