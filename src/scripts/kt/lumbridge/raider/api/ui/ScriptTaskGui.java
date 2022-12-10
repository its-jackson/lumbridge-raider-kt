/*
 * Created by JFormDesigner on Sat Dec 03 00:19:34 AST 2022
 */

package scripts.kt.lumbridge.raider.api.ui;

import java.awt.event.*;

import org.tribot.script.sdk.script.ScriptRuntimeInfo;
import org.tribot.script.sdk.util.Resources;
import scripts.kotlin.api.ScriptBreakControlData;
import scripts.kt.lumbridge.raider.api.ScriptBehavior;
import scripts.kt.lumbridge.raider.api.ScriptTask;
import scripts.kt.lumbridge.raider.api.ui.breakmanager.BreakManagerGui;
import scripts.kt.lumbridge.raider.api.ui.silly.SillyGui;
import scripts.kt.lumbridge.raider.api.ui.stop.condition.StopConditionGui;
import scripts.kt.lumbridge.raider.api.ui.task.combat.CombatTaskGui;
import scripts.kt.lumbridge.raider.api.ui.task.cooking.CookingTaskGui;
import scripts.kt.lumbridge.raider.api.ui.task.fishing.FishingTaskGui;
import scripts.kt.lumbridge.raider.api.ui.task.mining.MiningTaskGui;
import scripts.kt.lumbridge.raider.api.ui.task.prayer.PrayerTaskGui;
import scripts.kt.lumbridge.raider.api.ui.task.questing.QuestingGuiTask;
import scripts.kt.lumbridge.raider.api.ui.task.woodcutting.WoodcuttingGuiTask;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Polymorphic
 */
public class ScriptTaskGui extends JFrame {
    private SwingGuiState scriptTaskGuiState = SwingGuiState.CLOSED;

    private ScriptBreakControlData scriptBreakControlData = null;

    private final DefaultListModel<ScriptTask> scriptTaskDefaultListModel = new DefaultListModel<>();

    private final BreakManagerGui breakManagerGui = new BreakManagerGui(this);
    private final SillyGui sillyGui = new SillyGui(this);

    private final StopConditionGui stopConditionGui = new StopConditionGui(this);
    private final CombatTaskGui combatTaskGui = new CombatTaskGui(this);
    private final CookingTaskGui cookingTaskGui = new CookingTaskGui(this);
    private final FishingTaskGui fishingTaskGui = new FishingTaskGui(this);
    private final MiningTaskGui miningTaskGui = new MiningTaskGui(this);
    private final PrayerTaskGui prayerTaskGui = new PrayerTaskGui(this);
    private final QuestingGuiTask questingGuiTask = new QuestingGuiTask(this);
    private final WoodcuttingGuiTask woodcuttingGuiTask = new WoodcuttingGuiTask(this);

    public ScriptTaskGui() throws IOException {
        initComponents();
        setIconImage(Resources.getImage("scripts/kt/lumbridge/raider/api/resources/Tribot-Logo.png"));
        setTitle("LumbridgeRaider.kt v" + ScriptRuntimeInfo.getScriptRepoVersion());
        list1.setModel(scriptTaskDefaultListModel);
        Arrays.stream(ScriptBehavior.values()).forEach(scriptBehavior -> comboBox1.addItem(scriptBehavior));
    }

    public SwingGuiState getScriptTaskGuiState() {
        return scriptTaskGuiState;
    }

    public ScriptBreakControlData getScriptBreakControlData() {
        return scriptBreakControlData;
    }

    public void setScriptBreakControlData(ScriptBreakControlData scriptBreakControlData) {
        this.scriptBreakControlData = scriptBreakControlData;
    }

    public DefaultListModel<ScriptTask> getScriptTaskDefaultListModel() {
        return scriptTaskDefaultListModel;
    }

    private void button1(ActionEvent e) {
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
                break;
            case COOKING: cookingTaskGui.showCookingEditForm(selectedTask, selectedIndex);
                break;
            case FISHING: fishingTaskGui.showFishingEditForm(selectedTask, selectedIndex);
                break;
            case MINING: miningTaskGui.showMiningEditForm(selectedTask, selectedIndex);
                break;
            case PRAYER: prayerTaskGui.showPrayerEditForm(selectedTask, selectedIndex);
                break;
            case QUESTING: sillyGui.setVisible(true);
                break;
            case WOODCUTTING: woodcuttingGuiTask.showWoodcuttingEditForm(selectedTask, selectedIndex);
        }
    }

    private void button6(ActionEvent e) {
        if (comboBox1.getSelectedItem() == null) return;

        ScriptBehavior behavior = (ScriptBehavior) comboBox1.getSelectedItem();

        switch (behavior) {
            case COMBAT_MAGIC: combatTaskGui.showMagicAddForm();
                break;
            case COMBAT_MELEE: combatTaskGui.showMeleeAddForm();
                break;
            case COMBAT_RANGED: combatTaskGui.showRangedAddForm();
                break;
            case COOKING: cookingTaskGui.showCookingAddForm();
                break;
            case FISHING: fishingTaskGui.showFishingAddForm();
                break;
            case MINING: miningTaskGui.showMiningAddForm();
                break;
            case PRAYER: prayerTaskGui.showPrayerAddForm();
                break;
            case QUESTING: questingGuiTask.showQuestingAddForm();
                break;
            case WOODCUTTING: woodcuttingGuiTask.showWoodcuttingAddForm();
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

    private void showBreakManagerForm(ActionEvent e) {
        breakManagerGui.showForm();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        dialogPane = new JPanel();
        tabbedPane3 = new JTabbedPane();
        panel2 = new JPanel();
        scrollPane1 = new JScrollPane();
        list1 = new JList();
        panel3 = new JPanel();
        button11 = new JButton();
        button1 = new JButton();
        button2 = new JButton();
        button3 = new JButton();
        okButton2 = new JButton();
        panel4 = new JPanel();
        button6 = new JButton();
        comboBox1 = new JComboBox();
        panel7 = new JPanel();
        okButton = new JButton();
        menuBar1 = new JMenuBar();
        menu1 = new JMenu();
        menuItem1 = new JMenuItem();
        menuItem4 = new JMenuItem();
        menu4 = new JMenu();
        menuItem3 = new JMenuItem();
        menuItem5 = new JMenuItem();
        menu3 = new JMenu();
        menuItem6 = new JMenuItem();
        menu2 = new JMenu();
        menuItem2 = new JMenuItem();

        //======== this ========
        setTitle("LumbridgeRaider.kt");
        setFocusable(false);
        setMinimumSize(new Dimension(740, 470));
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== tabbedPane3 ========
            {

                //======== panel2 ========
                {
                    panel2.setLayout(new GridBagLayout());
                    ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0};
                    ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
                    ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
                    ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {1.0, 0.0, 0.0, 0.0, 1.0E-4};

                    //======== scrollPane1 ========
                    {

                        //---- list1 ----
                        list1.setBorder(new TitledBorder("Script Task Queue (First-In-First-Out)"));
                        scrollPane1.setViewportView(list1);
                    }
                    panel2.add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));

                    //======== panel3 ========
                    {
                        panel3.setBorder(new TitledBorder("Script Task Controls"));
                        panel3.setLayout(new GridBagLayout());
                        ((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0};
                        ((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
                        ((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
                        ((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

                        //---- button11 ----
                        button11.setText("Configure Stop Condition");
                        button11.addActionListener(e -> configStopCondition(e));
                        panel3.add(button11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 5), 0, 0));

                        //---- button1 ----
                        button1.setText("Edit");
                        button1.addActionListener(e -> button1(e));
                        panel3.add(button1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 5), 0, 0));

                        //---- button2 ----
                        button2.setText("Delete");
                        button2.addActionListener(e -> delete(e));
                        panel3.add(button2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 5), 0, 0));

                        //---- button3 ----
                        button3.setText("Move Up");
                        button3.addActionListener(e -> moveUp(e));
                        panel3.add(button3, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 5), 0, 0));

                        //---- okButton2 ----
                        okButton2.setText("Move Down");
                        okButton2.addActionListener(e -> moveDown(e));
                        panel3.add(okButton2, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 0), 0, 0));
                    }
                    panel2.add(panel3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));

                    //======== panel4 ========
                    {
                        panel4.setBorder(new TitledBorder("Script Task Selection"));
                        panel4.setLayout(new GridBagLayout());
                        ((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {0, 0, 0};
                        ((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0};
                        ((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
                        ((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

                        //---- button6 ----
                        button6.setText("Add New Task");
                        button6.addActionListener(e -> button6(e));
                        panel4.add(button6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 5), 0, 0));
                        panel4.add(comboBox1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 0), 0, 0));
                    }
                    panel2.add(panel4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));

                    //======== panel7 ========
                    {
                        panel7.setLayout(new BorderLayout());

                        //---- okButton ----
                        okButton.setText("Start Script");
                        okButton.addActionListener(e -> {
			ok(e);
			ok(e);
			ok(e);
		});
                        panel7.add(okButton, BorderLayout.EAST);
                    }
                    panel2.add(panel7, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
                }
                tabbedPane3.addTab("Display", panel2);
            }
            dialogPane.add(tabbedPane3, BorderLayout.CENTER);

            //======== menuBar1 ========
            {

                //======== menu1 ========
                {
                    menu1.setText("File");

                    //---- menuItem1 ----
                    menuItem1.setText("Settings");
                    menu1.add(menuItem1);

                    //---- menuItem4 ----
                    menuItem4.setText("Exit");
                    menu1.add(menuItem4);
                }
                menuBar1.add(menu1);

                //======== menu4 ========
                {
                    menu4.setText("Link");

                    //---- menuItem3 ----
                    menuItem3.setText("Github Repository");
                    menu4.add(menuItem3);

                    //---- menuItem5 ----
                    menuItem5.setText("TRiBot Repository");
                    menu4.add(menuItem5);
                }
                menuBar1.add(menu4);

                //======== menu3 ========
                {
                    menu3.setText("View");

                    //---- menuItem6 ----
                    menuItem6.setText("Break Manager");
                    menuItem6.addActionListener(e -> showBreakManagerForm(e));
                    menu3.add(menuItem6);
                }
                menuBar1.add(menu3);

                //======== menu2 ========
                {
                    menu2.setText("Help");

                    //---- menuItem2 ----
                    menuItem2.setText("About");
                    menu2.add(menuItem2);
                }
                menuBar1.add(menu2);
            }
            dialogPane.add(menuBar1, BorderLayout.NORTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel dialogPane;
    private JTabbedPane tabbedPane3;
    private JPanel panel2;
    private JScrollPane scrollPane1;
    private JList list1;
    private JPanel panel3;
    private JButton button11;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JButton okButton2;
    private JPanel panel4;
    private JButton button6;
    private JComboBox comboBox1;
    private JPanel panel7;
    private JButton okButton;
    private JMenuBar menuBar1;
    private JMenu menu1;
    private JMenuItem menuItem1;
    private JMenuItem menuItem4;
    private JMenu menu4;
    private JMenuItem menuItem3;
    private JMenuItem menuItem5;
    private JMenu menu3;
    private JMenuItem menuItem6;
    private JMenu menu2;
    private JMenuItem menuItem2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on


    public JList<ScriptTask> getList1() {
        return list1;
    }
}
