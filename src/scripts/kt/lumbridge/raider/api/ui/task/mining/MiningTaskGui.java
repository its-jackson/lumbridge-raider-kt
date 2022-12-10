/*
 * Created by JFormDesigner on Wed Dec 07 10:39:52 AST 2022
 */

package scripts.kt.lumbridge.raider.api.ui.task.mining;

import java.awt.event.*;

import scripts.kt.lumbridge.raider.api.ScriptBehavior;
import scripts.kt.lumbridge.raider.api.ScriptDisposal;
import scripts.kt.lumbridge.raider.api.ScriptMiningData;
import scripts.kt.lumbridge.raider.api.ScriptTask;
import scripts.kt.lumbridge.raider.api.behaviors.mining.Pickaxe;
import scripts.kt.lumbridge.raider.api.behaviors.mining.Rock;
import scripts.kt.lumbridge.raider.api.ui.ScriptTaskGui;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Polymorphic
 */
public class MiningTaskGui extends JFrame {
    private final ScriptTaskGui rootFrame;

    private final DefaultListModel<Rock> rockDefaultListModel = new DefaultListModel<>();

    private int editIndex;

    public MiningTaskGui(ScriptTaskGui rootFrame) {
        this.rootFrame = rootFrame;
        initComponents();
        Arrays.stream(Pickaxe.values()).forEach(pickaxe -> comboBox2.addItem(pickaxe));
        Arrays.stream(Rock.values()).forEach(rock -> comboBox3.addItem(rock));
        comboBox4.addItem(ScriptDisposal.BANK);
        comboBox4.addItem(ScriptDisposal.DROP);
        comboBox4.addItem(ScriptDisposal.M1D1);
        list1.setModel(rockDefaultListModel);
    }

    public void showMiningAddForm() {
        checkBox1.setSelected(false);
        comboBox2.setSelectedItem(Pickaxe.BRONZE);
        comboBox3.setSelectedItem(Rock.TIN_LUMBRIDGE_SWAMP);
        comboBox4.setSelectedItem(ScriptDisposal.BANK);
        rockDefaultListModel.clear();
        editIndex = -1;
        okButton.setText("Add");
        setVisible(true);
    }

    public void showMiningEditForm(ScriptTask selectedTask, int selectedIndex) {
        if (selectedTask == null || selectedIndex == -1) return;
        if (selectedTask.getMiningData() == null) return;
        if (selectedTask.getMiningData().getPickaxe() == null) return;
        if (selectedTask.getMiningData().getRocks() == null) return;
        if (selectedTask.getDisposal() == null) return;

        checkBox1.setSelected(selectedTask.getMiningData().getWieldPickaxe());
        comboBox2.setSelectedItem(selectedTask.getMiningData().getPickaxe());
        comboBox4.setSelectedItem(selectedTask.getDisposal());
        rockDefaultListModel.clear();
        rockDefaultListModel.addAll(selectedTask.getMiningData().getRocks());
        editIndex = selectedIndex;
        okButton.setText("Save");
        setVisible(true);
    }

    private void ok(ActionEvent e) {
        if (comboBox2.getSelectedItem() == null) return;
        if (comboBox4.getSelectedItem() == null) return;
        if (rockDefaultListModel.isEmpty()) return;

        boolean isWield = checkBox1.isSelected();
        Pickaxe pickaxe = (Pickaxe) comboBox2.getSelectedItem();
        ScriptDisposal disposal = (ScriptDisposal) comboBox4.getSelectedItem();
        List<Rock> rockSequence = Arrays.stream(rockDefaultListModel.toArray())
                .map(o -> (Rock) o)
                .collect(Collectors.toList());

        ScriptTask miningTask = new ScriptTask.Builder()
                .behavior(ScriptBehavior.MINING)
                .disposal(disposal)
                .miningData(
                        new ScriptMiningData(rockSequence, pickaxe, isWield)
                )
                .build();

        if (editIndex != -1)
            rootFrame.getScriptTaskDefaultListModel().set(editIndex, miningTask);
        else
            rootFrame.getScriptTaskDefaultListModel().addElement(miningTask);

        setVisible(false);
    }

    private void comboBox3(ActionEvent e) {
        if (comboBox3.getSelectedItem() == null) return;
        Rock rock = (Rock) comboBox3.getSelectedItem();
        rockDefaultListModel.addElement(rock);
    }

    private void cancel(ActionEvent e) {
        setVisible(false);
    }

    private void delete(ActionEvent e) {
        if (list1.getSelectedIndex() == -1) return;
        rockDefaultListModel.remove(list1.getSelectedIndex());
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        label2 = new JLabel();
        comboBox2 = new JComboBox();
        checkBox1 = new JCheckBox();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        panel2 = new JPanel();
        label3 = new JLabel();
        comboBox3 = new JComboBox();
        scrollPane1 = new JScrollPane();
        list1 = new JList();
        button1 = new JButton();
        panel3 = new JPanel();
        comboBox4 = new JComboBox();

        //======== this ========
        setMinimumSize(new Dimension(500, 435));
        setTitle("Mining Task");
        setResizable(false);
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setBorder(new TitledBorder("Pickaxe Preferences"));
                contentPanel.setLayout(new GridBagLayout());

                //---- label2 ----
                label2.setText("Pickaxe");
                contentPanel.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));
                contentPanel.add(comboBox2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- checkBox1 ----
                checkBox1.setText("Wield");
                contentPanel.add(checkBox1, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(contentPanel, BorderLayout.NORTH);

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

            //======== panel2 ========
            {
                panel2.setBorder(new TitledBorder("Rock Sequence (ORDER MATTERS)"));
                panel2.setLayout(new GridBagLayout());

                //---- label3 ----
                label3.setText("Rock");
                panel2.add(label3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- comboBox3 ----
                comboBox3.addActionListener(e -> comboBox3(e));
                panel2.add(comboBox3, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //======== scrollPane1 ========
                {
                    scrollPane1.setViewportView(list1);
                }
                panel2.add(scrollPane1, new GridBagConstraints(0, 1, 2, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- button1 ----
                button1.setText("Deleted Selected");
                button1.addActionListener(e -> delete(e));
                panel2.add(button1, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(panel2, BorderLayout.CENTER);

            //======== panel3 ========
            {
                panel3.setBorder(new TitledBorder("Disposal"));
                panel3.setLayout(new GridBagLayout());
                panel3.add(comboBox4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(panel3, BorderLayout.EAST);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel label2;
    private JComboBox comboBox2;
    private JCheckBox checkBox1;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    private JPanel panel2;
    private JLabel label3;
    private JComboBox comboBox3;
    private JScrollPane scrollPane1;
    private JList list1;
    private JButton button1;
    private JPanel panel3;
    private JComboBox comboBox4;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
