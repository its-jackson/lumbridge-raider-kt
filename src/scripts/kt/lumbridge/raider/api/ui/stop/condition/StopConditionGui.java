/*
 * Created by JFormDesigner on Mon Dec 05 21:58:53 AST 2022
 */

package scripts.kt.lumbridge.raider.api.ui.stop.condition;

import org.tribot.script.sdk.Skill;
import scripts.kotlin.api.AbstractStopCondition;
import scripts.kotlin.api.SkillLevelsReachedCondition;
import scripts.kotlin.api.TimeStopCondition;
import scripts.kt.lumbridge.raider.api.ScriptBehavior;
import scripts.kt.lumbridge.raider.api.ScriptTask;
import scripts.kt.lumbridge.raider.api.ui.ScriptTaskGui;
import scripts.kt.lumbridge.raider.api.ui.silly.SillyGui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Polymorphic
 */
public class StopConditionGui extends JFrame {
    private final ScriptTaskGui rootFrame;
    private final SillyGui sillyFrame = new SillyGui(this);

    private final DefaultListModel<HashMap<Skill, Integer>> skillLvlDefaultListModel = new DefaultListModel<>();

    private ScriptTask scriptTask = null;
    private AbstractStopCondition stopCondition = null;
    private int scriptIndex = -1;

    public StopConditionGui(ScriptTaskGui rootFrame) {
        this.rootFrame = rootFrame;
        initComponents();

        Long val = 0L;
        Long min = 0L;
        Long max = Long.MAX_VALUE;
        Long step = 1L;
        spinner5.setModel(new SpinnerNumberModel(val, min, max, step));
        spinner1.setModel(new SpinnerNumberModel(val, min, max, step));
        spinner2.setModel(new SpinnerNumberModel(val, min, max, step));
        spinner3.setModel(new SpinnerNumberModel(val, min, max, step));

        Arrays.stream(Skill.values())
                .forEach(skill -> comboBox1.addItem(skill));

        spinner4.setModel(new SpinnerNumberModel(1, 1, 99, 1));

        list1.setModel(skillLvlDefaultListModel);
    }

    private void setScriptTask(ScriptTask scriptTask) {
        this.scriptTask = scriptTask;
    }

    private void setStopCondition(AbstractStopCondition stopCondition) {
        this.stopCondition = stopCondition;
    }

    private void setScriptIndex(int scriptIndex) {
        this.scriptIndex = scriptIndex;
    }

    private void setState(ScriptTask scriptTask, int scriptIndex) {
        setScriptTask(scriptTask);
        setScriptIndex(scriptIndex);
        setStopCondition(scriptTask.getStopCondition());
    }

    private void resetComponents() {
        skillLvlDefaultListModel.clear();
        spinner5.setValue(0L);
        spinner4.setValue(1);
        spinner3.setValue(0L);
        spinner2.setValue(0L);
        spinner1.setValue(0L);
    }

    public void showForm(ScriptTask scriptTask, int scriptIndex) {
        if (scriptTask == null || scriptIndex == -1) return;

        if (scriptTask.getBehavior() == ScriptBehavior.COOKING ||
                scriptTask.getBehavior() == ScriptBehavior.PRAYER ||
                scriptTask.getBehavior() == ScriptBehavior.QUESTING ||
                scriptTask.getBehavior() == ScriptBehavior.ACCOUNT_CONFIG) {
            sillyFrame.setVisible(true);
            return;
        }

        setState(scriptTask, scriptIndex);
        resetComponents();

        if (stopCondition instanceof TimeStopCondition) {
            TimeStopCondition timeStopCondition = (TimeStopCondition) stopCondition;
            spinner5.setValue(timeStopCondition.getDays());
            spinner1.setValue(timeStopCondition.getHours());
            spinner2.setValue(timeStopCondition.getMinutes());
            spinner3.setValue(timeStopCondition.getSeconds());
        }

        if (stopCondition instanceof SkillLevelsReachedCondition) {
            SkillLevelsReachedCondition skillLevelsReachedCondition = (SkillLevelsReachedCondition) stopCondition;
            skillLevelsReachedCondition.getSkills().forEach((key, value) -> {
                HashMap<Skill, Integer> newMap = new HashMap<>();
                newMap.put(key, value);
                skillLvlDefaultListModel.addElement(newMap);
            });
        }

        setVisible(true);
    }

    private void onTimeConditionButton(ActionEvent e) {
        updateAndClose(
                new TimeStopCondition(
                        (Long) spinner5.getValue(), (Long) spinner1.getValue(),
                        (Long) spinner2.getValue(), (Long) spinner3.getValue()
                )
        );
    }

    private void onAddSkillLvlMapping(ActionEvent e) {
        if (comboBox1.getSelectedItem() == null) return;

        Skill skill = (Skill) comboBox1.getSelectedItem();
        int level = (int) spinner4.getValue();

        HashMap<Skill, Integer> map = new HashMap<>();
        map.put(skill, level);

        skillLvlDefaultListModel.addElement(map);
    }

    private void onSkillLvlConditionButton(ActionEvent e) {
        if (skillLvlDefaultListModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You forgot to add skill levels!");
            return;
        }

        HashMap<Skill, Integer> skillConditionMap = new HashMap<>();
        int size = skillLvlDefaultListModel.size();

        for (int i = 0; i < size; i++) {
            HashMap<Skill, Integer> currentMap = skillLvlDefaultListModel.get(i);
            skillConditionMap.putAll(currentMap);
        }

        updateAndClose(new SkillLevelsReachedCondition(skillConditionMap));
    }

    private void updateAndClose(AbstractStopCondition stopCondition) {
        ScriptTask updateScriptTask = new ScriptTask.Builder()
                .stopCondition(stopCondition)
                .behavior(scriptTask.getBehavior())
                .disposal(scriptTask.getDisposal())
                .combatData(scriptTask.getCombatData())
                .combatMagicData(scriptTask.getCombatMagicData())
                .fishingData(scriptTask.getFishingData())
                .miningData(scriptTask.getMiningData())
                .prayerData(scriptTask.getPrayerData())
                .questingData(scriptTask.getQuestingData())
                .woodcuttingData(scriptTask.getWoodcuttingData())
                .build();

        rootFrame.getScriptTaskDefaultListModel().set(scriptIndex, updateScriptTask);
        setVisible(false);
    }

    private void delete(ActionEvent e) {
        int toDelete = list1.getSelectedIndex();
        if (toDelete == -1) return;

        skillLvlDefaultListModel.remove(toDelete);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        dialogPane = new JPanel();
        panel2 = new JPanel();
        label4 = new JLabel();
        comboBox1 = new JComboBox();
        label5 = new JLabel();
        spinner4 = new JSpinner();
        button3 = new JButton();
        scrollPane1 = new JScrollPane();
        list1 = new JList();
        button4 = new JButton();
        button1 = new JButton();
        panel1 = new JPanel();
        label6 = new JLabel();
        spinner5 = new JSpinner();
        label1 = new JLabel();
        spinner1 = new JSpinner();
        label2 = new JLabel();
        spinner2 = new JSpinner();
        label3 = new JLabel();
        spinner3 = new JSpinner();
        button2 = new JButton();

        //======== this ========
        setResizable(false);
        setTitle("Stop Condition Configuration");
        setMinimumSize(new Dimension(420, 500));
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(null);
            dialogPane.setPreferredSize(new Dimension(400, 500));
            dialogPane.setLayout(new BorderLayout());

            //======== panel2 ========
            {
                panel2.setBorder(new TitledBorder("Skill Levels Condition"));
                panel2.setLayout(new GridBagLayout());

                //---- label4 ----
                label4.setText("Skill");
                panel2.add(label4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));
                panel2.add(comboBox1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                //---- label5 ----
                label5.setText("Until Level");
                panel2.add(label5, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));
                panel2.add(spinner4, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                //---- button3 ----
                button3.setText(" Add Skill/Lvl Mapping");
                button3.addActionListener(e -> onAddSkillLvlMapping(e));
                panel2.add(button3, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                //======== scrollPane1 ========
                {

                    //---- list1 ----
                    list1.setVisibleRowCount(10);
                    list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    list1.setMaximumSize(new Dimension(40, 57));
                    scrollPane1.setViewportView(list1);
                }
                panel2.add(scrollPane1, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                //---- button4 ----
                button4.setText("Delete Selected");
                button4.addActionListener(e -> delete(e));
                panel2.add(button4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- button1 ----
                button1.setText("Set/Use Skill Lvls Condition");
                button1.addActionListener(e -> onSkillLvlConditionButton(e));
                panel2.add(button1, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));
            }
            dialogPane.add(panel2, BorderLayout.NORTH);

            //======== panel1 ========
            {
                panel1.setBorder(new TitledBorder("Time Stop Condition"));
                panel1.setLayout(new GridBagLayout());

                //---- label6 ----
                label6.setText("Days");
                panel1.add(label6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));
                panel1.add(spinner5, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                //---- label1 ----
                label1.setText("Hours");
                panel1.add(label1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));
                panel1.add(spinner1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                //---- label2 ----
                label2.setText("Minutes");
                panel1.add(label2, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));
                panel1.add(spinner2, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                //---- label3 ----
                label3.setText("Seconds");
                panel1.add(label3, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));
                panel1.add(spinner3, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                //---- button2 ----
                button2.setText("Set/Use Time Condition");
                button2.addActionListener(e -> onTimeConditionButton(e));
                panel1.add(button2, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));
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
    private JPanel panel2;
    private JLabel label4;
    private JComboBox comboBox1;
    private JLabel label5;
    private JSpinner spinner4;
    private JButton button3;
    private JScrollPane scrollPane1;
    private JList list1;
    private JButton button4;
    private JButton button1;
    private JPanel panel1;
    private JLabel label6;
    private JSpinner spinner5;
    private JLabel label1;
    private JSpinner spinner1;
    private JLabel label2;
    private JSpinner spinner2;
    private JLabel label3;
    private JSpinner spinner3;
    private JButton button2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
