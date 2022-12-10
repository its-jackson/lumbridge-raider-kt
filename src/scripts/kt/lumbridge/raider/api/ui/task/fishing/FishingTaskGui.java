/*
 * Created by JFormDesigner on Tue Dec 06 23:38:47 AST 2022
 */

package scripts.kt.lumbridge.raider.api.ui.task.fishing;

import java.awt.event.*;

import scripts.kt.lumbridge.raider.api.ScriptBehavior;
import scripts.kt.lumbridge.raider.api.ScriptDisposal;
import scripts.kt.lumbridge.raider.api.ScriptFishingData;
import scripts.kt.lumbridge.raider.api.ScriptTask;
import scripts.kt.lumbridge.raider.api.behaviors.fishing.FishSpot;
import scripts.kt.lumbridge.raider.api.ui.ScriptTaskGui;

import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Polymorphic
 */
public class FishingTaskGui extends JFrame {
    private final ScriptTaskGui rootFrame;

    private int editIndex;

    public FishingTaskGui(ScriptTaskGui rootFrame) {
        this.rootFrame = rootFrame;
        initComponents();
        Arrays.stream(FishSpot.values()).forEach(fishSpot -> comboBox1.addItem(fishSpot));
        comboBox2.addItem(ScriptDisposal.BANK);
        comboBox2.addItem(ScriptDisposal.DROP);
        comboBox2.addItem(ScriptDisposal.COOK_THEN_BANK);
        comboBox2.addItem(ScriptDisposal.COOK_THEN_DROP);
    }

    public void showFishingAddForm() {
        editIndex = -1;
        comboBox1.setSelectedIndex(0);
        comboBox2.setSelectedIndex(0);
        okButton.setText("Add");
        setVisible(true);
    }

    public void showFishingEditForm(ScriptTask selectedTask, int selectedIndex) {
        if (selectedTask == null || selectedIndex == -1) return;
        if (selectedTask.getFishingData() == null || selectedTask.getDisposal() == null) return;

        editIndex = selectedIndex;
        comboBox1.setSelectedItem(selectedTask.getFishingData().getFishSpot());
        comboBox2.setSelectedItem(selectedTask.getDisposal());
        okButton.setText("Save");
        setVisible(true);
    }

    private void ok(ActionEvent e) {
        if (comboBox1.getSelectedItem() == null || comboBox2.getSelectedItem() == null) return;

        FishSpot fishSpot = (FishSpot) comboBox1.getSelectedItem();
        ScriptDisposal disposal = (ScriptDisposal) comboBox2.getSelectedItem();

        ScriptTask fishingTask = new ScriptTask.Builder()
                .behavior(ScriptBehavior.FISHING)
                .disposal(disposal)
                .fishingData(new ScriptFishingData(fishSpot))
                .build();

        if (editIndex != -1)
            rootFrame.getScriptTaskDefaultListModel().set(editIndex, fishingTask);
        else
            rootFrame.getScriptTaskDefaultListModel().addElement(fishingTask);

        setVisible(false);
    }

    private void cancel(ActionEvent e) {
        setVisible(false);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        dialogPane = new JPanel();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        panel1 = new JPanel();
        label1 = new JLabel();
        comboBox1 = new JComboBox();
        label2 = new JLabel();
        comboBox2 = new JComboBox();

        //======== this ========
        setTitle("Fishing Task");
        setMinimumSize(new Dimension(500, 300));
        setResizable(false);
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

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

            //======== panel1 ========
            {
                panel1.setBorder(new TitledBorder(""));
                panel1.setLayout(new GridBagLayout());

                //---- label1 ----
                label1.setText("Fish Spot");
                panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));
                panel1.add(comboBox1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- label2 ----
                label2.setText("Disposal");
                panel1.add(label2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));
                panel1.add(comboBox2, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(panel1, BorderLayout.CENTER);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel dialogPane;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    private JPanel panel1;
    private JLabel label1;
    private JComboBox comboBox1;
    private JLabel label2;
    private JComboBox comboBox2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
