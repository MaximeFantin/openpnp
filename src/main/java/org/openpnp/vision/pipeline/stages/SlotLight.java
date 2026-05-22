package org.openpnp.vision.pipeline.stages;

import java.awt.Color;

import org.openpnp.machine.freeslot.FreeSlotFeeder;
import org.openpnp.vision.pipeline.CvPipeline;
import org.openpnp.vision.pipeline.CvStage;
import org.openpnp.vision.pipeline.Stage;
import org.openpnp.vision.pipeline.stages.convert.ColorConverter;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.convert.Convert;

@Stage(description = "Manage FreeSlotFeeder slot light. Machine must be connected, otherwise error is thrown.")
public class SlotLight extends CvStage{

    @Attribute(required = false)
    private boolean lit = true;

    @Element(required = false)
    @Convert(ColorConverter.class)
    private Color color = new Color(0xffffff);

    @Attribute(required = false)
    private boolean allSlots = false;

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

    @Override
    public Result process(CvPipeline pipeline) throws Exception{

        if(pipeline.getFeeder() instanceof FreeSlotFeeder) {
            FreeSlotFeeder feeder = (FreeSlotFeeder) pipeline.getFeeder();
            if (lit) {
                if(allSlots){
                    if(color == null){
                        FreeSlotFeeder.litAll();
                    }
                    else{
                        FreeSlotFeeder.litAll(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
                    }
                }
                else{
                    if(color == null){
                        feeder.litUp();
                    }
                    else{
                        feeder.litUp(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
                    }
                }
            }
            else {
                feeder.unlit();
            }
        }

        return null;
    }

    
}
