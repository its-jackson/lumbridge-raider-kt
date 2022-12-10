/*
 * Created by JFormDesigner on Fri Dec 09 20:29:13 AST 2022
 */

package scripts.kt.lumbridge.raider.api.ui.breakmanager;

import org.tribot.script.sdk.Tribot;
import org.tribot.script.sdk.util.ScriptSettings;
import scripts.kotlin.api.ScriptBreakControlData;
import scripts.kt.lumbridge.raider.api.ui.ScriptTaskGui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * @author Polymorphic
 */
public class BreakManagerGui extends JFrame {
    private final ScriptTaskGui rootFrame;
    private final ScriptBreakControlData defaultBreakControlData = new ScriptBreakControlData();

    private final String base = Tribot.getDirectory().getAbsolutePath() + File.separator + "script-config";
    private final ScriptSettings settingsHandler;

    public BreakManagerGui(ScriptTaskGui rootFrame) throws IOException {
        this.rootFrame = rootFrame;
        initComponents();

        String basePath = base + File.separator + "polymorphic-break-manager";

        settingsHandler = ScriptSettings.builder()
                .basePath(basePath)
                .build();

        settingsHandler.getSaveNames().forEach(name -> comboBox1.addItem(name));
    }

    public void showForm() {
        setVisible(true);
    }

    private ScriptBreakControlData getScriptBreakData() {
        double frequencyMean;
        double frequencyStd;
        double timeMean;
        double timeStd;

        try {
            frequencyMean = Double.parseDouble(textField1.getText().trim());
            frequencyStd = Double.parseDouble(textField3.getText().trim());
            timeMean = Double.parseDouble(textField2.getText().trim());
            timeStd = Double.parseDouble(textField4.getText().trim());
        } catch (NumberFormatException formatException) {
            return null;
        }

        if (frequencyMean < 0 || frequencyStd < 0 || timeMean < 0 || timeStd < 0) return null;

        return new ScriptBreakControlData(
                frequencyMean, frequencyStd,
                timeMean, timeStd
        );
    }

    private void ok(ActionEvent e) {
        if (checkBox1.isSelected()) {
            rootFrame.setScriptBreakControlData(defaultBreakControlData);
        } else {
            ScriptBreakControlData breakControlData = getScriptBreakData();
            if (breakControlData == null) return;
            rootFrame.setScriptBreakControlData(breakControlData);
        }

        JOptionPane.showMessageDialog(this, "Updated script break control data successfully.");
        setVisible(false);
    }

    private void cancel(ActionEvent e) {
        setVisible(false);
    }

    private void seededBreakPreferences(ActionEvent e) {
        panel1.setVisible(!checkBox1.isSelected());
    }

    private void save(ActionEvent e) {
        ScriptBreakControlData breakControlData = getScriptBreakData();
        if (breakControlData == null) return;

        String name = textField5.getText().trim();
        if (name.isEmpty() || name.isBlank()) return;

        if (settingsHandler.save(name, breakControlData)) {
            comboBox1.addItem(name);
            JOptionPane.showMessageDialog(this, "Profile saved successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "Unable to save profile.");
        }
    }

    private void load(ActionEvent e) {
        if (comboBox1.getSelectedItem() == null) return;
        String name = (String) comboBox1.getSelectedItem();

        settingsHandler.load(name, ScriptBreakControlData.class)
                .ifPresentOrElse(data -> {
                    textField1.setText(String.valueOf(data.getFrequencyMeanMinutes()));
                    textField3.setText(String.valueOf(data.getFrequencyStdMinutes()));
                    textField2.setText(String.valueOf(data.getTimeMeanMinutes()));
                    textField4.setText(String.valueOf(data.getTimeStdMinutes()));
                }, () -> {
                    JOptionPane.showMessageDialog(this, "Unable to load profile: " + name);
                });
    }

    private void delete(ActionEvent e) {
        if (comboBox1.getSelectedItem() == null) return;
        String name = (String) comboBox1.getSelectedItem();

        if (settingsHandler.delete(name)) {
            comboBox1.removeItem(name);
            JOptionPane.showMessageDialog(this, "Profile deleted successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "Unable to delete profile: " + name);
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        label1 = new JLabel();
        comboBox1 = new JComboBox();
        button1 = new JButton();
        button3 = new JButton();
        button2 = new JButton();
        textField5 = new JTextField();
        buttonBar = new JPanel();
        checkBox1 = new JCheckBox();
        okButton = new JButton();
        cancelButton = new JButton();
        panel1 = new JPanel();
        label2 = new JLabel();
        label4 = new JLabel();
        textField1 = new JTextField();
        textField3 = new JTextField();
        label3 = new JLabel();
        label5 = new JLabel();
        textField2 = new JTextField();
        textField4 = new JTextField();

        //======== this ========
        setTitle("Break Manager");
        setResizable(false);
        setMinimumSize(new Dimension(300, 300));
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
                ((GridBagLayout) contentPanel.getLayout()).columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
                ((GridBagLayout) contentPanel.getLayout()).rowHeights = new int[]{0, 0};
                ((GridBagLayout) contentPanel.getLayout()).columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
                ((GridBagLayout) contentPanel.getLayout()).rowWeights = new double[]{0.0, 1.0E-4};

                //---- label1 ----
                label1.setText("Profile");
                contentPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));
                contentPanel.add(comboBox1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- button1 ----
                button1.setText("Load");
                button1.addActionListener(e -> load(e));
                contentPanel.add(button1, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- button3 ----
                button3.setText("Delete");
                button3.addActionListener(e -> delete(e));
                contentPanel.add(button3, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- button2 ----
                button2.setText("Save");
                button2.addActionListener(e -> save(e));
                contentPanel.add(button2, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));
                contentPanel.add(textField5, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(contentPanel, BorderLayout.NORTH);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[]{0, 85, 80};
                ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[]{1.0, 0.0, 0.0};

                //---- checkBox1 ----
                checkBox1.setText("Use Seeded Break Preferences");
                checkBox1.addActionListener(e -> seededBreakPreferences(e));
                buttonBar.add(checkBox1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

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

                //---- label2 ----
                label2.setText("Frequency Mean");
                panel1.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 20, 20), 0, 0));

                //---- label4 ----
                label4.setText("Frequency Std");
                panel1.add(label4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 20, 0), 0, 0));
                panel1.add(textField1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 20, 20), 0, 0));
                panel1.add(textField3, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 20, 0), 0, 0));

                //---- label3 ----
                label3.setText("Time Mean");
                panel1.add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 20, 20), 0, 0));

                //---- label5 ----
                label5.setText("Time Std");
                panel1.add(label5, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 20, 0), 0, 0));
                panel1.add(textField2, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 20), 0, 0));
                panel1.add(textField4, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
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
    private JPanel contentPanel;
    private JLabel label1;
    private JComboBox comboBox1;
    private JButton button1;
    private JButton button3;
    private JButton button2;
    private JTextField textField5;
    private JPanel buttonBar;
    private JCheckBox checkBox1;
    private JButton okButton;
    private JButton cancelButton;
    private JPanel panel1;
    private JLabel label2;
    private JLabel label4;
    private JTextField textField1;
    private JTextField textField3;
    private JLabel label3;
    private JLabel label5;
    private JTextField textField2;
    private JTextField textField4;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
