/*
 * Created by JFormDesigner on Wed Dec 07 18:47:25 AST 2022
 */

package scripts.kt.lumbridge.raider.api.ui.task.prayer;

import org.tribot.script.sdk.Inventory;
import scripts.kotlin.api.ResourceGainedCondition;
import scripts.kt.lumbridge.raider.api.ScriptBehavior;
import scripts.kt.lumbridge.raider.api.ScriptPrayerData;
import scripts.kt.lumbridge.raider.api.ScriptTask;
import scripts.kt.lumbridge.raider.api.ui.ScriptTaskGui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Polymorphic
 */
public class PrayerTaskGui extends JFrame {
    private final ScriptTaskGui rootFrame;

    private int editIndex;

    private final ButtonGroup patternGroup = new ButtonGroup();

    public PrayerTaskGui(ScriptTaskGui rootFrame) {
        this.rootFrame = rootFrame;
        initComponents();

        patternGroup.add(radioButton1);
        patternGroup.add(radioButton2);
        patternGroup.add(radioButton3);
        patternGroup.add(radioButton4);
        patternGroup.add(radioButton5);
    }

    public void showPrayerAddForm() {
        textField1.setText("");
        textField2.setText("");
        patternGroup.clearSelection();
        editIndex = -1;
        okButton.setText("Add");
        setVisible(true);
    }

    public void showPrayerEditForm(ScriptTask selectedTask, int selectedIndex) {
        if (selectedTask == null || selectedIndex == -1) return;
        if (selectedTask.getResourceGainedCondition() == null) return;
        if (selectedTask.getPrayerData() == null) return;

        ResourceGainedCondition gainedCondition = selectedTask.getResourceGainedCondition();
        int id = gainedCondition.getId();
        int amount = gainedCondition.getAmount();

        textField1.setText(String.valueOf(id));
        textField2.setText(String.valueOf(amount));
        patternGroup.clearSelection();
        editIndex = selectedIndex;
        okButton.setText("Save");

        Inventory.DropPattern pattern = selectedTask.getPrayerData().getBuryPattern();

        if (pattern == null)
            radioButton5.setSelected(true);
        else if (pattern == Inventory.DropPattern.TOP_TO_BOTTOM)
            radioButton1.setSelected(true);
        else if (pattern == Inventory.DropPattern.TOP_TO_BOTTOM_ZIGZAG)
            radioButton2.setSelected(true);
        else if (pattern == Inventory.DropPattern.LEFT_TO_RIGHT)
            radioButton3.setSelected(true);
        else
            radioButton4.setSelected(true);

        setVisible(true);
    }

    private ScriptPrayerData getPrayerData() {
        if (radioButton1.isSelected())
            return new ScriptPrayerData(Inventory.DropPattern.TOP_TO_BOTTOM);
        else if (radioButton2.isSelected())
            return new ScriptPrayerData(Inventory.DropPattern.TOP_TO_BOTTOM_ZIGZAG);
        else if (radioButton3.isSelected())
            return new ScriptPrayerData(Inventory.DropPattern.LEFT_TO_RIGHT);
        else if (radioButton4.isSelected())
            return new ScriptPrayerData(Inventory.DropPattern.ZIGZAG);
        else if (radioButton5.isSelected())
            return new ScriptPrayerData(null);
        else
            return null;
    }

    private void ok(ActionEvent e) {
        String bonesIdStr = textField1.getText()
                .trim();
        String boneAmountStr = textField2.getText()
                .trim();

        int bonesId, boneAmount = -1;

        try {
            bonesId = Integer.parseInt(bonesIdStr);
        } catch (NumberFormatException formatException) {
            return;
        }
        try {
            boneAmount = Integer.parseInt(boneAmountStr);
        } catch (NumberFormatException formatException) {
            if (!boneAmountStr.isEmpty()) return;
        }

        ScriptPrayerData prayerData = getPrayerData();

        if (prayerData == null) return;

        ScriptTask prayerTask = new ScriptTask.Builder()
                .behavior(ScriptBehavior.PRAYER)
                .stopCondition(new ResourceGainedCondition(bonesId, boneAmount))
                .prayerData(prayerData)
                .build();

        if (editIndex != -1)
            rootFrame.getScriptTaskDefaultListModel().set(editIndex, prayerTask);
        else
            rootFrame.getScriptTaskDefaultListModel().addElement(prayerTask);

        setVisible(false);
    }

    private void cancel(ActionEvent e) {
        setVisible(false);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        label1 = new JLabel();
        textField1 = new JTextField();
        label2 = new JLabel();
        textField2 = new JTextField();
        label3 = new JLabel();
        radioButton1 = new JRadioButton();
        radioButton2 = new JRadioButton();
        radioButton3 = new JRadioButton();
        radioButton4 = new JRadioButton();
        radioButton5 = new JRadioButton();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        label4 = new JLabel();

        //======== this ========
        setTitle("Prayer Task");
        setMinimumSize(new Dimension(400, 300));
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setBorder(new TitledBorder(""));
                contentPanel.setLayout(new GridBagLayout());

                //---- label1 ----
                label1.setText("Bones ID");
                contentPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));
                contentPanel.add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                //---- label2 ----
                label2.setText("Amount To Bone ");
                contentPanel.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));
                contentPanel.add(textField2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                //---- label3 ----
                label3.setText("Boning Pattern");
                contentPanel.add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                //---- radioButton1 ----
                radioButton1.setText("Top To Bottom");
                contentPanel.add(radioButton1, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                //---- radioButton2 ----
                radioButton2.setText("Top To Bottom Zigzag");
                contentPanel.add(radioButton2, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                //---- radioButton3 ----
                radioButton3.setText("Left To Right");
                contentPanel.add(radioButton3, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                //---- radioButton4 ----
                radioButton4.setText("Zigzag");
                contentPanel.add(radioButton4, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                //---- radioButton5 ----
                radioButton5.setText("Antiban Props");
                contentPanel.add(radioButton5, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[]{0, 85, 80};
                ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[]{1.0, 0.0, 0.0};

                //---- okButton ----
                okButton.setText("OK");
                okButton.addActionListener(e -> ok(e));
                buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setText("Cancel");
                cancelButton.addActionListener(e -> cancel(e));
                buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);

            //---- label4 ----
            label4.setText("Tip: leave amount blank, or -1, for until empty.");
            dialogPane.add(label4, BorderLayout.NORTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel label1;
    private JTextField textField1;
    private JLabel label2;
    private JTextField textField2;
    private JLabel label3;
    private JRadioButton radioButton1;
    private JRadioButton radioButton2;
    private JRadioButton radioButton3;
    private JRadioButton radioButton4;
    private JRadioButton radioButton5;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    private JLabel label4;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
