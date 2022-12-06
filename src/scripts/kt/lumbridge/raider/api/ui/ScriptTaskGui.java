/*
 * Created by JFormDesigner on Sat Dec 03 00:19:34 AST 2022
 */

package scripts.kt.lumbridge.raider.api.ui;

import java.awt.event.*;

import org.tribot.script.sdk.script.ScriptRuntimeInfo;
import scripts.kt.lumbridge.raider.api.ScriptBehavior;
import scripts.kt.lumbridge.raider.api.ScriptTask;
import scripts.kt.lumbridge.raider.api.ui.stop.condition.StopConditionGui;
import scripts.kt.lumbridge.raider.api.ui.task.combat.CombatTaskGui;

import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Polymorphic
 */
public class ScriptTaskGui extends JFrame {
    private SwingGuiState scriptTaskGuiState = SwingGuiState.CLOSED;

    private final DefaultListModel<ScriptTask> scriptTaskDefaultListModel = new DefaultListModel<>();

    private final StopConditionGui stopConditionGui = new StopConditionGui(this);

    private final CombatTaskGui combatTaskGui = new CombatTaskGui(this);

    public ScriptTaskGui() {
        initComponents();
        setTitle("LumbridgeRaider.kt v" + ScriptRuntimeInfo.getScriptRepoVersion());
        list1.setModel(scriptTaskDefaultListModel);
        Arrays.stream(ScriptBehavior.values()).forEach(scriptBehavior -> comboBox1.addItem(scriptBehavior));
    }

    public SwingGuiState getScriptTaskGuiState() {
        return scriptTaskGuiState;
    }

    public DefaultListModel<ScriptTask> getScriptTaskDefaultListModel() {
        return scriptTaskDefaultListModel;
    }

    private void button1(ActionEvent e) {
        // TODO Edit Task

        if (list1.getSelectedValue() == null || list1.getSelectedIndex() == -1) return;

        ScriptTask selectedTask = (ScriptTask) list1.getSelectedValue();
        int selectedIndex = list1.getSelectedIndex();

        if (selectedTask.getBehavior() == null) return;

        switch (selectedTask.getBehavior()) {
            case COMBAT_MAGIC: combatTaskGui.showMagicEditForm(selectedTask, selectedIndex);
                break;
            case COMBAT_MELEE: combatTaskGui.showMeleeEditForm(selectedTask, selectedIndex);
                break;
            case COMBAT_RANGED: combatTaskGui.showRangedEditForm(selectedTask, selectedIndex);
        }
    }

    private void button6(ActionEvent e) {
        // TODO Add Task

        if (comboBox1.getSelectedItem() == null) return;

        ScriptBehavior behavior = (ScriptBehavior) comboBox1.getSelectedItem();

        switch (behavior) {
            case COMBAT_MAGIC: combatTaskGui.showMagicAddForm();
                break;
            case COMBAT_MELEE: combatTaskGui.showMeleeAddForm();
                break;
            case COMBAT_RANGED:combatTaskGui.showRangedAddForm();
        }
    }

    private void delete(ActionEvent e) {
        int selectedIndex = list1.getSelectedIndex();
        if (selectedIndex == -1) return;

        scriptTaskDefaultListModel.remove(selectedIndex);
    }

    private void moveUp(ActionEvent e) {
        move(-1);
    }

    private void moveDown(ActionEvent e) {
        move(1);
    }

    private void move(int shift) {
        int currentSize = scriptTaskDefaultListModel.size();
        int currentIndex = list1.getSelectedIndex();
        int neighborIndex = currentIndex + shift;

        if (currentIndex == -1 || neighborIndex >= currentSize || neighborIndex < 0) return;

        ScriptTask neighborTask = scriptTaskDefaultListModel.getElementAt(neighborIndex);
        ScriptTask currentTask = (ScriptTask) list1.getSelectedValue();

        scriptTaskDefaultListModel.set(neighborIndex, currentTask);
        scriptTaskDefaultListModel.set(currentIndex, neighborTask);

        list1.setSelectedIndex(neighborIndex);
    }

    private void ok(ActionEvent e) {
        if (getScriptTaskDefaultListModel().isEmpty()) {
            JOptionPane.showMessageDialog(this, "You forgot to add script tasks!");
            return;
        }

        scriptTaskGuiState = SwingGuiState.STARTED;
        setVisible(false);
    }

    private void configStopCondition(ActionEvent e) {
        stopConditionGui.showForm((ScriptTask) list1.getSelectedValue(), list1.getSelectedIndex());
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        dialogPane = new JPanel();
        buttonBar = new JPanel();
        button4 = new JButton();
        toggleButton1 = new JToggleButton();
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
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 0, 0, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0};

                //---- button4 ----
                button4.setText("Reset Script Cache");
                buttonBar.add(button4, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- toggleButton1 ----
                toggleButton1.setText("Enable Break Handler");
                buttonBar.add(toggleButton1, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- okButton ----
                okButton.setText("Start Script");
                okButton.addActionListener(e -> {
			ok(e);
			ok(e);
			ok(e);
		});
                buttonBar.add(okButton, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
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
                        button2.addActionListener(e -> delete(e));
                        buttonBar2.add(button2, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- button3 ----
                        button3.setText("Move Up");
                        button3.addActionListener(e -> moveUp(e));
                        buttonBar2.add(button3, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- okButton2 ----
                        okButton2.setText("Move Down");
                        okButton2.addActionListener(e -> moveDown(e));
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
                        button11.addActionListener(e -> configStopCondition(e));
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
    private JButton button4;
    private JToggleButton toggleButton1;
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
