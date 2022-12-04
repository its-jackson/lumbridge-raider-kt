/*
 * Created by JFormDesigner on Sat Dec 03 00:19:34 AST 2022
 */

package scripts.kt.lumbridge.raider.api.ui;

import java.awt.event.*;

import scripts.kt.lumbridge.raider.api.ScriptBehavior;
import scripts.kt.lumbridge.raider.api.ScriptTask;
import scripts.kt.lumbridge.raider.api.ui.task.combat.CombatTaskGui;

import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Polymorphic
 */
public class MainGui extends JFrame {
    private SwingGuiState guiState;

    private final DefaultListModel<ScriptTask> scriptTaskDefaultListModel = new DefaultListModel<>();

    private final CombatTaskGui combatTaskGui = new CombatTaskGui(this);

    public MainGui() {
        initComponents();

        list1.setModel(scriptTaskDefaultListModel);

        Arrays.stream(ScriptBehavior.values())
                .forEach(scriptBehavior -> comboBox1.addItem(scriptBehavior));
    }

    public SwingGuiState getGuiState() {
        return guiState;
    }

    public DefaultListModel<ScriptTask> getScriptTaskDefaultListModel() {
        return scriptTaskDefaultListModel;
    }

    private void button6(ActionEvent e) {
        if (comboBox1.getSelectedItem() == null) return;

        ScriptBehavior behavior = (ScriptBehavior) comboBox1.getSelectedItem();

        if (behavior == ScriptBehavior.COMBAT_MELEE || behavior == ScriptBehavior.COMBAT_RANGED) {
            combatTaskGui.setOkButtonText("OK");
            combatTaskGui.setEditMode(false);
            combatTaskGui.setVisible(true);
        }
    }

    private void ok(ActionEvent e) {
        if (getScriptTaskDefaultListModel().isEmpty()) {
            JOptionPane.showMessageDialog(this, "You forgot to add script tasks!");
            return;
        }

        guiState = SwingGuiState.STARTED;
        setVisible(false);
    }

    private void button1(ActionEvent e) {
        // TODO Edit Task

        if (list1.getSelectedValue() == null || list1.getSelectedIndex() == -1) return;
        ScriptTask selectedTask = (ScriptTask) list1.getSelectedValue();
        int selectedIndex = list1.getSelectedIndex();

        if (selectedTask.getScriptBehavior() == ScriptBehavior.COMBAT_MELEE || selectedTask.getScriptBehavior() == ScriptBehavior.COMBAT_RANGED) {
            if (selectedTask.getScriptCombatData() == null) return;

            if (selectedTask.getScriptCombatData().getAttackStyle() == null) return;
            combatTaskGui.setAttackStyle(selectedTask.getScriptCombatData().getAttackStyle());

            if (selectedTask.getScriptCombatData().getMonsters() == null) return;
            combatTaskGui.getMonsterDefaultListModel().clear();
            combatTaskGui.getMonsterDefaultListModel().addAll(selectedTask.getScriptCombatData().getMonsters());
            combatTaskGui.setLootItemsCheckBox(selectedTask.getScriptCombatData().getLootGroundItems());
            combatTaskGui.setEquipmentItemList(selectedTask.getScriptCombatData().getEquipmentItems());
            combatTaskGui.setInventoryItemList(selectedTask.getScriptCombatData().getInventoryItems());
            combatTaskGui.setEditIndex(selectedIndex);
            combatTaskGui.setEditMode(true);
            combatTaskGui.setOkButtonText("SAVE");
            combatTaskGui.setVisible(true);
        }

    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        dialogPane = new JPanel();
        buttonBar = new JPanel();
        button9 = new JButton();
        okButton = new JButton();
        tabbedPane3 = new JTabbedPane();
        panel2 = new JPanel();
        label1 = new JLabel();
        scrollPane1 = new JScrollPane();
        list1 = new JList();
        buttonBar2 = new JPanel();
        label2 = new JLabel();
        label4 = new JLabel();
        comboBox1 = new JComboBox();
        button1 = new JButton();
        button2 = new JButton();
        button3 = new JButton();
        okButton2 = new JButton();
        button6 = new JButton();
        button7 = new JButton();
        button8 = new JButton();
        button10 = new JButton();
        button11 = new JButton();

        //======== this ========
        setTitle("LumbridgeRaider.kt");
        setFocusable(false);
        setMinimumSize(new Dimension(800, 600));
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
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 0, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

                //---- button9 ----
                button9.setText("Clear Cache");
                buttonBar.add(button9, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- okButton ----
                okButton.setText("Start Script");
                okButton.addActionListener(e -> ok(e));
                buttonBar.add(okButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);

            //======== tabbedPane3 ========
            {

                //======== panel2 ========
                {
                    panel2.setLayout(new GridBagLayout());
                    ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0};
                    ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
                    ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
                    ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

                    //---- label1 ----
                    label1.setText("Script Task List");
                    panel2.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                        new Insets(0, 0, 0, 0), 0, 0));

                    //======== scrollPane1 ========
                    {

                        //---- list1 ----
                        list1.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                        scrollPane1.setViewportView(list1);
                    }
                    panel2.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));

                    //======== buttonBar2 ========
                    {
                        buttonBar2.setBorder(new EmptyBorder(12, 0, 0, 0));
                        buttonBar2.setLayout(new GridBagLayout());
                        ((GridBagLayout)buttonBar2.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 80};
                        ((GridBagLayout)buttonBar2.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0, 0.0};

                        //---- label2 ----
                        label2.setText("Task Selection");
                        buttonBar2.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- label4 ----
                        label4.setText("Task Controls");
                        buttonBar2.add(label4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));
                        buttonBar2.add(comboBox1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- button1 ----
                        button1.setText("Edit");
                        button1.addActionListener(e -> button1(e));
                        buttonBar2.add(button1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- button2 ----
                        button2.setText("Delete");
                        buttonBar2.add(button2, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- button3 ----
                        button3.setText("Move Up");
                        buttonBar2.add(button3, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- okButton2 ----
                        okButton2.setText("Move Down");
                        buttonBar2.add(okButton2, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- button6 ----
                        button6.setText("Add New Task");
                        button6.addActionListener(e -> button6(e));
                        buttonBar2.add(button6, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 5), 0, 0));

                        //---- button7 ----
                        button7.setText("Save List");
                        buttonBar2.add(button7, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 5), 0, 0));

                        //---- button8 ----
                        button8.setText("Load List");
                        buttonBar2.add(button8, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 5), 0, 0));

                        //---- button10 ----
                        button10.setText("Delete List");
                        buttonBar2.add(button10, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 5), 0, 0));

                        //---- button11 ----
                        button11.setText("Configure Stop Condition");
                        buttonBar2.add(button11, new GridBagConstraints(4, 4, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 0), 0, 0));
                    }
                    panel2.add(buttonBar2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
                }
                tabbedPane3.addTab("Index", panel2);
            }
            dialogPane.add(tabbedPane3, BorderLayout.CENTER);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel dialogPane;
    private JPanel buttonBar;
    private JButton button9;
    private JButton okButton;
    private JTabbedPane tabbedPane3;
    private JPanel panel2;
    private JLabel label1;
    private JScrollPane scrollPane1;
    private JList list1;
    private JPanel buttonBar2;
    private JLabel label2;
    private JLabel label4;
    private JComboBox comboBox1;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JButton okButton2;
    private JButton button6;
    private JButton button7;
    private JButton button8;
    private JButton button10;
    private JButton button11;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on


    public JList<ScriptTask> getList1() {
        return list1;
    }
}
