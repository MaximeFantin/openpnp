

package org.openpnp.machine.freeslot;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.awt.Color;
import java.awt.Font;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.openpnp.gui.MainFrame;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.components.LocationButtonsPanel;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.gui.support.MutableLocationProxy;
import org.openpnp.gui.support.DoubleConverter;
import org.openpnp.gui.support.Icons;
import org.openpnp.machine.reference.feeder.wizards.AbstractReferenceFeederConfigurationWizard;
import org.openpnp.machine.reference.feeder.wizards.AdvancedLoosePartFeederConfigurationWizard;
import org.openpnp.model.Configuration;
import org.openpnp.model.Location;
import org.openpnp.spi.Camera;
import org.openpnp.spi.Feeder;
import org.openpnp.spi.Machine;
import org.openpnp.util.MovableUtils;
import org.openpnp.util.UiUtils;
import org.openpnp.util.Utils2D;
import org.openpnp.util.VisionUtils;
import org.openpnp.vision.pipeline.CvPipeline;
import org.openpnp.vision.pipeline.ui.CvPipelineEditor;
import org.openpnp.vision.pipeline.ui.CvPipelineEditorDialog;
import org.openpnp.Translations;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class FreeSlotFeederConfigurationWizard
        extends AdvancedLoosePartFeederConfigurationWizard {
    private final FreeSlotFeeder feeder;

    private JPanel panelFreeSlotFeederConfig;
    private JLabel lblNewLabel_5;
    private JLabel lblNewLabel_6;
    private JLabel lblNewLabel_7;
    private JLabel lblNewLabel_8;
    private JLabel lblNewLabel_9;
    private JLabel lblNewLabel_10;
    private JLabel lblNewLabel_11;
    private JTextField slotX;
    private JTextField slotY;
    private JTextField pitch;
    private JTextField widthX;
    private JTextField widthY;

    public FreeSlotFeederConfigurationWizard(FreeSlotFeeder feeder) {
        super(feeder);
        this.feeder = feeder;
        createUi();
    }

    private void createUi() {
        
        panelFreeSlotFeederConfig = new JPanel();
        panelFreeSlotFeederConfig.setBorder(new TitledBorder(null, "Free Slot Feeder Config", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        contentPanel.add(panelFreeSlotFeederConfig);
        panelFreeSlotFeederConfig.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,},
            new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,}));

        lblNewLabel_7 = new JLabel("X");
        panelFreeSlotFeederConfig.add(lblNewLabel_7, "4, 2, center, default");
        lblNewLabel_8 = new JLabel("Y");
        panelFreeSlotFeederConfig.add(lblNewLabel_8, "6, 2, center, default");

        lblNewLabel_5 = new JLabel("Slot");
        panelFreeSlotFeederConfig.add(lblNewLabel_5, "2, 4, right, default");
        
        slotX = new JTextField();
        panelFreeSlotFeederConfig.add(slotX, "4, 4");
        slotX.setColumns(10);
        
        slotY = new JTextField();
        panelFreeSlotFeederConfig.add(slotY, "6, 4");
        slotY.setColumns(10);
        
        lblNewLabel_6 = new JLabel("Pitch");
        panelFreeSlotFeederConfig.add(lblNewLabel_6, "2, 6, right, default");
        
        pitch = new JTextField();
        panelFreeSlotFeederConfig.add(pitch, "4, 6, left, default");
        pitch.setColumns(10);

        JButton btnTest = new JButton("Test");
        panelFreeSlotFeederConfig.add(btnTest, "6, 6, left, default");
        btnTest.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                feeder.litUp();
            }
        });


        // Config UI
        lblNewLabel_9 = new JLabel("X");
        panelFreeSlotFeederConfig.add(lblNewLabel_9, "10, 2, center, default");
        
        lblNewLabel_10 = new JLabel("Y");
        panelFreeSlotFeederConfig.add(lblNewLabel_10, "12, 2, center, default");

        lblNewLabel_11 = new JLabel("   Width");
        panelFreeSlotFeederConfig.add(lblNewLabel_11, "8, 4, right, default");

        widthX = new JTextField();
        panelFreeSlotFeederConfig.add(widthX, "10, 4, left, default");
        widthX.setColumns(10);

        widthY = new JTextField();
        panelFreeSlotFeederConfig.add(widthY, "12, 4, left, default");
        widthY.setColumns(10);


        // Center camera button
        // Important pour gérer l'emplacement de la caméra par rapport au slot
        //FreeSlotLocationButtonPanel locationButtonsPanel = new FreeSlotLocationButtonPanel(textFieldLocationX, textFieldLocationY, textFieldLocationZ, textFieldLocationC);
        //panelLocation.add(locationButtonsPanel, "10, 4");
    }

    @Override
    public void createBindings() {
        super.createBindings();

        IntegerConverter intConverter = new IntegerConverter();
        DoubleConverter doubleConverter = new DoubleConverter(Configuration.get().getLengthDisplayFormat());
        
        addWrappedBinding(feeder, "slotX", slotX, "text", intConverter);
        ComponentDecorators.decorateWithAutoSelect(slotX);

        addWrappedBinding(feeder, "slotY", slotY, "text", intConverter);
        ComponentDecorators.decorateWithAutoSelect(slotY);

        addWrappedBinding(feeder, "pitch", pitch, "text", intConverter);
        ComponentDecorators.decorateWithAutoSelect(pitch);

        addWrappedBinding(feeder, "widthX", widthX, "text", doubleConverter);
        ComponentDecorators.decorateWithAutoSelect(widthX);

        addWrappedBinding(feeder, "widthY", widthY, "text", doubleConverter);
        ComponentDecorators.decorateWithAutoSelect(widthY);
    }
}
