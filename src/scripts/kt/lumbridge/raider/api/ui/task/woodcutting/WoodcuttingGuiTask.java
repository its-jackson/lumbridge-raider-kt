/*
 * Created by JFormDesigner on Thu Dec 08 14:53:06 AST 2022
 */

package scripts.kt.lumbridge.raider.api.ui.task.woodcutting;

import java.awt.event.*;

import scripts.kt.lumbridge.raider.api.*;
import scripts.kt.lumbridge.raider.api.behaviors.mining.Rock;
import scripts.kt.lumbridge.raider.api.behaviors.woodcutting.Axe;
import scripts.kt.lumbridge.raider.api.behaviors.woodcutting.Tree;
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
public class WoodcuttingGuiTask extends JFrame {
    private final ScriptTaskGui rootFrame;

    private final DefaultListModel<Tree> treeDefaultListModel = new DefaultListModel<>();

    private int editIndex;

    public WoodcuttingGuiTask(ScriptTaskGui rootFrame) {
        this.rootFrame = rootFrame;
        initComponents();

        Arrays.stream(Axe.values()).forEach(axe -> this.comboBox1.addItem(axe));
        Arrays.stream(Tree.values()).forEach(tree -> this.comboBox2.addItem(tree));
        this.comboBox3.addItem(ScriptDisposal.BANK);
        this.comboBox3.addItem(ScriptDisposal.DROP);
        this.list1.setModel(treeDefaultListModel);
    }

    public void showWoodcuttingAddForm() {
        editIndex = -1;
        comboBox1.setSelectedIndex(0);
        comboBox2.setSelectedIndex(0);
        comboBox3.setSelectedIndex(0);
        checkBox1.setSelected(false);
        treeDefaultListModel.clear();
        okButton.setText("Add");
        setVisible(true);
    }

    public void showWoodcuttingEditForm(ScriptTask selectedTask, int selectedIndex) {
        if (selectedTask == null || selectedIndex == -1) return;
        if (selectedTask.getDisposal() == null) return;
        if (selectedTask.getWoodcuttingData() == null) return;
        if (selectedTask.getWoodcuttingData().getAxe() == null) return;
        if (selectedTask.getWoodcuttingData().getTrees() == null) return;

        editIndex = selectedIndex;
        comboBox1.setSelectedItem(selectedTask.getWoodcuttingData().getAxe());
        checkBox1.setSelected(selectedTask.getWoodcuttingData().getWieldAxe());
        treeDefaultListModel.clear();
        treeDefaultListModel.addAll(selectedTask.getWoodcuttingData().getTrees());
        comboBox3.setSelectedItem(selectedTask.getDisposal());
        okButton.setText("Save");
        setVisible(true);
    }

    private void ok(ActionEvent e) {
        if (comboBox1.getSelectedItem() == null) return;
        if (comboBox2.getSelectedItem() == null) return;
        if (comboBox3.getSelectedItem() == null) return;
        if (treeDefaultListModel.isEmpty()) return;

        boolean isWield = checkBox1.isSelected();
        Axe axe =  (Axe) comboBox1.getSelectedItem();
        ScriptDisposal disposal = (ScriptDisposal) comboBox3.getSelectedItem();
        List<Tree> rockSequence = Arrays.stream(treeDefaultListModel.toArray())
                .map(o -> (Tree) o)
                .collect(Collectors.toList());

        ScriptTask woodcuttingTask = new ScriptTask.Builder()
                .behavior(ScriptBehavior.WOODCUTTING)
                .disposal(disposal)
                .woodcuttingData(new ScriptWoodcuttingData(rockSequence, axe, isWield))
                .build();

        if (editIndex != -1)
            rootFrame.getScriptTaskDefaultListModel().set(editIndex, woodcuttingTask);
        else
            rootFrame.getScriptTaskDefaultListModel().addElement(woodcuttingTask);

        setVisible(false);
    }

    private void cancel(ActionEvent e) {
        setVisible(false);
    }

    private void delete(ActionEvent e) {
        int toDelete = list1.getSelectedIndex();
        if (toDelete == -1) return;

        treeDefaultListModel.remove(toDelete);
    }

    private void comboBox2(ActionEvent e) {
        if (comboBox2.getSelectedItem() == null) return;
        treeDefaultListModel.addElement((Tree) comboBox2.getSelectedItem());
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        label1 = new JLabel();
        comboBox1 = new JComboBox();
        checkBox1 = new JCheckBox();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        panel1 = new JPanel();
        label2 = new JLabel();
        comboBox2 = new JComboBox();
        scrollPane1 = new JScrollPane();
        list1 = new JList();
        button1 = new JButton();
        panel2 = new JPanel();
        comboBox3 = new JComboBox();

        //======== this ========
        setTitle("Woodcutting Task");
        setMinimumSize(new Dimension(500, 400));
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setBorder(new TitledBorder("Axe Preferences"));
                contentPanel.setLayout(new GridBagLayout());

                //---- label1 ----
                label1.setText("Axe");
                contentPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));
                contentPanel.add(comboBox1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
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

            //======== panel1 ========
            {
                panel1.setBorder(new TitledBorder("Tree Sequence (ORDER MATTERS)"));
                panel1.setLayout(new GridBagLayout());

                //---- label2 ----
                label2.setText("Tree");
                panel1.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- comboBox2 ----
                comboBox2.addActionListener(e -> comboBox2(e));
                panel1.add(comboBox2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //======== scrollPane1 ========
                {
                    scrollPane1.setViewportView(list1);
                }
                panel1.add(scrollPane1, new GridBagConstraints(0, 2, 5, 4, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- button1 ----
                button1.setText("Delete Selected");
                button1.addActionListener(e -> delete(e));
                panel1.add(button1, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));
            }
            dialogPane.add(panel1, BorderLayout.CENTER);

            //======== panel2 ========
            {
                panel2.setBorder(new TitledBorder("Disposal"));
                panel2.setLayout(new GridBagLayout());
                panel2.add(comboBox3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(panel2, BorderLayout.EAST);
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
    private JComboBox comboBox1;
    private JCheckBox checkBox1;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    private JPanel panel1;
    private JLabel label2;
    private JComboBox comboBox2;
    private JScrollPane scrollPane1;
    private JList list1;
    private JButton button1;
    private JPanel panel2;
    private JComboBox comboBox3;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
