/*
 * Created by JFormDesigner on Thu Dec 15 13:06:16 AST 2022
 */

package scripts.kt.lumbridge.raider.api.ui.task.account.config;

import java.awt.event.*;

import scripts.kt.lumbridge.raider.api.ScriptAccountConfigData;
import scripts.kt.lumbridge.raider.api.ScriptBehavior;
import scripts.kt.lumbridge.raider.api.ScriptTask;
import scripts.kt.lumbridge.raider.api.ui.ScriptTaskGui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Polymorphic
 */
public class AccountConfigTaskGui extends JFrame {
    private final ScriptTaskGui rootFrame;

    private int editIndex;

    public AccountConfigTaskGui(ScriptTaskGui rootFrame) {
        this.rootFrame = rootFrame;
        initComponents();
        setResizable(false);
        spinner1.setModel(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.1));
    }

    public void showAccountConfigAddForm() {
        editIndex = -1;
        checkBox1.setSelected(false);
        checkBox2.setSelected(false);
        checkBox3.setSelected(false);
        spinner1.setValue(0.00);
        okButton.setText("Add");
        setVisible(true);
    }

    public void showAccountConfigEditForm(ScriptTask selectedTask, int selectedIndex) {
        if (selectedTask == null || selectedIndex == -1) return;
        if (selectedTask.getAccountConfigData() == null) return;

        ScriptAccountConfigData accountConfigData = selectedTask.getAccountConfigData();
        editIndex = selectedIndex;
        checkBox1.setSelected(accountConfigData.getSolveNewCharacterBankAccGuide());
        checkBox2.setSelected(accountConfigData.getEnableShiftClick());
        checkBox3.setSelected(accountConfigData.getEnableRoofs());
        spinner1.setValue(accountConfigData.getCameraZoomPercent());
        okButton.setText("Save");
        setVisible(true);
    }

    private void ok(ActionEvent e) {
        ScriptTask configTask = new ScriptTask.Builder()
                .behavior(ScriptBehavior.ACCOUNT_CONFIG)
                .accountConfigData(
                        new ScriptAccountConfigData(
                                checkBox1.isSelected(),
                                checkBox2.isSelected(),
                                checkBox3.isSelected(),
                                (Double) spinner1.getValue()
                        )
                )
                .build();

        if (editIndex != -1)
            rootFrame.getScriptTaskDefaultListModel().set(editIndex, configTask);
        else
            rootFrame.getScriptTaskDefaultListModel().addElement(configTask);

        setVisible(false);
    }

    private void cancel(ActionEvent e) {
        setVisible(false);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        checkBox1 = new JCheckBox();
        checkBox2 = new JCheckBox();
        checkBox3 = new JCheckBox();
        separator1 = new JSeparator();
        label1 = new JLabel();
        spinner1 = new JSpinner();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        setTitle("Account Config Task");
        setMinimumSize(new Dimension(460, 350));
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

                //---- checkBox1 ----
                checkBox1.setText("Solve New Character Bank Account Screen");
                contentPanel.add(checkBox1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- checkBox2 ----
                checkBox2.setText("Enable Shift Click");
                contentPanel.add(checkBox2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- checkBox3 ----
                checkBox3.setText("Enable Roofs");
                contentPanel.add(checkBox3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                    new Insets(0, 0, 5, 0), 0, 0));
                contentPanel.add(separator1, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- label1 ----
                label1.setText("Camera Zoom Percent");
                contentPanel.add(label1, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));
                contentPanel.add(spinner1, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

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
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JCheckBox checkBox1;
    private JCheckBox checkBox2;
    private JCheckBox checkBox3;
    private JSeparator separator1;
    private JLabel label1;
    private JSpinner spinner1;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
