/*
 * Created by JFormDesigner on Fri Dec 09 23:27:27 AST 2022
 */

package scripts.kt.lumbridge.raider.api.ui.settings;

import org.tribot.script.sdk.util.ScriptSettings;
import scripts.kt.lumbridge.raider.api.ScriptTask;
import scripts.kt.lumbridge.raider.api.ui.ScriptTaskGui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

/**
 * @author Polymorphic
 */
public class SettingsGui extends JFrame {
    private final ScriptTaskGui rootFrame;

    private ScriptSettings defaultSettingsHandler;

    public SettingsGui(ScriptTaskGui rootFrame) {
        this.rootFrame = rootFrame;
        initComponents();
    }

    public void setDefaultSettingsHandler(ScriptSettings defaultSettingsHandler) {
        this.defaultSettingsHandler = defaultSettingsHandler;
    }

    public void showForm() {
        if (defaultSettingsHandler != null && comboBox1.getModel().getSize() == 0)
            defaultSettingsHandler.getSaveNames().forEach(name -> comboBox1.addItem(name));

        setVisible(true);
    }

    private void save(ActionEvent e) {
        String name = textField1.getText()
                .trim();

        if (name.isEmpty() || name.isBlank() || defaultSettingsHandler == null) return;

        ScriptTask[] toSave = Arrays.stream(rootFrame.getScriptTaskDefaultListModel().toArray())
                .map(o -> (ScriptTask) o)
                .toArray(ScriptTask[]::new);

        if (defaultSettingsHandler.save(name, toSave)) {
            comboBox1.addItem(name);
            JOptionPane.showMessageDialog(this, "Profile saved successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "Unable to save profile: " + name);
        }
    }

    private void load(ActionEvent e) {
        if (comboBox1.getSelectedItem() == null || defaultSettingsHandler == null) return;
        String toLoad = (String) comboBox1.getSelectedItem();

        defaultSettingsHandler.load(toLoad, ScriptTask[].class)
                .ifPresentOrElse(scriptTasks -> {
                    Arrays.stream(scriptTasks)
                            .forEach(scriptTask -> rootFrame.getScriptTaskDefaultListModel().addElement(scriptTask));
                    JOptionPane.showMessageDialog(this, "Profile loaded successfully.");
                }, () -> {
                    JOptionPane.showMessageDialog(this, "Unable to load profile: " + toLoad);
                });
    }

    private void delete(ActionEvent e) {
        if (comboBox1.getSelectedItem() == null || defaultSettingsHandler == null) return;
        String toDelete = (String) comboBox1.getSelectedItem();

        if (defaultSettingsHandler.delete(toDelete)) {
            comboBox1.removeItem(toDelete);
            JOptionPane.showMessageDialog(this, "Profile deleted successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "Unable to delete profile: " + toDelete);
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
        panel1 = new JPanel();
        label2 = new JLabel();
        textField1 = new JTextField();
        button2 = new JButton();

        //======== this ========
        setTitle("Settings");
        setMinimumSize(new Dimension(400, 300));
        setResizable(false);
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
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(contentPanel, BorderLayout.NORTH);

            //======== panel1 ========
            {
                panel1.setBorder(new TitledBorder(""));
                panel1.setLayout(new GridBagLayout());

                //---- label2 ----
                label2.setText("Name");
                panel1.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));
                panel1.add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- button2 ----
                button2.setText("Save");
                button2.addActionListener(e -> save(e));
                panel1.add(button2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
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
    private JPanel panel1;
    private JLabel label2;
    private JTextField textField1;
    private JButton button2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
