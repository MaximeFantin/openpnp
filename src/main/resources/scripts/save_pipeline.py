from java.awt.event import ActionListener
from javax.swing import JDialog, JLabel, JComboBox, JButton
from javax.swing.JOptionPane import showMessageDialog
from com.jgoodies.forms.layout import FormLayout, FormSpecs

from org.openpnp.gui import MainFrame
from org.openpnp.model import Configuration

import xml.etree.ElementTree as ET


class SavePipelineDialog(JDialog):
    def __init__(self):
        super(SavePipelineDialog, self).__init__(MainFrame.get(), "Save Pipeline", True)

        self.feeder = MainFrame.get().getFeedersTab().getSelection()
        self.pipeline = self.feeder.getPipeline()
        try:
            self.trainingPipeline = self.feeder.getTrainingPipeline()
        except:
            self.trainingPipeline = None

        self.getContentPane().setLayout(FormLayout([
            FormSpecs.RELATED_GAP_COLSPEC,
            FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC,
            FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC,
            FormSpecs.DEFAULT_COLSPEC,
        ],
        [
            FormSpecs.RELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC,
        ]))

        self.lblName = JLabel("Name")
        self.getContentPane().add(self.lblName, "2, 2, right, default")

        self.valueName = JComboBox()
        self.getContentPane().add(self.valueName, "4, 2")
        SaveManager.loadFile()
        for i in SaveManager.pipelines:
            self.valueName.addItem(i)
        self.valueName.setPrototypeDisplayValue("long text for combo box")
        self.valueName.setEditable(True)
        self.valueName.setSelectedItem("")
        #self.valueName.setSelectedItem(self.feeder.getName())

        class SaveAction(ActionListener):
            def __init__(self, parent):
                self.parent = parent

            def actionPerformed(self, e):
                name = self.parent.valueName.getSelectedItem()
                if not name:
                    showMessageDialog(self.parent, "Please enter a name")
                    return

                pipeline = self.parent.pipeline
                if self.parent.trainingPipeline and self.parent.selectPipelineBtn.getSelectedItem() == "Training pipeline":
                    pipeline = self.parent.trainingPipeline

                SaveManager.savePipeline(name, pipeline)
                self.parent.setVisible(False)

        self.saveBtn = JButton("Save")
        self.saveBtn.addActionListener(SaveAction(self))
        self.getContentPane().add(self.saveBtn, "6, 2")

        class CancelAction(ActionListener):
            def __init__(self, parent):
                self.parent = parent

            def actionPerformed(self, e):
                self.parent.setVisible(False)

        self.cancelBtn = JButton("Cancel")
        self.cancelBtn.addActionListener(CancelAction(self))
        self.getContentPane().add(self.cancelBtn, "6, 4")

        if self.trainingPipeline:
            self.targetLabel = JLabel("Target")
            self.getContentPane().add(self.targetLabel, "2, 4, right, default")

            self.selectPipelineBtn = JComboBox(["Feed pipeline", "Training pipeline"])
            self.getContentPane().add(self.selectPipelineBtn, "4, 4")

        self.pack()
        self.setLocationRelativeTo(MainFrame.get())


class SaveManager:
    SAVE_FILE = Configuration.get().getConfigurationDirectory().getAbsolutePath() + "/pipelines.xml"
    pipelines = {}
    file = None

    @staticmethod
    def loadFile():
        with open(SaveManager.SAVE_FILE, "r+") as file:
            content = file.read()
            if not content:
                file.write("<saves></saves>")
        
        SaveManager.file = ET.ElementTree(file=SaveManager.SAVE_FILE)
        for element in SaveManager.file.findall("save"):
            SaveManager.pipelines[element.attrib["name"]] = element
    
    @staticmethod
    def savePipeline(name, pipeline):
        SaveManager.loadFile()
        SaveManager.pipelines[name] = ET.fromstring("<save name='" + name + "'>" + pipeline.toXmlString() + "</save>")
        xml = ET.fromstring("<saves></saves>")
        for element in SaveManager.pipelines.values():
            xml.append(element)
        tree = ET.ElementTree()
        tree._setroot(xml)
        tree.write(SaveManager.SAVE_FILE)

    @staticmethod
    def loadPipeline(name):
        pipeline = MainFrame.get().getFeedersTab().getSelection().getPipeline()
        xml = ET.tostring(SaveManager.pipelines[name].find("cv-pipeline"))
        pipeline.fromXmlString(xml)

    @staticmethod
    def deletePipeline(name):
        del SaveManager.pipelines[name]
        xml = ET.fromstring("<saves></saves>")
        for element in SaveManager.pipelines.values():
            xml.append(element)
        tree = ET.ElementTree()
        tree._setroot(xml)
        tree.write(SaveManager.SAVE_FILE)


hasFeeder = True
hasPipeline = True
try:
    feeder = MainFrame.get().getFeedersTab().getSelection()
except:
    showMessageDialog(None, "No feeder selected")
    hasFeeder = False

if hasFeeder:
    try:
        pipeline = feeder.getPipeline()
    except:
        showMessageDialog(None, "Selected feeder has no pipeline")
        hasPipeline = False


if hasFeeder and hasPipeline:
    SavePipelineDialog().setVisible(True)

