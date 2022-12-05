/*
 * Created by JFormDesigner on Sat Dec 03 19:43:34 AST 2022
 */

package scripts.kt.lumbridge.raider.api.ui.task.combat;

import org.tribot.script.sdk.Combat;
import org.tribot.script.sdk.Login;
import org.tribot.script.sdk.query.Query;
import org.tribot.script.sdk.types.EquipmentItem;
import org.tribot.script.sdk.types.InventoryItem;
import scripts.kt.lumbridge.raider.api.ScriptBehavior;
import scripts.kt.lumbridge.raider.api.ScriptCombatData;
import scripts.kt.lumbridge.raider.api.ScriptTask;
import scripts.kt.lumbridge.raider.api.behaviors.combat.Monster;
import scripts.kt.lumbridge.raider.api.ui.ScriptTaskGui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Polymorphic
 */
public class CombatTaskGui extends JFrame {
    private final ScriptTaskGui rootFrame;
    private final DefaultListModel<Monster> monsterDefaultListModel = new DefaultListModel<>();

    private List<InventoryItem> inventoryItemList = Collections.emptyList();
    private List<EquipmentItem> equipmentItemList = Collections.emptyList();

    private boolean editMode;
    private int editIndex;

    public CombatTaskGui(ScriptTaskGui rootFrame) {
        this.rootFrame = rootFrame;
        initComponents();

        Arrays.stream(Combat.AttackStyle.values())
                .forEach(attackStyle -> comboBox2.addItem(attackStyle));

        Arrays.stream(Monster.values())
                .forEach(monster -> comboBox3.addItem(monster));

        list2.setModel(monsterDefaultListModel);
    }

    public void showAddForm() {
        getMonsterDefaultListModel().clear();
        setMonster(Monster.CHICKEN_LUMBRIDGE_EAST);
        setAttackStyle(Combat.AttackStyle.ACCURATE);
        setInventoryItemList(Collections.emptyList());
        setEquipmentItemList(Collections.emptyList());
        setLootItemsCheckBox(false);
        setEditMode(false);
        setEditIndex(-1);
        setOkButtonText("Add");
        setVisible(true);
    }

    public void showEditForm(ScriptTask selectedTask, int selectedIndex) {
        if (selectedTask == null || selectedIndex == -1) return;
        if (selectedTask.getCombatData() == null) return;
        if (selectedTask.getCombatData().getAttackStyle() == null) return;
        if (selectedTask.getCombatData().getMonsters() == null) return;

        setAttackStyle(selectedTask.getCombatData().getAttackStyle());
        getMonsterDefaultListModel().clear();
        getMonsterDefaultListModel().addAll(selectedTask.getCombatData().getMonsters());
        setLootItemsCheckBox(selectedTask.getCombatData().getLootGroundItems());
        setEquipmentItemList(selectedTask.getCombatData().getEquipmentItems());
        setInventoryItemList(selectedTask.getCombatData().getInventoryItems());
        setEditIndex(selectedIndex);
        setEditMode(true);
        setOkButtonText("Save");
        setVisible(true);
    }

    private DefaultListModel<Monster> getMonsterDefaultListModel() {
        return monsterDefaultListModel;
    }

    private void setInventoryItemList(List<InventoryItem> inventoryItemList) {
        this.inventoryItemList = inventoryItemList;
    }

    private void setEquipmentItemList(List<EquipmentItem> equipmentItemList) {
        this.equipmentItemList = equipmentItemList;
    }

    private void setAttackStyle(Combat.AttackStyle attackStyle) {
        comboBox2.setSelectedItem(attackStyle);
    }

    private void setMonster(Monster monster) {
        this.comboBox3.setSelectedItem(monster);
    }

    private void setLootItemsCheckBox(boolean loot) {
        checkBox1.setSelected(loot);
    }

    private boolean isEditMode() {
        return editMode;
    }

    private void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    private void setEditIndex(int editIndex) {
        this.editIndex = editIndex;
    }

    private void setOkButtonText(String txt) {
        okButton.setText(txt);
    }

    private void ok(ActionEvent e) {
        if (comboBox2.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "You forgot to selected an attack style!");
            return;
        }

        if (monsterDefaultListModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You forgot to add monsters to kill!");
            return;
        }

        ScriptTask combatScriptTask = new ScriptTask.Builder()
                .behavior(ScriptBehavior.COMBAT_MELEE)
                .combatData(
                        new ScriptCombatData.Builder()
                                .inventoryItems(inventoryItemList)
                                .equipmentItems(equipmentItemList)
                                .attackStyle((Combat.AttackStyle) comboBox2.getSelectedItem())
                                .lootGroundItems(checkBox1.isSelected())
                                .monsters(
                                        Arrays.stream(monsterDefaultListModel.toArray())
                                                .map(o -> (Monster) o)
                                                .collect(Collectors.toList())
                                )
                                .build()
                )
                .build();

        if (isEditMode())
            rootFrame.getScriptTaskDefaultListModel().set(editIndex, combatScriptTask);
        else
            rootFrame.getScriptTaskDefaultListModel().addElement(combatScriptTask);

        setVisible(false);
    }

    private void button1(ActionEvent e) {
        if (!Login.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Character is not logged-in! Login first!");
            return;
        }

        inventoryItemList = Query.inventory()
                .toList();

        equipmentItemList = Query.equipment()
                .toList();

        JOptionPane.showMessageDialog(this, "Gear/Inventory Configured Successfully.");
    }

    private void comboBox3(ActionEvent e) {
        if (comboBox3.getSelectedItem() == null) return;
        monsterDefaultListModel.addElement((Monster) comboBox3.getSelectedItem());
    }

    private void button2(ActionEvent e) {
        int selected = list2.getSelectedIndex();

        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Monster not selected!");
            return;
        }

        monsterDefaultListModel.remove(selected);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        dialogPane = new JPanel();
        panel1 = new JPanel();
        label3 = new JLabel();
        comboBox2 = new JComboBox();
        label4 = new JLabel();
        comboBox3 = new JComboBox();
        label5 = new JLabel();
        scrollPane2 = new JScrollPane();
        list2 = new JList();
        buttonBar = new JPanel();
        checkBox1 = new JCheckBox();
        button2 = new JButton();
        button1 = new JButton();
        okButton = new JButton();

        //======== this ========
        setTitle("Combat Task (Melee/Ranged)");
        setResizable(false);
        setMinimumSize(new Dimension(600, 375));
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new GridBagLayout());
            ((GridBagLayout)dialogPane.getLayout()).columnWidths = new int[] {0, 0};
            ((GridBagLayout)dialogPane.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
            ((GridBagLayout)dialogPane.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
            ((GridBagLayout)dialogPane.getLayout()).rowWeights = new double[] {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

            //======== panel1 ========
            {
                panel1.setLayout(new GridBagLayout());
                ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
                ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
                ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
                ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

                //---- label3 ----
                label3.setText("Attack Style To Use");
                panel1.add(label3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
                panel1.add(comboBox2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- label4 ----
                label4.setText("Monsters To Kill");
                panel1.add(label4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- comboBox3 ----
                comboBox3.addActionListener(e -> comboBox3(e));
                panel1.add(comboBox3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));
            }
            dialogPane.add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

            //---- label5 ----
            label5.setText("Kill List");
            dialogPane.add(label5, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

            //======== scrollPane2 ========
            {
                scrollPane2.setViewportView(list2);
            }
            dialogPane.add(scrollPane2, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 0, 0, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0};

                //---- checkBox1 ----
                checkBox1.setText("Loot Drops");
                buttonBar.add(checkBox1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- button2 ----
                button2.setText("Remove Selected Monster");
                button2.addActionListener(e -> button2(e));
                buttonBar.add(button2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- button1 ----
                button1.setText("Get Current Gear/Inventory");
                button1.addActionListener(e -> button1(e));
                buttonBar.add(button1, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- okButton ----
                okButton.setText("OK");
                okButton.addActionListener(e -> ok(e));
                buttonBar.add(okButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel dialogPane;
    private JPanel panel1;
    private JLabel label3;
    private JComboBox comboBox2;
    private JLabel label4;
    private JComboBox comboBox3;
    private JLabel label5;
    private JScrollPane scrollPane2;
    private JList list2;
    private JPanel buttonBar;
    private JCheckBox checkBox1;
    private JButton button2;
    private JButton button1;
    private JButton okButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
